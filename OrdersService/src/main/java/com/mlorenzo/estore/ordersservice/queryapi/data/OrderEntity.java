package com.mlorenzo.estore.ordersservice.queryapi.data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.mlorenzo.estore.ordersservice.core.models.OrderStatus;

import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity {

	@Id
	public String orderId;
	
	private String productId;
	private String userId;
	private int quantity;
	private String addressId;
	
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;
}
