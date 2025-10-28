package com.growmighty.examples.ddd.domain.vo;

/**
 * 주문 우선순위 값 객체 (Value Object)
 * 
 * DDD에서 Value Object는:
 * 1. 불변성 (Immutable)
 * 2. 동등성 (Equality) - 값으로 비교
 * 3. 도메인 개념 표현
 * 
 * 주문 우선순위는 주문 도메인의 핵심 개념으로,
 * 도메인 서비스나 다른 도메인 객체에서 재사용 가능해야 합니다.
 */
public enum OrderPriority {
    HIGH("높음", 1),
    MEDIUM("중간", 2),
    LOW("낮음", 3);
    
    private final String description;
    private final int level;
    
    OrderPriority(String description, int level) {
        this.description = description;
        this.level = level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * 다른 우선순위와 비교하여 더 높은 우선순위인지 확인
     * @param other 비교할 우선순위
     * @return 현재 우선순위가 더 높으면 true
     */
    public boolean isHigherThan(OrderPriority other) {
        return this.level < other.level; // 숫자가 작을수록 높은 우선순위
    }
    
    /**
     * 다른 우선순위와 비교하여 더 낮은 우선순위인지 확인
     * @param other 비교할 우선순위
     * @return 현재 우선순위가 더 낮으면 true
     */
    public boolean isLowerThan(OrderPriority other) {
        return this.level > other.level;
    }
    
    /**
     * 두 우선순위가 같은지 확인
     * @param other 비교할 우선순위
     * @return 같은 우선순위면 true
     */
    public boolean isSameAs(OrderPriority other) {
        return this.level == other.level;
    }
}
