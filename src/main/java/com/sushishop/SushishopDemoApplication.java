package com.sushishop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SushishopDemoApplication {

	public SushishopDemoApplication(){
		
	}

	public static void main(String[] args) {
		SpringApplication.run(SushishopDemoApplication.class, args);
	}

}
