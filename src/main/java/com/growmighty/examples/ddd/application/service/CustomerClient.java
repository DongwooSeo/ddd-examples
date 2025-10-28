package com.growmighty.examples.ddd.application.service;

import com.growmighty.examples.ddd.domain.vo.CustomerId;

/**
 * 고객 애플리케이션 서비스 인터페이스
 * 
 * DDD 원칙:
 * - 애플리케이션 계층에 인터페이스 정의
 * - 외부 Bounded Context와의 통신을 위한 Anti-Corruption Layer
 * - 인프라스트럭처 계층에서 구현
 */
public interface CustomerClient {
    
    /**
     * 고객 주문 가능 여부 확인
     * 
     * @param customerId 고객 ID
     * @return 주문 가능 여부
     */
    boolean canOrder(CustomerId customerId);
}
