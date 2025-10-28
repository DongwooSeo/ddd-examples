package com.growmighty.examples.ddd.presentation.dto.response;

import com.growmighty.examples.ddd.application.dto.OrderResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 조회 응답 DTO (Presentation Layer)
 * <p>
 * 특징:
 * - API 클라이언트 친화적 구조
 * - JSON 직렬화 최적화
 * - 클라이언트가 필요한 모든 정보 포함
 */
public record OrderDetailResponse(
        Long orderId,
        Long customerId,
        String orderStatus,
        List<OrderItemResponse> orderItems,
        String totalAmount,
        String couponCode,
        String discountAmount,
        String finalAmount,
        boolean cancellable,
        LocalDateTime orderedAt
) {
    public static OrderDetailResponse from(OrderResult result) {
        return new OrderDetailResponse(
                result.orderId(),
                result.customerId(),
                result.status(),
                result.items().stream().map(OrderItemResponse::from).toList(),
                result.totalAmount(),
                result.couponCode(),
                result.discountAmount(),
                result.finalAmount(),
                result.cancellable(),
                result.orderedAt()
        );
    }
}
