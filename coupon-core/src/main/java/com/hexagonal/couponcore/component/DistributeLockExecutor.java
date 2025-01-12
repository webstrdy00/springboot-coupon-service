package com.hexagonal.couponcore.component;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class DistributeLockExecutor {

    private final RedissonClient redissonClient;
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * Redis를 사용한 분산 락을 실행하는 메소드
     * @param lockName 락의 고유 이름 (키 값)
     * @param waitMilliSecond 락 획득 대기 시간 (밀리초)
     * @param leaseMilliSecond 락 임대 시간 (밀리초)
     * @param logic 락 안에서 실행할 비즈니스 로직
     */
    public void execute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        // Redis에서 제공하는 분산 락 객체 획득
        RLock lock = redissonClient.getLock(lockName);
        try {
            // 지정된 대기 시간 동안 락 획득 시도
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new IllegalStateException("[" + lockName + "] lock 획득 실패");
            }
            // 락 획득 설공시 비즈니스 로직 실행
            logic.run();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 현재 스레드가 락을 보유하고 있다면 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
