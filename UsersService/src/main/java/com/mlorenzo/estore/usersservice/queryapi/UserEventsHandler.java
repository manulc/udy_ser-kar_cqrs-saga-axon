package com.mlorenzo.estore.usersservice.queryapi;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.core.models.PaymentDetails;
import com.mlorenzo.estore.core.models.User;
import com.mlorenzo.estore.core.queries.FetchUserPaymentDetailsQuery;


@Component
public class UserEventsHandler {
	
	// Nota sobre los manejadores de Queries.
	// Estos manejadores consumen Queries, en función de su tipo, que son despachados o publicados en el Query Bus por el Query Gateway.
	
	// Anotación para que este método sea un manejador de Queries(En este caso, de tipo FetchUserPaymentDetailsQuery)
	@QueryHandler
	public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query) {
		PaymentDetails paymentDetails = PaymentDetails.builder()
				.cardNumber("123Card")
				.cvv("123")
				.name("JHON DOE")
				.validUntilMonth(12)
				.validUntilYear(2030)
				.build();
		User user = User.builder()
				.firstName("Jhon")
				.lastName("Doe")
				.userId(query.getUserId())
				.paymentDetails(paymentDetails)
				.build();
		return user;
	}
}
