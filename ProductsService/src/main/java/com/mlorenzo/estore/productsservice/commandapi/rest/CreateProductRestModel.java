package com.mlorenzo.estore.productsservice.commandapi.rest;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateProductRestModel {
	
	@NotBlank(message = "Product title is a required field")
	private String title;
	
	@NotNull(message = "Product price is a required field")
	@Min(value = 1, message = "Price cannot be lower than 1")
	private BigDecimal price;
	
	@NotNull(message = "Product quantity is a required field")
	@Min(value = 1, message = "Quantity cannot be lower than 1")
	@Max(value = 5, message = "Quantity cannot be larger than 5")
	private Integer quantity;
}
