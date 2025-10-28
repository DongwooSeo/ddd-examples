package com.growmighty.examples.ddd.domain.vo;

/**
 * 주문 상태 값 객체
 * 
 * 비즈니스 규칙을 타입으로 표현하고 상태 전이 규칙을 캡슐화
 */
public enum OrderStatus {
    
    PENDING("주문 대기") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == PAID || newStatus == CANCELLED;
        }
        
        @Override
        public boolean canBeCancelled() {
            return true;
        }
    },
    
    PAID("결제 완료") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == SHIPPED || newStatus == CANCELLED;
        }
        
        @Override
        public boolean canBeCancelled() {
            return true;  // 단, 시간 제한 있음 (24시간)
        }
    },
    
    SHIPPED("배송 중") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DELIVERED;
        }
        
        @Override
        public boolean canBeCancelled() {
            return false;
        }
    },
    
    DELIVERED("배송 완료") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
        
        @Override
        public boolean canBeCancelled() {
            return false;
        }
    },
    
    CANCELLED("취소됨") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
        
        @Override
        public boolean canBeCancelled() {
            return false;
        }
    };
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 상태 전이 가능 여부 체크
     */
    public abstract boolean canTransitionTo(OrderStatus newStatus);
    
    /**
     * 취소 가능 여부
     */
    public abstract boolean canBeCancelled();
    
    /**
     * 결제 가능 상태인지 확인
     */
    public boolean canBePaid() {
        return this == PENDING;
    }
    
    /**
     * 배송 가능 상태인지 확인
     */
    public boolean canBeShipped() {
        return this == PAID;
    }
}
