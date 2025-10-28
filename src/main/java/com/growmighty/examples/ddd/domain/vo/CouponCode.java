package com.growmighty.examples.ddd.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠폰 코드 값 객체
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponCode {
    
    private String code;
    
    private CouponCode(String code) {
        validateCode(code);
        this.code = code;
    }
    
    public static CouponCode of(String code) {
        return new CouponCode(code);
    }
    
    public String getValue() {
        return code;
    }
    
    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("쿠폰 코드는 필수입니다");
        }
        if (!code.matches("^[A-Z0-9]{6,20}$")) {
            throw new IllegalArgumentException(
                "쿠폰 코드는 6-20자의 영문 대문자와 숫자만 가능합니다");
        }
    }
}

