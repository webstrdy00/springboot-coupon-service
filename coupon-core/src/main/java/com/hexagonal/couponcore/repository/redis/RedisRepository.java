package com.hexagonal.couponcore.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexagonal.couponcore.exception.CouponIssueException;
import com.hexagonal.couponcore.repository.redis.dto.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hexagonal.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.hexagonal.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.hexagonal.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Repository
@RequiredArgsConstructor
public class RedisRepository {  // Redis Set 연산 관련 메서드들
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final String issueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    // Set에 값 추가
    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    // Set의 크기 조회
    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // Set 멤버 존재 여부 확인
    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // Redis List 연산 관련 메서드
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);  // List 끝에  값 추가
    }

    /**
     * Redis List의 크기 조회
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * List의 왼쪽(처음)에서 요소 제거 및 반환
     */
    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * List의 특정 위치 요소 조회
     * @param index 조회할 위치 (0부터 시작)
     */
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * Lua 스크립트를 사용한 원자적 쿠폰 발급 요청 처리
     * @param couponId
     * @param userId
     * @param totalIssueQuantity
     * @return 발급 결과 코드 (1: 성공, 2: 중복발급, 3: 수량초과)
     */
    public void issueRequest(long couponId, long userId, int totalIssueQuantity) {
        String issueRequestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);

        try {
            // Lua 스크립트 실행으로 원자적 처리 보장
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(issueRequestKey, issueRequestQueueKey),
                    String.valueOf(userId),
                    String.valueOf(totalIssueQuantity),
                    objectMapper.writeValueAsString(couponIssueRequest)
            );
            CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(code));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(couponIssueRequest));
        }
    }

    /**
     * 쿠폰 발급 처리를 위한 Lua 스크립트
     * 1. 중복 발급 체크
     * 2. 수량 체크
     * 3. 발급 요청 처리
     */
    private RedisScript<String> issueRequestScript() {
        String script = """
                -- 중복 발급 체크
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                
                -- 수량 체크 및 발급 처리  
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGV[3])
                    return '1'
                end
                                
                return '3'
                """;

        return RedisScript.of(script, String.class);
    }
}
