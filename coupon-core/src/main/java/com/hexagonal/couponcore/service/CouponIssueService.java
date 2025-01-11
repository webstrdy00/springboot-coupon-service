package com.hexagonal.couponcore.service;

import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.model.Coupon;
import com.hexagonal.couponcore.model.CouponIssue;
import com.hexagonal.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.hexagonal.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hexagonal.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static com.hexagonal.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

/**
 * 쿠폰 발급 관련 핵심 비즈니스 로직을 처리하는 서비스 클래스
 * 쿠폰 발급, 조회, 중복 검증 등의 도메인 로직을 담당
 */
@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;

    /**
     * 쿠폰 발급을 처리하는 메인 메서드
     * @param couponId 발급할 쿠폰 ID
     * @param userId 발급 대상 사용자 ID
     */
    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();  // 쿠폰 발급 가능 여부 확인 및 발급 처리
        saveCouponIssue(couponId, userId);
    }

    /**
     * 쿠폰 정책 조회
     * @param couponId 조회할 쿠폰 ID
     * @return 조회된 쿠폰 정책
     */
    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(() ->
                new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId))
        );
    }

    /**
     * 쿠폰 발급 이력 저장
     * @param couponId 발급할 쿠폰 ID
     * @param userId 발급 대상 사용자 ID
     * @return 저장된 쿠폰 발급 이력
     */
    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue issue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(issue);
    }

    /**
     * 쿠폰 중복 발급 검증
     * @param couponId 검증할 쿠폰 ID
     * @param userId 검증할 사용자 ID
     */
    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueJpaRepository.findFirstCouponIssue(couponId, userId);
        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE,
                    "이미 발급된 쿠폰입니다. user_id: %s, coupon_id: %s".formatted(userId, couponId));
        }
    }
}
