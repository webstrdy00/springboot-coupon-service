package com.hexagonal.couponcore.component;

import com.hexagonal.couponcore.model.event.CouponIssueCompleteEvent;
import com.hexagonal.couponcore.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 쿠폰 이벤트를 처리하는 리스너 컴포넌트
 * 트랜잭션 완료 후 캐시 갱신을 담당
 */
@Component
@RequiredArgsConstructor
public class CouponEventListener {
    private final CouponCacheService couponCacheService;

    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * 쿠폰 발급 완료 이벤트 처리
     * 트랜잭션 커밋 후(AFTER_COMMIT) 실행되어 캐시 갱신
     * 1. Redis 캐시 갱신
     * 2. Local 캐시 갱신
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event) {
        log.info("issue complete. cache refresh start couponId: %s".formatted(event.couponId()));
        couponCacheService.putCouponCache(event.couponId());
        couponCacheService.putCouponLocalCache(event.couponId());
        log.info("issue complete. cache refresh end couponId: %s".formatted(event.couponId()));
    }
}
