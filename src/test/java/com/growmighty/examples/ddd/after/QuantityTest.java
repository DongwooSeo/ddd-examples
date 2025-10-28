package com.growmighty.examples.ddd.after;

import com.growmighty.examples.ddd.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * 수량 값 객체 테스트
 */
class QuantityTest {
    
    @Test
    @DisplayName("최소 수량보다 작으면 생성할 수 없다")
    void belowMinimum() {
        assertThatThrownBy(() -> Quantity.of(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("최소 1개 이상");
    }
    
    @Test
    @DisplayName("최대 수량보다 크면 생성할 수 없다")
    void aboveMaximum() {
        assertThatThrownBy(() -> Quantity.of(101))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("최대 100개");
    }
    
    @Test
    @DisplayName("유효한 범위의 수량은 생성할 수 있다")
    void validQuantity() {
        Quantity quantity = Quantity.of(50);
        
        assertThat(quantity.getValue()).isEqualTo(50);
    }
}

