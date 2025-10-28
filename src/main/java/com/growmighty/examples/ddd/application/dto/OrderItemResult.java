package com.growmighty.examples.ddd.application.dto;

import com.growmighty.examples.ddd.domain.entity.OrderItem;

/**
 * 주문 항목 Response 객체
 */
public record OrderItemResult(
    Long productId,
    String productName,
    String price,
    int quantity,
    String totalPrice
) {
    public static OrderItemResult from(OrderItem item) {
        return new OrderItemResult(
            item.getProductId().getId(),
            item.getProductName(),
            item.getPrice().toString(),
            item.getQuantity().getValue(),
            item.calculateTotalPrice().toString()
        );
    }
}

