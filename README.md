# springboot-coupon-service

## 프로젝트 소개
대규모 트래픽 환경에서의 선착순 쿠폰 발급 시스템 구현 프로젝트입니다.
- 동시성 제어를 위한 다양한 전략 (DB Lock, Redis Lock, Lua Script)
- Two-Level Cache를 통한 성능 최적화
- 이벤트 기반의 캐시 정합성 관리
- 비동기 처리를 통한 안정적인 대용량 트래픽 처리

## 기술 스택
- Backend : Java 17, Spring Boot 3.3.4, QueryDSL, Spring Data JPA
- Database : MySQL, Redis
- Cache : Caffeine, Redis
- Monitoring : Prometheus & Grafana
- Build : Gradle, Docker

## 시스템 아키텍처
### 1. 멀티 모듈 구조
- `coupon-core`: 핵심 비즈니스 로직
- `coupon-api`: 외부 요청 처리를 위한 API 서버
- `coupon-consumer`: 비동기 쿠폰 발급 처리, 비동기 처리 서버

### 2. 핵심 기능
1. 동기식 쿠폰 발급 (V1)
   - DB 비관적 락을 통한 동시성 제어
   - 실시간 발급 처리
   - 강력한 데이터 정합성 보장

2. 비동기식 쿠폰 발급 (V2)
   - Redis Lua 스크립트를 통한 원자적 처리
   - 메시지 큐를 활용한 비동기 처리
   - 부하 분산 및 성능 최적화

3. Two-Level 캐시 전략
   - Local Cache (Caffeine)
   - Distributed Cache (Redis)
   - 이벤트 기반 캐시 갱신

## 성능 최적화 전략
1. 캐시 계층
   - Local Cache (10초) TTL
   - Redis Cache (30분) TTL
   - 캐시 갱신 이벤트 기반 처리

2. 동시성 제어
   - DB 비관적 락
   - Redis 분산 락
   - Lua 스크립트를 통한 원자적 처리

3. 비동기 처리
   - Redis Queue를 통한 요청 처리
   - 배치 처리를 통한 DB 부하 감소

## 모니터링
- Prometheus & Grafana를 통한 실시간 모니터링
- 주요 모니터링 지표
  - 시스템 메트릭: CPU, Memory, GC
  - 비즈니스 메트릭: 발급 성공/실패율, TPS
  - 캐시 메트릭: Hit/Miss 비율

## 고려사항
### 캐시 정합성 문제
- 문제 : 쿠폰 소진 시 캐시 갱신 누락
- 해결 : 이벤트 기반 캐시 갱신 도입

```
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
void issueComplete(CouponIssueCompleteEvent event) {
    couponCacheService.putCouponCache(event.couponId());
    couponCacheService.putCouponLocalCache(event.couponId());
}
```
### Redis 락 관리
- 문제 : 락 해제 누락 가능성
- 해결 : try-finally 블록으로 안전한 락 해제
```
public void execute(String lockName, long waitTime, 
                   long leaseTime, Runnable logic) {
    RLock lock = redissonClient.getLock(lockName);
    try {
        boolean isLocked = lock.tryLock(waitTime, leaseTime, 
                                      TimeUnit.MILLISECONDS);
        if (!isLocked) {
            throw new IllegalStateException("Lock 획득 실패");
        }
        logic.run();
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```
