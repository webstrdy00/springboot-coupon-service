package com.hexagonal.couponcore.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {  // Redis Set 연산 관련 메서드들
    private final RedisTemplate<String, String> redisTemplate;

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

    // Redis List 연산 관려 메서드
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);  // List 끝에  값 추가
    }
}
