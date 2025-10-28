package com.growmighty.examples.ddd.infrastructure.repository;

import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.vo.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA 기반 주문 리포지터리 인터페이스
 * 
 * 인프라스트럭처 계층의 역할:
 * 1. Spring Data JPA의 자동 구현 기능 활용
 * 2. JPA/Spring Data 기술적 세부사항 캡슐화
 * 3. 도메인 계층과 데이터베이스 사이의 어댑터 역할
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 특정 고객의 주문들 조회
     * Spring Data JPA가 자동으로 메서드 이름을 분석하여 구현
     * 
     * @param customerId 고객 ID
     * @return 고객의 주문 목록
     */
    List<Order> findByCustomerId(CustomerId customerId);
}
