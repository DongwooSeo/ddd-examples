package com.growmighty.examples.ddd.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * 금액 값 객체 (Value Object)
 * 
 * 특징:
 * 1. 불변성 (Immutable)
 * 2. 자가 검증 (Self-Validation)
 * 3. 도메인 개념 표현
 * 4. 비즈니스 로직 포함
 */
@Getter
@EqualsAndHashCode
public class Money {
    
    private final BigDecimal amount;
    
    private Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다");
        }
        // ✅ DDD 원칙: 값 객체는 자가 검증을 통해 항상 유효한 상태 유지
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 음수일 수 없습니다");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("금액은 소수점 둘째 자리까지만 가능합니다");
        }
        this.amount = amount;
    }
    
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
    
    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }
    
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
    
    // 비즈니스 로직을 값 객체에 캡슐화
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }
    
    public Money multiply(BigDecimal rate) {
        return new Money(this.amount.multiply(rate));
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public Money min(Money other) {
        return this.amount.compareTo(other.amount) <= 0 ? this : other;
    }
    
    @Override
    public String toString() {
        return amount.toString();
    }
}
