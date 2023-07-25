package com.mlorenzo.estore.core.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentDetails {
	private String name;
	private String cardNumber;
	private int validUntilMonth;
	private int validUntilYear;
	private String cvv;
}
