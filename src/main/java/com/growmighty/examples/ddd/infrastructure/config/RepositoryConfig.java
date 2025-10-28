package com.growmighty.examples.ddd.infrastructure.config;

import com.growmighty.examples.ddd.domain.repository.OrderRepository;
import com.growmighty.examples.ddd.infrastructure.repository.JpaOrderRepository;
import com.growmighty.examples.ddd.infrastructure.repository.OrderRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 리포지터리 설정 클래스
 * 
 * DDD 원칙:
 * - 도메인 계층은 인프라스트럭처에 의존하지 않음
 * - 인프라스트럭처 계층에서 도메인 인터페이스의 구현체를 제공
 * - 의존성 역전 원칙 (DIP) 적용
 * - 어댑터 패턴으로 도메인과 인프라스트럭처 분리
 */
@Configuration
public class RepositoryConfig {
    
    /**
     * 주문 리포지터리 어댑터 등록
     * 
     * 어댑터 패턴을 사용하여:
     * - 도메인 인터페이스 (OrderRepository)와 
     * - JPA 인터페이스 (JpaOrderRepository)를 연결
     */
    @Bean
    public OrderRepository orderRepository(JpaOrderRepository jpaOrderRepository) {
        return new OrderRepositoryAdapter(jpaOrderRepository);
    }
}
