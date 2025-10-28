package com.growmighty.examples.ddd.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 도메인 이벤트 기반 클래스
 */
@Getter
public abstract class OrderDomainEvent {
    private final LocalDateTime occurredAt;
    
    protected OrderDomainEvent() {
        this.occurredAt = LocalDateTime.now();
    }
}

