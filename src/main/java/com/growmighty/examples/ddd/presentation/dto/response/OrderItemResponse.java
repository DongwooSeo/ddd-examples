package com.growmighty.examples.ddd.presentation.dto.response;

import com.growmighty.examples.ddd.application.dto.OrderItemResult;

/**
 * 주문 항목 응답 DTO (Presentation Layer)
 * <p>
 * 특징:
 * - API 클라이언트 친화적 구조
 * - JSON 직렬화 최적화
 */
public record OrderItemResponse(
        Long productId,
        String productName,
        String unitPrice,
        Integer quantity,
        String totalPrice
) {
    public static OrderItemResponse from(OrderItemResult result) {
        return new OrderItemResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.quantity(),
                result.totalPrice()
        );
    }
}
