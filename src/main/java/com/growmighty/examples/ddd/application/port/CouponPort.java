package com.growmighty.examples.ddd.application.port;

import com.growmighty.examples.ddd.domain.vo.CouponCode;
import com.growmighty.examples.ddd.domain.vo.Money;

import java.util.Optional;

/**
 * 쿠폰 서비스와 주고받는 창구.
 *
 * 할인 금액 계산이나 쿠폰 유효성 판단은 쿠폰 도메인의 몫이라 여기서는 다루지 않는다.
 * 실제 호출 방법(HTTP 등)은 인프라 계층 구현체가 정한다.
 */
public interface CouponPort {

    /**
     * 주문 금액에 쿠폰을 적용했을 때의 할인 금액을 쿠폰 서비스에 물어본다.
     * 쓸 수 없는 쿠폰이면 빈 값을 돌려준다.
     */
    Optional<Money> calculateDiscount(CouponCode couponCode, Money orderAmount);

    /**
     * 쿠폰을 사용 처리한다.
     */
    boolean useCoupon(CouponCode couponCode, String customerId);

    /**
     * 사용한 쿠폰을 되돌린다. (주문 취소 등)
     */
    boolean restoreCoupon(CouponCode couponCode);
}
