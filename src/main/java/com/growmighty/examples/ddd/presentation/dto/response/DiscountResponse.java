package com.growmighty.examples.ddd.presentation.dto.response;

import java.math.BigDecimal;

/**
 * 할인 계산 응답 DTO (Presentation Layer)
 * 
 * 특징:
 * - API 클라이언트 친화적 구조
 * - JSON 직렬화 최적화
 */
public record DiscountResponse(
        Long orderId,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {}
