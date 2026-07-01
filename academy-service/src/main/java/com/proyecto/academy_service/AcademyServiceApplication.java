package com.proyecto.academy_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class AcademyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcademyServiceApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	public WebClient webClient(WebClient.Builder builder, @Value("${api.user-service.base.url}") String baseUrl) {
		return builder.baseUrl(baseUrl).build();
	}

}
