package com.growmighty.examples.ddd.presentation.dto.response;

/**
 * 주문 생성 응답 DTO
 */
public record OrderCreateResponse(
        Long orderId,
        String message
) {}
