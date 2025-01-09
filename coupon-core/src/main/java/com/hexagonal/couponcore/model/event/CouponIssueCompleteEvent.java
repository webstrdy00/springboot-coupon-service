package com.hexagonal.couponcore.model.event;

public record CouponIssueCompleteEvent(long couponId) {  // 쿠폰 발급 완료 이벤트를 표현하는 레코드
}
