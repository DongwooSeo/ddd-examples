package com.growmighty.examples.ddd.infrastructure.repository;

import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.repository.OrderRepository;
import com.growmighty.examples.ddd.domain.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 주문 리포지터리 어댑터
 * 
 * 어댑터 패턴을 사용하여:
 * 1. 도메인 인터페이스 (OrderRepository)와 JPA 인터페이스 (JpaOrderRepository) 연결
 * 2. 도메인 계층과 인프라스트럭처 계층의 분리
 * 3. 의존성 역전 원칙 (DIP) 적용
 * 
 * 역할:
 * - 도메인 인터페이스를 구현
 * - JPA 리포지터리를 위임하여 실제 데이터베이스 작업 수행
 */
@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    
    private final JpaOrderRepository jpaOrderRepository;
    
    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return jpaOrderRepository.findById(id);
    }
    
    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return jpaOrderRepository.findByCustomerId(customerId);
    }
    
    @Override
    public void delete(Order order) {
        jpaOrderRepository.delete(order);
    }
}
