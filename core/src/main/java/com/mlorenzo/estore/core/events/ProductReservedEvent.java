package com.mlorenzo.estore.core.events;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductReservedEvent {
	private String productId;
	private int quantity;
	private String orderId;
	private String userId;
}
