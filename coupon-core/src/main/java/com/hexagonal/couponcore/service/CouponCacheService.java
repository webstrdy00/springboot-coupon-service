package com.hexagonal.couponcore.service;

import com.hexagonal.couponcore.model.Coupon;
import com.hexagonal.couponcore.repository.redis.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *  쿠폰 정보 캐싱 서비스
 *  Redis(분산 캐시)와 Caffeine(로컬 캐시)을 함께 사용하는 Two-Level 캐시 구현
 */
@Service
@RequiredArgsConstructor
public class CouponCacheService {
    private final CouponIssueService couponIssueService;

    /**
     * 쿠폰 정보 캐시조회
     * @param couponId 쿠폰 ID
     * @return 캐시된 쿠폰 정보
     * @Cacheable: 동일한 couponId로 호출 시 캐시된 데이터 반환
     */
    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }

    /**
     * 로컬 캐시에서 쿠폰 정보 조회
     * 캐시 미스 시 Redis 캐시 조회
     */
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    /**
     * Redis 캐시 강제 갱신
     */
    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(long couponId) {
        return getCouponCache(couponId);
    }

    /**
     * 로컬 캐시 강제 갱신
     */
    @CachePut(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity putCouponLocalCache(long couponId) {
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }
}
