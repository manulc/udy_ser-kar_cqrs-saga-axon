package com.mlorenzo.estore.ordersservice.core.models;

import lombok.Value;

@Value
public class OrderSummary {
	private String orderId;
	private OrderStatus orderStatus;
	private String message;
}
