package com.growmighty.examples.ddd.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 외부 상품 도메인 DTO
 * 
 * 주문 도메인의 관점에서 상품 도메인은 외부 Bounded Context입니다.
 * Anti-Corruption Layer 패턴을 적용하여 DTO로 통신합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean available;
}

