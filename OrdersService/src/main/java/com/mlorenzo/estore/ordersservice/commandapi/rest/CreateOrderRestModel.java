package com.mlorenzo.estore.ordersservice.commandapi.rest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateOrderRestModel {
	
	@NotBlank(message = "Order productId is a required field")
	private String productId;
	
	@NotNull(message = "Order quantity is a required field")
	@Min(value = 1, message = "Quantity cannot be lower than 1")
    @Max(value = 5, message = "Quantity cannot be larger than 5")
	private int quantity;
	
	@NotBlank(message = "Order addressId is a required field")
	private String addressId;
}
