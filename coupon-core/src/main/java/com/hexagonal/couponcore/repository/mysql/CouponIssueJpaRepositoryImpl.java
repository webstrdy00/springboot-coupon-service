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

    /**
     * 특정 사용자의 특정 쿠폰 발급 이력을 조회
     * @param couponId 조회할 쿠폰 ID
     * @param userId 조회할 사용자 ID
     * @return 찾은 쿠폰 발급 이력, 없으면 null 반환
     */
    @Override
    public CouponIssue findFirstCouponIssue(long couponId, long userId) {
        return queryFactory.selectFrom(couponIssue)
                .where(couponIssue.couponId.eq(couponId))
                .where(couponIssue.userId.eq(userId))
                .fetchFirst();
    }
}
