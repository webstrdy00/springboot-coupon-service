package com.hexagonal.couponcore.service;

import com.hexagonal.couponcore.model.Coupon;
import com.hexagonal.couponcore.repository.redis.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *  쿠폰 정보 캐싱 서비스
 *  Redis를 이용한 쿠폰 정보 캐싱으로 DB 부하 감소
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
}
