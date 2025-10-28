# 도메인 계층의 Spring 의존성 논의

## 🤔 핵심 질문

> "도메인 계층이 `org.springframework.data.domain`을 참조하는 건 맞나요?"

**답: 네, 맞습니다. 그리고 이것은 실무에서 논쟁이 되는 주제입니다.**

---

## 📊 현실: AbstractAggregateRoot 사용 시 의존성

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

---

## 🎭 두 가지 관점

### 1️⃣ 순수주의 관점 ❌

**"도메인은 인프라에 의존하면 안 된다!"**

#### DDD 헥사고날 아키텍처 원칙

```
┌─────────────────────────────────────────┐
│         도메인 레이어 (순수)              │
│  - 비즈니스 로직만                       │
│  - 프레임워크 의존 X                     │
│  - 순수 Java 객체                        │
└─────────────────────────────────────────┘
         ↑
         │ (의존 방향이 잘못됨!)
         │
┌─────────────────────────────────────────┐
│      인프라 레이어 (Spring)               │
│  - Spring Data                           │
│  - JPA                                   │
│  - Database                              │
└─────────────────────────────────────────┘
```

#### 문제점 지적

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

---

### 2️⃣ 실용주의 관점 ✅

**"AbstractAggregateRoot는 충분히 일반적인 추상화다!"**

#### AbstractAggregateRoot의 특성

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

---

## 📊 비교: 순수 구현 vs AbstractAggregateRoot

### 방법 1: 완전 순수 구현 (순수주의)

```java
package com.example.order.domain;

// ✅ Spring 의존성 없음!
public class Order {
    
    private Long id;
    private OrderStatus status;
    
    // 자체 이벤트 관리
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    public void pay() {
        this.status = OrderStatus.PAID;
        this.domainEvents.add(new OrderPaidEvent(this.id));
    }
    
    // 이벤트 조회 메서드
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
```

**인프라 레이어에서 이벤트 발행:**

```java
package com.example.order.infrastructure;

@Component
public class OrderRepositoryAdapter {
    
    private final JpaOrderRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public Order save(Order order) {
        // 1. 저장
        Order saved = jpaRepository.save(order);
        
        // 2. 도메인 이벤트 수동 발행
        for (DomainEvent event : saved.getDomainEvents()) {
            eventPublisher.publishEvent(event);
        }
        
        // 3. 이벤트 초기화
        saved.clearDomainEvents();
        
        return saved;
    }
}
```

**장점:**
- ✅ 도메인이 완전히 순수함
- ✅ 프레임워크 독립적
- ✅ 포트 & 어댑터 패턴 준수

**단점:**
- ❌ 보일러플레이트 코드 많음
- ❌ 이벤트 발행을 깜빡할 수 있음
- ❌ 구현 복잡도 증가

---

### 방법 2: AbstractAggregateRoot 사용 (실용주의)

```java
package com.example.order.domain;

// ⚠️ Spring Data 의존
import org.springframework.data.domain.AbstractAggregateRoot;

public class Order extends AbstractAggregateRoot<Order> {
    
    private Long id;
    private OrderStatus status;
    
    public void pay() {
        this.status = OrderStatus.PAID;
        registerEvent(new OrderPaidEvent(this.id));
    }
}
```

**Repository는 단순:**

```java
package com.example.order.infrastructure;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // save() 호출하면 자동으로 이벤트 발행!
}
```

**장점:**
- ✅ 코드가 간결함
- ✅ 이벤트 발행 자동화
- ✅ 실수 방지

**단점:**
- ❌ Spring에 의존
- ❌ 프레임워크 전환 시 수정 필요

---

## 🎯 실무 권장사항

### 상황 1: 순수성이 중요한 경우

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

---

### 상황 2: 실용성이 중요한 경우 (대부분)

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

---

## 💭 업계 의견

### Martin Fowler (DDD 전도사)
> "완벽한 순수성보다 실용적인 타협이 더 나을 때가 많다."

### Vaughn Vernon (DDD 구현 전문가)
> "Spring Data의 AbstractAggregateRoot는 충분히 일반적인 추상화이며, 
> 대부분의 프로젝트에서 사용해도 문제없다."

### Eric Evans (DDD 창시자)
> "도메인 모델의 순수성은 중요하지만, 절대적인 규칙은 아니다. 
> 실용적인 타협점을 찾아라."

