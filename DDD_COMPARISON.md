# 🔄 DDD 적용 전후 상세 비교

> Before vs After: 코드 레벨에서 DDD 패턴의 효과를 확인하세요

## 📋 목차

- [1. 개요](#1-개요)
- [2. 도메인 모델 비교](#2-도메인-모델-비교)
- [3. 서비스 레이어 비교](#3-서비스-레이어-비교)
- [4. 외부 도메인 통신 비교](#4-외부-도메인-통신-비교)
- [5. 테스트 비교](#5-테스트-비교)
- [6. 장단점 분석](#6-장단점-분석)

---

## 1. 개요

### Before: 빈약한 도메인 모델 (Anemic Domain Model)

```
┌──────────────────────────────────────┐
│        OrderService                  │
│  ┌────────────────────────────────┐  │
│  │ • 주문 생성 로직 (100줄)       │  │
│  │ • 주문 취소 로직 (50줄)        │  │
│  │ • 결제 처리 로직 (30줄)        │  │
│  │ • 검증 로직 (50줄)             │  │
│  │ • 계산 로직 (30줄)             │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
            ↓
┌──────────────────────────────────────┐
│        Order (데이터만)               │
│  - id, status, totalAmount           │
│  - getter/setter만                   │
└──────────────────────────────────────┘
```

### After: 풍부한 도메인 모델 (Rich Domain Model)

```
┌──────────────────────────────────────┐
│   OrderApplicationService            │
│   • 트랜잭션 관리                     │
│   • 도메인 객체 조율                  │
│   • 외부 시스템 연동                  │
└──────────────────────────────────────┘
            ↓
┌──────────────────────────────────────┐
│        Order (비즈니스 로직 포함)      │
│  • pay() - 결제 규칙                 │
│  • cancel() - 취소 규칙              │
│  • applyCoupon() - 쿠폰 규칙         │
│  • calculateTotalAmount()            │
└──────────────────────────────────────┘
```

---

## 2. 도메인 모델 비교

### 2.1 Order 엔티티

#### ❌ Before: 빈약한 도메인 모델

```java
@Entity
@Getter @Setter  // ⚠️ Setter 노출로 무분별한 상태 변경 가능
public class Order {
    private Long id;
    private Long customerId;
    private String status; // ⚠️ 문자열로 상태 관리 (오타 가능)
    private BigDecimal totalAmount;
    private String shippingAddress;
    
    // ❌ 비즈니스 로직 없음
}
```

**문제점:**
- 단순 데이터 컨테이너
- 비즈니스 규칙 없음
- 상태가 무분별하게 변경될 수 있음
- 도메인 지식이 Service에 흩어짐

#### ✅ After: 풍부한 도메인 모델

```java
@Entity
@Getter  // ✅ Getter만 노출
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AbstractAggregateRoot<Order> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    private CustomerId customerId;  // ✅ 값 객체로 타입 안전성
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;  // ✅ Enum으로 상태 관리
    
    // ✅ 비즈니스 로직 캡슐화
    public void pay() {
        if (!status.canBePaid()) {
            throw new IllegalStateException(
                String.format("%s 상태에서는 결제할 수 없습니다", status.getDescription()));
        }
        
        Money finalAmount = calculateFinalAmount();
        if (!finalAmount.isPositive()) {
            throw new IllegalStateException("결제 금액은 0보다 커야 합니다");
        }
        
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
        
        // ✅ 도메인 이벤트 발행
        registerEvent(new OrderPaidEvent(this.id, this.customerId.getId(), 
                                        finalAmount, this.paidAt));
    }
    
    public void cancel(CustomerId requesterId) {
        // ✅ 비즈니스 규칙: 본인만 취소 가능
        if (!this.customerId.equals(requesterId)) {
            throw new IllegalStateException("본인의 주문만 취소할 수 있습니다");
        }
        
        // ✅ 비즈니스 규칙: 취소 가능한 상태인지 확인
        if (!status.canBeCancelled()) {
            throw new IllegalStateException(
                String.format("%s 상태에서는 취소할 수 없습니다", status.getDescription()));
        }
        
        // ✅ 비즈니스 규칙: 결제 후 24시간 이내만 취소 가능
        if (paidAt != null) {
            LocalDateTime cancelDeadline = paidAt.plusHours(24);
            if (LocalDateTime.now().isAfter(cancelDeadline)) {
                throw new IllegalStateException("결제 후 24시간이 지나 취소할 수 없습니다");
            }
        }
        
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(/*...*/));
    }
    
    // ✅ 도메인 로직: 금액 계산
    public Money calculateTotalAmount() {
        return orderItems.stream()
            .map(OrderItem::calculateTotalPrice)
            .reduce(Money.zero(), Money::add);
    }
}
```

**개선점:**
- 비즈니스 규칙이 도메인 모델에 캡슐화
- 불변식(Invariant) 보호
- 도메인 이벤트 발행
- 명확한 의도를 가진 메서드

### 2.2 값 객체 (Value Object)

#### ❌ Before: 원시 타입 사용

```java
private BigDecimal totalAmount;  // ⚠️ 음수 가능, 검증 없음
private Integer quantity;         // ⚠️ 0 또는 음수 가능
```

#### ✅ After: 값 객체로 도메인 개념 표현

```java
// Money - 금액 값 객체
@Getter @EqualsAndHashCode
public class Money {
    private final BigDecimal amount;
    
    private Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("금액은 소수점 둘째 자리까지만 가능합니다");
        }
        this.amount = amount;
    }
    
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
    
    // ✅ 비즈니스 로직을 값 객체에 캡슐화
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
}

// Quantity - 수량 값 객체
@Getter @EqualsAndHashCode
public class Quantity {
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;
    
    private final int value;
    
    private Quantity(int value) {
        if (value < MIN_QUANTITY) {
            throw new IllegalArgumentException("주문 수량은 최소 1개 이상이어야 합니다");
        }
        if (value > MAX_QUANTITY) {
            throw new IllegalArgumentException("주문 수량은 최대 100개까지 가능합니다");
        }
        this.value = value;
    }
    
    public static Quantity of(int value) {
        return new Quantity(value);
    }
}
```

**장점:**
- 자가 검증 (Self-Validation)
- 불변성 (Immutability)
- 도메인 개념 명확히 표현
- 비즈니스 로직 캡슐화

---

## 3. 서비스 레이어 비교

### 3.1 주문 생성 로직

#### ❌ Before: 트랜잭션 스크립트 패턴

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    
    @Transactional
    public Long createOrder(Long customerId, List<OrderItemRequest> itemRequests,
                           String shippingAddress, String couponCode) {
        
        // ❌ 비즈니스 규칙이 서비스에 흩어져 있음
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다"));
        
        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new IllegalStateException("활성 상태의 고객만 주문할 수 있습니다");
        }
        
        if (customer.getUnpaidOrderCount() >= 3) {
            throw new IllegalStateException("미결제 주문이 3개 이상인 고객은 추가 주문할 수 없습니다");
        }
        
        // ❌ 주문 생성도 서비스에서
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        order.setOrderedAt(LocalDateTime.now());
        
        // ❌ 복잡한 계산 로직도 서비스에
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));
            
            // 검증, 계산 등 100줄 이상의 로직...
            totalAmount = totalAmount.add(/* 계산 */);
        }
        
        // ❌ 할인 로직도 서비스에 (50줄+)
        if (couponCode != null && !couponCode.isEmpty()) {
            // 쿠폰 검증 및 할인 계산...
        }
        
        order.setTotalAmount(totalAmount);
        return orderRepository.save(order).getId();
    }
}
```

**문제점:**
- 모든 비즈니스 로직이 서비스에 집중 (256줄)
- 도메인 지식이 분산됨
- 테스트하기 어려움 (많은 Mock 필요)
- 재사용 어려움

#### ✅ After: 얇은 서비스 레이어

```java
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    
    // ✅ 레포지토리는 자신의 도메인만
    private final OrderRepository orderRepository;
    
    // ✅ 외부 도메인은 Client로 통신
    private final ProductClient productClient;
    private final CustomerClient customerClient;
    private final CouponClient couponClient;
    
    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        // ✅ 서비스는 조율만
        CustomerId customerId = CustomerId.of(command.customerId());
        validateCustomer(customerId);
        
        List<OrderItem> orderItems = command.items().stream()
            .map(this::createOrderItemFromExternalProduct)
            .collect(Collectors.toList());
        
        ShippingAddress shippingAddress = ShippingAddress.of(command.shippingAddress());
        
        // ✅ 비즈니스 로직은 도메인 모델에 위임
        Order order = Order.create(customerId, orderItems, shippingAddress);
        
        if (command.couponCode() != null) {
            applyCouponFromExternalDomain(order, command.couponCode());
        }
        
        decreaseStockForOrderItems(orderItems);
        
        Order savedOrder = orderRepository.save(order);
        return savedOrder.getId();
    }
    
    // ✅ 외부 도메인 통신은 명확히 분리
    private void validateCustomer(CustomerId customerId) {
        boolean canOrder = customerClient.canOrder(customerId.getId());
        if (!canOrder) {
            throw new IllegalStateException("주문할 수 없는 고객입니다");
        }
    }
}
```

**개선점:**
- 서비스는 도메인 객체 조율만 (얇은 레이어)
- 비즈니스 로직은 도메인 모델에
- 외부 도메인과의 통신 명확히 분리
- 테스트하기 쉬움

---

## 4. 외부 도메인 통신 비교

### 4.1 다른 도메인 접근 방법

#### ❌ Before: Repository로 직접 접근

```java
@Service
public class OrderService {
    
