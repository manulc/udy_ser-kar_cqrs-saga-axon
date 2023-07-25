package com.mlorenzo.estore.core.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
	private String firstName;
	private String lastName;
	private String userId;
	private PaymentDetails paymentDetails;
}
