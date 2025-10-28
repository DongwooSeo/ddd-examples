package com.growmighty.examples.ddd.domain.entity;

import com.growmighty.examples.ddd.domain.vo.Money;
import com.growmighty.examples.ddd.domain.vo.ProductId;
import com.growmighty.examples.ddd.domain.vo.Quantity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 항목 엔티티
 * 
 * DDD 적용 후 - 풍부한 도메인 모델 (Rich Domain Model)
 * - 비즈니스 로직을 엔티티에 포함
 * - 불변성을 통한 안전성 확보
 * - 도메인 언어로 메서드 명명
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "product_id"))
    private ProductId productId;
    
    private String productName;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price"))
    })
    private Money price;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "quantity"))
    })
    private Quantity quantity;
    
    // 생성자를 통한 불변성 보장
    public OrderItem(ProductId productId, String productName, Money price, Quantity quantity) {
        validateProduct(productId, productName, price);
        
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
    
    private void validateProduct(ProductId productId, String productName, Money price) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (price == null || !price.isPositive()) {
            throw new IllegalArgumentException("상품 가격은 0보다 커야 합니다");
        }
    }
    
    /**
     * 주문 항목의 총 금액 계산
     * 비즈니스 로직을 엔티티 메서드로 캡슐화
     */
    public Money calculateTotalPrice() {
        return price.multiply(quantity.getValue());
    }
    
    /**
     * 수량 변경 (비즈니스 규칙 포함)
     */
    public void changeQuantity(Quantity newQuantity) {
        if (newQuantity == null) {
            throw new IllegalArgumentException("수량은 null일 수 없습니다");
        }
        this.quantity = newQuantity;
    }
    
    /**
     * 같은 상품인지 확인
     */
    public boolean isSameProduct(ProductId productId) {
        return this.productId.equals(productId);
    }
}

