package com.growmighty.examples.ddd.application.port;

import com.growmighty.examples.ddd.domain.vo.CustomerId;

/**
 * 고객 도메인 아웃바운드 포트 (Outbound Port)
 *
 * DDD / 헥사고날 아키텍처 원칙:
 * - 애플리케이션 계층에 포트(인터페이스)를 정의
 * - 외부 Bounded Context와의 통신을 위한 Anti-Corruption Layer
 * - 실제 통신 방식(HTTP, gRPC 등)은 인프라스트럭처 계층의 어댑터에서 구현
 */
public interface CustomerPort {

    /**
     * 고객 주문 가능 여부 확인
     *
     * @param customerId 고객 ID
     * @return 주문 가능 여부
     */
    boolean canOrder(CustomerId customerId);
}
