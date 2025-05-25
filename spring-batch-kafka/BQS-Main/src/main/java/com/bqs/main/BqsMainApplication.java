package com.bqs.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BqsMainApplication {
	public static void main(String[] args) {
		SpringApplication.run(BqsMainApplication.class, args);
	}
}
 