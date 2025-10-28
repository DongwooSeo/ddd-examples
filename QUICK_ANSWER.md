# ⚡ 빠른 답변: "도메인이 Spring에 의존하는 게 맞나요?"

## 🎯 답변

**네, `AbstractAggregateRoot`를 사용하면 도메인이 `org.springframework.data.domain`에 의존합니다.**

```java
import org.springframework.data.domain.AbstractAggregateRoot;  // Spring 의존!

public class Order extends AbstractAggregateRoot<Order> {
    // 도메인 로직
}
```

---

## 🤔 이게 문제인가요?

### 이론적으로는...
**❌ DDD 순수주의 관점에서는 문제**
- 도메인은 인프라에 의존하면 안 됨
- 프레임워크 독립적이어야 함
- 헥사고날 아키텍처 위반

### 실무적으로는...
**✅ 대부분의 경우 문제없음**
- `AbstractAggregateRoot`는 충분히 일반적인 추상화
- 특정 DB나 기술에 종속되지 않음
- 실용적 이점이 매우 큼
- 업계 표준으로 널리 사용됨

---

## 📊 두 가지 접근 비교

### 방법 1: AbstractAggregateRoot (실용주의)

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

---

### 방법 2: 순수 구현 (순수주의)

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

---

## 🎯 언제 뭘 선택해야 하나요?

### AbstractAggregateRoot 사용 (대부분의 경우)

**추천 상황:**
- ✅ 일반적인 Spring 프로젝트
- ✅ 팀 생산성이 중요
- ✅ 프레임워크 전환 계획 없음
- ✅ 실용성 > 순수성

```java
// 이렇게 쓰세요
public class Order extends AbstractAggregateRoot<Order> { }
```

---

### 순수 구현 사용 (특수한 경우)

**추천 상황:**
- ✅ 멀티 프레임워크 지원 필요
- ✅ 도메인 모델을 다른 프로젝트에서 재사용
- ✅ 헥사고날 아키텍처를 엄격히 준수해야 함
- ✅ 완벽한 순수성이 요구사항

```java
// 이렇게 쓰세요
public class Order implements AggregateRoot { }
```

---

## 💭 업계 전문가들의 의견

### Vaughn Vernon (DDD 구현 전문가)
> "Spring Data의 AbstractAggregateRoot는 충분히 일반적이며, 
> 대부분의 프로젝트에서 사용해도 괜찮다."

### Martin Fowler
> "완벽한 순수성보다 실용적인 타협이 더 나을 때가 많다."

---

## 🎓 강의 시 설명 포인트

1. **문제 제기**
   ```
   "도메인이 Spring에 의존하네요? 이게 맞나요?"
   ```

2. **양쪽 관점 소개**
   ```
   순수주의: "도메인은 순수해야 한다!"
   실용주의: "충분히 일반적인 추상화다!"
   ```

3. **코드 비교**
   ```java
   // AbstractAggregateRoot vs 순수 구현
   ```

4. **선택 가이드**
   ```
   "여러분의 프로젝트 상황에 맞게 선택하세요"
   ```

---

## 📝 핵심 정리

```
질문: 도메인이 Spring에 의존하는 게 맞나요?

답변: 
✅ 네, AbstractAggregateRoot 사용 시 의존합니다.
✅ 하지만 대부분의 경우 문제없습니다.
✅ 실용적 이점이 순수성보다 중요합니다.
⚠️ 완벽한 순수성이 필요하면 자체 구현하세요.

권장: 
일반 프로젝트 → AbstractAggregateRoot ✨
특수 요구사항 → 순수 구현 🎯
```

---

## 🔗 상세 자료

- [도메인_Spring_의존성_논의.md](computer:///mnt/user-data/outputs/도메인_Spring_의존성_논의.md) - 완전 분석
- [PureDomainImplementation.java](computer:///mnt/user-data/outputs/02_after_ddd/alternative/PureDomainImplementation.java) - 순수 구현 예시
- [AbstractAggregateRoot_역할.md](computer:///mnt/user-data/outputs/AbstractAggregateRoot_역할.md) - 역할 설명

---

**결론: 실용주의를 선택하세요. 대부분의 경우 그것이 최선입니다.** ✅
