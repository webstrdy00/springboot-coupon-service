package com.hexagonal.couponapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * 쿠폰 발급 결과 응답 데이터 전송 객체
 * 클라이언트에게 전달할 쿠폰 발급 결과를 담는 record 클래스
 * @param isSuccess 발급 성공 여부
 * @param comment 실패 시 에러 메시지 (성공 시 null)
 */
@JsonInclude(value = NON_NULL)
public record CouponIssueResponseDto(boolean isSuccess, String comment) {
}
