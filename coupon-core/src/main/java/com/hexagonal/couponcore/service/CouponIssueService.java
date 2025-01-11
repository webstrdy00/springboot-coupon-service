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

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;

    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(() ->
                new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId))
        );
    }

    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue issue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(issue);
    }

    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueJpaRepository.findFirstCouponIssue(couponId, userId);
        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE,
                    "이미 발급된 쿠폰입니다. user_id: %s, coupon_id: %s".formatted(userId, couponId));
        }
    }
}
