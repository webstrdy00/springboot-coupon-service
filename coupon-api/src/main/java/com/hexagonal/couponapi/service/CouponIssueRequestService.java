package com.hexagonal.couponapi.service;

import com.hexagonal.couponapi.dto.CouponIssueRequestDto;
import com.hexagonal.couponcore.service.AsyncCouponIssueServiceV1;
import com.hexagonal.couponcore.service.AsyncCouponIssueServiceV2;
import com.hexagonal.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 발급 요청을 처리하는 서비스
 * 동기식과 비동기식 발급 방식을 모두 지원하며
 * 각 방식에 맞는 처리 로직을 제공
 */
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {  // 쿠폰 발급 요청을 처리하는 서비스 계층

    private final CouponIssueService couponIssueService;
    private final AsyncCouponIssueServiceV1 asyncCouponIssueServiceV1;
    private final AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * V1 동기식 쿠폰 발급 처리
     * DB 락을 사용하여 동시성 제어
     * 실제 발급이 완료될 때까지 대기
     *
     * @param requestDto 쿠폰 발급 요청 정보
     * 발급 성공 시 로그를 남기고, 실패 시 예외를 발생시킴
     * 각 쿠폰별로 unique한 락을 생성하여 동시성 제어
     */
    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }

    /**
     * V1 비동기식 쿠폰 발급 처리
     * Redis를 사용한 발급 요청 큐잉
     * 분산 락으로 동시성 제어
     *
     * @param requestDto 쿠폰 발급 요청 정보
     */
    public void asyncIssueRequestV1(CouponIssueRequestDto requestDto) {
        asyncCouponIssueServiceV1.issue(requestDto.couponId(), requestDto.userId());
    }

    /**
     * V2 비동기식 쿠폰 발급 처리
     * Redis Lua 스크립트를 활용한 원자적 처리
     * 향상된 성능과 안정성
     *
     * @param requestDto 쿠폰 발급 요청 정보
     */
    public void asyncIssueRequestV2(CouponIssueRequestDto requestDto) {
        asyncCouponIssueServiceV2.issue(requestDto.couponId(), requestDto.userId());
    }
}
