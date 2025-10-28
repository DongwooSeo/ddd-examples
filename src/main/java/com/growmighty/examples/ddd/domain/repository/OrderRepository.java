package com.growmighty.examples.ddd.domain.repository;

import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.vo.CustomerId;

import java.util.List;
import java.util.Optional;

/**
 * 주문 도메인 리포지터리 인터페이스
 * 
 * DDD 원칙:
 * 1. 도메인 계층은 인프라스트럭처에 의존하지 않음
 * 2. 순수한 도메인 개념만 표현
 * 3. 구현체는 인프라스트럭처 계층에서 제공
 */
public interface OrderRepository {
    
    /**
     * 주문 저장
     * @param order 저장할 주문
     * @return 저장된 주문
     */
    Order save(Order order);
    
    /**
     * ID로 주문 조회
     * @param id 주문 ID
     * @return 주문 (없으면 Optional.empty())
     */
    Optional<Order> findById(Long id);
    
    /**
     * 특정 고객의 주문들 조회
     * @param customerId 고객 ID
     * @return 고객의 주문 목록
     */
    List<Order> findByCustomerId(CustomerId customerId);
    
    /**
     * 주문 삭제
     * @param order 삭제할 주문
     */
    void delete(Order order);
}

