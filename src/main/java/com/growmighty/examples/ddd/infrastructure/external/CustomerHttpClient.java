package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.port.CustomerPort;
import com.growmighty.examples.ddd.domain.vo.CustomerId;
import com.growmighty.examples.ddd.infrastructure.external.dto.CustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 고객 포트의 HTTP 어댑터 (Anti-Corruption Layer)
 *
 * DDD / 헥사고날 아키텍처:
 * - {@link CustomerPort} 아웃바운드 포트의 구현체(어댑터)
 * - 외부 Bounded Context(고객 도메인)와의 HTTP 통신을 담당
 * - 주문 도메인은 고객의 ID만 알고 있으면 됨
 *
 * 실제 환경에서는:
 * @RequiredArgsConstructor
 * private final WebClient webClient;
 */
@Slf4j
@Component
public class CustomerHttpClient implements CustomerPort {

    /**
     * 주문 가능 여부 확인
     */
    @Override
    public boolean canOrder(CustomerId customerId) {
        log.info("[External API] 주문 가능 여부 확인: customerId={}", customerId.getId());

        CustomerDTO customer = getCustomer(customerId.getId());
        return customer.isCanOrder();
    }

    /**
     * 고객 정보 조회
     *
     * 실제 환경에서는:
     * return webClient.get()
     *     .uri("/customers/{id}", customerId)
     *     .retrieve()
     *     .bodyToMono(CustomerDTO.class)
     *     .block();
     */
    private CustomerDTO getCustomer(Long customerId) {
        log.info("[External API] 고객 조회 요청: customerId={}", customerId);

        // Mock 데이터 반환 (예시용)
        return new CustomerDTO(
            customerId,
            "고객" + customerId,
            "customer" + customerId + "@example.com",
            true,
            "GOLD"
        );
    }
}
