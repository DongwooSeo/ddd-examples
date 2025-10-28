package com.growmighty.examples.ddd.domain.event;

import com.growmighty.examples.ddd.domain.vo.Money;
import lombok.Getter;

/**
 * 주문 생성 이벤트
 * 
 * 용도:
 * - 주문 생성 알림 발송
 * - 통계 데이터 업데이트
 * - 추천 시스템 학습 데이터로 활용
 */
@Getter
public class OrderCreatedEvent extends OrderDomainEvent {
    private final Long customerId;
    private final Money totalAmount;
    
    public OrderCreatedEvent(Long customerId, Money totalAmount) {
        super();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }
}

