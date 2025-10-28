package com.growmighty.examples.ddd.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 주문 취소 요청 DTO
 */
public record CancelOrderRequest(
        @NotNull(message = "고객 ID는 필수입니다")
        @Positive(message = "고객 ID는 양수여야 합니다")
        Long customerId,

        @NotBlank(message = "취소 사유는 필수입니다")
        String reason
) {}
