package com.hexagonal.couponconsumer;

import com.hexagonal.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponConsumerApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application-core,application-consumer");
		System.setProperty("spring.profiles.active", "local");
		SpringApplication.run(CouponConsumerApplication.class, args);
	}

}
