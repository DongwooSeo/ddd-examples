package com.growmighty.examples.ddd.infrastructure.external.dto;

import java.util.Map;

/**
 * 재고 차감/복구 요청 본문. 상품 ID별 수량을 담는다.
 */
public record StockChangeRequest(Map<Long, Integer> quantities) {
}
