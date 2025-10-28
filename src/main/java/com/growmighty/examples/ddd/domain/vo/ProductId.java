package com.growmighty.examples.ddd.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 ID 값 객체
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductId {
    
    private Long id;
    
    private ProductId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다");
        }
        this.id = id;
    }
    
    public static ProductId of(Long id) {
        return new ProductId(id);
    }
}

