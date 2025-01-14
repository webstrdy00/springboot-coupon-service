package com.hexagonal.couponapi.controller;

import com.hexagonal.couponapi.dto.CouponIssueRequestDto;
import com.hexagonal.couponapi.dto.CouponIssueResponseDto;
import com.hexagonal.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 쿠폰 발급 API를 제공하는 컨트롤러
 * 동기식과 비동기식 발급 방식을 모두 지원
 */
@RestController
@RequiredArgsConstructor
public class CouponIssueController {
    private final CouponIssueRequestService couponIssueRequestService;

    /**
     * 쿠폰 발급 요청을 처리하는 V1 API 엔드포인트
     * DB 락을 사용하여 동시성을 제어
     * 실시간으로 쿠폰 발급 결과를 반환
     *
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
     * Redis를 사용하여 발급 요청을 큐에 저장
     * 분산 락을 통한 동시성 제어
     *
     * @param body 쿠폰 발급 요청 정보 (사용자 ID, 쿠폰 ID)
     * @return 쿠폰 발급 결과 응답 (성공 여부, 실패시 메시지)
     */
    @PostMapping("/v1/issue-async")
    public CouponIssueResponseDto asyncIssueV1(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.asyncIssueRequestV1(body);
        return new CouponIssueResponseDto(true, null);
    }

    /**
     * V2 비동기식 쿠폰 발급 API
     * Redis Lua 스크립트를 사용한 원자적 발급 처리
     * 향상된 성능과 안정성 제공
     *
     * @param body 쿠폰 발급 요청 정보 (사용자 ID, 쿠폰 ID)
     * @return 쿠폰 발급 요청 접수 결과
     */
    @PostMapping("/v2/issue-async")
    public CouponIssueResponseDto asyncIssueV2(@RequestBody CouponIssueRequestDto body) {
        couponIssueRequestService.asyncIssueRequestV2(body);
        return new CouponIssueResponseDto(true, null);
    }
}
