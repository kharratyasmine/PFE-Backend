package com.workpilot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@SpringBootApplication
@EntityScan(basePackages = "com.workpilot")
public class WorkpilotApplication {
	public static void main(String[] args) {
		SpringApplication.run(WorkpilotApplication.class, args);
	}

	@Bean
	public CommandLineRunner printRoutes(ApplicationContext ctx) {
		return args -> {
			System.out.println("📌 Liste des routes exposées :");
			Arrays.stream(ctx.getBeanNamesForAnnotation(RestController.class)).forEach(bean -> {
				Object controller = ctx.getBean(bean);
				System.out.println(" → " + controller.getClass().getName());
			});
		};
	}

}