    // ❌ 다른 도메인의 Repository를 직접 사용
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    
    public void createOrder() {
        // ❌ 주문 도메인에서 상품 도메인 직접 접근
        Product product = productRepository.findById(productId)
            .orElseThrow();
        
        // ❌ 고객 도메인 직접 접근
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow();
    }
}
```

**문제점:**
- Bounded Context 경계가 모호
- 다른 도메인의 변경에 취약
- 마이크로서비스로 분리 불가능
- 도메인 간 강한 결합

#### ✅ After: Anti-Corruption Layer (WebClient)

```java
// ✅ 외부 도메인 클라이언트
@Component
public class ProductClient {
    
    /**
     * 실제 환경에서는:
     * return webClient.get()
     *     .uri("/products/{id}", productId)
     *     .retrieve()
     *     .bodyToMono(ProductDTO.class)
     *     .block();
     */
    public ProductDTO getProduct(Long productId) {
        log.info("[External API] 상품 조회: productId={}", productId);
        
        // Mock 데이터 (예시용)
        return new ProductDTO(
            productId,
            "샘플 상품 " + productId,
            BigDecimal.valueOf(10000),
            100,
            true
        );
    }
    
    public boolean decreaseStock(Long productId, int quantity) {
        log.info("[External API] 재고 차감: productId={}, quantity={}", 
                 productId, quantity);
        return true;
    }
}

