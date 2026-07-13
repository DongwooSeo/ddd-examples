package com.growmighty.examples.ddd.application.port;

import com.growmighty.examples.ddd.domain.vo.CustomerId;

/**
 * 고객 서비스와 주고받는 창구.
 *
 * 주문 가능 여부는 고객 도메인이 판단한다. 실제 호출 방법은 인프라 계층 구현체가 정한다.
 */
public interface CustomerPort {

    /**
     * 이 고객이 지금 주문할 수 있는 상태인지 확인한다.
     */
    boolean canOrder(CustomerId customerId);
}
