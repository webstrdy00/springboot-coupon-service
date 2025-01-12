package com.hexagonal.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexagonal.couponcore.component.DistributeLockExecutor;
import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.model.Coupon;
import com.hexagonal.couponcore.repository.redis.RedisRepository;
import com.hexagonal.couponcore.repository.redis.dto.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hexagonal.couponcore.exception.ErrorCode.*;
import static com.hexagonal.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.hexagonal.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 비동기 쿠폰 발급 처리
     * 처리 과정:
     * 1. 쿠폰 기본 정보 조회 및 발급 가능 기간 검증
     * 2. Redis 분산 락을 통한 동시성 제어
     * 3. 발급 가능 수량 및 중복 발급 검증
     * 4. Redis Queue에 발급 요청 저장
     * @param couponId 발급할 쿠폰 ID
     * @param userId 사용자 ID
     */
    public void issue(long couponId, long userId) {
        // 쿠폰 정보 조회 및 발급 가능 일자 확인
        Coupon coupon = couponIssueService.findCoupon(couponId);
        if (!coupon.availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. couponId: %s, issueStart: %s, issueEnd: %s"
                            .formatted(couponId, coupon.getDateIssuedStart(), coupon.getDateIssuedEnd()));
        }

        // 분산 락을 통한 동시성 제어
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            // 총 발급 가능 수량 체크
            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY,
                        "발급 가능한 수량을 초과합니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            // 사용자별 중복 발급 체크
            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(DUPLICATED_COUPON_ISSUE,
                        "이미 발급 요청이 처리됐습니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }

            issueRequest(couponId, userId);
        });
    }

    /**
     * Redis에 쿠폰 발급 요청 정보 저장
     * 1. 발급 요청 Set애 사용자 ID 저장 (중복 체크용)
     * 2. 발급 요청 Queue에 요청 정보 저장 (실제 처리용)
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     */
    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }
}
