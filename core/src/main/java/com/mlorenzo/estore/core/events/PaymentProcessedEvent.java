package com.mlorenzo.estore.core.events;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentProcessedEvent {
	private String orderId;
	private String paymentId;
}