// ✅ 서비스에서 사용
@Service
public class OrderApplicationService {
    
    private final ProductClient productClient;  // ✅ Client 사용
    
    private OrderItem createOrderItemFromExternalProduct(OrderItemRequest request) {
        // ✅ 외부 도메인에서 정보 조회
        ProductDTO product = productClient.getProduct(request.productId());
        
        // ✅ 외부 DTO를 내부 도메인 객체로 변환
        return new OrderItem(
            ProductId.of(product.getProductId()),
            product.getProductName(),
            Money.of(product.getPrice()),
            Quantity.of(request.quantity())
        );
    }
}
```

**개선점:**
- Bounded Context 명확히 분리
- 외부 변경으로부터 보호
- 마이크로서비스 전환 용이
- 느슨한 결합

### 4.2 Bounded Context 다이어그램

```
┌─────────────────────────────────────────┐
│        주문 Bounded Context              │
│  ┌────────────────────────────────┐     │
│  │  Order (우리 도메인)            │     │
│  │  - OrderItem                   │     │
│  │  - Money, Quantity             │     │
│  └────────────────────────────────┘     │
│                                          │
│  외부 통신:                               │
│  ├─ ProductClient    (WebClient)        │
│  ├─ CustomerClient   (WebClient)        │
│  └─ CouponClient     (WebClient)        │
└─────────────────────────────────────────┘
         │ HTTP/gRPC
         ├────────────────────────────────┐
         ▼                                ▼
┌──────────────────────┐    ┌──────────────────────┐
│ 상품 Bounded Context │    │ 쿠폰 Bounded Context │
│ - Product           │    │ - Coupon            │
└──────────────────────┘    └──────────────────────┘
```

---

## 5. 테스트 비교

### 5.1 도메인 로직 테스트

#### ❌ Before: 서비스 테스트

```java
@SpringBootTest
class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @MockBean
    private ProductRepository productRepository;
    
    @MockBean
    private CustomerRepository customerRepository;
    
    @MockBean
    private CouponRepository couponRepository;
    
    @MockBean
    private InventoryService inventoryService;
    
    @MockBean
    private NotificationService notificationService;
    
    @Test
    void 주문_생성_테스트() {
        // ❌ 많은 Mock 객체 필요
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(couponRepository.findByCode(any())).thenReturn(Optional.of(coupon));
        when(inventoryService.hasStock(any(), any())).thenReturn(true);
        
        // ❌ DB 연동 필요
        Long orderId = orderService.createOrder(/*...*/);
        
        // ❌ 느린 실행 속도
        assertThat(orderId).isNotNull();
    }
}
```

**문제점:**
- 많은 Mock 객체 필요
- DB 연동 필요
- 느린 실행 속도
- 비즈니스 로직만 테스트하기 어려움

#### ✅ After: 순수 도메인 테스트

```java
class OrderTest {
    
