package com.growmighty.examples.ddd.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 주문 항목 요청 객체
 */
public record OrderItemRequest(
    @NotNull(message = "상품 ID는 필수입니다")
    Long productId,

    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 양수여야 합니다")
    int quantity
) {}

