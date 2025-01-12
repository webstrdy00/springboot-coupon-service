package com.hexagonal.couponapi.controller;

import com.hexagonal.couponapi.dto.CouponIssueRequestDto;
import com.hexagonal.couponapi.dto.CouponIssueResponseDto;
import com.hexagonal.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {  // 쿠폰 발급 요청을 처리하는 REST 컨트롤러
    private final CouponIssueRequestService couponIssueRequestService;

    /**
     * 쿠폰 발급 요청을 처리하는 V1 API 엔드포인트
     * 동기식 쿠폰 발급 API - DB락을 사용하여 동시성 제어
     * @param body 쿠폰 발급 요청 정보 (사용자 ID, 쿠폰 ID)
     * @return 쿠폰 발급 결과 응답 (성공 여부, 실패시 메시지)
     */
    @PostMapping("/v1/issue")
    public CouponIssueResponseDto issueV1(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.issueRequestV1(body);
        return new CouponIssueResponseDto(true, null);
    }
    /**
     * 쿠폰 발급 요청을 처리하는 V1 API 엔드포인트
     * 비동기식 쿠폰 발급 API - Redis를 사용하여 발급 요청을 큐에 저장
     * @param body 쿠폰 발급 요청 정보 (사용자 ID, 쿠폰 ID)
     * @return 쿠폰 발급 결과 응답 (성공 여부, 실패시 메시지)
     */
    @PostMapping("/v1/issue-async")
    public CouponIssueResponseDto asyncIssueV1(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.asyncIssueRequestV1(body);
        return new CouponIssueResponseDto(true, null);
    }
}
