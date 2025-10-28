package com.growmighty.examples.ddd.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배송 주소 값 객체
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {
    
    private String address;
    
    private ShippingAddress(String address) {
        validateAddress(address);
        this.address = address;
    }
    
    public static ShippingAddress of(String address) {
        return new ShippingAddress(address);
    }
    
    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("배송 주소는 필수입니다");
        }
        if (address.length() < 10) {
            throw new IllegalArgumentException("배송 주소는 10자 이상이어야 합니다");
        }
        if (address.length() > 200) {
            throw new IllegalArgumentException("배송 주소는 200자 이하여야 합니다");
        }
    }
}

