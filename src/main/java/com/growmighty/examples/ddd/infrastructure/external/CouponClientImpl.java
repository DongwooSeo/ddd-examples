package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.service.CouponClient;
import com.growmighty.examples.ddd.domain.vo.CouponCode;
import com.growmighty.examples.ddd.domain.vo.Money;
import com.growmighty.examples.ddd.infrastructure.external.dto.CouponDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 쿠폰 도메인 클라이언트 (Anti-Corruption Layer)
 * 
 * DDD Pattern:
 * - 외부 Bounded Context(쿠폰 도메인)와의 통신을 담당
 * - 외부 쿠폰 도메인에서 할인 금액 계산을 담당
 * - 주문 도메인은 쿠폰 코드와 할인 금액만 관리
 * 
 * 실제 환경에서는:
 * @RequiredArgsConstructor
 * private final WebClient webClient;
 */
@Slf4j
@Component
public class CouponClientImpl implements CouponClient {
    
    /**
     * 쿠폰 할인 금액 계산
     * 
     * 실제 환경에서는:
     * return webClient.post()
     *     .uri("/coupons/{code}/calculate", couponCode.getValue())
     *     .bodyValue(new CalculateDiscountRequest(orderAmount))
     *     .retrieve()
     *     .bodyToMono(Money.class)
     *     .blockOptional();
     */
    @Override
    public Optional<Money> calculateDiscount(CouponCode couponCode, Money orderAmount) {
        log.info("[External API] 쿠폰 할인 계산 요청: couponCode={}, orderAmount={}", 
                couponCode.getValue(), orderAmount);
        
        // Mock 데이터로 쿠폰 정보 조회
        Optional<CouponDTO> couponOpt = getCouponFromExternal(couponCode.getValue());
        if (couponOpt.isEmpty()) {
            return Optional.empty();
        }
        
        CouponDTO coupon = couponOpt.get();
        
        // 외부 쿠폰 도메인의 비즈니스 로직에 따라 할인 금액 계산
        return calculateDiscountAmount(coupon, orderAmount);
    }
    
    /**
     * 외부 쿠폰 도메인에서 쿠폰 정보 조회
     */
    private Optional<CouponDTO> getCouponFromExternal(String couponCode) {
        // Mock 데이터 반환 (예시용)
        if (couponCode.startsWith("WELCOME")) {
            return Optional.of(new CouponDTO(
                couponCode,
                "PERCENTAGE",
                BigDecimal.valueOf(10), // 10% 할인
                BigDecimal.valueOf(10000), // 최소 주문 금액 10,000원
                true
            ));
        }
        
        return Optional.empty();
    }
    
    /**
     * 외부 쿠폰 도메인의 비즈니스 로직에 따라 할인 금액 계산
     */
    private Optional<Money> calculateDiscountAmount(CouponDTO coupon, Money orderAmount) {
        if (!coupon.isValid()) {
            return Optional.empty();
        }
        
        // 최소 주문 금액 검증
        Money minimumAmount = Money.of(coupon.getMinimumOrderAmount());
        if (orderAmount.isLessThan(minimumAmount)) {
            log.warn("최소 주문 금액 미달: required={}, actual={}", minimumAmount, orderAmount);
            return Optional.empty();
        }
        
        // 할인 타입에 따라 계산
        Money discountAmount = switch (coupon.getDiscountType()) {
            case "PERCENTAGE" -> calculatePercentageDiscount(orderAmount, coupon.getDiscountValue());
            case "FIXED_AMOUNT" -> Money.of(coupon.getDiscountValue());
            default -> Money.zero();
        };
        
        // 할인 금액이 주문 금액을 초과하지 않도록 제한
        if (discountAmount.isGreaterThan(orderAmount)) {
            discountAmount = orderAmount;
        }
        
        log.info("쿠폰 할인 계산 완료: discountAmount={}", discountAmount);
        return Optional.of(discountAmount);
    }
    
    /**
     * 퍼센트 할인 계산
     */
    private Money calculatePercentageDiscount(Money orderAmount, BigDecimal discountRate) {
        BigDecimal rate = discountRate.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return orderAmount.multiply(rate);
    }
    
    @Override
    public boolean useCoupon(CouponCode couponCode, String customerId) {
        log.info("[External API] 쿠폰 사용 처리: couponCode={}, customerId={}", 
                couponCode.getValue(), customerId);
        
        // 실제 환경에서는 외부 쿠폰 도메인에 사용 처리 요청
        // return webClient.post()
        //     .uri("/coupons/{code}/use", couponCode.getValue())
        //     .bodyValue(new UseCouponRequest(customerId))
        //     .retrieve()
        //     .bodyToMono(Boolean.class)
        //     .block();
        
        return true; // Mock 성공 응답
    }
    
    @Override
    public boolean restoreCoupon(CouponCode couponCode) {
        log.info("[External API] 쿠폰 복구: couponCode={}", couponCode.getValue());
        
        // 실제 환경에서는 외부 쿠폰 도메인에 복구 요청
        // return webClient.post()
        //     .uri("/coupons/{code}/restore", couponCode.getValue())
        //     .retrieve()
        //     .bodyToMono(Boolean.class)
        //     .block();
        
        return true; // Mock 성공 응답
    }
}

