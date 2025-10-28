# Command 패키지

## 개요
애플리케이션 계층의 **입력(Command)** 객체들을 관리합니다.

## 패키지 구조
```
application/command/
├── CreateOrderCommand.java      # 주문 생성 명령
├── PayOrderCommand.java         # 주문 결제 명령
├── CancelOrderCommand.java      # 주문 취소 명령
└── OrderItemRequest.java        # 주문 항목 요청
```

## Command 객체 특징
- **불변성**: Record를 사용하여 불변 객체로 설계
- **입력 검증**: 애플리케이션 서비스에서 검증
- **도메인 변환**: 애플리케이션 서비스에서 도메인 객체로 변환
- **단순성**: 복잡한 로직 없이 데이터만 담는 객체

## 사용 예시
```java
// 주문 생성
CreateOrderCommand command = new CreateOrderCommand(
    customerId,
    items,
    shippingAddress,
    couponCode
);
Long orderId = orderService.createOrder(command);
```

## DDD 원칙
- **CQRS**: Command와 Query 분리
- **도메인 중심**: 도메인 로직은 도메인 계층에서 처리
- **의존성 역전**: Command는 도메인에 의존하지 않음
