package com.hexagonal.couponapi.dto;

/**
 * 쿠폰 발급 요청 데이터 전송 객체
 * 클라이언트로부터 받는 쿠폰 발급 요청 데이터를 담는 record 클래스
 * @param userId 쿠폰을 발급받을 사용자 ID
 * @param couponId 발급할 쿠폰 ID
 */
public record CouponIssueRequestDto(long userId, long couponId) {
}
