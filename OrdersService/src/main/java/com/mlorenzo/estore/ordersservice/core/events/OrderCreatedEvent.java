package com.mlorenzo.estore.ordersservice.core.events;

import com.mlorenzo.estore.ordersservice.core.models.OrderStatus;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class OrderCreatedEvent {
	private String orderId;
	private String productId;
	private String userId;
	private int quantity;
	private String addressId;
	private OrderStatus orderStatus = OrderStatus.CREATED;
}
