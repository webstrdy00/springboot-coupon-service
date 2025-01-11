package com.hexagonal.couponcore.repository.mysql;

import com.hexagonal.couponcore.model.CouponIssue;

public interface CouponIssueJpaRepositoryCustom {
    CouponIssue findFirstCouponIssue(long couponId, long userId);
}
