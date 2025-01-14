package com.hexagonal.couponcore.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
/**
 * 로컬 캐시 설정 클래스
 * In-Memory 캐시로 Caffeine을 사용하기 위한 설정
 */
@Configuration
public class LocalCacheConfiguration {
    /**
     * Caffeine 로컬 캐시 매니저 설정
     * - TTL: 10초
     * - 최대 크기: 1000개
     */
    @Bean
    public CacheManager localCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .maximumSize(1000)
        );
        return caffeineCacheManager;
    }
}
