package com.growmighty.examples.ddd.presentation.dto.response;

/**
 * 주문 결제 응답 DTO
 */
public record OrderPayResponse(
        Long orderId,
        String message
) {}
