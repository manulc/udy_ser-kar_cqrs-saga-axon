package com.mlorenzo.estore.paymentsservice.queryapi.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "payments")
public class PaymentEntity {

	@Id
	private String paymentId;
	
	@Column(unique = true)
	public String orderId;
}
