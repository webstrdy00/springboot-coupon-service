package com.hexagonal.couponapi;

import com.hexagonal.couponapi.dto.CouponIssueResponseDto;
import com.hexagonal.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class CouponControllerAdvice {  // 쿠폰 관련 예외를 처리하는 전역 예외 처리기
    /**
     * CouponIssueException 예외를 처리하는 핸들러
     * @param exception 발생한 쿠폰 발급 예외
     * @return 클라이언트에게 전달할 에러 응답
     */
    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponIssueException exception) {
        return new CouponIssueResponseDto(false, exception.getErrorCode().message);
    }
}
