package com.workpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.workpilot")
public class WorkpilotApplication {
	public static void main(String[] args) {
		SpringApplication.run(WorkpilotApplication.class, args);
	}
}
