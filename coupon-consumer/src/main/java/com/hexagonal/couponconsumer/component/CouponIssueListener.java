package com.hexagonal.couponconsumer.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexagonal.couponcore.repository.redis.RedisRepository;
import com.hexagonal.couponcore.repository.redis.dto.CouponIssueRequest;
import com.hexagonal.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.hexagonal.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

/**
 * Redis Queue에 저장된 쿠폰 발급 요청을 처리하는 스케줄링 컴포넌트
 * 비동기 쿠폰 발급 요청을 실제 DB에 반영하는 Consumer 역할
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CouponIssueListener {
    private final RedisRepository redisRepository;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String issueRequestQueueKey = getIssueRequestQueueKey();

    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * Redis Queue에서 발급 요청을 주기적으로 처리하는 메서드
     * 1초 간격으로 실행되며, Queue가 비어있을 때까지 반복 처리
     * @throws JsonProcessingException JSON 파싱 실패 시
     */
    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("listen...");
        // Queue에 처리할 요청이 잇는 동안 계속 처리
        while (existCouponIssueTarget()) {
            CouponIssueRequest target = getIssueTarget();   // Queue에 첫 번째 요청 조회
            log.info("발급 시작 target: %s".formatted(target));
            couponIssueService.issue(target.couponId(), target.userId());  // 실제 DB 발급 처리
            log.info("발급 완료 target: %s".formatted(target));
            removeIssuedTarget();  // 처리 완료된 요청 제거
        }
    }

    /**
     * Queue에 처리할 발급 요청이 있는지 확인
     */
    private boolean existCouponIssueTarget() {
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    /**
     * Queue의 첫 번째 발급 요청 조회
     */
    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey, 0), CouponIssueRequest.class);
    }

    /**
     * 처리 완료된 발급 요청을 Queue에서 제거
     */
    private void removeIssuedTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }
}
