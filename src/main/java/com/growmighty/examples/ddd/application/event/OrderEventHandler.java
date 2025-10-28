package com.growmighty.examples.ddd.application.event;

import com.growmighty.examples.ddd.domain.entity.OrderItem;
import com.growmighty.examples.ddd.domain.event.OrderCancelledEvent;
import com.growmighty.examples.ddd.domain.event.OrderCreatedEvent;
import com.growmighty.examples.ddd.domain.event.OrderPaidEvent;
import com.growmighty.examples.ddd.domain.vo.CouponCode;
import com.growmighty.examples.ddd.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트 핸들러
 *
 * 장점:
 * 1. 관심사의 분리 (Separation of Concerns)
 * 2. 느슨한 결합 (Loose Coupling)
 * 3. 확장성 (Extensibility)
 * 4. 재사용성 (Reusability)
 *
 * 주의:
 * - @TransactionalEventListener를 사용하여 트랜잭션 커밋 후 이벤트 처리
 * - @Async를 사용하여 비동기 처리 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;

    /**
     * 주문 생성 이벤트 처리
     *
     * 비즈니스 로직과 분리된 부가 기능:
     * - 알림 발송
     * - 통계 데이터 업데이트
     * - 추천 시스템 학습
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 처리: customerId={}, amount={}",
                event.getCustomerId(), event.getTotalAmount());

        // 주문 생성 알림 발송
        notificationService.sendOrderCreatedNotification(
                event.getCustomerId(),
                event.getTotalAmount()
        );

        // 통계 데이터 업데이트
        analyticsService.recordOrderCreated(
                event.getCustomerId(),
                event.getTotalAmount()
        );
    }

    /**
     * 결제 완료 이벤트 처리
     *
     * DDD의 장점:
     * - 결제 로직과 알림 로직이 분리됨
     * - 새로운 처리 로직 추가가 쉬움
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("결제 완료 이벤트 처리: orderId={}, amount={}",
                event.getOrderId(), event.getPaidAmount());

        // 결제 완료 알림
        notificationService.sendPaymentConfirmation(
                event.getCustomerId(),
                event.getOrderId(),
                event.getPaidAmount()
        );

        // 배송 프로세스 시작 (별도 시스템과 연동)
        // shippingService.initiateShipping(event.getOrderId());

        // 회계 시스템 연동
        // accountingService.recordPayment(event);
    }

    /**
     * 주문 취소 이벤트 처리
     *
     * 이벤트를 통한 관심사 분리:
     * - 주문 취소 로직 (Order 애그리거트)
     * - 재고 복구 로직 (Inventory 시스템)
     * - 쿠폰 복구 로직 (Coupon 시스템)
     * - 알림 로직 (Notification 시스템)
     *
     * 각 시스템이 독립적으로 동작
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 처리: orderId={}", event.getOrderId());

        // 1. 재고 복구
        restoreInventory(event);

        // 2. 쿠폰 복구
        restoreCoupon(event);

        // 3. 취소 알림 (비동기)
        notifyOrderCancellation(event);
    }

    /**
     * 재고 복구
     */
    private void restoreInventory(OrderCancelledEvent event) {
        for (OrderItem item : event.getOrderItems()) {
            inventoryService.increaseStock(
                    item.getProductId().getId(),
                    item.getQuantity().getValue()
            );
            log.info("재고 복구: productId={}, quantity={}",
                    item.getProductId().getId(),
                    item.getQuantity().getValue());
        }
    }

    /**
     * 쿠폰 복구
     */
    private void restoreCoupon(OrderCancelledEvent event) {
        if (event.getCouponCode() != null) {
            couponService.restoreCoupon(event.getCouponCode());
            log.info("쿠폰 복구: couponCode={}", event.getCouponCode());
        }
    }

    /**
     * 취소 알림
     */
    @Async
    private void notifyOrderCancellation(OrderCancelledEvent event) {
        notificationService.sendOrderCancellation(
                event.getCustomerId(),
                event.getOrderId()
        );
    }
}

/**
 * 인프라스트럭처 서비스들 (간략화)
 */
interface InventoryService {
    void increaseStock(Long productId, int quantity);
}

interface CouponService {
    void restoreCoupon(CouponCode couponCode);
}

interface NotificationService {
    void sendOrderCreatedNotification(Long customerId, Money amount);
    void sendPaymentConfirmation(Long customerId, Long orderId, Money amount);
    void sendOrderCancellation(Long customerId, Long orderId);
}

interface AnalyticsService {
    void recordOrderCreated(Long customerId, Money amount);
}