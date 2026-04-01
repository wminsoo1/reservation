# 🎟️ 대규모 트래픽 처리를 위한 선착순 쿠폰 발급 시스템

단기간에 트래픽이 폭증하는 선착순 이벤트 상황에서, 시스템 다운 없이 안정적으로 트래픽을 제어하고 데이터 정합성을 보장하기 위한 분산 아키텍처를 설계 및 구현했습니다.

초기 RDB 중심의 동기적 처리 구조에서 발생하던 **DB 커넥션 풀 고갈, Race Condition으로 인한 초과 발급 및 성능 저하 문제**를 해결했습니다.

---

## 🏆 핵심 성과 (nGrinder 부하 테스트 검증)

약 200명의 가상 유저(Vuser)가 1분간 지속적으로 요청을 쏘는 극단적인 동시 접속 환경에서 테스트를 진행했습니다.

| 지표 | 개선 전 (RDB 동기) | 개선 후 (분산 비동기) | 성과 |
| :--- | :---: | :---: | :---: |
| **에러율** | **70%** (병목/타임아웃) | **0%** (완벽 방어) | **안정성 확보** |
| **평균 TPS** | 7.7 | **37.5** | **4.8배(380%) 향상** |

> **"1,737건의 요청을 에러율 0%로 완벽하게 처리해 냈습니다."**

---

## 🛠️ Technology Stack

<p>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=Spring%20Boot&logoColor=white">
  <img src="https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=Java&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
  <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=Apache%20Kafka&logoColor=white">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white">
</p>

---

## 🏗️ 아키텍처 설계 흐름 (System Architecture)

거대한 트래픽을 안전하게 소화하기 위해 시스템을 크게 두 가지 도메인(책임)으로 나누어 설계했습니다.

### Phase 1. 트래픽 격리 및 통제 (대기열 시스템)

무의미한 DB 조회를 막기 위해 Redis ZSet을 활용하여 유저를 줄 세우고, 백그라운드 스케줄러가 시스템이 수용 가능한 인원(N명)에게만 순차적으로 입장 권한을 부여하여 트래픽을 통제했습니다.

<img src="https://github.com/wminsoo1/reservation/blob/main/redis%20%EB%8C%80%EA%B8%B0%EC%97%B4.png" alt="대기열 아키텍처" width="100%">

**Key Implementation:**
- 프론트엔드에서 1초마다 폴링하며 대기 순위 확인
- Redis ZSet의 타임스탬프를 이용한 선입선출(FIFO) 보장
- `zpopmin`을 활용한 원자적(Atomic) 유저 추출

### Phase 2. 비동기 발급 및 최종 일관성 보장 (쿠폰 발급 시스템)

대기열을 통과한 유저의 발급 요청은 Outbox 패턴과 Kafka를 거쳐 백그라운드 Worker가 비동기로 처리하도록 위임하여 시스템의 처리량(TPS)을 극대화했습니다.

<img src="https://github.com/wminsoo1/reservation/blob/main/%EC%BF%A0%ED%8F%B0%20%EB%B0%9C%EA%B8%89.png" alt="쿠폰 발급 아키텍처" width="100%">

**Key Implementation:**
- **Redis Lua Script:** 재고 차감과 중복 검증을 원자적으로 처리하여 초과 발급 원천 차단
- **Transactional Outbox 패턴:** DB 저장과 카프카 이벤트 발행의 원자성을 보장하여 메시지 유실 방지
- **Consumer `INSERT IGNORE`:** 카프카 메시지 중복 수신 시 멱등성을 보장하여 Exactly-once 달성

---
