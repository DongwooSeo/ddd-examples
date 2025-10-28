package com.growmighty.examples.ddd.domain.event;

import com.growmighty.examples.ddd.domain.vo.Money;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 주문 결제 완료 이벤트
 * 
 * 용도:
 * - 결제 완료 알림 발송
 * - 배송 프로세스 시작
 * - 회계 시스템 연동
 */
@Getter
public class OrderPaidEvent extends OrderDomainEvent {
    private final Long orderId;
    private final Long customerId;
    private final Money paidAmount;
    private final LocalDateTime paidAt;
    
    public OrderPaidEvent(Long orderId, Long customerId, Money paidAmount, LocalDateTime paidAt) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.paidAmount = paidAmount;
        this.paidAt = paidAt;
    }
}

