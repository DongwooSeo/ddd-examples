package com.growmighty.examples.ddd.application.dto;

import java.math.BigDecimal;

/**
 * 할인 계산 응답 DTO
 */
public record DiscountResult(
        Long orderId,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {}
