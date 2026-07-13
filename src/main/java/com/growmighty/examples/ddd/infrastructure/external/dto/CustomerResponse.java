package com.growmighty.examples.ddd.infrastructure.external.dto;

/**
 * 고객 서비스 응답. 주문 가능 여부는 고객 도메인이 판단한다.
 */
public record CustomerResponse(
        Long customerId,
        String name,
        boolean canOrder
) {
}
