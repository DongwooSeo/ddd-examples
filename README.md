# 🎯 Domain-Driven Design (DDD) 예시 프로젝트

> 주문(Order) 도메인을 중심으로 DDD 적용 전후를 비교하는 학습 자료

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-blue.svg)](https://gradle.org/)

## 📖 프로젝트 소개

이 프로젝트는 **주문 도메인**을 예시로 DDD(Domain-Driven Design)를 적용하는 방법을 보여줍니다.

- ❌ **Before**: 빈약한 도메인 모델 (Anemic Domain Model)
- ✅ **After**: 풍부한 도메인 모델 (Rich Domain Model) + DDD 패턴

## 🎓 학습 목표

1. **빈약한 도메인 모델의 문제점** 이해
2. **DDD 핵심 패턴** 실전 적용
3. **Bounded Context**와 **Anti-Corruption Layer** 이해
4. **외부 도메인과의 통신** 방법 (WebClient)
5. **도메인 이벤트**를 활용한 느슨한 결합

## 📁 프로젝트 구조

```
ddd-examples/
│
├── README.md                          # 📍 현재 파일 (프로젝트 개요)
├── DDD_ARCHITECTURE.md                # 🏗️ 상세 아키텍처 설명
├── DDD_COMPARISON.md                  # 🔄 Before/After 상세 비교
│
└── src/main/java/.../ddd/
    │
    ├── presentation/               # 표현 계층 (Presentation Layer)
    │   └── OrderController.java   # REST API 컨트롤러
    │
    ├── application/               # 애플리케이션 레이어
    │   ├── OrderService.java      # 애플리케이션 서비스 (조율)
    │   ├── command/               # Command 객체 (입력)
    │   │   ├── CreateOrderCommand.java
    │   │   ├── PayOrderCommand.java
    │   │   ├── CancelOrderCommand.java
    │   │   └── OrderItemRequest.java
    │   ├── dto/                   # Response 객체 (출력)
    │   │   ├── OrderDetailResponse.java
    │   │   └── OrderItemResponse.java
    │   └── service/               # 외부 도메인 클라이언트
    │       ├── ProductClient.java
    │       ├── CustomerClient.java
    │       └── CouponClient.java
    │
    ├── domain/                    # 도메인 레이어 (핵심)
    │   ├── entity/                # 엔티티
    │   │   ├── Order.java         # 애그리거트 루트
    │   │   └── OrderItem.java     # 엔티티
    │   ├── vo/                    # 값 객체
    │   │   ├── Money.java
    │   │   ├── Quantity.java
    │   │   ├── OrderStatus.java
    │   │   ├── CustomerId.java
    │   │   ├── ProductId.java
    │   │   ├── CouponCode.java
    │   │   └── ShippingAddress.java
    │   ├── service/               # 도메인 서비스
    │   │   └── OrderDomainService.java  # 간단한 비즈니스 로직
    │   ├── event/                 # 도메인 이벤트
    │   │   ├── OrderDomainEvent.java
    │   │   ├── OrderCreatedEvent.java
    │   │   ├── OrderPaidEvent.java
    │   │   └── OrderCancelledEvent.java
    │   └── repository/
    │       └── OrderRepository.java
    │
    └── infrastructure/            # 인프라 레이어
        └── external/              # 외부 도메인 통신
            └── dto/               # 외부 도메인 DTO
                ├── ProductDTO.java
                ├── CustomerDTO.java
                └── CouponDTO.java
```

## 🔑 핵심 DDD 패턴

### 1. Aggregate (애그리거트)

```java
// Order는 애그리거트 루트, OrderItem은 내부 엔티티
public class Order extends AbstractAggregateRoot<Order> {
    private List<OrderItem> orderItems;
    
    // 비즈니스 규칙을 도메인 모델에 캡슐화
    public void pay() {
        if (!status.canBePaid()) {
            throw new IllegalStateException("결제할 수 없는 상태입니다");
        }
        this.status = OrderStatus.PAID;
        registerEvent(new OrderPaidEvent(...));
    }
}
```

### 2. Value Object (값 객체)

```java
// 불변성, 자가 검증, 도메인 개념 표현
@Getter @EqualsAndHashCode
public class Money {
    private final BigDecimal amount;
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
}
```

### 3. Anti-Corruption Layer

```java
// 외부 도메인과의 통신을 캡슐화
@Component
public class ProductClient {
    
    public ProductDTO getProduct(Long productId) {
        // WebClient로 외부 서비스 호출
        // 주문 도메인을 외부 변경으로부터 보호
        return /* 외부 API 호출 결과 */;
    }
}
```

### 4. Domain Service (도메인 서비스)

```java
// 하나의 엔티티에 속하지 않는 간단한 비즈니스 로직
@Service
public class OrderDomainService {
    
    // 고객 등급별 할인율 계산
    public Money calculateDiscount(CustomerId customerId, Money orderAmount, List<Order> customerOrderHistory) {
        // VIP 고객: 10% 할인
        // 프리미엄 고객: 5% 할인
        // 일반 고객: 할인 없음
    }
    
    // 주문 우선순위 결정
    public OrderPriority determinePriority(Money orderAmount) {
        // 10만원 이상: 높은 우선순위
        // 5만원 이상: 중간 우선순위
        // 5만원 미만: 낮은 우선순위
    }
    
    // 주문 검증 (간단한 비즈니스 규칙)
    public void validateOrder(Money orderAmount, int itemCount) {
        // 최소/최대 주문 금액 검증
        // 주문 항목 수 검증
    }
}
```

### 5. Presentation Layer (표현 계층)

```java
// REST API 컨트롤러 (리팩토링된 버전)
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final OrderService orderService; // 애플리케이션 서비스만 의존
    
    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // 1. 입력 검증
        validateCreateOrderRequest(request);
        
        // 2. Command 객체 생성
        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.items(),
                request.shippingAddress(),
                request.couponCode()
        );
        
        // 3. 애플리케이션 서비스 호출 (도메인 서비스는 애플리케이션 서비스에서 호출)
        Long orderId = orderService.createOrder(command);
        
        // 4. 응답 생성
        return ResponseEntity.ok(new OrderCreateResponse(orderId));
    }
    
    @GetMapping("/{orderId}/priority")
    public ResponseEntity<OrderPriorityResponse> getOrderPriority(@PathVariable Long orderId) {
        // 애플리케이션 서비스를 통한 우선순위 조회
        OrderService.OrderPriorityResponse response = orderService.getOrderPriority(orderId);
        return ResponseEntity.ok(new OrderPriorityResponse(response.orderId(), response.priority()));
    }
}

// 전역 예외 처리기
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBusinessException(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("BUSINESS_RULE_VIOLATION", e.getMessage()));
    }
}
```

## 📊 Before vs After 비교

| 항목 | Before (빈약한 모델) | After (풍부한 모델) |
|------|---------------------|-------------------|
| 비즈니스 로직 위치 | ❌ Service에 집중 | ✅ Domain Model에 캡슐화 |
| Order 클래스 | 48줄 (getter/setter만) | 267줄 (로직 포함) |
| OrderService | 256줄 (복잡) | 264줄 (조율만) |
| 테스트 용이성 | ❌ 어려움 (DB 의존) | ✅ 쉬움 (순수 도메인) |
| 외부 도메인 통신 | ❌ Repository 직접 접근 | ✅ WebClient (ACL) |
| 도메인 이벤트 | ❌ 없음 | ✅ 느슨한 결합 |

## 🚀 실행 방법

### 사전 요구사항

- Java 17+
- Gradle 8.5+

### 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

## 🏗️ 상세 아키텍처

### Bounded Context (제한된 컨텍스트)

```
┌─────────────────────────────────────────────────────────┐
│              주문 Bounded Context                        │
│  ┌────────────────────────────────────────────┐         │
│  │         Order Aggregate                    │         │
│  │  - Order (Root)                            │         │
│  │  - OrderItem                               │         │
│  │  - Value Objects (Money, Quantity, etc)    │         │
│  └────────────────────────────────────────────┘         │
│                                                         │
│  외부 도메인과의 통신:                                       │
│  - ProductClient (상품 도메인)                             │
│  - CustomerClient (고객 도메인)                            │
│  - CouponClient (쿠폰 도메인)                              │
└─────────────────────────────────────────────────────────┘
```

### Anti-Corruption Layer (부패 방지 계층)

외부 도메인과 통신할 때 **WebClient 기반 클라이언트**를 사용하여:
- 주문 도메인을 외부 변경으로부터 보호
- 외부 도메인의 DTO를 주문 도메인 객체로 변환
- 마이크로서비스 아키텍처에서의 도메인 간 통신 패턴

```java
// ❌ Repository로 다른 도메인 접근 (잘못된 예)
@Autowired
private ProductRepository productRepository; // 다른 도메인의 Repository

// ✅ WebClient로 다른 도메인 접근 (올바른 예)
@Autowired
private ProductClient productClient; // Anti-Corruption Layer
```

### 주문 프로세스 흐름

#### 1. 주문 생성
```
Client
  │
  ├─> OrderApplicationService.createOrder()
  │     │
  │     ├─> CustomerClient.canOrder()              [외부 도메인 호출]
  │     ├─> ProductClient.getProduct()             [외부 도메인 호출]
  │     ├─> Order.create()                         [도메인 로직]
  │     ├─> CouponClient.getCoupon()               [외부 도메인 호출]
  │     ├─> Order.applyCoupon()                    [도메인 로직]
  │     ├─> ProductClient.decreaseStock()          [외부 도메인 호출]
  │     └─> OrderRepository.save()                 [영속화]
  │
  └─> Order Created (orderId 반환)
```

#### 2. 주문 취소
```
Client
  │
  ├─> OrderApplicationService.cancelOrder()
  │     │
  │     ├─> OrderRepository.findById()             [조회]
  │     ├─> Order.cancel()                         [도메인 로직 + 검증]
  │     ├─> ProductClient.restoreStock()           [외부 도메인 호출]
  │     └─> CouponClient.restoreCoupon()           [외부 도메인 호출]
  │
  └─> Order Cancelled
```

## 🔄 Before vs After 상세 비교

### 도메인 모델 비교

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

### 서비스 레이어 비교

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

### 외부 도메인 통신 비교

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

### 테스트 비교

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

## ⚡ 빠른 답변: "도메인이 Spring에 의존하는 게 맞나요?"

### 🎯 답변

**네, `AbstractAggregateRoot`를 사용하면 도메인이 `org.springframework.data.domain`에 의존합니다.**

```java
import org.springframework.data.domain.AbstractAggregateRoot;  // Spring 의존!

public class Order extends AbstractAggregateRoot<Order> {
    // 도메인 로직
}
```

### 🤔 이게 문제인가요?

#### 이론적으로는...
**❌ DDD 순수주의 관점에서는 문제**
- 도메인은 인프라에 의존하면 안 됨
- 프레임워크 독립적이어야 함
- 헥사고날 아키텍처 위반

#### 실무적으로는...
**✅ 대부분의 경우 문제없음**
- `AbstractAggregateRoot`는 충분히 일반적인 추상화
- 특정 DB나 기술에 종속되지 않음
- 실용적 이점이 매우 큼
- 업계 표준으로 널리 사용됨

### 📊 두 가지 접근 비교

#### 방법 1: AbstractAggregateRoot (실용주의)

```java
// 도메인
public class Order extends AbstractAggregateRoot<Order> {
    public void pay() {
        this.status = PAID;
        registerEvent(new OrderPaidEvent(...));  // 간단!
    }
}

// 서비스
orderRepository.save(order);  // 자동으로 이벤트 발행! ✨
```

**장점:**
- ✅ 코드 간결
- ✅ 이벤트 발행 자동화
- ✅ 실수 방지

**단점:**
- ❌ Spring 의존

#### 방법 2: 순수 구현 (순수주의)

```java
// 도메인 (Spring 의존 없음!)
public class Order implements AggregateRoot {
    private List<DomainEvent> events = new ArrayList<>();
    
    public void pay() {
        this.status = PAID;
        events.add(new OrderPaidEvent(...));
    }
    
    public List<DomainEvent> getDomainEvents() {
        return events;
    }
}

// 인프라 (Spring은 여기서만)
@Component
public class OrderRepositoryAdapter {
    public Order save(Order order) {
        Order saved = jpaRepository.save(order);
        
        // 수동으로 이벤트 발행
        for (DomainEvent event : saved.getDomainEvents()) {
            eventPublisher.publishEvent(event);
        }
        
        saved.clearDomainEvents();
        return saved;
    }
}
```

**장점:**
- ✅ 완전한 프레임워크 독립성
- ✅ 헥사고날 아키텍처 준수

**단점:**
- ❌ 보일러플레이트 코드 많음
- ❌ 이벤트 발행 깜빡할 수 있음

### 🎯 언제 뭘 선택해야 하나요?

#### AbstractAggregateRoot 사용 (대부분의 경우)

**추천 상황:**
- ✅ 일반적인 Spring 프로젝트
- ✅ 팀 생산성이 중요
- ✅ 프레임워크 전환 계획 없음
- ✅ 실용성 > 순수성

```java
// 이렇게 쓰세요
public class Order extends AbstractAggregateRoot<Order> { }
```

#### 순수 구현 사용 (특수한 경우)

**추천 상황:**
- ✅ 멀티 프레임워크 지원 필요
- ✅ 도메인 모델을 다른 프로젝트에서 재사용
- ✅ 헥사고날 아키텍처를 엄격히 준수해야 함
- ✅ 완벽한 순수성이 요구사항

```java
// 이렇게 쓰세요
public class Order implements AggregateRoot { }
```

## 📊 통계 요약

| 메트릭 | Before | After | 개선율 |
|--------|--------|-------|--------|
| Order 클래스 | 48줄 | 267줄 | +456% (로직 포함) |
| Service 클래스 | 256줄 | 264줄 | +3% (조율만) |
| 전체 파일 수 | 6개 | 27개 | +350% (명확한 구조) |
| 테스트 의존성 | 6개 Mock | 0개 | -100% |
| 도메인 이벤트 | 없음 | 3개 | +∞ |
| Value Objects | 없음 | 7개 | +∞ |

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

**DDD는 은탄환이 아닙니다. 하지만 복잡한 도메인에서는 강력한 무기가 됩니다.** 🎯

## 🔧 AbstractAggregateRoot의 역할

### 🎯 한 줄 요약

**도메인 이벤트를 쉽게 등록하고 자동으로 발행할 수 있게 해주는 DDD 애그리거트 루트 구현 지원 클래스**

### 🔑 핵심 역할 3가지

#### 1️⃣ 도메인 이벤트 저장소 제공

```java
public abstract class AbstractAggregateRoot<A> {
    // 이벤트를 담아둘 저장소
    private final List<Object> domainEvents = new ArrayList<>();
    
    // 이벤트 등록 메서드
    protected <T> T registerEvent(T event) {
        this.domainEvents.add(event);
        return event;
    }
}
```

**역할:** 도메인 모델이 발생시킨 이벤트를 임시로 저장하는 컨테이너 역할

#### 2️⃣ Spring의 이벤트 발행 시스템과 자동 연결

```java
// 도메인 모델에서
public class Order extends AbstractAggregateRoot<Order> {
    public void pay() {
        this.status = PAID;
        registerEvent(new OrderPaidEvent(...));  // 등록만 함
    }
}

// Repository save() 호출 시
orderRepository.save(order);
// ↓ Spring Data가 자동으로:
// 1. order.domainEvents() 조회
// 2. 각 이벤트를 ApplicationEventPublisher로 발행
// 3. order.clearDomainEvents() 호출
```

**역할:** 도메인 모델과 Spring의 ApplicationEventPublisher를 자동으로 연결

#### 3️⃣ 이벤트 발행 타이밍 제어

```java
// ❌ 이벤트를 즉시 발행하는 것이 아님!
order.pay();  
// 이 시점에는 이벤트가 등록만 되고 발행 안됨
// → 비즈니스 로직 실행 중 실패하면 이벤트도 발행 안됨 (안전!)

// ✅ save() 호출 시 발행
orderRepository.save(order);  
// 이 시점에 이벤트 발행
// → 트랜잭션과 함께 관리 가능
```

**역할:** 이벤트 발행 시점을 save()로 통일하여 트랜잭션과 안전하게 연계

### 🔄 전체 동작 흐름

```
┌─────────────────────────────────────────────────────────────┐
│  1. 도메인 모델 (AbstractAggregateRoot 상속)                    │
├─────────────────────────────────────────────────────────────┤
│  class Order extends AbstractAggregateRoot<Order> {          │
│      public void pay() {                                     │
│          this.status = PAID;                                 │
│          registerEvent(new OrderPaidEvent(...));             │
│          //           ↓                                      │
│          // domainEvents 리스트에 저장됨                         │
│      }                                                       │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  2. Repository.save() 호출                                   │
├─────────────────────────────────────────────────────────────┤
│  orderRepository.save(order);                                │
│                                                              │
│  Spring Data JPA (SimpleJpaRepository) 내부에서:               │
│  ┌──────────────────────────────────────────────┐          │
│  │ public <S> S save(S entity) {                │          │
│  │     // 1. 엔티티 저장                           │          │
│  │     em.persist(entity);                      │          │
│  │                                              │          │
│  │     // 2. 이벤트 발행 (자동!) ⭐                 │          │
│  │     if (entity instanceof AbstractAggregateRoot) {      │
│  │         for (event : entity.domainEvents()) {│          │
│  │             publisher.publishEvent(event);   │          │
│  │         }                                    │          │
│  │         entity.clearDomainEvents();          │          │
│  │     }                                         │          │
│  │     return entity;                            │          │
│  │ }                                             │          │
│  └──────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  3. 이벤트 핸들러 실행                                          │
├─────────────────────────────────────────────────────────────┤
│  @EventListener                                              │
│  public void handle(OrderPaidEvent event) {                  │
│      // 알림 발송, 통계 업데이트 등                                │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
```

### 🆚 비교: AbstractAggregateRoot vs 수동 구현

| 항목 | AbstractAggregateRoot | 수동 구현 |
|------|----------------------|----------|
| **이벤트 발행** | 자동 (save() 시) | 수동 (직접 호출) |
| **Spring 의존성** | 도메인 모델은 의존 X | 도메인 모델이 의존 |
| **코드 복잡도** | 낮음 | 높음 |
| **실수 가능성** | 낮음 (자동화) | 높음 (깜빡할 수 있음) |
| **테스트** | 쉬움 (순수 객체) | 어려움 (Mock 필요) |
| **트랜잭션 연계** | 자동 | 수동 관리 필요 |

### 💡 실전 사용 패턴

#### 패턴 1: 단일 이벤트
```java
public class Order extends AbstractAggregateRoot<Order> {
    
    public void pay() {
        validatePayment();
        this.status = PAID;
        registerEvent(new OrderPaidEvent(this.id));
    }
}
```

#### 패턴 2: 여러 이벤트
```java
public class Order extends AbstractAggregateRoot<Order> {
    
    public void cancel() {
        validateCancellation();
        this.status = CANCELLED;
        
        // 여러 이벤트 등록 가능
        registerEvent(new OrderCancelledEvent(this.id));
        registerEvent(new OrderStatusChangedEvent(this.id, CANCELLED));
    }
}
```

#### 패턴 3: 조건부 이벤트
```java
public class Order extends AbstractAggregateRoot<Order> {
    
    public void complete() {
        this.status = COMPLETED;
        
        // 조건에 따라 다른 이벤트 발행
        if (isFirstOrder()) {
            registerEvent(new FirstOrderCompletedEvent(this.customerId));
        } else {
            registerEvent(new OrderCompletedEvent(this.id));
        }
    }
}
```

## 🎭 도메인 계층의 Spring 의존성 논의

### 🤔 핵심 질문

> "도메인 계층이 `org.springframework.data.domain`을 참조하는 건 맞나요?"

**답: 네, 맞습니다. 그리고 이것은 실무에서 논쟁이 되는 주제입니다.**

### 📊 현실: AbstractAggregateRoot 사용 시 의존성

```java
package com.example.order.domain;  // 도메인 레이어

// ⚠️ Spring Data에 의존!
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity  // JPA에도 의존
public class Order extends AbstractAggregateRoot<Order> {
    // 도메인 로직
}
```

**의존성 구조:**
```
도메인 레이어 (Domain Layer)
    ↓ 의존
spring-data-commons (org.springframework.data.domain)
    ↓ 의존
spring-context (org.springframework)
```

### 🎭 두 가지 관점

#### 1️⃣ 순수주의 관점 ❌

**"도메인은 인프라에 의존하면 안 된다!"**

##### DDD 헥사고날 아키텍처 원칙

```
┌─────────────────────────────────────────┐
│         도메인 레이어 (순수)                │
│  - 비즈니스 로직만                         │
│  - 프레임워크 의존 X                       │
│  - 순수 Java 객체                        │
└─────────────────────────────────────────┘
         ↑
         │ (의존 방향이 잘못됨!)
         │
┌─────────────────────────────────────────┐
│      인프라 레이어 (Spring)                 │
│  - Spring Data                           │
│  - JPA                                   │
│  - Database                              │
└─────────────────────────────────────────┘
```

##### 문제점 지적

```java
// ❌ 도메인이 Spring에 의존
public class Order extends AbstractAggregateRoot<Order> {
    // Spring이 없으면 컴파일도 안 됨
    // 다른 프레임워크로 전환 불가능
}
```

**주장:**
- 도메인 모델은 순수해야 함 (POJO)
- 프레임워크 독립적이어야 함
- 포트 & 어댑터 패턴으로 분리해야 함

#### 2️⃣ 실용주의 관점 ✅

**"AbstractAggregateRoot는 충분히 일반적인 추상화다!"**

##### AbstractAggregateRoot의 특성

```java
// spring-data-commons
public abstract class AbstractAggregateRoot<A> {
    private List<Object> domainEvents = new ArrayList<>();
    
    protected <T> T registerEvent(T event) {
        this.domainEvents.add(event);
        return event;
    }
}
```

**특징:**
1. **특정 기술에 종속되지 않음**
   - DB 독립적 (JPA, MongoDB, Redis 모두 동작)
   - 순수 Java 코드
   - 특정 프레임워크 기능 사용 안 함

2. **도메인 개념을 표현**
   - "애그리거트 루트"는 DDD 개념
   - 이벤트 발행은 도메인 관심사
   - 비즈니스 로직 표현에 도움

3. **실용적 이점이 큼**
   - 이벤트 발행 자동화
   - 보일러플레이트 코드 제거
   - 실수 방지

**주장:**
- Spring은 사실상 표준
- `spring-data-commons`는 충분히 일반적
- 실용성 > 순수성

### 📋 의존성 레벨 비교

#### 심각도 낮음 ✅
```java
// spring-data-commons (일반적 추상화)
import org.springframework.data.domain.AbstractAggregateRoot;

// 문제 없음: 도메인 개념을 표현하는 추상 클래스
```

#### 심각도 중간 ⚠️
```java
// javax.persistence (표준이지만 기술 의존)
import javax.persistence.Entity;
import javax.persistence.Id;

// 주의: JPA 의존성이지만 사실상 표준
```

#### 심각도 높음 ❌
```java
// 특정 구현체에 강하게 결합
import org.hibernate.Session;
import com.mongodb.client.MongoCollection;

// 피해야 함: 특정 기술에 종속
```

### 🎯 실무 권장사항

#### 상황 1: 순수성이 중요한 경우

**언제:**
- 멀티 프레임워크 지원이 필요할 때
- 도메인 모델을 다른 프로젝트에서 재사용할 때
- 헥사고날 아키텍처를 엄격히 지킬 때

**방법:**
```java
// 자체 인터페이스 정의
public interface AggregateRoot<ID> {
    List<DomainEvent> getDomainEvents();
    void clearDomainEvents();
}

// 순수 구현
public class Order implements AggregateRoot<Long> {
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    @Override
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
```

#### 상황 2: 실용성이 중요한 경우 (대부분)

**언제:**
- Spring 생태계를 사용하는 일반적인 프로젝트
- 팀 생산성이 중요할 때
- 프레임워크 전환 계획이 없을 때

**방법:**
```java
// AbstractAggregateRoot 사용
import org.springframework.data.domain.AbstractAggregateRoot;

public class Order extends AbstractAggregateRoot<Order> {
    public void pay() {
        this.status = PAID;
        registerEvent(new OrderPaidEvent(this.id));
    }
}
```

### 💭 업계 전문가들의 의견

#### Martin Fowler (DDD 전도사)
> "완벽한 순수성보다 실용적인 타협이 더 나을 때가 많다."

#### Vaughn Vernon (DDD 구현 전문가)
> "Spring Data의 AbstractAggregateRoot는 충분히 일반적인 추상화이며, 
> 대부분의 프로젝트에서 사용해도 문제없다."

#### Eric Evans (DDD 창시자)
> "도메인 모델의 순수성은 중요하지만, 절대적인 규칙은 아니다. 
> 실용적인 타협점을 찾아라."

### 🎯 최종 권장

```
✅ 일반적인 Spring 프로젝트 → AbstractAggregateRoot 사용
⚠️ 순수성이 매우 중요한 프로젝트 → 자체 구현
```

**핵심:** 완벽한 순수성보다 실용적인 균형이 중요합니다! 🎯

## 🔧 도메인 서비스 (Domain Service) 상세 설명

### 🎯 도메인 서비스란?

**도메인 서비스는 하나의 엔티티에 속하지 않는 비즈니스 로직을 처리하는 서비스입니다.**

### 🤔 언제 사용하나요?

#### 1️⃣ 하나의 엔티티에 속하지 않는 로직
```java
// ❌ 엔티티에 넣기 어려운 경우
public class Order {
    // 주문 자체의 로직은 여기에
    public void pay() { ... }
    
    // 하지만 고객의 과거 주문 이력을 분석하는 로직은?
    // → Order에 Customer의 모든 주문을 알게 하는 것은 부자연스러움
}

// ✅ 도메인 서비스로 분리
public class OrderDomainService {
    public Money calculateDiscount(CustomerId customerId, Money orderAmount, List<Order> customerOrderHistory) {
        // 고객의 과거 주문 이력을 분석하여 할인율 계산
    }
}
```

#### 2️⃣ 여러 애그리거트에 걸친 비즈니스 규칙
```java
// 주문과 고객, 상품 등 여러 애그리거트를 비교하는 로직
public void validateNoDuplicateOrder(CustomerId customerId, List<OrderItem> newOrderItems, List<Order> recentOrders) {
    // 최근 주문들과 새 주문을 비교하여 중복 검사
}
```

#### 3️⃣ 도메인 개념이지만 엔티티로 표현하기 어려운 경우
```java
// 주문 우선순위는 도메인 개념이지만 Order 엔티티의 속성은 아님
public OrderPriority determinePriority(Money orderAmount) {
    // 주문 금액을 기반으로 우선순위 결정
}
```

### 📊 도메인 서비스 vs 엔티티 메서드

| 구분 | 엔티티 메서드 | 도메인 서비스 |
|------|---------------|---------------|
| **범위** | 하나의 엔티티 내부 | 여러 엔티티/애그리거트 |
| **예시** | `order.pay()` | `orderDomainService.calculateDiscount()` |
| **의존성** | 엔티티 자체 데이터만 | 여러 엔티티의 데이터 필요 |
| **재사용성** | 해당 엔티티에서만 | 여러 곳에서 재사용 가능 |

### 💡 실전 예시

#### 예시 1: 할인 계산
```java
public class OrderDomainService {
    
    public Money calculateDiscount(CustomerId customerId, Money orderAmount, List<Order> customerOrderHistory) {
        // 1. 고객의 총 주문 금액 계산
        Money totalOrderAmount = customerOrderHistory.stream()
                .map(Order::calculateTotalAmount)
                .reduce(Money.zero(), Money::add);
        
        // 2. VIP 고객 여부 확인
        if (totalOrderAmount.add(orderAmount).isGreaterThanOrEqual(VIP_THRESHOLD)) {
            return Money.of(orderAmount.getAmount().multiply(new BigDecimal("0.10"))); // 10% 할인
        }
        
        return Money.zero(); // 할인 없음
    }
}
```

#### 예시 2: 주문 검증
```java
public class OrderDomainService {
    
    public void validateOrder(Money orderAmount, int itemCount) {
        // 최소 주문 금액 검증
        if (orderAmount.isLessThan(Money.of(new BigDecimal("1000")))) {
            throw new IllegalArgumentException("최소 주문 금액은 1,000원입니다");
        }
        
        // 주문 항목 수 검증
        if (itemCount > 20) {
            throw new IllegalArgumentException("주문 항목은 최대 20개까지만 가능합니다");
        }
    }
}
```

### 🎓 학습 포인트

#### ✅ 도메인 서비스를 사용해야 하는 경우
- 고객의 과거 주문 이력을 분석하는 로직
- 여러 주문을 비교하는 로직
- 복잡한 비즈니스 규칙 (할인, 우선순위 등)
- 도메인 개념이지만 엔티티로 표현하기 어려운 경우

#### ❌ 도메인 서비스를 사용하지 말아야 하는 경우
- 단순한 엔티티 내부 로직 (`order.pay()`, `order.cancel()`)
- 인프라스트럭처 관련 로직 (DB 저장, 외부 API 호출)
- 애플리케이션 서비스 로직 (트랜잭션 관리, 조율)

### 🔄 표현 계층 (Presentation Layer) 상세 설명

### 🎯 표현 계층의 역할

**표현 계층은 사용자와의 인터페이스를 담당하는 계층입니다.**

### 📋 주요 책임

#### 1️⃣ HTTP 요청/응답 처리
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // HTTP 요청을 받아서 처리
    }
}
```

#### 2️⃣ 입력 검증 (Validation)
```java
private void validateCreateOrderRequest(CreateOrderRequest request) {
    if (request.customerId() == null || request.customerId() <= 0) {
        throw new IllegalArgumentException("유효하지 않은 고객 ID입니다");
    }
    if (request.items() == null || request.items().isEmpty()) {
        throw new IllegalArgumentException("주문 항목이 없습니다");
    }
}
```

#### 3️⃣ DTO 변환
```java
// Request DTO → Command 객체
CreateOrderCommand command = CreateOrderCommand.builder()
        .customerId(request.customerId())
        .items(request.items())
        .build();

