package com.hexagonal.couponcore.repository.redis.dto;

public record CouponIssueRequest(long couponId, long userId) {
}
