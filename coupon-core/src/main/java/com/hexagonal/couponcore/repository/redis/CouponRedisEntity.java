package com.hexagonal.couponcore.repository.redis;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.model.Coupon;
import com.hexagonal.couponcore.model.CouponType;

import java.time.LocalDateTime;

import static com.hexagonal.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.hexagonal.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

/**
 * Redis에 캐시할 쿠폰 정보를 위한 Record 클래스
 * 성능 최적화를 위해 필요한 최소한의 쿠폰 정보만 포함
 */
public record CouponRedisEntity(
        Long id,   // 쿠폰 ID
        CouponType couponType,  // 쿠폰 유형
        Integer totalQuantity,  // 총 발급 가능 수량
        boolean availableIssueQuantity,  // 발급 가능 수량 존재 여부

        @JsonSerialize(using = LocalDateTimeSerializer.class)    // JSON 직렬화 설정
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssuedStart,    // 발급 시작일

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssuedEnd    // 발급 종료일
) {
    /**
     * Coupon 엔티티로부터 Redis 캐시용 객체 생성
     */
    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.availableIssueQuantity(),
                coupon.getDateIssuedStart(),
                coupon.getDateIssuedEnd()
        );
    }

    /**
     * 쿠폰 발급 가능 여부를 종합적으로 검증
     * 1. 발급 가능 수량 확인
     * 2. 발급 가능 기간 확인
     */
    public void checkIssuableCoupon() {
        if (!availableIssueQuantity) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY,
                    "모든 발급 수량이 소진되었습니다. couponId: %s".formatted(id));
        }

        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. couponId: %s, issueStart: %s, issueEnd: %s".formatted(id, dateIssuedStart, dateIssuedEnd));
        }
    }

    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssuedStart.isBefore(now) && dateIssuedEnd.isAfter(now);
    }
}
