package com.mlorenzo.estore.ordersservice.core.events;

import com.mlorenzo.estore.ordersservice.core.models.OrderStatus;

import lombok.Value;

@Value
public class OrderRejectedEvent {
	private String orderId;
	private String reason;
	private OrderStatus orderStatus = OrderStatus.REJECTED;
}
