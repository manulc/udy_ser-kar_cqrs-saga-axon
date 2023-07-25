package com.mlorenzo.estore.ordersservice.commandapi.rest;

import java.util.UUID;

import javax.validation.Valid;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mlorenzo.estore.ordersservice.commandapi.CreateOrderCommand;
import com.mlorenzo.estore.ordersservice.core.models.OrderSummary;
import com.mlorenzo.estore.ordersservice.core.queries.FindOrderQuery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrdersCommandController {
	private static final String USER_ID = "27b95829-4f3f-4ddf-8983-151ba010e35b";
	
	private final CommandGateway commandGateway;
	private final QueryGateway queryGateway;
	
	// Nota: Puede usarse tanto la anotación @Valid de JPA como la anotación @Validated de Spring para realizar las validaciones
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public OrderSummary createOrder(@Valid @RequestBody CreateOrderRestModel createOrderRestModel) {
		String orderId = UUID.randomUUID().toString();
		CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
				.orderId(orderId)
				.userId(USER_ID)
				.productId(createOrderRestModel.getProductId())
				.addressId(createOrderRestModel.getAddressId())
				.quantity(createOrderRestModel.getQuantity())
				.build();
		SubscriptionQueryResult<OrderSummary, OrderSummary> queryResult = queryGateway.subscriptionQuery(new FindOrderQuery(orderId),
				ResponseTypes.instanceOf(OrderSummary.class),
				ResponseTypes.instanceOf(OrderSummary.class));
		// Debido a que en una aplicación basada en CQRS y Event Sourcing la sincronización de datos entre el lado del Command API y el lado del Query API es eventual y no inmediata,
		// cuando una aplicación cliente envía un Command al controlador del Command API y recibe una respuesta satisfactoria cuando ese Command se ha ejecutado, no significa que los datos se
		// actualicen inmediantamente en la base de datos de lectura del lado del Query API. Y si ese cliente a continuación realiza una consulta al Query API, es posible que los datos que reciba
		// no estén acorder con el Command que envió previamente al lado del Command API.
		// Podemos usamos las Subscription Queries para responder al cliente desde el componente SAGA en vez desde la ejecución del Command. En nuestro caso en concreto, emitimos las respuestas al
		// cliente en aquellos eventos del flujo del componente SAGA que son finales y, como son finales, los datos ya se encontrarían actualizados en la base de datos del lado del Query API(ver
		// clase OrderSaga).
		try {
			// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo CreateOrderCommand) al Command Bus.
			// Operación bloqueante. Existe otro método llamado "send" que es no bloqueante.
			commandGateway.sendAndWait(createOrderCommand);
			// El método "updates" devuelve un flujo reactivo de tipo Flux con las actualizaciones emitidas a la Subscription Query, es decir, en nuestro caso sería un flujo reactivo Flux con los datos
			// emitidos desde los eventos finales de nuestro componente SAGA(ver clase OrderSaga).
			// NOTA: Este método no tiene en cuenta la respuesta incial que se obtiene de la consulta a la base de datos de lectura del lado del Query API.
			return queryResult.updates().blockFirst();
			// A diferencia del método  "updates", el método "initialResult" devuelve un flujo reactivo de tipo Mono con los datos de la respuesta inicial, es decir, en nuestro caso sería un flujo
			// reactivo de tipo Mono con la respuesta de la consulta FindOrderQuery a la base de datos de lectura del lado del Query API(ver clase OrderQueryHandler).
			// NOTA: Este método no tiene en cuenta las actualizaciones que se emiten a la Subscription Query.
			//return queryResult.initialResult().block();
		}
		finally {
			queryResult.close();
		}
	}
}
