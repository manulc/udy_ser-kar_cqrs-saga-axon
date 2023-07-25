package com.mlorenzo.estore.ordersservice.commandapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class RejectOrderCommand {
	
	// Esta anotación es usada por el framework Axon para asociar los Commands con objetos Aggregate y poder determinar qué
	// objeto Aggregate debe manejar un determinado Command.
	@TargetAggregateIdentifier
	private String orderId;
	
	private String reason;
}
