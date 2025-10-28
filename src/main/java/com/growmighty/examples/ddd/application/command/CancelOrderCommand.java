package com.growmighty.examples.ddd.application.command;

/**
 * 주문 취소 Command 객체
 */
public record CancelOrderCommand(
    Long orderId,
    Long customerId
) {}

