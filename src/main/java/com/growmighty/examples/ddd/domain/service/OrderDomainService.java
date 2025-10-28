package com.growmighty.examples.ddd.domain.service;

import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.vo.CustomerId;
import com.growmighty.examples.ddd.domain.vo.Money;
import com.growmighty.examples.ddd.domain.vo.OrderPriority;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 도메인 서비스 (Domain Service)
 *
 * 도메인 서비스는 언제 사용하나요?
 * 1. 하나의 엔티티에 속하지 않는 비즈니스 로직
 * 2. 여러 애그리거트에 걸친 비즈니스 규칙
 * 3. 도메인 개념이지만 엔티티나 값 객체로 표현하기 어려운 경우
 *
 * 이 예시에서 다루는 간단한 도메인 서비스:
 * 1. 주문 금액 할인 계산 (고객 등급별)
 * 2. 주문 우선순위 결정 (금액 기반)
 * 3. 주문 검증 (간단한 비즈니스 규칙)
 */
@Slf4j
public class OrderDomainService {

    private static final Money VIP_THRESHOLD = Money.of(new BigDecimal("100000")); // 10만원
    private static final Money PREMIUM_THRESHOLD = Money.of(new BigDecimal("50000")); // 5만원

    /**
     * 고객 등급별 할인율 계산
     * 
     * 비즈니스 규칙:
     * - VIP 고객 (총 주문 금액 10만원 이상): 10% 할인
     * - 프리미엄 고객 (총 주문 금액 5만원 이상): 5% 할인
     * - 일반 고객: 할인 없음
     * 
     * 왜 도메인 서비스인가?
     * - 고객의 과거 주문 이력을 분석해야 함
     * - 하나의 엔티티에 속하지 않는 로직
     * - 도메인 개념이지만 엔티티로 표현하기 어려움
     */
    public Money calculateDiscount(CustomerId customerId, Money orderAmount, List<Order> customerOrderHistory) {
        log.info("할인 계산 시작: customerId={}, orderAmount={}", customerId.getId(), orderAmount.getAmount());
        
        // 1. 고객의 총 주문 금액 계산
        Money totalOrderAmount = customerOrderHistory.stream()
                .map(Order::calculateTotalAmount)
                .reduce(Money.zero(), Money::add);
        
        // 2. 현재 주문을 포함한 총 금액
        Money totalWithNewOrder = totalOrderAmount.add(orderAmount);
        
        // 3. 할인율 결정
        BigDecimal discountRate = BigDecimal.ZERO;
        String customerGrade = "일반";
        
        if (totalWithNewOrder.isGreaterThanOrEqual(VIP_THRESHOLD)) {
            discountRate = new BigDecimal("0.10"); // 10% 할인
            customerGrade = "VIP";
        } else if (totalWithNewOrder.isGreaterThanOrEqual(PREMIUM_THRESHOLD)) {
            discountRate = new BigDecimal("0.05"); // 5% 할인
            customerGrade = "프리미엄";
        }
        
        // 4. 할인 금액 계산
        Money discountAmount = Money.of(orderAmount.getAmount().multiply(discountRate));
        
        log.info("할인 계산 완료: 고객등급={}, 할인율={}%, 할인금액={}", 
                customerGrade, discountRate.multiply(new BigDecimal("100")), discountAmount.getAmount());
        
        return discountAmount;
    }

    /**
     * 주문 우선순위 결정
     * 
     * 비즈니스 규칙:
     * - 10만원 이상: 높은 우선순위
     * - 5만원 이상: 중간 우선순위
     * - 5만원 미만: 낮은 우선순위
     * 
     * 왜 도메인 서비스인가?
     * - 주문 금액을 기반으로 한 비즈니스 규칙
     * - 도메인 개념이지만 엔티티로 표현하기 어려움
     */
    public OrderPriority determinePriority(Money orderAmount) {
        log.info("주문 우선순위 결정: orderAmount={}", orderAmount.getAmount());
        
        OrderPriority priority;
        
        if (orderAmount.isGreaterThanOrEqual(VIP_THRESHOLD)) {
            priority = OrderPriority.HIGH;
        } else if (orderAmount.isGreaterThanOrEqual(PREMIUM_THRESHOLD)) {
            priority = OrderPriority.MEDIUM;
        } else {
            priority = OrderPriority.LOW;
        }
        
        log.info("주문 우선순위 결정 완료: {}", priority.getDescription());
        return priority;
    }

    /**
     * 주문 검증 (간단한 비즈니스 규칙)
     * 
     * 비즈니스 규칙:
     * - 최소 주문 금액: 1,000원
     * - 최대 주문 금액: 1,000,000원
     * - 주문 항목 수: 1개 이상 20개 이하
     * 
     * 왜 도메인 서비스인가?
     * - 여러 조건을 종합한 복잡한 검증 로직
     * - 도메인 지식이지만 엔티티로 표현하기 어려움
     */
    public void validateOrder(Money orderAmount, int itemCount) {
        log.info("주문 검증 시작: orderAmount={}, itemCount={}", orderAmount.getAmount(), itemCount);
        
        // 최소 주문 금액 검증
        Money minimumAmount = Money.of(new BigDecimal("1000"));
        if (orderAmount.isLessThan(minimumAmount)) {
            throw new IllegalArgumentException("최소 주문 금액은 1,000원입니다");
        }
        
        // 최대 주문 금액 검증
        Money maximumAmount = Money.of(new BigDecimal("1000000"));
        if (orderAmount.isGreaterThan(maximumAmount)) {
            throw new IllegalArgumentException("최대 주문 금액은 1,000,000원입니다");
        }
        
        // 주문 항목 수 검증
        if (itemCount < 1) {
            throw new IllegalArgumentException("주문 항목은 최소 1개 이상이어야 합니다");
        }
        
        if (itemCount > 20) {
            throw new IllegalArgumentException("주문 항목은 최대 20개까지만 가능합니다");
        }
        
        log.info("주문 검증 완료: 모든 검증 통과");
    }

}
