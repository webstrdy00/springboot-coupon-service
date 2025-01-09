package com.hexagonal.couponcore.model;

import com.hexagonal.couponcore.exception.CouponIssueException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.hexagonal.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.hexagonal.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity{  // 쿠폰 기본 정보와 발급 관련 로직을 담당하는 엔티티

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;   // 쿠폰 제목

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;  // 쿠폰 유형

    private Integer totalQuantity; // 발급 가능한 총 수량

    @Column(nullable = false)
    private int issuedQuantity; // 현재까지 발급된 수량

    @Column(nullable = false)
    private int discountAmount; // 할인 금액

    @Column(nullable = false)
    private int minAvailableAmount; // 쿠폰 사용 가능한 최소 주문 금액

    @Column(nullable = false)
    private LocalDateTime dateIssuedStart; // 쿠폰 발급 시작 일시

    @Column(nullable = false)
    private LocalDateTime dateIssuedEnd; // 쿠폰 발급 종료 일시

    // 발급 가능한 수량이 남아있는지 확인
    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;   // totalQuantity가 null이면 무제한 발급 가능
        }
        return totalQuantity > issuedQuantity;
    }

    // 현재 발급 가능한 기간인지 확인
    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssuedStart.isBefore(now) && dateIssuedEnd.isAfter(now);
    }

    // 쿠폰 발급이 완료되었는지 확인 (기간 만료 또는 수량 소진)
    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssuedEnd.isBefore(now) || !availableIssueQuantity();
    }

    // 쿠폰 발급 처리 및 유효성 검증
    public void issue() {
        if (!availableIssueQuantity()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY,
                    "발급 가능한 수량을 초과합니다. total: %s, issued: %s".formatted(totalQuantity, issuedQuantity));
        }
        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. request: %s, issueStart: %s, issueEnd: %s".formatted(LocalDateTime.now(), dateIssuedStart, dateIssuedEnd));
        }

        issuedQuantity++;
    }
}
