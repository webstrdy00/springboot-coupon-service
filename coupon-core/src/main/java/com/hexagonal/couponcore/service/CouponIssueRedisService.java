package com.hexagonal.couponcore.service;

import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.repository.redis.CouponRedisEntity;
import com.hexagonal.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hexagonal.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.hexagonal.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static com.hexagonal.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {
    private final RedisRepository redisRepository;

    /**
     * 쿠폰 발급 가능 여부 종합 검증
     * 1. 총 발급 수량 검증
     * 2. 사용자별 중복 발급 검증
     */
    public void checkCouponIssueQuantity(CouponRedisEntity coupon, long userId) {
        if (!availableTotalIssueQuantity(coupon.totalQuantity(), coupon.id())) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY,
                    "발급 가능한 수량을 초과합니다. couponId: %s, userId: %s".formatted(coupon.id(), userId));
        }
        if (!availableUserIssueQuantity(coupon.id(), userId)) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE,
                    "이미 발급 요청이 처리됐습니다. couponId: %s, userId: %s".formatted(coupon.id(), userId));
        }
    }

    /**
     * 쿠폰의 총 발급 가능 수량 검증
     * 검증 로직:
     * 1. 무제한 수량(totalQuantity가 null)인 경우 항상 true
     * 2. Redis Set의 크기(현재까지 발급 요청 수)와 총 수량 비교
     * @param totalQuantity 쿠폰의 총 발급 가능 수량 (null인 경우 무제한)
     * @param couponId 쿠폰 ID
     * @return 발급 가능 여부
     */
    public boolean availableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) {
            return true;
        }

        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    /**
     * 사용자별 중복 발급 검증
     * 검증 로직:
     * - Redis Set에 해당 사용자 ID가 있는지 확인
     * - Set에 없으면 발급 가능(true), 있으면 중복 발급(false)
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     * @return 발급 가능 여부 (true: 발급 가능, false: 이미 발급됨)
     */
    public boolean availableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}
