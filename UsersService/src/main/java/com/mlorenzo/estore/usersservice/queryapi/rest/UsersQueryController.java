package com.mlorenzo.estore.usersservice.queryapi.rest;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlorenzo.estore.core.models.User;
import com.mlorenzo.estore.core.queries.FetchUserPaymentDetailsQuery;

@RestController
@RequestMapping("/users")
public class UsersQueryController {
    private final QueryGateway queryGateway;
    
    public UsersQueryController(QueryGateway queryGateway) {
    	this.queryGateway = queryGateway;
    }

    @GetMapping("/{userId}/payment-details")
    public User getUserPaymentDetails(@PathVariable String userId) {
    	FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(userId);
    	// Método que hace que el Query Gateway despache o envíe una query(En este caso de tipo FetchUserPaymentDetailsQuery) al Query Bus
        return queryGateway.query(query, ResponseTypes.instanceOf(User.class)).join();
    }
}
