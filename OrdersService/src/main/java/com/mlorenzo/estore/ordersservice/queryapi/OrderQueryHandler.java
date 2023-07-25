package com.mlorenzo.estore.ordersservice.queryapi;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.ordersservice.core.models.OrderSummary;
import com.mlorenzo.estore.ordersservice.core.queries.FindOrderQuery;
import com.mlorenzo.estore.ordersservice.queryapi.data.OrderRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class OrderQueryHandler {
	private final OrderRepository orderRepository;
	
	// Nota sobre los manejadores de Queries.
	// Estos manejadores consumen Queries, en función de su tipo, que son despachados o publicados en el Query Bus por el Query Gateway.
		
	// Anotación para que este método sea un manejador de Queries(En este caso, de tipo FindOrderQuery)
	@QueryHandler
	public OrderSummary findOrder(FindOrderQuery findOrderQuery) {
		return orderRepository.findById(findOrderQuery.getOrderId())
				.map(order -> new OrderSummary(order.getOrderId(), order.getOrderStatus(), ""))
				.orElseThrow();
	}
}
