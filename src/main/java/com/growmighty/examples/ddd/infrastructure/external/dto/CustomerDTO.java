package com.growmighty.examples.ddd.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 외부 고객 도메인 DTO
 * 
 * 주문 도메인의 관점에서 고객 도메인은 외부 Bounded Context입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long customerId;
    private String customerName;
    private String email;
    private boolean canOrder;
    private String membershipLevel;
}

