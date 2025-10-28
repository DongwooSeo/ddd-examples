package com.growmighty.examples.ddd.after;

import com.growmighty.examples.ddd.domain.vo.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * 주문 상태 테스트
 */
class OrderStatusTest {
    
    @Test
    @DisplayName("PENDING 상태는 PAID로 전환할 수 있다")
    void pendingToPaid() {
        boolean canTransition = OrderStatus.PENDING
            .canTransitionTo(OrderStatus.PAID);
        
        assertThat(canTransition).isTrue();
    }
    
    @Test
    @DisplayName("SHIPPED 상태는 취소할 수 없다")
    void shippedCannotBeCancelled() {
        boolean canBeCancelled = OrderStatus.SHIPPED.canBeCancelled();
        
        assertThat(canBeCancelled).isFalse();
    }
    
    @Test
    @DisplayName("PAID 상태는 SHIPPED로 전환할 수 있다")
    void paidToShipped() {
        boolean canTransition = OrderStatus.PAID
            .canTransitionTo(OrderStatus.SHIPPED);
        
        assertThat(canTransition).isTrue();
    }
}

