package com.growmighty.examples.ddd.application.port;

import com.growmighty.examples.ddd.domain.vo.ProductId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상품 서비스와 주고받는 창구.
 *
 * 상품 정보와 재고는 상품 도메인이 관리한다. 주문 쪽은 조회하고 재고 변경을 요청할 뿐이다.
 * 실제 호출 방법(HTTP 등)은 인프라 계층 구현체가 정한다.
 */
public interface ProductPort {

    /**
     * 상품 하나를 조회한다. 없으면 빈 값.
     */
    Optional<ProductInfo> getProduct(ProductId productId);

    /**
     * 여러 상품을 한 번에 조회한다.
     */
    Map<ProductId, ProductInfo> getProducts(List<ProductId> productIds);

    /**
     * 재고를 차감한다.
     */
    boolean decreaseStock(ProductId productId, int quantity);

    /**
     * 재고를 복구한다.
     */
    boolean restoreStock(ProductId productId, int quantity);

    /**
     * 여러 상품의 재고를 한 번에 차감한다.
     */
    boolean decreaseStocks(Map<ProductId, Integer> stockDecreaseMap);

    /**
     * 여러 상품의 재고를 한 번에 복구한다.
     */
    void restoreStocks(Map<ProductId, Integer> stockRestoreMap);

    /**
     * 주문을 만드는 데 필요한 만큼만 추린 상품 정보.
     */
    record ProductInfo(
            ProductId productId,
            String productName,
            long price,
            int stockQuantity,
            boolean available
    ) {}
}
