package com.growmighty.examples.ddd.domain.entity;

import com.growmighty.examples.ddd.domain.event.OrderCancelledEvent;
import com.growmighty.examples.ddd.domain.event.OrderCreatedEvent;
import com.growmighty.examples.ddd.domain.event.OrderPaidEvent;
import com.growmighty.examples.ddd.domain.vo.CustomerId;
import com.growmighty.examples.ddd.domain.vo.CouponCode;
import com.growmighty.examples.ddd.domain.vo.Money;
import com.growmighty.examples.ddd.domain.vo.OrderStatus;
import com.growmighty.examples.ddd.domain.vo.ShippingAddress;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot; // <-- 해당 부분은 문서를 참고하세요

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 주문 애그리거트 루트 (Aggregate Root)
 *
 * 핵심 개념:
 * 1. 애그리거트 내부 일관성 유지
 * 2. 비즈니스 규칙 캡슐화
 * 3. 불변식(Invariant) 보호
 * 4. 도메인 이벤트 발행
 *
 * 애그리거트 경계:
 * - Order (루트)
 * - OrderItem (내부 엔티티)
 * - Money, Quantity, OrderStatus (값 객체)
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AbstractAggregateRoot<Order> {

    private static final int MAX_UNPAID_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "customer_id"))
    private CustomerId customerId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "shipping_address"))
    })
    private ShippingAddress shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "coupon_code"))
    })
    private CouponCode couponCode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount"))
    })
    private Money discountAmount;

    private LocalDateTime orderedAt;

    private LocalDateTime paidAt;

    // ========== 생성 메서드 ==========

    /**
     * 주문 생성 팩토리 메서드
     * 모든 비즈니스 규칙을 여기서 검증
     */
    public static Order create(
            CustomerId customerId,
            List<OrderItem> orderItems,
            ShippingAddress shippingAddress) {

        // 비즈니스 규칙 검증
        validateOrderItems(orderItems);

        Order order = new Order();
        order.customerId = customerId;
        order.orderItems.addAll(orderItems);
        order.shippingAddress = shippingAddress;
        order.status = OrderStatus.PENDING;
        order.orderedAt = LocalDateTime.now();
        order.discountAmount = Money.zero();

        // 도메인 이벤트 발행
        order.registerEvent(new OrderCreatedEvent(
                order.customerId.getId(),
                order.calculateTotalAmount()
        ));

        return order;
    }

    private static void validateOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 항목은 최소 1개 이상이어야 합니다");
        }
    }

    // ========== 비즈니스 로직 ==========

    /**
     * 쿠폰 적용
     * 할인 금액 계산 로직을 도메인 모델에 캡슐화
     */
    public void applyCoupon(CouponCode couponCode, Money discountAmount) {
        validateCouponApplication(discountAmount);

        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
    }

    private void validateCouponApplication(Money discountAmount) {
        if (discountAmount == null) {
            throw new IllegalArgumentException("할인 금액은 null일 수 없습니다");
        }

        Money totalAmount = calculateTotalAmount();
        if (discountAmount.isGreaterThan(totalAmount)) {
            throw new IllegalStateException("할인 금액이 주문 금액보다 클 수 없습니다");
        }
    }

    /**
     * 결제 처리
     * 상태 변경과 관련된 비즈니스 규칙을 캡슐화
     */
    public void pay() {
        // 비즈니스 규칙: 대기 상태에서만 결제 가능
        if (!status.canBePaid()) {
            throw new IllegalStateException(
                    String.format("%s 상태에서는 결제할 수 없습니다", status.getDescription()));
        }

        // 비즈니스 규칙: 결제 금액이 0보다 커야 함
        Money finalAmount = calculateFinalAmount();
        if (!finalAmount.isPositive()) {
            throw new IllegalStateException("결제 금액은 0보다 커야 합니다");
        }

        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();

        // 도메인 이벤트 발행 (결제 완료 알림 등에 사용)
        registerEvent(new OrderPaidEvent(
                this.id,
                this.customerId.getId(),
                finalAmount,
                this.paidAt
        ));
    }

    /**
     * 주문 취소
     * 복잡한 취소 규칙을 도메인 모델에 캡슐화
     */
    public void cancel(CustomerId requesterId) {
        // 비즈니스 규칙: 본인만 취소 가능
        if (!this.customerId.equals(requesterId)) {
            throw new IllegalStateException("본인의 주문만 취소할 수 있습니다");
        }

        // 비즈니스 규칙: 취소 가능한 상태인지 확인
        if (!status.canBeCancelled()) {
            throw new IllegalStateException(
                    String.format("%s 상태에서는 취소할 수 없습니다", status.getDescription()));
        }

        // 비즈니스 규칙: 결제 후 24시간 이내만 취소 가능
        if (paidAt != null) {
            LocalDateTime cancelDeadline = paidAt.plusHours(MAX_UNPAID_HOURS);
            if (LocalDateTime.now().isAfter(cancelDeadline)) {
                throw new IllegalStateException(
                        String.format("결제 후 %d시간이 지나 취소할 수 없습니다", MAX_UNPAID_HOURS));
            }
        }

        this.status = OrderStatus.CANCELLED;
        
        registerEvent(new OrderCancelledEvent(
                this.id,
                this.customerId.getId(),
                this.orderItems,
                this.couponCode
        ));
    }

    /**
     * 배송 시작
     */
    public void ship() {
        if (!status.canBeShipped()) {
            throw new IllegalStateException(
                    String.format("%s 상태에서는 배송을 시작할 수 없습니다", status.getDescription()));
        }

        this.status = OrderStatus.SHIPPED;
    }

    // ========== 조회 메서드 ==========

    /**
     * 총 주문 금액 계산 (할인 전)
     */
    public Money calculateTotalAmount() {
        return orderItems.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(Money.zero(), Money::add);
    }

    /**
     * 최종 결제 금액 계산 (할인 후)
     */
    public Money calculateFinalAmount() {
        Money totalAmount = calculateTotalAmount();
        Money finalAmount = totalAmount.subtract(discountAmount);

        // 음수 방지
        return finalAmount.isNegative() ? Money.zero() : finalAmount;
    }

    /**
     * 주문 항목 수 조회
     */
    public int getOrderItemCount() {
        return orderItems.size();
    }

    /**
     * 읽기 전용 주문 항목 목록 반환
     * 외부에서 직접 수정 불가 (캡슐화)
     */
    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    /**
     * 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        if (!status.canBeCancelled()) {
            return false;
        }

        if (paidAt != null) {
            LocalDateTime deadline = paidAt.plusHours(MAX_UNPAID_HOURS);
            return LocalDateTime.now().isBefore(deadline);
        }

        return true;
    }
}