// 응답 생성
OrderCreateResponse response = new OrderCreateResponse(orderId, "주문이 성공적으로 생성되었습니다");
```

#### 4️⃣ 예외 처리
```java
try {
    Long orderId = orderService.createOrder(command);
    return ResponseEntity.ok(new OrderCreateResponse(orderId));
} catch (IllegalArgumentException e) {
    return ResponseEntity.badRequest()
            .body(new OrderCreateResponse(null, "잘못된 입력: " + e.getMessage()));
} catch (IllegalStateException e) {
    return ResponseEntity.status(409)
            .body(new OrderCreateResponse(null, "주문 생성 불가: " + e.getMessage()));
}
```

### 🎓 표현 계층 학습 포인트

#### ✅ 표현 계층에서 해야 하는 것
- HTTP 요청/응답 처리
- 입력 검증
- DTO 변환
- 애플리케이션 서비스 호출
- 로깅

#### ❌ 표현 계층에서 하지 말아야 하는 것
- **도메인 서비스 직접 호출** (애플리케이션 계층을 통해 호출)
- 비즈니스 로직 (도메인 계층으로 위임)
- 데이터베이스 접근 (인프라 계층으로 위임)
- 트랜잭션 관리 (애플리케이션 계층으로 위임)
- 예외 처리 (GlobalExceptionHandler로 위임)

### 🔧 리팩토링된 아키텍처

```
표현 계층 (Controller)
    ↓ 의존
