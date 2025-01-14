package com.hexagonal.couponcore.service;

import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.model.Coupon;
import com.hexagonal.couponcore.model.CouponIssue;
import com.hexagonal.couponcore.model.event.CouponIssueCompleteEvent;
import com.hexagonal.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.hexagonal.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 쿠폰 발급 프로세스 실행
     * 1. 비관적 락으로 쿠폰 조회
     * 2. 발급 처리
     * 3. 발급 이력 저장
     * 4. 쿠폰 소진 시 이벤트 발행
     * @param couponId 발급할 쿠폰 ID
     * @param userId 발급 대상 사용자 ID
     */
    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCouponWithLock(couponId);
        coupon.issue();  // 쿠폰 발급 가능 여부 확인 및 발급 처리
        saveCouponIssue(couponId, userId);
        publishCouponEvent(coupon);  // 쿠폰 소진 시 이벤트 발행
    }

    /*
        lock 획득
        트랜잭션 시작
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
        트랜잭션 커밋
        lock 반납
    */

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
     * findCouponWithLock을 사용한 락 처리 흐름:
     * 1. DB에서 비관적 락 획득 (다른 트랜잭션의 접근 차단)
     * 2. 트랜잭션 시작
     * 3. 쿠폰 조회 및 발급 처리
     * 4. 트랜잭션 커밋
     * 5. 락 자동 해제
     * @param couponId 조회할 쿠폰 ID
     * @return 조회된 쿠폰 정책
     */
    @Transactional(readOnly = true)
    public Coupon findCouponWithLock(long couponId) {
        return couponJpaRepository.findCouponWithLock(couponId).orElseThrow(() ->
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

    /**
     * 쿠폰 발급 완료 시 이벤트 발행
     * 발급이 완료되면 캐시 갱신을 위한 이벤트 발행
     */
    private void publishCouponEvent(Coupon coupon) {
        if (coupon.isIssueComplete()) {
            applicationEventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId()));
        }
    }
}
