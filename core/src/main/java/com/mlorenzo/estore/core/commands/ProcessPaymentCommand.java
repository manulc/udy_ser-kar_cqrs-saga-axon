package com.mlorenzo.estore.core.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.mlorenzo.estore.core.models.PaymentDetails;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProcessPaymentCommand {
	
	// Esta anotación es usada por el framework Axon para asociar los Commands con objetos Aggregate y poder determinar qué
	// objeto Aggregate debe manejar un determinado Command.
	@TargetAggregateIdentifier
	private String paymentId;
	
	private String orderId;
	private PaymentDetails paymentDetails;
}
