package com.hexagonal.couponcore.repository.redis;

import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.exception.ErrorCode;

/**
 * 쿠폰 발급 요청 결과 코드
 */
public enum CouponIssueRequestCode {

    SUCCESS(1),   // 발급 성공
    DUPLICATED_COUPON_ISSUE(2),  // 중복 발급
    INVALID_COUPON_ISSUE_QUANTITY(3);   // 수량 초과

    CouponIssueRequestCode(int code) {
    }

    public static CouponIssueRequestCode find(String code) {
        int codeValue = Integer.parseInt(code);

        if (codeValue == 1) return SUCCESS;
        if (codeValue == 2) return DUPLICATED_COUPON_ISSUE;
        if (codeValue == 3) return INVALID_COUPON_ISSUE_QUANTITY;

        throw new IllegalArgumentException("존재하지 않는 코드입니다. %s".formatted(code));
    }

    /**
     * 발급 결과 코드 검증
     * @param code
     */
    public static void checkRequestResult(CouponIssueRequestCode code) {
        if (code == INVALID_COUPON_ISSUE_QUANTITY) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다.");
        }
        if (code == DUPLICATED_COUPON_ISSUE) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청된 쿠폰입니다.");
        }
    }
}
