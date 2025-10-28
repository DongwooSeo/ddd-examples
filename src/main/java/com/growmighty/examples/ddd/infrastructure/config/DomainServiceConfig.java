package com.growmighty.examples.ddd.infrastructure.config;

import com.growmighty.examples.ddd.domain.service.OrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 도메인 서비스 설정 클래스
 * <p>
 * DDD 원칙:
 * - 도메인 계층은 인프라스트럭처에 의존하지 않음
 * - 인프라스트럭처 계층에서 도메인 서비스를 빈으로 등록
 * - 도메인 서비스는 순수한 도메인 로직만 포함
 * <p>
 * 개선사항:
 * - CouponDomainService 제거: 외부 쿠폰 도메인의 로직을 주문 도메인에서 처리하지 않음
 * - 외부 도메인과의 통신은 Application Service에서 처리
 */
@Configuration
public class DomainServiceConfig {
    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainService();
    }
}
