package com.kanapa4.travel_brief;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TravelBriefApplication {
	public static void main(String[] args) {
		SpringApplication.run(TravelBriefApplication.class, args);
	}
}
