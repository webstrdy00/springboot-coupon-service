package com.hexagonal.couponapi.service;

import com.hexagonal.couponapi.dto.CouponIssueRequestDto;
import com.hexagonal.couponcore.component.DistributeLockExecutor;
import com.hexagonal.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {  // 쿠폰 발급 요청을 처리하는 서비스 계층

    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * 분산 락을 이용한 쿠폰 발급 요청 처리 V1
     * @param requestDto 쿠폰 발급 요청 정보
     * 발급 성공 시 로그를 남기고, 실패 시 예외를 발생시킴
     * 각 쿠폰별로 unique한 락을 생성하여 동시성 제어
     */
    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        distributeLockExecutor.execute("lock_" + requestDto.couponId(), 10000, 10000, () ->
                couponIssueService.issue(requestDto.couponId(), requestDto.userId())
        );
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
}
