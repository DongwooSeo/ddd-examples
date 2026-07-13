package com.growmighty.examples.ddd.infrastructure.external;

import com.growmighty.examples.ddd.application.port.ProductPort;
import com.growmighty.examples.ddd.domain.vo.ProductId;
import com.growmighty.examples.ddd.infrastructure.external.dto.ProductResponse;
import com.growmighty.examples.ddd.infrastructure.external.dto.StockChangeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 서비스 호출을 담당하는 어댑터.
 *
 * 상품 조회와 재고 처리는 모두 상품 도메인의 몫이다.
 * 이 클래스는 REST 호출과 응답 변환만 맡는다.
 */
@Slf4j
@Component
public class ProductRestClient implements ProductPort {

    private static final ParameterizedTypeReference<List<ProductResponse>> PRODUCT_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public ProductRestClient(RestClient.Builder builder,
                             @Value("${external.product.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<ProductInfo> getProduct(ProductId productId) {
        try {
            ProductResponse response = restClient.get()
                    .uri("/products/{id}", productId.getId())
                    .retrieve()
                    .body(ProductResponse.class);

            return Optional.ofNullable(response).map(this::toProductInfo);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Map<ProductId, ProductInfo> getProducts(List<ProductId> productIds) {
        List<Long> ids = productIds.stream().map(ProductId::getId).toList();

        List<ProductResponse> responses = restClient.post()
                .uri("/products/search")
                .body(ids)
                .retrieve()
                .body(PRODUCT_LIST);

        if (responses == null) {
            return Map.of();
        }

        return responses.stream()
                .map(this::toProductInfo)
                .collect(Collectors.toMap(ProductInfo::productId, info -> info));
    }

    @Override
    public boolean decreaseStock(ProductId productId, int quantity) {
        restClient.post()
                .uri("/products/{id}/stock/decrease", productId.getId())
                .body(Map.of("quantity", quantity))
                .retrieve()
                .toBodilessEntity();
        return true;
    }

    @Override
    public boolean restoreStock(ProductId productId, int quantity) {
        restClient.post()
                .uri("/products/{id}/stock/restore", productId.getId())
                .body(Map.of("quantity", quantity))
                .retrieve()
                .toBodilessEntity();
        return true;
    }

    @Override
    public boolean decreaseStocks(Map<ProductId, Integer> stockDecreaseMap) {
        restClient.post()
                .uri("/products/stock/decrease")
                .body(new StockChangeRequest(toIdMap(stockDecreaseMap)))
                .retrieve()
                .toBodilessEntity();
        return true;
    }

    @Override
    public void restoreStocks(Map<ProductId, Integer> stockRestoreMap) {
        restClient.post()
                .uri("/products/stock/restore")
                .body(new StockChangeRequest(toIdMap(stockRestoreMap)))
                .retrieve()
                .toBodilessEntity();
    }

    private ProductInfo toProductInfo(ProductResponse response) {
        return new ProductInfo(
                ProductId.of(response.productId()),
                response.productName(),
                response.price().longValue(),
                response.stockQuantity(),
                response.available()
        );
    }

    private Map<Long, Integer> toIdMap(Map<ProductId, Integer> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue));
    }
}
