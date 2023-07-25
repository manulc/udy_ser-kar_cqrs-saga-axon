package com.mlorenzo.estore.ordersservice;

import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

// Opcional ya que basta con tener la dependencia "spring-cloud-starter-netflix-eureka-client" en el classpath para que este microservicio se registre en el servidor Eureka
@EnableEurekaClient
@SpringBootApplication
public class OrdersServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersServiceApplication.class, args);
	}
	
	// Este bean de Spring crea un programador de Deadlines de tipo SimpleDeadlineManager(Los Deadlines programados con este tipo de programador se crean en memoria y no se persisten.
	// Por lo tanto, si se reinicia la aplicación, los Deadlines programados se perderán).
	// Un Deadline nos permite establecer un timeout para ejecutar un proceso y, en caso de cumplirse, nos permite ejecutar un manejador de Deadlines.
	// En nuestro caso, vamos a usar Deadlines para establecer timeouts en procesos del componente SAGA pero también es posible usarlos en procesos del Aggregate.
	@Bean
	public DeadlineManager deadlineManager(Configuration configuration, SpringTransactionManager transactionManager) {
		return SimpleDeadlineManager.builder()
				.scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
				.transactionManager(transactionManager)
				.build();
	}
}
