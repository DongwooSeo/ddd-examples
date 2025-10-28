package com.growmighty.examples.ddd.domain.event;

import com.growmighty.examples.ddd.domain.entity.OrderItem;
import com.growmighty.examples.ddd.domain.vo.CouponCode;
import lombok.Getter;
import java.util.List;

/**
 * 주문 취소 이벤트
 * 
 * 용도:
 * - 재고 복구 (이벤트 핸들러에서 처리)
 * - 쿠폰 복구
 * - 취소 알림 발송
 * - 결제 취소 처리
 */
@Getter
public class OrderCancelledEvent extends OrderDomainEvent {
    private final Long orderId;
    private final Long customerId;
    private final List<OrderItem> orderItems;
    private final CouponCode couponCode;
    
    public OrderCancelledEvent(
            Long orderId, 
            Long customerId, 
            List<OrderItem> orderItems, 
            CouponCode couponCode) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderItems = orderItems;
        this.couponCode = couponCode;
    }
}

