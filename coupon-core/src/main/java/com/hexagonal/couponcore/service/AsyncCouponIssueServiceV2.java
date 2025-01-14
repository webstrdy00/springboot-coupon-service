package com.hexagonal.couponcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexagonal.couponcore.repository.redis.CouponRedisEntity;
import com.hexagonal.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * V2 비동기 쿠폰 발급 서비스
 * Redis Lua 스크립트를 활용한 원자적 쿠폰 발급 처리
 */
@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV2 {
    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 쿠폰 발급 요청 처리
     * 1. Redis 캐시에서 쿠폰 정보 조회
     * 2. 발급 가능 여부 검증
     * 3. Redis Lua 스크립트로 원자적 발급 처리
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     */
    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    /**
     * 실제 발급 요청 처리
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     * @param totalIssueQuantity null인 경우 무제한 발급으로 처리
     */
    private void issueRequest(long couponId, long userId, Integer totalIssueQuantity) {
        if (totalIssueQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }
}
