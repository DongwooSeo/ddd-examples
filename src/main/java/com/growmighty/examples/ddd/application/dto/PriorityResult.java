package com.growmighty.examples.ddd.application.dto;

/**
 * 주문 우선순위 응답 DTO
 */
public record PriorityResult(
        Long orderId,
        String priority,
        Integer level
) {}
