package com.hexagonal.couponcore.repository.mysql;

import com.hexagonal.couponcore.model.CouponIssue;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.hexagonal.couponcore.model.QCouponIssue.couponIssue;

@Repository
@RequiredArgsConstructor
public class CouponIssueJpaRepositoryImpl implements CouponIssueJpaRepositoryCustom {

    private final JPQLQueryFactory queryFactory;

    @Override
    public CouponIssue findFirstCouponIssue(long couponId, long userId) {
        return queryFactory.selectFrom(couponIssue)
                .where(couponIssue.couponId.eq(couponId))
                .where(couponIssue.userId.eq(userId))
                .fetchFirst();
    }
}
