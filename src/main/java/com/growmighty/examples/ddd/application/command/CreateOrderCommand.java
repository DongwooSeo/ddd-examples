package com.growmighty.examples.ddd.application.command;

import java.util.List;

/**
 * 주문 생성 Command 객체 (불변)
 */
public record CreateOrderCommand(
    Long customerId,
    List<OrderItemRequest> items,
    String shippingAddress,
    String couponCode
) {}

