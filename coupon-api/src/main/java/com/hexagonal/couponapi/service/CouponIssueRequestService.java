package com.hexagonal.couponapi.service;

import com.hexagonal.couponapi.dto.CouponIssueRequestDto;
import com.hexagonal.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {  // 쿠폰 발급 요청을 처리하는 서비스 계층

    private final CouponIssueService couponIssueService;
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * V1 쿠폰 발급 요청을 처리하는 메서드
     * @param requestDto 쿠폰 발급 요청 정보
     * 발급 성공 시 로그를 남기고, 실패 시 예외를 발생시킴
     */
    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
}