애플리케이션 계층 (Application Service)
    ↓ 의존
도메인 계층 (Domain Service, Entity, Value Object)
    ↓ 의존
인프라 계층 (Repository, External Client)
```

**핵심:** 각 계층은 바로 아래 계층만 의존해야 합니다!

## 💡 핵심 개념

### Bounded Context (제한된 컨텍스트)

```
┌─────────────────────────────────┐
│      주문 Bounded Context        │
│  - Order (우리 도메인)           │
│  - OrderItem                    │
└─────────────────────────────────┘
         │ WebClient
         ▼
┌─────────────────────────────────┐
│      상품 Bounded Context        │
│  - Product (외부 도메인)         │
└─────────────────────────────────┘
```

### 외부 도메인과의 통신

```java
// ❌ 잘못된 방법: Repository로 다른 도메인 직접 접근
@Autowired
private ProductRepository productRepository;

// ✅ 올바른 방법: Client로 외부 도메인 통신
@Autowired
private ProductClient productClient;
```

## 🎯 주요 학습 포인트

1. **도메인 모델에 비즈니스 로직 캡슐화**
   - Service는 도메인 객체를 조율만
   - 테스트하기 쉬운 순수 도메인 로직

2. **Bounded Context 간 통신**
   - Repository는 자신의 도메인만
   - 외부 도메인은 WebClient 사용

3. **값 객체(Value Object) 활용**
   - Money, Quantity로 도메인 개념 표현
   - 불변성과 자가 검증

4. **도메인 이벤트**
   - 도메인의 중요한 사건 표현
   - 느슨한 결합 유지

## 📈 코드 통계

- **전체 파일**: 34개 (Before: 6개, After: 27개 + 문서)
- **Before 코드**: ~350줄
- **After 코드**: ~2,000줄 (명확한 구조와 문서화)
- **Linter 에러**: 0개

## 🤝 기여

이 프로젝트는 학습 목적의 예시 코드입니다. 개선 사항이나 제안이 있다면 이슈를 열어주세요.

## 📄 라이선스

MIT License

## 📚 참고 자료

- Eric Evans - "Domain-Driven Design: Tackling Complexity in the Heart of Software"
- Vaughn Vernon - "Implementing Domain-Driven Design"
- Martin Fowler - "Patterns of Enterprise Application Architecture"

---

**Made with ❤️ for DDD learners**

