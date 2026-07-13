package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.port.CustomerPort;
import com.growmighty.examples.ddd.domain.vo.CustomerId;
import com.growmighty.examples.ddd.infrastructure.external.dto.CustomerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * 고객 서비스 호출을 담당하는 어댑터.
 *
 * 주문 가능 여부의 판단 기준은 고객 도메인이 갖고 있다.
 * 주문 쪽은 그 결과만 물어본다.
 */
@Slf4j
@Component
public class CustomerRestClient implements CustomerPort {

    private final RestClient restClient;

    public CustomerRestClient(RestClient.Builder builder,
                              @Value("${external.customer.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean canOrder(CustomerId customerId) {
        try {
            CustomerResponse response = restClient.get()
                    .uri("/customers/{id}", customerId.getId())
                    .retrieve()
                    .body(CustomerResponse.class);

            return response != null && response.canOrder();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
