package com.growmighty.examples.ddd.presentation.dto.response;

/**
 * 주문 취소 응답 DTO
 */
public record OrderCancelResponse(
        Long orderId,
        String message
) {}
