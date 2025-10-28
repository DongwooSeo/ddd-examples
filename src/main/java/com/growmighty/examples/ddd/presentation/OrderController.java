package com.growmighty.examples.ddd.presentation;

// ========== Application Layer ==========

import com.growmighty.examples.ddd.application.OrderService;
import com.growmighty.examples.ddd.application.command.CancelOrderCommand;
import com.growmighty.examples.ddd.application.command.CreateOrderCommand;
import com.growmighty.examples.ddd.application.command.PayOrderCommand;

// ========== Presentation Layer DTOs ==========
import com.growmighty.examples.ddd.application.dto.DiscountResult;
import com.growmighty.examples.ddd.application.dto.PriorityResult;
import com.growmighty.examples.ddd.presentation.dto.ApiResponse;
import com.growmighty.examples.ddd.presentation.dto.request.CreateOrderRequest;
import com.growmighty.examples.ddd.presentation.dto.request.CancelOrderRequest;
import com.growmighty.examples.ddd.presentation.dto.response.OrderCreateResponse;
import com.growmighty.examples.ddd.presentation.dto.response.OrderPayResponse;
import com.growmighty.examples.ddd.presentation.dto.response.OrderCancelResponse;
import com.growmighty.examples.ddd.presentation.dto.response.OrderDetailResponse;
import com.growmighty.examples.ddd.presentation.dto.response.OrderPriorityResponse;
import com.growmighty.examples.ddd.presentation.dto.response.DiscountResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 표현 계층 (Presentation Layer)
 * <p>
 * 역할:
 * 1. HTTP 요청/응답 처리
 * 2. Spring Validation을 통한 입력 검증
 * 3. DTO 변환
 * 4. 애플리케이션 서비스 호출
 * <p>
 * 특징:
 * - 비즈니스 로직은 애플리케이션 서비스에 위임
 * - 예외 처리는 GlobalExceptionHandler에서 처리
 * - Spring Validation으로 입력 검증
 * - RESTful API 설계
 * - 간결하고 명확한 구조
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("주문 생성 요청: customerId={}", request.customerId());

        // 1. Command 객체 생성
        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.items(),
                request.shippingAddress(),
                request.couponCode()
        );

        // 2. 애플리케이션 서비스 호출
        Long orderId = orderService.createOrder(command);

        // 3. 응답 생성
        OrderCreateResponse response = new OrderCreateResponse(
                orderId,
                "주문이 성공적으로 생성되었습니다"
        );

        log.info("주문 생성 성공: orderId={}", orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 주문 결제
     * POST /api/orders/{orderId}/pay
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderPayResponse>> payOrder(
            @PathVariable @Min(value = 1, message = "주문 ID는 양수여야 합니다") Long orderId) {
        log.info("주문 결제 요청: orderId={}", orderId);

        // 1. Command 객체 생성
        PayOrderCommand command = new PayOrderCommand(orderId);

        // 2. 애플리케이션 서비스 호출
        orderService.payOrder(command);

        // 3. 응답 생성
        OrderPayResponse response = new OrderPayResponse(
                orderId,
                "결제가 성공적으로 완료되었습니다"
        );

        log.info("주문 결제 성공: orderId={}", orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 주문 취소
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(
            @PathVariable @Min(value = 1, message = "주문 ID는 양수여야 합니다") Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {

        log.info("주문 취소 요청: orderId={}, customerId={}", orderId, request.customerId());

        // 1. Command 객체 생성
        CancelOrderCommand command = new CancelOrderCommand(orderId, request.customerId());

        // 2. 애플리케이션 서비스 호출
        orderService.cancelOrder(command);

        // 3. 응답 생성
        OrderCancelResponse response = new OrderCancelResponse(
                orderId,
                "주문이 성공적으로 취소되었습니다"
        );

        log.info("주문 취소 성공: orderId={}", orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 주문 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable @Min(value = 1, message = "주문 ID는 양수여야 합니다") Long orderId) {
        log.info("주문 조회 요청: orderId={}", orderId);

        // 1. 애플리케이션 서비스 호출
        OrderDetailResponse response = OrderDetailResponse.from(orderService.getOrder(orderId));

        log.info("주문 조회 성공: orderId={}", orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 주문 우선순위 조회
     * GET /api/orders/{orderId}/priority
     */
    @GetMapping("/{orderId}/priority")
    public ResponseEntity<ApiResponse<OrderPriorityResponse>> getOrderPriority(
            @PathVariable @Min(value = 1, message = "주문 ID는 양수여야 합니다") Long orderId) {
        log.info("주문 우선순위 조회 요청: orderId={}", orderId);

        // 1. 애플리케이션 서비스 호출
        PriorityResult priorityResult = orderService.getOrderPriority(orderId);

        // 2. 응용 계층 DTO를 프레젠테이션 계층 DTO로 변환
        OrderPriorityResponse response = new OrderPriorityResponse(
                priorityResult.orderId(),
                priorityResult.priority(),
                priorityResult.level()
        );

        log.info("주문 우선순위 조회 성공: orderId={}, priority={}", orderId, response.priority());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 주문 할인 계산
     * GET /api/orders/{orderId}/discount
     */
    @GetMapping("/{orderId}/discount")
    public ResponseEntity<ApiResponse<DiscountResponse>> calculateDiscount(
            @PathVariable @Min(value = 1, message = "주문 ID는 양수여야 합니다") Long orderId) {
        log.info("주문 할인 계산 요청: orderId={}", orderId);

        // 1. 애플리케이션 서비스 호출
        DiscountResult discountResult = orderService.calculateDiscount(orderId);

        // 2. 응용 계층 DTO를 프레젠테이션 계층 DTO로 변환
        DiscountResponse response = new DiscountResponse(
                discountResult.orderId(),
                discountResult.discountAmount(),
                discountResult.totalAmount()
        );

        log.info("주문 할인 계산 성공: orderId={}, discountAmount={}", orderId, response.discountAmount());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}