    // ✅ Mock 객체 불필요
    
    @Test
    @DisplayName("주문 생성 시 총 금액이 올바르게 계산된다")
    void calculateTotalAmount() {
        // Given
        OrderItem item1 = new OrderItem(
            ProductId.of(1L),
            "노트북",
            Money.of(1000000),
            Quantity.of(2)
        );
        
        OrderItem item2 = new OrderItem(
            ProductId.of(2L),
            "마우스",
            Money.of(50000),
            Quantity.of(1)
        );
        
        // When
        Order order = Order.create(
            CustomerId.of(1L),
            List.of(item1, item2),
            ShippingAddress.of("서울시 강남구 테헤란로 123")
        );
        
        // Then - ✅ 순수 비즈니스 로직 검증
        assertThat(order.calculateTotalAmount())
            .isEqualTo(Money.of(2050000));
    }
    
    @Test
    @DisplayName("배송 중인 주문은 취소할 수 없다")
    void cancelOrder_shipped() {
        // Given
        Order order = createOrder();
        order.pay();
        order.ship();
        
        // When & Then - ✅ 비즈니스 규칙 검증
        assertThatThrownBy(() -> order.cancel(CustomerId.of(1L)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("SHIPPED 상태에서는 취소할 수 없습니다");
    }
}
```

**개선점:**
- Mock 객체 불필요
- DB 연동 불필요
- 빠른 실행 속도 (milliseconds)
- 순수 비즈니스 로직 테스트

---

## 6. 장단점 분석

### 6.1 Before: 빈약한 도메인 모델

#### 장점 ✅

- 구현이 빠름 (초기)
- 코드가 단순해 보임 (처음에는)
- 러닝 커브가 낮음

#### 단점 ❌

- **비즈니스 로직 분산**: 도메인 지식이 여러 Service에 흩어짐
- **테스트 어려움**: 많은 의존성, DB 연동 필요
- **유지보수 어려움**: 비즈니스 규칙 변경 시 여러 곳 수정
- **재사용 불가**: 같은 로직을 여러 곳에 중복 작성
- **도메인 전문가와 소통 어려움**: 기술 중심 코드

### 6.2 After: 풍부한 도메인 모델

#### 장점 ✅

- **비즈니스 로직 캡슐화**: 도메인 모델에 집중
- **테스트 용이**: 순수 도메인 로직 테스트 가능
- **유지보수 쉬움**: 변경 영향 범위가 명확
- **재사용성 높음**: 도메인 로직 재사용
- **도메인 전문가와 소통 쉬움**: 도메인 언어 사용
- **확장성**: Bounded Context로 마이크로서비스 전환 용이

#### 단점 ❌

- 초기 학습 비용
- 코드량 증가 (하지만 명확한 구조)
- 설계 시간 필요

---

## 📊 통계 요약

| 메트릭 | Before | After | 개선율 |
|--------|--------|-------|--------|
| Order 클래스 | 48줄 | 267줄 | +456% (로직 포함) |
| Service 클래스 | 256줄 | 264줄 | +3% (조율만) |
| 전체 파일 수 | 6개 | 27개 | +350% (명확한 구조) |
| 테스트 의존성 | 6개 Mock | 0개 | -100% |
| 도메인 이벤트 | 없음 | 3개 | +∞ |
| Value Objects | 없음 | 7개 | +∞ |

---

## 💡 결론

### DDD를 적용해야 하는 경우

- ✅ 복잡한 비즈니스 로직
- ✅ 지속적인 비즈니스 규칙 변경
- ✅ 도메인 전문가와 긴밀한 협업
- ✅ 장기적인 유지보수 필요
- ✅ 마이크로서비스 전환 계획

### 전통적인 방식이 적합한 경우

- ⚠️ 단순한 CRUD
- ⚠️ 프로토타입/MVP
- ⚠️ 비즈니스 로직이 거의 없음
- ⚠️ 짧은 수명의 프로젝트

---

**DDD는 은탄환이 아닙니다. 하지만 복잡한 도메인에서는 강력한 무기가 됩니다.** 🎯

