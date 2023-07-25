package com.mlorenzo.estore.productsservice.core.errorhandler;

import java.util.Date;
import java.util.stream.Collectors;

import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ProductServiceErrorHandler {
	
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ErrorMessage handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		String message = ex.getAllErrors().stream()
				.map(error -> error.getDefaultMessage())
				.collect(Collectors.joining(","));
		return new ErrorMessage(new Date(), message);
	}
	
	@ExceptionHandler(value = CommandExecutionException.class)
	public ResponseEntity<ErrorMessage> handleCommandExecutionException(CommandExecutionException ex) {
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorMessage> handleOthersExceptions(Exception ex) {
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
