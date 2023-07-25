package com.mlorenzo.estore.ordersservice.core.events;

import com.mlorenzo.estore.ordersservice.core.models.OrderStatus;

import lombok.Value;

@Value
public class OrderApprovedEvent {
	private String orderId;
	private OrderStatus orderStatus = OrderStatus.APPROVED;
}