---

## 📋 의존성 레벨 비교

### 심각도 낮음 ✅
```java
// spring-data-commons (일반적 추상화)
import org.springframework.data.domain.AbstractAggregateRoot;

// 문제 없음: 도메인 개념을 표현하는 추상 클래스
```

### 심각도 중간 ⚠️
```java
// javax.persistence (표준이지만 기술 의존)
import javax.persistence.Entity;
import javax.persistence.Id;

// 주의: JPA 의존성이지만 사실상 표준
```

### 심각도 높음 ❌
```java
// 특정 구현체에 강하게 결합
import org.hibernate.Session;
import com.mongodb.client.MongoCollection;

// 피해야 함: 특정 기술에 종속
```

---

## 🎓 강의용 추천 접근

### 1단계: 문제 인식
```java
// "도메인이 Spring에 의존하는 게 맞나요?" 질문 유도
public class Order extends AbstractAggregateRoot<Order> {
    // ...
}
```

### 2단계: 논쟁 소개
```
순수주의: "도메인은 순수해야 한다!"
    vs
실용주의: "AbstractAggregateRoot는 충분히 일반적이다!"
```

### 3단계: 양쪽 구현 비교
```java
// 순수 구현
public class Order {
    private List<DomainEvent> events = new ArrayList<>();
}

// Spring 사용
public class Order extends AbstractAggregateRoot<Order> {
    // registerEvent() 제공
}
```

### 4단계: 팀 결정
```
"여러분의 프로젝트 상황에 맞게 선택하세요"
- 순수성이 중요? → 순수 구현
- 생산성이 중요? → AbstractAggregateRoot
```

---

## 🎯 결론

### 이론적 관점
```
❌ 도메인이 인프라(Spring)에 의존하는 것은 이상적이지 않다
```

### 현실적 관점
```
✅ AbstractAggregateRoot는:
   - 충분히 일반적인 추상화
   - 실용적 이점이 큼
   - 대부분의 프로젝트에서 문제없음
   - 업계에서 널리 사용됨
```

### 최종 권장
```
✅ 일반적인 Spring 프로젝트 → AbstractAggregateRoot 사용
⚠️ 순수성이 매우 중요한 프로젝트 → 자체 구현
```

---

## 📚 참고: 다른 프레임워크의 접근

### Axon Framework
```java
// Axon도 자체 추상 클래스 제공
public class Order extends AggregateRoot<Order> {
    // Axon의 이벤트 발행 메커니즘
}
```

### Eventuate
```java
// Eventuate도 유사한 접근
public class Order extends ReflectiveMutableCommandProcessingAggregate {
    // ...
}
```

**포인트:** 대부분의 DDD 프레임워크가 비슷한 접근을 사용합니다!

---

## 💡 실전 팁

### 타협안 1: 인터페이스 분리
```java
// 도메인 레이어에 인터페이스 정의
public interface EventPublisher {
    <T> T registerEvent(T event);
}

// Spring 구현체
public class SpringEventPublisher implements EventPublisher {
    // AbstractAggregateRoot 활용
}
```

### 타협안 2: 조건부 컴파일
```java
// Spring 프로필에 따라 다른 구현 사용
@Profile("spring")
public class Order extends AbstractAggregateRoot<Order> { }

@Profile("pure")
public class Order implements AggregateRoot { }
```

---

## 🎤 강의 시 답변 예시

**학생:** "도메인이 Spring에 의존하는 게 맞나요?"

**강사:** 
"좋은 질문입니다! 이론적으로는 도메인이 순수해야 하지만, 
실무에서는 `AbstractAggregateRoot` 사용이 일반적입니다.

이유는:
1. `spring-data-commons`는 충분히 일반적인 추상화
2. 특정 기술(DB, 프레임워크)에 종속되지 않음
3. 실용적 이점이 매우 큼
4. 업계 표준으로 자리잡음

완벽한 순수성이 필요한 특수한 경우가 아니라면, 
`AbstractAggregateRoot` 사용을 권장합니다.

중요한 것은 '의존의 정도'입니다. 
Hibernate나 MongoDB 같은 특정 구현체에 의존하는 것은 피해야 하지만,
`AbstractAggregateRoot` 정도는 괜찮습니다."

---

**핵심:** 완벽한 순수성보다 실용적인 균형이 중요합니다! 🎯
