package com.mlorenzo.estore.productsservice.core.errorhandler;

import java.util.Date;

import lombok.Value;

@Value
public class ErrorMessage {
	private Date timestamp;
	private String message;
}
