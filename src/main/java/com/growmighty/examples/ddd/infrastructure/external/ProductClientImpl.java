package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.service.ProductClient;
import com.growmighty.examples.ddd.domain.vo.ProductId;
import com.growmighty.examples.ddd.infrastructure.external.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 도메인 클라이언트 (Anti-Corruption Layer)
 * 
 * DDD Pattern:
 * - 외부 Bounded Context(상품 도메인)와의 통신을 담당
 * - WebClient를 사용하여 HTTP 통신
 * - 주문 도메인을 외부 도메인의 변경으로부터 보호
 * 
 * 실제 환경에서는:
 * - 마이크로서비스 아키텍처에서 다른 서비스와 통신
 * - REST API 또는 gRPC 사용
 * - WebClient, RestTemplate, Feign Client 등 사용
 * 
 * 예시 코드:
 * @RequiredArgsConstructor
 * private final WebClient webClient;
 */
@Slf4j
@Component
public class ProductClientImpl implements ProductClient {
    
    /**
     * 상품 정보 조회
     * 
     * 실제 환경에서는:
     * return webClient.get()
     *     .uri("/products/{id}", productId)
     *     .retrieve()
     *     .bodyToMono(ProductDTO.class)
     *     .block();
     */
    public ProductDTO getProduct(Long productId) {
        log.info("[External API] 상품 조회 요청: productId={}", productId);
        
        // Mock 데이터 반환 (예시용)
        return new ProductDTO(
            productId,
            "샘플 상품 " + productId,
            BigDecimal.valueOf(10000),
            100,
            true
        );
    }
    
    /**
     * 재고 확인 및 차감
     * 
     * 실제 환경에서는:
     * return webClient.post()
     *     .uri("/products/{id}/decrease-stock", productId)
     *     .bodyValue(Map.of("quantity", quantity))
     *     .retrieve()
     *     .bodyToMono(Boolean.class)
     *     .block();
     */
    public boolean decreaseStock(Long productId, int quantity) {
        log.info("[External API] 재고 차감 요청: productId={}, quantity={}", productId, quantity);
        
        // Mock 응답 (예시용)
        return true;
    }
    
    /**
     * 재고 복구 (주문 취소 시)
     */
    public void restoreStock(Long productId, int quantity) {
        log.info("[External API] 재고 복구 요청: productId={}, quantity={}", productId, quantity);
    }

    @Override
    public Optional<ProductInfo> getProduct(ProductId productId) {
        return Optional.empty();
    }

    @Override
    public Map<ProductId, ProductInfo> getProducts(List<ProductId> productIds) {
        log.info("[External API] 상품 배치 조회 요청: {}", productIds);
        
        // 실제 환경에서는:
        // return webClient.post()
        //     .uri("/products/batch")
        //     .bodyValue(productIds)
        //     .retrieve()
        //     .bodyToMono(new ParameterizedTypeReference<Map<ProductId, ProductInfo>>() {})
        //     .block();
        
        // Mock 데이터 반환 (예시용)
        return productIds.stream()
                .collect(Collectors.toMap(
                        productId -> productId,
                        productId -> new ProductClient.ProductInfo(
                                productId,
                                "샘플 상품 " + productId.getId(),
                                10000L,
                                100,
                                true
                        )
                ));
    }

    @Override
    public boolean decreaseStock(ProductId productId, int quantity) {
        return false;
    }

    @Override
    public boolean restoreStock(ProductId productId, int quantity) {
        return false;
    }

    @Override
    public boolean decreaseStocks(Map<ProductId, Integer> stockDecreaseMap) {
        log.info("[External API] 재고 차감 배치 요청: {}", stockDecreaseMap);
        
        // 실제 환경에서는:
        // return webClient.post()
        //     .uri("/products/batch/decrease-stock")
        //     .bodyValue(stockDecreaseMap)
        //     .retrieve()
        //     .bodyToMono(Boolean.class)
        //     .block();
        
        // Mock 응답 (예시용) - 모든 상품의 재고가 충분하다고 가정
        return true;
    }

    @Override
    public void restoreStocks(Map<ProductId, Integer> stockRestoreMap) {
        log.info("[External API] 재고 복구 배치 요청: {}", stockRestoreMap);
        
        // 실제 환경에서는:
        // webClient.post()
        //     .uri("/products/batch/restore-stock")
        //     .bodyValue(stockRestoreMap)
        //     .retrieve()
        //     .bodyToMono(Void.class)
        //     .block();
    }
}

