package com.mlorenzo.estore.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

// Opcional ya que basta con tener la dependencia "spring-cloud-starter-netflix-eureka-client" en el classpath para que este microservicio se registre en el servidor Eureka
@EnableEurekaClient
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
