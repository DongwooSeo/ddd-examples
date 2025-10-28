# AbstractAggregateRoot의 역할

## 🎯 한 줄 요약

**도메인 이벤트를 쉽게 등록하고 자동으로 발행할 수 있게 해주는 DDD 애그리거트 루트 구현 지원 클래스**

---

## 🔑 핵심 역할 3가지

### 1️⃣ 도메인 이벤트 저장소 제공

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

---

### 2️⃣ Spring의 이벤트 발행 시스템과 자동 연결

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

---

### 3️⃣ 이벤트 발행 타이밍 제어

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

---

## 📊 왜 필요한가?

### ❌ AbstractAggregateRoot 없이 구현하면

```java
@Entity
public class Order {
    
    public void pay() {
        this.status = PAID;
        // 이벤트를 어떻게 발행할까? 🤔
    }
}

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;  // 주입 필요!
    
    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.pay();
        orderRepository.save(order);
        
        // 😭 문제점들:
        // 1. 수동으로 이벤트 발행 필요 (깜빡할 수 있음!)
        eventPublisher.publishEvent(new OrderPaidEvent(...));
        
        // 2. 도메인 모델에서 이벤트 발행 불가
        // 3. 서비스에 이벤트 발행 코드 분산
        // 4. 도메인 로직과 기술적 코드 섞임
    }
}
```

**문제점:**
- ❌ 이벤트 발행을 깜빡할 수 있음
- ❌ 도메인 모델이 Spring에 의존해야 함 (순수하지 않음)
- ❌ 서비스 레이어가 복잡해짐
- ❌ 테스트하기 어려움

---

### ✅ AbstractAggregateRoot 사용하면

```java
@Entity
public class Order extends AbstractAggregateRoot<Order> {
    
    public void pay() {
        this.status = PAID;
        
        // 도메인 모델에서 직접 이벤트 등록!
        registerEvent(new OrderPaidEvent(
            this.id,
            this.calculateFinalAmount()
        ));
        
        // Spring에 의존하지 않음 (순수한 도메인 모델)
        // ApplicationEventPublisher 주입 불필요!
    }
}

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    // ApplicationEventPublisher 필요 없음!
    
    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.pay();  // 이벤트 등록
        orderRepository.save(order);  // 자동 발행! ✨
        
        // 끝! 이벤트 발행 코드 불필요
    }
}
```

**장점:**
- ✅ 이벤트 발행 자동화 (깜빡할 일 없음)
- ✅ 도메인 모델이 순수함 (Spring에 의존 X)
- ✅ 서비스 레이어 간결
- ✅ 테스트 용이

---

## 🔄 전체 동작 흐름

```
┌─────────────────────────────────────────────────────────────┐
│  1. 도메인 모델 (AbstractAggregateRoot 상속)                 │
├─────────────────────────────────────────────────────────────┤
│  class Order extends AbstractAggregateRoot<Order> {          │
│      public void pay() {                                     │
│          this.status = PAID;                                 │
│          registerEvent(new OrderPaidEvent(...));             │
│          //           ↓                                      │
│          // domainEvents 리스트에 저장됨                     │
│      }                                                       │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  2. Repository.save() 호출                                   │
├─────────────────────────────────────────────────────────────┤
│  orderRepository.save(order);                                │
│                                                              │
│  Spring Data JPA (SimpleJpaRepository) 내부에서:            │
│  ┌──────────────────────────────────────────────┐          │
│  │ public <S> S save(S entity) {                │          │
│  │     // 1. 엔티티 저장                         │          │
│  │     em.persist(entity);                      │          │
│  │                                               │          │
│  │     // 2. 이벤트 발행 (자동!) ⭐              │          │
│  │     if (entity instanceof AbstractAggregateRoot) {      │
│  │         for (event : entity.domainEvents()) {│          │
│  │             publisher.publishEvent(event);   │          │
│  │         }                                     │          │
│  │         entity.clearDomainEvents();          │          │
│  │     }                                         │          │
│  │     return entity;                            │          │
│  │ }                                             │          │
│  └──────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  3. 이벤트 핸들러 실행                                        │
├─────────────────────────────────────────────────────────────┤
│  @EventListener                                              │
│  public void handle(OrderPaidEvent event) {                  │
│      // 알림 발송, 통계 업데이트 등                          │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎓 주요 메서드와 역할

### registerEvent()
```java
protected <T> T registerEvent(T event)
```
- **역할:** 이벤트를 내부 리스트에 등록
- **호출 시점:** 도메인 메서드 내부 (pay(), cancel() 등)
- **특징:** 즉시 발행하지 않고 저장만 함

### domainEvents()
```java
protected Collection<Object> domainEvents()
```
- **역할:** 등록된 모든 이벤트 조회
- **호출 시점:** Spring Data가 save() 시 자동 호출
- **특징:** 읽기 전용 컬렉션 반환

### clearDomainEvents()
```java
@AfterDomainEventPublication
protected void clearDomainEvents()
```
- **역할:** 이벤트 발행 후 리스트 초기화
- **호출 시점:** Spring Data가 이벤트 발행 후 자동 호출
- **특징:** 같은 이벤트가 중복 발행되는 것 방지

### andEvent() (Spring Data 3.x+)
```java
protected final A andEvent(Object event)
```
- **역할:** registerEvent() + 메서드 체이닝
- **사용 예:**
```java
return this.andEvent(new OrderPaidEvent(...))
           .andEvent(new PaymentRecordedEvent(...));
```

---

## 🆚 비교: AbstractAggregateRoot vs 수동 구현

| 항목 | AbstractAggregateRoot | 수동 구현 |
|------|----------------------|----------|
| **이벤트 발행** | 자동 (save() 시) | 수동 (직접 호출) |
| **Spring 의존성** | 도메인 모델은 의존 X | 도메인 모델이 의존 |
| **코드 복잡도** | 낮음 | 높음 |
| **실수 가능성** | 낮음 (자동화) | 높음 (깜빡할 수 있음) |
| **테스트** | 쉬움 (순수 객체) | 어려움 (Mock 필요) |
| **트랜잭션 연계** | 자동 | 수동 관리 필요 |

---

## 💡 실전 사용 패턴

### 패턴 1: 단일 이벤트
```java
public class Order extends AbstractAggregateRoot<Order> {
    
    public void pay() {
        validatePayment();
        this.status = PAID;
        registerEvent(new OrderPaidEvent(this.id));
    }
}
```

### 패턴 2: 여러 이벤트
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

### 패턴 3: 조건부 이벤트
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

---

## ⚠️ 주의사항

### 1. save() 호출 필수
```java
// ❌ 잘못된 사용
order.pay();  // 이벤트 등록만 됨
// save()를 호출하지 않으면 이벤트가 발행되지 않음!

// ✅ 올바른 사용
order.pay();
orderRepository.save(order);  // 이벤트 자동 발행
```

### 2. 트랜잭션 범위
```java
//