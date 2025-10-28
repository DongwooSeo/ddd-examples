package com.growmighty.examples.ddd.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 주문 수량 값 객체
 * 
 * 비즈니스 규칙:
 * - 최소 수량: 1개
 * - 최대 수량: 100개
 */
@Getter
@EqualsAndHashCode
public class Quantity {
    
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;
    
    private final int value;
    
    private Quantity(int value) {
        validate(value);
        this.value = value;
    }
    
    public static Quantity of(int value) {
        return new Quantity(value);
    }
    
    private void validate(int value) {
        if (value < MIN_QUANTITY) {
            throw new IllegalArgumentException(
                String.format("주문 수량은 최소 %d개 이상이어야 합니다", MIN_QUANTITY));
        }
        if (value > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                String.format("주문 수량은 최대 %d개까지 가능합니다", MAX_QUANTITY));
        }
    }
    
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
    
    public boolean isGreaterThan(int target) {
        return this.value > target;
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
