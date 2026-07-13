package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.port.CouponPort;
import com.growmighty.examples.ddd.domain.vo.CouponCode;
import com.growmighty.examples.ddd.domain.vo.Money;
import com.growmighty.examples.ddd.infrastructure.external.dto.CouponDiscountRequest;
import com.growmighty.examples.ddd.infrastructure.external.dto.CouponDiscountResponse;
import com.growmighty.examples.ddd.infrastructure.external.dto.CouponUseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * 쿠폰 서비스 호출을 담당하는 어댑터.
 *
 * 할인 금액이 얼마인지, 쿠폰이 유효한지는 전부 쿠폰 도메인이 판단한다.
 * 여기서는 요청을 보내고 돌아온 값을 주문 도메인 타입으로 옮겨 담기만 한다.
 */
@Slf4j
@Component
public class CouponRestClient implements CouponPort {

    private final RestClient restClient;

    public CouponRestClient(RestClient.Builder builder,
                            @Value("${external.coupon.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<Money> calculateDiscount(CouponCode couponCode, Money orderAmount) {
        try {
            CouponDiscountResponse response = restClient.post()
                    .uri("/coupons/{code}/discount", couponCode.getValue())
                    .body(new CouponDiscountRequest(orderAmount.getAmount()))
                    .retrieve()
                    .body(CouponDiscountResponse.class);

            return Optional.ofNullable(response)
                    .map(CouponDiscountResponse::discountAmount)
                    .map(Money::of);
        } catch (HttpClientErrorException.NotFound e) {
            // 쿠폰이 없거나 조건을 못 채우면 쿠폰 서비스가 404로 답한다.
            return Optional.empty();
        }
    }

    @Override
    public boolean useCoupon(CouponCode couponCode, String customerId) {
        restClient.post()
                .uri("/coupons/{code}/use", couponCode.getValue())
                .body(new CouponUseRequest(customerId))
                .retrieve()
                .toBodilessEntity();
        return true;
    }

    @Override
    public boolean restoreCoupon(CouponCode couponCode) {
        restClient.post()
                .uri("/coupons/{code}/restore", couponCode.getValue())
                .retrieve()
                .toBodilessEntity();
        return true;
    }
}
