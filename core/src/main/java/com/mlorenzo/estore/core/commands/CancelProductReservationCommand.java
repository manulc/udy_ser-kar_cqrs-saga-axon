package com.mlorenzo.estore.core.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CancelProductReservationCommand {
	
	// Esta anotación es usada por el framework Axon para asociar los Commands con objetos Aggregate y poder determinar qué
	// objeto Aggregate debe manejar un determinado Command.
	@TargetAggregateIdentifier
	private String productId;
	
	private String orderId;
	private int quantity;
	private String userId;
	private String reason;
}
