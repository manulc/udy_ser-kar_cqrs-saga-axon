package com.mlorenzo.estore.productsservice.commandapi;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateProductCommand {
	
	// Esta anotación es usada por el framework Axon para asociar los Commands con objetos Aggregate y poder determinar qué
	// objeto Aggregate debe manejar un determinado Command.
	@TargetAggregateIdentifier
	private String productId;
	
	private String title;
	private BigDecimal price;
	private Integer quantity;
}
