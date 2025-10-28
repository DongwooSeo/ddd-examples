package com.growmighty.examples.ddd.application.service;

import com.growmighty.examples.ddd.domain.vo.ProductId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상품 애플리케이션 서비스 인터페이스
 * 
 * DDD 원칙:
 * - 애플리케이션 계층에 인터페이스 정의
 * - 외부 Bounded Context와의 통신을 위한 Anti-Corruption Layer
 * - 인프라스트럭처 계층에서 구현
 */
public interface ProductClient {
    
    /**
     * 상품 정보 조회
     * 
     * @param productId 상품 ID
     * @return 상품 정보 (없으면 Optional.empty())
     */
    Optional<ProductInfo> getProduct(ProductId productId);
    
    /**
     * 상품 정보 배치 조회
     * 
     * @param productIds 상품 ID 목록
     * @return 상품 ID별 정보 맵
     */
    Map<ProductId, ProductInfo> getProducts(List<ProductId> productIds);
    
    /**
     * 재고 차감
     * 
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     * @return 성공 여부
     */
    boolean decreaseStock(ProductId productId, int quantity);
    
    /**
     * 재고 복구
     * 
     * @param productId 상품 ID
     * @param quantity 복구할 수량
     * @return 성공 여부
     */
    boolean restoreStock(ProductId productId, int quantity);
    
    /**
     * 재고 차감 (배치 처리)
     * 
     * @param stockDecreaseMap 상품별 차감 수량 맵
     * @return 전체 성공 여부
     */
    boolean decreaseStocks(Map<ProductId, Integer> stockDecreaseMap);
    
    /**
     * 재고 복구 (배치 처리)
     * 
     * @param stockRestoreMap 상품별 복구 수량 맵
     */
    void restoreStocks(Map<ProductId, Integer> stockRestoreMap);
    
    /**
     * 상품 정보 도메인 모델
     */
    record ProductInfo(
        ProductId productId,
        String productName,
        long price,
        int stockQuantity,
        boolean available
    ) {}
}
