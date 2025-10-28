package com.growmighty.examples.ddd.application.command;

/**
 * 주문 결제 Command 객체
 */
public record PayOrderCommand(
    Long orderId
) {}

