package com.growmighty.examples.ddd.application.service;

import com.growmighty.examples.ddd.domain.vo.CouponCode;
import com.growmighty.examples.ddd.domain.vo.Money;

import java.util.Optional;

/**
 * 쿠폰 애플리케이션 서비스 인터페이스
 * 
 * DDD 원칙:
 * - 애플리케이션 계층에 인터페이스 정의
 * - 외부 Bounded Context와의 통신을 위한 Anti-Corruption Layer
 * - 인프라스트럭처 계층에서 구현
 * 
 * 개선사항:
 * - 외부 쿠폰 도메인에서 할인 금액 계산을 담당
 * - 주문 도메인은 쿠폰 코드와 할인 금액만 관리
 */
public interface CouponClient {
    
    /**
     * 쿠폰 할인 금액 계산
     * 
     * @param couponCode 쿠폰 코드
     * @param orderAmount 주문 금액
     * @return 할인 금액 (쿠폰이 유효하지 않으면 Optional.empty())
     */
    Optional<Money> calculateDiscount(CouponCode couponCode, Money orderAmount);
    
    /**
     * 쿠폰 사용 처리
     * 
     * @param couponCode 쿠폰 코드
     * @param customerId 고객 ID
     * @return 성공 여부
     */
    boolean useCoupon(CouponCode couponCode, String customerId);
    
    /**
     * 쿠폰 복구
     * 
     * @param couponCode 쿠폰 코드
     * @return 성공 여부
     */
    boolean restoreCoupon(CouponCode couponCode);
}
