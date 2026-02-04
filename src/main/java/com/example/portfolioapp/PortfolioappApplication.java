package com.example.portfolioapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortfolioappApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioappApplication.class, args);
	}

}
