package com.growmighty.examples.ddd.presentation.dto.request;

import com.growmighty.examples.ddd.application.command.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
public record CreateOrderRequest(
        @NotNull(message = "고객 ID는 필수입니다")
        @Positive(message = "고객 ID는 양수여야 합니다")
        Long customerId,

        @NotNull(message = "주문 항목은 필수입니다")
        @NotEmpty(message = "주문 항목이 비어있습니다")
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "배송 주소는 필수입니다")
        String shippingAddress,

        String couponCode
) {}
