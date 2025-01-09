package com.hexagonal.couponcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupon_issues")
public class CouponIssue extends BaseTimeEntity{  // 쿠폰 발급 이력을 관리하는 엔티티
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long couponId; // 쿠폰

    @Column(nullable = false)
    private Long userId;  // 사용자

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime dateIssued;  // 발급된 날짜

    private LocalDateTime dateUsed;  // 사용된 날짜
}
