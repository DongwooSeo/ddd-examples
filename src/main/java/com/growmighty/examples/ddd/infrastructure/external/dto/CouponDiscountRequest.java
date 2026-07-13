package com.growmighty.examples.ddd.infrastructure.external.dto;

import java.math.BigDecimal;

/**
 * 쿠폰 서비스에 할인 계산을 요청할 때 보내는 본문.
 */
public record CouponDiscountRequest(BigDecimal orderAmount) {
}
