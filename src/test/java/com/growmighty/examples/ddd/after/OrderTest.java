package com.growmighty.examples.ddd.after;

import com.growmighty.examples.ddd.domain.entity.Order;
import com.growmighty.examples.ddd.domain.entity.OrderItem;
import com.growmighty.examples.ddd.domain.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DDD 적용 후 - 도메인 모델 테스트
 * 
 * 장점:
 * 1. Mock 객체 불필요 (순수 도메인 로직 테스트)
 * 2. 빠른 실행 속도 (DB, 외부 의존성 없음)
 * 3. 명확한 테스트 의도
 * 4. 비즈니스 규칙 문서화
 */
class OrderTest {
    
    @Test
    @DisplayName("주문 생성 시 총 금액이 올바르게 계산된다")
    void calculateTotalAmount() {
        // Given
        OrderItem item1 = new OrderItem(
            ProductId.of(1L),
            "노트북",
            Money.of(1000000),
            Quantity.of(2)
        );
        
        OrderItem item2 = new OrderItem(
            ProductId.of(2L),
            "마우스",
            Money.of(50000),
            Quantity.of(1)
        );
        
        // When
        Order order = Order.create(
            CustomerId.of(1L),
            List.of(item1, item2),
            ShippingAddress.of("서울시 강남구 테헤란로 123")
        );
        
        // Then
        assertThat(order.calculateTotalAmount())
            .isEqualTo(Money.of(2050000));  // (1,000,000 * 2) + (50,000 * 1)
    }
    
    @Test
    @DisplayName("쿠폰 적용 시 할인 금액이 차감된다")
    void applyCoupon() {
        // Given
        Order order = createOrder();
        Money discountAmount = Money.of(50000);
        
        // When
        order.applyCoupon(
            CouponCode.of("WELCOME2024"),
            discountAmount
        );
        
        // Then
        assertThat(order.getDiscountAmount()).isEqualTo(discountAmount);
        assertThat(order.calculateFinalAmount())
            .isEqualTo(Money.of(2000000));  // 2,050,000 - 50,000
    }
    
    @Test
    @DisplayName("대기 상태의 주문만 결제할 수 있다")
    void payOrder_success() {
        // Given
        Order order = createOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        
        // When
        order.pay();
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }
    
    @Test
    @DisplayName("이미 결제된 주문은 다시 결제할 수 없다")
    void payOrder_alreadyPaid() {
        // Given
        Order order = createOrder();
        order.pay();  // 첫 번째 결제
        
        // When & Then
        assertThatThrownBy(() -> order.pay())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("결제할 수 없습니다");
    }
    
    @Test
    @DisplayName("주문 취소 시 상태가 CANCELLED로 변경된다")
    void cancelOrder_success() {
        // Given
        CustomerId customerId = CustomerId.of(1L);
        Order order = createOrder(customerId);
        
        // When
        order.cancel(customerId);
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
    
    @Test
    @DisplayName("다른 고객은 주문을 취소할 수 없다")
    void cancelOrder_notOwner() {
        // Given
        Order order = createOrder(CustomerId.of(1L));
        CustomerId otherCustomerId = CustomerId.of(2L);
        
        // When & Then
        assertThatThrownBy(() -> order.cancel(otherCustomerId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("본인의 주문만 취소할 수 있습니다");
    }
    
    @Test
    @DisplayName("배송 중인 주문은 취소할 수 없다")
    void cancelOrder_shipped() {
        // Given
        Order order = createOrder();
        order.pay();
        order.ship();  // 배송 시작
        
        // When & Then
        assertThatThrownBy(() -> order.cancel(CustomerId.of(1L)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("SHIPPED 상태에서는 취소할 수 없습니다");
    }
    
    @Test
    @DisplayName("주문 항목이 없으면 주문을 생성할 수 없다")
    void createOrder_noItems() {
        // When & Then
        assertThatThrownBy(() -> 
            Order.create(
                CustomerId.of(1L),
                List.of(),  // 빈 리스트
                ShippingAddress.of("서울시 강남구")
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("주문 항목은 최소 1개 이상이어야 합니다");
    }
    
    // ========== 테스트 헬퍼 메서드 ==========
    
    private Order createOrder() {
        return createOrder(CustomerId.of(1L));
    }
    
    private Order createOrder(CustomerId customerId) {
        OrderItem item1 = new OrderItem(
            ProductId.of(1L),
            "노트북",
            Money.of(1000000),
            Quantity.of(2)
        );
        
        OrderItem item2 = new OrderItem(
            ProductId.of(2L),
            "마우스",
            Money.of(50000),
            Quantity.of(1)
        );
        
        return Order.create(
            customerId,
            List.of(item1, item2),
            ShippingAddress.of("서울시 강남구 테헤란로 123")
        );
    }
}
