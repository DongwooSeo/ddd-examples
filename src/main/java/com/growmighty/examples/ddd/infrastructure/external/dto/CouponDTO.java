package com.growmighty.examples.ddd.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 외부 쿠폰 도메인 DTO
 * 
 * 주문 도메인의 관점에서 쿠폰 도메인은 외부 Bounded Context입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private String couponCode;
    private String discountType; // "PERCENTAGE", "FIXED_AMOUNT"
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private boolean valid;
}

