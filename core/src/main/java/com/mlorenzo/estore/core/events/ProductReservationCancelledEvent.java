package com.mlorenzo.estore.core.events;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductReservationCancelledEvent {
	private String productId;
	private String orderId;
	private int quantity;
	private String userId;
	private String reason;
}
