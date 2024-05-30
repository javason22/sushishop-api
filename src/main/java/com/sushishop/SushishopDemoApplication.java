package com.sushishop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.embedded.RedisServer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class SushishopDemoApplication {

	public SushishopDemoApplication(){
		
	}

	public static void main(String[] args) {

		SpringApplication.run(SushishopDemoApplication.class, args);
	}

}
