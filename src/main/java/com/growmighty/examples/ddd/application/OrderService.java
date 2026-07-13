package com.growmighty.examples.ddd.application;

import com.growmighty.examples.ddd.application.command.CancelOrderCommand;
import com.growmighty.examples.ddd.application.command.CreateOrderCommand;
import com.growmighty.examples.ddd.application.command.OrderItemCommand;
import com.growmighty.examples.ddd.application.command.PayOrderCommand;
import com.growmighty.examples.ddd.application.dto.OrderResult;
import com.growmighty.examples.ddd.application.dto.PriorityResult;
import com.growmighty.examples.ddd.application.dto.DiscountResult;
import com.growmighty.examples.ddd.application.port.ProductPort;
import com.growmighty.examples.ddd.application.port.CustomerPort;
import com.growmighty.examples.ddd.application.port.CouponPort;
import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.entity.OrderItem;
import com.growmighty.examples.ddd.domain.repository.OrderRepository;
import com.growmighty.examples.ddd.domain.service.OrderDomainService;
import com.growmighty.examples.ddd.domain.vo.*;
import com.growmighty.examples.ddd.domain.vo.OrderPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 주문 유스케이스를 조율한다.
 *
 * 트랜잭션을 잡고, 도메인 객체를 불러 흐름을 엮고, 외부 서비스는 포트로 호출한다.
 * 실제 규칙은 도메인 모델이 갖고 있으므로 이 클래스는 얇게 유지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;

    private final ProductPort productPort;
    private final CustomerPort customerPort;
    private final CouponPort couponPort;

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
        if (!customerPort.canOrder(customerId)) {
            throw new IllegalStateException("주문할 수 없는 고객입니다");
        }
    }

    private List<OrderItem> createOrderItems(List<OrderItemCommand> itemRequests) {
        List<ProductId> productIds = itemRequests.stream()
                .map(request -> ProductId.of(request.productId()))
                .distinct()
                .toList();

        Map<ProductId, ProductPort.ProductInfo> productMap = productPort.getProducts(productIds);

        return itemRequests.stream()
                .map(request -> createOrderItem(request, productMap))
                .toList();
    }

    private OrderItem createOrderItem(OrderItemCommand request,
                                      Map<ProductId, ProductPort.ProductInfo> productMap) {
        ProductId productId = ProductId.of(request.productId());
        ProductPort.ProductInfo product = productMap.get(productId);

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

        // 할인 금액은 쿠폰 서비스가 계산해 준다.
        Money orderAmount = order.calculateTotalAmount();
        Money discountAmount = couponPort.calculateDiscount(couponCode, orderAmount)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰입니다"));

        order.applyCoupon(couponCode, discountAmount);
        couponPort.useCoupon(couponCode, order.getCustomerId().getId().toString());
    }

    private void decreaseStocks(List<OrderItem> orderItems) {
        Map<ProductId, Integer> stockDecreaseMap = orderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(item -> item.getQuantity().getValue())
                ));

        if (!productPort.decreaseStocks(stockDecreaseMap)) {
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
            couponPort.restoreCoupon(order.getCouponCode());
        }

        log.info("주문 취소 완료: orderId={}", command.orderId());
    }

    private void restoreStocks(Order order) {
        Map<ProductId, Integer> stockRestoreMap = order.getOrderItems().stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(item -> item.getQuantity().getValue())
                ));

        productPort.restoreStocks(stockRestoreMap);
    }

    public OrderResult getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        return OrderResult.from(order);
    }

    public PriorityResult getOrderPriority(Long orderId) {
        log.info("주문 우선순위 조회: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        OrderPriority priority = orderDomainService.determinePriority(
                order.calculateTotalAmount()
        );

        return new PriorityResult(
                orderId,
                priority.getDescription(),
                priority.getLevel()
        );
    }

    public DiscountResult calculateDiscount(Long orderId) {
        log.info("주문 할인 계산: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        List<Order> customerOrderHistory = orderRepository.findByCustomerId(order.getCustomerId());

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
