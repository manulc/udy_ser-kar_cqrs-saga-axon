package com.mlorenzo.estore.productsservice.commandapi.rest;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mlorenzo.estore.productsservice.commandapi.CreateProductCommand;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductsCommandController {
	private final CommandGateway commandGateway;
	
	// Nota: Puede usarse tanto la anotación @Valid de JPA como la anotación @Validated de Spring para realizar las validaciones
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public String createProduct(@Validated @RequestBody CreateProductRestModel createProductRestModel) {
		CreateProductCommand createProductCommand = CreateProductCommand.builder()
				.productId(UUID.randomUUID().toString())
				.title(createProductRestModel.getTitle())
				.price(createProductRestModel.getPrice())
				.quantity(createProductRestModel.getQuantity())
				.build();
		// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo CreateProductCommand) al Command Bus.
		// Operación bloqueante. Existe otro método llamado "send" que es no bloqueante.
		return commandGateway.sendAndWait(createProductCommand);
	}
}
