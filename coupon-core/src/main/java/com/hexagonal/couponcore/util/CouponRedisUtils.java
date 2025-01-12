package com.hexagonal.couponcore.util;

public class CouponRedisUtils {
    /**
     * 특정 쿠폰의 발급 요청 Set을 위한 Redis 키 생성
     * - 각 쿠폰별로 고유한 Set을 생성하여 발급 요청한 사용자 ID들을 관리
     * - 사용 예: "issue.request.couponId=123"
     * @param couponId 쿠폰 ID
     * @return Redis Set 키
     */
    public static String getIssueRequestKey(long couponId) {
        return "issue.request.couponId=%s".formatted(couponId);
    }

    /**
     * 쿠폰 발급 요청 Queue를 위한 Redis 키 생성
     * - 모든 쿠폰 발급 요청이 저장되는 단일 Queue의 키
     * - 실제 발급 처리를 위한 작업 Queue로 사용
     * - 고정 키: "issue.request"
     * @return Redis Queue 키
     */
    public static String getIssueRequestQueueKey() {
        return "issue.request";
    }
}
