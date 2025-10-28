# DTO 패키지

## 개요
애플리케이션 계층의 **출력(Response)** 객체들을 관리합니다.

## 패키지 구조
```
application/dto/
├── OrderDetailResponse.java     # 주문 상세 응답
├── OrderItemResponse.java       # 주문 항목 응답
├── OrderPriorityResponse.java   # 주문 우선순위 응답
└── DiscountResponse.java        # 할인 계산 응답
```

## DTO 객체 특징
- **불변성**: Record를 사용하여 불변 객체로 설계
- **변환 로직**: 도메인 객체를 DTO로 변환하는 정적 메서드 제공
- **표현 계층 독립**: 도메인 객체의 내부 구조를 숨김
- **직렬화**: JSON 직렬화에 최적화

## 사용 예시
```java
// 주문 상세 조회
OrderDetailResponse response = orderService.getOrder(orderId);

// 도메인 객체에서 DTO 변환
OrderItemResponse itemResponse = OrderItemResponse.from(orderItem);
```

## DDD 원칙
- **도메인 보호**: 도메인 객체의 내부 구조를 외부에 노출하지 않음
- **계층 분리**: 도메인 계층과 표현 계층 사이의 변환 담당
- **의존성 방향**: DTO는 도메인에 의존, 도메인은 DTO에 의존하지 않음

## 변환 패턴
- **from() 메서드**: 도메인 객체 → DTO 변환
- **정적 팩토리**: 생성자 대신 명확한 의도를 표현
- **캡슐화**: 도메인 객체의 복잡한 변환 로직을 DTO에 캡슐화
