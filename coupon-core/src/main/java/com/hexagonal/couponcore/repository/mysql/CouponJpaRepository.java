package com.hexagonal.couponcore.repository.mysql;

import com.hexagonal.couponcore.model.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    /**
     * 비관적 락(Pessimistic Lock)을 사용하여 쿠폰을 조회
     * - LockModeType.PESSIMISTIC_WRITE: 다른 트랜잭션의 읽기/쓰기를 모두 차단
     * - 해당 쿠폰에 대한 동시 접근을 DB 수준에서 제어
     * - 실제 SQL에서는 'SELECT FOR UPDATE' 구문으로 변환됨
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findCouponWithLock(long id);
}
