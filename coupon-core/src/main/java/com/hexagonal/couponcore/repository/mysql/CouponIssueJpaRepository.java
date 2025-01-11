package com.hexagonal.couponcore.repository.mysql;

import com.hexagonal.couponcore.model.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueJpaRepository extends JpaRepository<CouponIssue, Long>, CouponIssueJpaRepositoryCustom {
}
