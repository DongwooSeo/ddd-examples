# DDD 아키텍처 설명

## 🎯 주문 도메인 중심 설계

이 프로젝트는 **주문(Order) 도메인**을 중심으로 DDD를 적용한 예시 코드입니다.

### 핵심 개념

#### 1. Bounded Context (제한된 컨텍스트)

```
┌─────────────────────────────────────────────────────────┐
│              주문 Bounded Context                        │
│  ┌────────────────────────────────────────────┐         │
│  │         Order Aggregate                    │         │
│  │  - Order (Root)                           │         │
│  │  - OrderItem                              │         │
│  │  - Value Objects (Money, Quantity, etc)   │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  외부 도메인과의 통신:                                    │
│  - ProductClient (상품 도메인)                          │
│  - CustomerClient (고객 도메인)                         │
│  - CouponClient (쿠폰 도메인)                           │
└─────────────────────────────────────────────────────────┘
```

#### 2. Anti-Corruption Layer (부패 방지 계층)

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

## 📁 프로젝트 구조

```
src/main/java/com/growmighty/examples/ddd/after/
│
├── application/                           # 애플리케이션 레이어
│   ├── OrderApplicationService.java      # 애플리케이션 서비스 (트랜잭션, 조율)
│   ├── command/                           # Command 객체 (입력)
│   │   ├── CreateOrderCommand.java
│   │   ├── PayOrderCommand.java
│   │   ├── CancelOrderCommand.java
│   │   └── OrderItemRequest.java
│   └── dto/                               # Response 객체 (출력)
│       ├── OrderDetailResponse.java
│       └── OrderItemResponse.java
│
├── domain/                                # 도메인 레이어 (핵심 비즈니스 로직)
│   ├── entity/                            # 엔티티
│   │   ├── Order.java                     # 애그리거트 루트
│   │   └── OrderItem.java                 # 엔티티
│   ├── vo/                                # 값 객체 (Value Objects)
│   │   ├── Money.java                     # 금액
│   │   ├── Quantity.java                  # 수량
│   │   ├── OrderStatus.java               # 주문 상태
│   │   ├── CustomerId.java                # 고객 ID
│   │   ├── ProductId.java                 # 상품 ID
│   │   ├── CouponCode.java                # 쿠폰 코드
│   │   └── ShippingAddress.java           # 배송 주소
│   ├── event/                             # 도메인 이벤트
│   │   ├── OrderDomainEvent.java
│   │   ├── OrderCreatedEvent.java
│   │   ├── OrderPaidEvent.java
│   │   └── OrderCancelledEvent.java
│   └── repository/                        # 레포지토리 인터페이스
│       └── OrderRepository.java
│
└── infrastructure/                        # 인프라 레이어
    └── external/                          # 외부 도메인 통신
        ├── ProductClient.java             # 상품 도메인 클라이언트
        ├── CustomerClient.java            # 고객 도메인 클라이언트
        ├── CouponClient.java              # 쿠폰 도메인 클라이언트
        └── dto/                           # 외부 도메인 DTO
            ├── ProductDTO.java
            ├── CustomerDTO.java
            └── CouponDTO.java
```

## 🔑 핵심 DDD 패턴

### 1. Aggregate (애그리거트)

```java
// Order는 애그리거트 루트
// OrderItem은 애그리거트 내부 엔티티
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
@Getter
@EqualsAndHashCode
public class Money {
    private final BigDecimal amount;
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
}
```

### 3. Domain Event (도메인 이벤트)

```java
// 도메인에서 발생한 중요한 사건을 표현
public class OrderPaidEvent extends OrderDomainEvent {
    private final Long orderId;
    private final Money paidAmount;
    private final LocalDateTime paidAt;
}
```

### 4. Repository Pattern (레포지토리 패턴)

```java
// 주문 도메인의 영속성만 담당
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 주문 도메인 자체의 조회만 담당
    // 다른 도메인 조회는 WebClient 사용
}
```

### 5. Anti-Corruption Layer (부패 방지 계층)

```java
// 외부 도메인과의 통신을 캡슐화
@Component
public class ProductClient {
    
    public ProductDTO getProduct(Long productId) {
        // 실제 환경에서는 WebClient로 HTTP 호출
        // return webClient.get()
        //     .uri("/products/{id}", productId)
        //     .retrieve()
        //     .bodyToMono(ProductDTO.class)
        //     .block();
        
        // 예시: Mock 데이터 반환
        return new ProductDTO(...);
    }
}
```

## 🔄 주문 프로세스 흐름

### 1. 주문 생성

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

### 2. 주문 취소

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

## 💡 DDD Before vs After

### Before (빈약한 도메인 모델)

```java
// 도메인 객체는 데이터만 담고 있음
@Getter @Setter
public class Order {
    private String status;
    private BigDecimal totalAmount;
}

// 비즈니스 로직이 서비스에 집중
@Service
public class OrderService {
    public void payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        
        // ❌ 비즈니스 로직이 서비스에 흩어짐
        if (!"PENDING".equals(order.getStatus())) {
            throw new Exception("결제 불가");
        }
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
    }
}
```

### After (풍부한 도메인 모델)

```java
// 도메인 객체가 비즈니스 로직을 포함
public class Order {
    private OrderStatus status;
    
    // ✅ 비즈니스 로직이 도메인 모델에 캡슐화
    public void pay() {
        if (!status.canBePaid()) {
            throw new IllegalStateException("결제할 수 없는 상태입니다");
        }
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
        registerEvent(new OrderPaidEvent(...));
    }
}

// 서비스는 도메인 객체를 조율만 함
@Service
public class OrderApplicationService {
    public void payOrder(PayOrderCommand command) {
        Order order = orderRepository.findById(command.orderId());
        order.pay(); // 도메인 모델에 위임
    }
}
```

## 🎓 배운 점

### 1. 외부 도메인과의 통신

- ❌ **Repository 사용**: 다른 도메인의 데이터에 직접 접근
- ✅ **WebClient 사용**: Anti-Corruption Layer를 통한 통신

### 2. DTO vs Entity

- **Entity**: 주문 도메인 내부 (Order, OrderItem)
- **DTO**: 외부 도메인 정보 (ProductDTO, CustomerDTO, CouponDTO)

### 3. 도메인 이벤트

- 주문 생성/결제/취소 시 이벤트 발행
- 다른 Bounded Context는 이벤트를 구독하여 처리
- 느슨한 결합 유지

## 📊 통계

- **총 파일 수**: 27개
- **도메인 레이어**: 14개 (핵심)
- **애플리케이션 레이어**: 7개
- **인프라 레이어**: 6개

## 🚀 실행 방법

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

## 📚 참고 자료

- Eric Evans - Domain-Driven Design
- Vaughn Vernon - Implementing Domain-Driven Design
- Martin Fowler - Patterns of Enterprise Application Architecture

