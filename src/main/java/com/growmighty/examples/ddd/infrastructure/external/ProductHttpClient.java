package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.port.ProductPort;
import com.growmighty.examples.ddd.domain.vo.ProductId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 포트의 HTTP 어댑터 (Anti-Corruption Layer)
 *
 * DDD / 헥사고날 아키텍처:
 * - {@link ProductPort} 아웃바운드 포트의 구현체(어댑터)
 * - 외부 Bounded Context(상품 도메인)와의 HTTP 통신을 담당
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
public class ProductHttpClient implements ProductPort {

    /**
     * 상품 정보 조회
     *
     * 실제 환경에서는:
     * return webClient.get()
     *     .uri("/products/{id}", productId.getId())
     *     .retrieve()
     *     .bodyToMono(ProductInfo.class)
     *     .blockOptional();
     */
    @Override
    public Optional<ProductInfo> getProduct(ProductId productId) {
        log.info("[External API] 상품 조회 요청: productId={}", productId.getId());

        // Mock 데이터 반환 (예시용)
        return Optional.of(new ProductInfo(
                productId,
                "샘플 상품 " + productId.getId(),
                10000L,
                100,
                true
        ));
    }

    /**
     * 상품 정보 배치 조회
     *
     * 실제 환경에서는:
     * return webClient.post()
     *     .uri("/products/batch")
     *     .bodyValue(productIds)
     *     .retrieve()
     *     .bodyToMono(new ParameterizedTypeReference<Map<ProductId, ProductInfo>>() {})
     *     .block();
     */
    @Override
    public Map<ProductId, ProductInfo> getProducts(List<ProductId> productIds) {
        log.info("[External API] 상품 배치 조회 요청: {}", productIds);

        // Mock 데이터 반환 (예시용)
        return productIds.stream()
                .collect(Collectors.toMap(
                        productId -> productId,
                        productId -> new ProductInfo(
                                productId,
                                "샘플 상품 " + productId.getId(),
                                10000L,
                                100,
                                true
                        )
                ));
    }

    /**
     * 재고 차감
     *
     * 실제 환경에서는:
     * return webClient.post()
     *     .uri("/products/{id}/decrease-stock", productId.getId())
     *     .bodyValue(Map.of("quantity", quantity))
     *     .retrieve()
     *     .bodyToMono(Boolean.class)
     *     .block();
     */
    @Override
    public boolean decreaseStock(ProductId productId, int quantity) {
        log.info("[External API] 재고 차감 요청: productId={}, quantity={}", productId.getId(), quantity);

        // Mock 응답 (예시용)
        return true;
    }

    /**
     * 재고 복구 (주문 취소 시)
     */
    @Override
    public boolean restoreStock(ProductId productId, int quantity) {
        log.info("[External API] 재고 복구 요청: productId={}, quantity={}", productId.getId(), quantity);

        // Mock 응답 (예시용)
        return true;
    }

    /**
     * 재고 차감 (배치 처리)
     *
     * 실제 환경에서는:
     * return webClient.post()
     *     .uri("/products/batch/decrease-stock")
     *     .bodyValue(stockDecreaseMap)
     *     .retrieve()
     *     .bodyToMono(Boolean.class)
     *     .block();
     */
    @Override
    public boolean decreaseStocks(Map<ProductId, Integer> stockDecreaseMap) {
        log.info("[External API] 재고 차감 배치 요청: {}", stockDecreaseMap);

        // Mock 응답 (예시용) - 모든 상품의 재고가 충분하다고 가정
        return true;
    }

    /**
     * 재고 복구 (배치 처리)
     *
     * 실제 환경에서는:
     * webClient.post()
     *     .uri("/products/batch/restore-stock")
     *     .bodyValue(stockRestoreMap)
     *     .retrieve()
     *     .bodyToMono(Void.class)
     *     .block();
     */
    @Override
    public void restoreStocks(Map<ProductId, Integer> stockRestoreMap) {
        log.info("[External API] 재고 복구 배치 요청: {}", stockRestoreMap);
    }
}
