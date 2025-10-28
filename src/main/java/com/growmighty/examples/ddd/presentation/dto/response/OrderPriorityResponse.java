package com.growmighty.examples.ddd.presentation.dto.response;

/**
 * 주문 우선순위 응답 DTO (Presentation Layer)
 * 
 * 특징:
 * - API 클라이언트 친화적 구조
 * - JSON 직렬화 최적화
 */
public record OrderPriorityResponse(
        Long orderId,
        String priority,
        Integer level
) {}
