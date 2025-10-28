package com.growmighty.examples.ddd.application.dto;

import com.growmighty.examples.ddd.domain.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 Response 객체
 */
public record OrderResult(
        Long orderId,
        Long customerId,
        List<OrderItemResult> items,
        String address,
        String status,
        String couponCode,
        String totalAmount,
        String discountAmount,
        String finalAmount,
        boolean cancellable,
        LocalDateTime orderedAt
) {
    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId(),
                order.getCustomerId().getId(),
                order.getOrderItems().stream()
                        .map(OrderItemResult::from)
                        .toList(),
                order.getShippingAddress().getAddress(),
                order.getStatus().name(),
                order.getCouponCode().getCode(),
                order.calculateTotalAmount().toString(),
                order.getDiscountAmount().toString(),
                order.calculateFinalAmount().toString(),
                order.isCancellable(),
                order.getOrderedAt()
        );
    }
}

