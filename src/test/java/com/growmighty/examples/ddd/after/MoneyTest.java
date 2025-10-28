package com.growmighty.examples.ddd.after;

import com.growmighty.examples.ddd.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * 값 객체 테스트
 */
class MoneyTest {
    
    @Test
    @DisplayName("금액 덧셈이 올바르게 동작한다")
    void add() {
        Money money1 = Money.of(1000);
        Money money2 = Money.of(500);
        
        Money result = money1.add(money2);
        
        assertThat(result).isEqualTo(Money.of(1500));
    }
    
    @Test
    @DisplayName("금액 곱셈이 올바르게 동작한다")
    void multiply() {
        Money money = Money.of(1000);
        
        Money result = money.multiply(3);
        
        assertThat(result).isEqualTo(Money.of(3000));
    }
    
    @Test
    @DisplayName("음수 금액은 생성할 수 없다")
    void negativeAmount() {
        assertThatThrownBy(() -> Money.of(-1000))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("같은 금액의 Money 객체는 동일하다")
    void equality() {
        Money money1 = Money.of(1000);
        Money money2 = Money.of(1000);
        
        assertThat(money1).isEqualTo(money2);
    }
}

