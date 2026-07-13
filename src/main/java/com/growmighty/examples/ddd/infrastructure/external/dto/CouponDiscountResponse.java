package com.growmighty.examples.ddd.infrastructure.external.dto;

import java.math.BigDecimal;

/**
 * 쿠폰 서비스가 계산해 돌려주는 할인 금액.
 * 할인 금액은 쿠폰 도메인이 정하며, 주문 쪽은 그대로 받아 쓴다.
 */
public record CouponDiscountResponse(BigDecimal discountAmount) {
}
