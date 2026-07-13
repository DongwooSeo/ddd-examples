package com.growmighty.examples.ddd.infrastructure.external.dto;

import java.math.BigDecimal;

/**
 * 상품 서비스 응답. 외부 표현이므로 주문 도메인 모델과는 분리해 둔다.
 */
public record ProductResponse(
        Long productId,
        String productName,
        BigDecimal price,
        int stockQuantity,
        boolean available
) {
}
