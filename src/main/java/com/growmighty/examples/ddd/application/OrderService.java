package com.growmighty.examples.ddd.application;

// ========== Command (입력) ==========

import com.growmighty.examples.ddd.application.command.CancelOrderCommand;
import com.growmighty.examples.ddd.application.command.CreateOrderCommand;
import com.growmighty.examples.ddd.application.command.OrderItemRequest;
import com.growmighty.examples.ddd.application.command.PayOrderCommand;

// ========== Application Layer DTOs ==========
import com.growmighty.examples.ddd.application.dto.OrderItemResult;
import com.growmighty.examples.ddd.application.dto.OrderResult;
import com.growmighty.examples.ddd.application.dto.PriorityResult;
import com.growmighty.examples.ddd.application.dto.DiscountResult;

// ========== Domain ==========
import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.entity.OrderItem;
import com.growmighty.examples.ddd.domain.repository.OrderRepository;
import com.growmighty.examples.ddd.domain.service.OrderDomainService;
import com.growmighty.examples.ddd.domain.vo.*;
import com.growmighty.examples.ddd.domain.vo.OrderPriority;

// ========== Application Services ==========
import com.growmighty.examples.ddd.application.service.ProductClient;
import com.growmighty.examples.ddd.application.service.CustomerClient;
import com.growmighty.examples.ddd.application.service.CouponClient;

// ========== Framework ==========
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 애플리케이션 서비스
 * <p>
 * 역할:
 * 1. 트랜잭션 관리
 * 2. 도메인 객체 조율
 * 3. 인프라스트럭처 레이어와 연동
 * 4. DTO 변환
 * <p>
 * 특징:
 * - 비즈니스 로직은 도메인 모델에 위임
 * - 얇은 서비스 레이어 (Thin Service Layer)
 * - 도메인 이벤트는 자동으로 발행됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;

    private final ProductClient productClient;
    private final CustomerClient customerClient;
    private final CouponClient couponClient;

    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        log.info("주문 생성 시작: customerId={}", command.customerId());

        CustomerId customerId = CustomerId.of(command.customerId());
        validateCustomer(customerId);

        List<OrderItem> orderItems = createOrderItems(command.items());
        ShippingAddress shippingAddress = ShippingAddress.of(command.shippingAddress());

        Order order = Order.create(customerId, orderItems, shippingAddress);

        if (command.couponCode() != null && !command.couponCode().isBlank()) {
            applyCoupon(order, command.couponCode());
        }

        decreaseStocks(orderItems);
        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료: orderId={}", savedOrder.getId());
        return savedOrder.getId();
    }

    private void validateCustomer(CustomerId customerId) {
        if (!customerClient.canOrder(customerId)) {
            throw new IllegalStateException("주문할 수 없는 고객입니다");
        }
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequest> itemRequests) {
        List<ProductId> productIds = itemRequests.stream()
                .map(request -> ProductId.of(request.productId()))
                .distinct()
                .toList();

        Map<ProductId, ProductClient.ProductInfo> productMap = productClient.getProducts(productIds);

        return itemRequests.stream()
                .map(request -> createOrderItem(request, productMap))
                .toList();
    }

    private OrderItem createOrderItem(OrderItemRequest request,
                                      Map<ProductId, ProductClient.ProductInfo> productMap) {
        ProductId productId = ProductId.of(request.productId());
        ProductClient.ProductInfo product = productMap.get(productId);

        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + request.productId());
        }

        if (!product.available()) {
            throw new IllegalArgumentException("상품을 구매할 수 없습니다: " + product.productName());
        }

        if (product.stockQuantity() < request.quantity()) {
            throw new IllegalArgumentException("재고가 부족합니다: " + product.productName());
        }

        return new OrderItem(
                product.productId(),
                product.productName(),
                Money.of(product.price()),
                Quantity.of(request.quantity())
        );
    }

    private void applyCoupon(Order order, String couponCodeStr) {
        CouponCode couponCode = CouponCode.of(couponCodeStr);

        // 외부 쿠폰 도메인에서 할인 금액 계산
        Money orderAmount = order.calculateTotalAmount();
        Money discountAmount = couponClient.calculateDiscount(couponCode, orderAmount)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰입니다"));

        // 주문에 쿠폰 적용
        order.applyCoupon(couponCode, discountAmount);

        // 외부 쿠폰 도메인에 사용 처리 요청
        couponClient.useCoupon(couponCode, order.getCustomerId().getId().toString());
    }

    private void decreaseStocks(List<OrderItem> orderItems) {
        Map<ProductId, Integer> stockDecreaseMap = orderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(item -> item.getQuantity().getValue())
                ));

        if (!productClient.decreaseStocks(stockDecreaseMap)) {
            throw new IllegalStateException("재고 차감 실패: 일부 상품의 재고가 부족합니다");
        }
    }

    @Transactional
    public void payOrder(PayOrderCommand command) {
        log.info("주문 결제 시작: orderId={}", command.orderId());

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        order.pay();

        log.info("주문 결제 완료: orderId={}", command.orderId());
    }

    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        log.info("주문 취소 시작: orderId={}", command.orderId());

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        CustomerId requesterId = CustomerId.of(command.customerId());
        order.cancel(requesterId);

        restoreStocks(order);

        if (order.getCouponCode() != null) {
            couponClient.restoreCoupon(order.getCouponCode());
        }

        log.info("주문 취소 완료: orderId={}", command.orderId());
    }

    private void restoreStocks(Order order) {
        Map<ProductId, Integer> stockRestoreMap = order.getOrderItems().stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(item -> item.getQuantity().getValue())
                ));

        productClient.restoreStocks(stockRestoreMap);
    }

    public OrderResult getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        return OrderResult.from(order);
    }

    /**
     * 주문 우선순위 조회 (도메인 서비스 활용)
     */
    public PriorityResult getOrderPriority(Long orderId) {
        log.info("주문 우선순위 조회: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        // 도메인 서비스를 통한 우선순위 계산
        OrderPriority priority = orderDomainService.determinePriority(
                order.calculateTotalAmount()
        );

        return new PriorityResult(
                orderId,
                priority.getDescription(),
                priority.getLevel()
        );
    }

    /**
     * 주문 할인 계산 (도메인 서비스 활용)
     */
    public DiscountResult calculateDiscount(Long orderId) {
        log.info("주문 할인 계산: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        // 고객의 주문 이력 조회
        List<Order> customerOrderHistory = orderRepository.findByCustomerId(order.getCustomerId());

        // 도메인 서비스를 통한 할인 계산
        Money discountAmount = orderDomainService.calculateDiscount(
                order.getCustomerId(),
                order.calculateTotalAmount(),
                customerOrderHistory
        );

        return new DiscountResult(
                orderId,
                discountAmount.getAmount(),
                order.calculateTotalAmount().getAmount()
        );
    }

}
