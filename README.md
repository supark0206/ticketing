# 콘서트 좌석 예매 시스템

실시간 좌석 선택, 결제 처리, 알림 서비스를 갖춘 콘서트 티켓 관리용 Spring Boot 애플리케이션입니다.

---

## 프로젝트 개요

이 티켓팅 시스템은 콘서트 티켓 관리를 위한 완전한 솔루션입니다.

- **Redis 기반 실시간 좌석 선택**
- **Webhook 연동을 통한 안전한 결제 처리**
- **RabbitMQ 메시징을 통한 이메일 알림**
- **관리자/사용자 역할 기반 접근 제어**
- **Prometheus, Grafana, Loki를 활용한 모니터링**
- **Redis 기반 대기열 등록및 SSE를 통한 실시간 조회**

---

## 기술 스택

### Backend

- **Java 17** (Spring Boot 3.3.13)
- **Spring Security** (JWT 인증)
- **Spring Data JPA** + QueryDSL
- **PostgreSQL** 데이터베이스
- **Redis** (캐싱 및 세션 관리)
- **RabbitMQ** (비동기 메시징)
- **Swagger / OpenAPI 3** (API 문서화)

### ERD

[접속링크](https://www.mermaidchart.com/app/projects/093e7a54-fdfe-49ac-8b92-fa9a9385b74e/diagrams/2638dfd7-99af-45af-8113-4f173a5c27ff/share/invite/eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkb2N1bWVudElEIjoiMjYzOGRmZDctOTlhZi00NWFmLTgxMTMtNGYxNzNhNWMyN2ZmIiwiYWNjZXNzIjoiRWRpdCIsImlhdCI6MTc1MTg4MTMwMn0.V7sdDuy6ubOGtdwBRsLD8fyPYEvSlljGiwG27A7MwV8)

![img.png](image/img.png)

- 인덱스 전략
  - `users.email` (UK)
  - `users.username` (UK)
  - `seat.concert_id` (FK 인덱스)
  - `reservation.user_id` (FK 인덱스)
  - `reservation.concert_id` (FK 인덱스)
  - `reservation_seat.reservation_id` (FK 인덱스)
  - `reservation_seat.seat_id` (FK 인덱스)
  - `payments.reservation_id` (FK 인덱스)
  - `payments.transaction_id` (UK)


### 모니터링 & 관찰 가능성
- **Grafana** (시각화 대시보드)
- **Prometheus** (메트릭 수집)
  - ![img_2.png](image/img_2.png)
- **Loki** (중앙집중 로그 관리)
  - ![img_1.png](image/img_1.png)
- **Promtail** (로그 수집)
- **Spring Boot Actuator** (애플리케이션 메트릭)

---

### 사전 준비

- Java 17 이상
- Docker & Docker Compose
- Git

### Docker Compose로 실행

   ```bash
   # 레포지토리 클론
   git clone https://github.com/supark0206/ticketing
   
   # develop 브랜치 이동
   git checkout develop
   
   # 프로젝트 이동
   cd ticketing
   
   # 서비스 실행
   docker-compose up -d # 또는
   docker-compose up --build
   
   # 로그 확인
   docker-compose logs -f concert-app
   ```

### 접속 정보

- 애플리케이션: http://localhost:8080

- Swagger UI: http://localhost:8080/swagger-ui.html

- Grafana: http://localhost:3000 (admin/admin)

- Prometheus: http://localhost:9090

- RabbitMQ Management: http://localhost:15672 (admin/admin123)

- MailHog: http://localhost:8025

- Postgres: localhost:5433/ticketing (postgres/1234)
  - 도커 실행시 5433포트 / 로컬 직접 실행시 5432

---

### 애플리케이션 프로필
- `dev` - 상세 로깅이 포함된 개발 환경
- `prod` - 최적화된 설정의 프로덕션 환경

---

## 구현 시 고려사항

### 성능 최적화
- **Redis 캐싱**: 좌석 가용성 및 사용자 세션 캐싱으로 데이터베이스 부하 감소
- **QueryDSL**: 복잡한 쿼리 최적화 및 타입 안전성 보장
- **비동기 처리**: RabbitMQ를 통한 이메일 발송 및 알림 처리

### 동시성 제어
- **좌석 선택**: Redis 기반 락으로 동시 선택 방지
- **결제 처리**: 멱등성 키(`transaction_id`)로 중복 결제 방지
- **대기열 관리**: Redis Sorted Set 기반 실시간 좌석 가용성 체크로 동적 대기열 처리

### 모니터링 및 관찰성
- **메트릭 수집**: 비즈니스 메트릭 및 기술 메트릭 분리
- **로그 관리**: 구조적 로깅 및 중앙집중 관리


