package com.mlorenzo.estore.ordersservice.commandapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateOrderCommand {
	
	// Esta anotación es usada por el framework Axon para asociar los Commands con objetos Aggregate y poder determinar qué
	// objeto Aggregate debe manejar un determinado Command.
	@TargetAggregateIdentifier
	public String orderId;
	
	private String userId;
	private String productId;
	private int quantity;
	private String addressId;
}
