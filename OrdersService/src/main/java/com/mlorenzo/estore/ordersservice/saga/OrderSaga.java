package com.mlorenzo.estore.ordersservice.saga;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mlorenzo.estore.core.commands.CancelProductReservationCommand;
import com.mlorenzo.estore.core.commands.ProcessPaymentCommand;
import com.mlorenzo.estore.core.commands.ReserveProductCommand;
import com.mlorenzo.estore.core.events.PaymentProcessedEvent;
import com.mlorenzo.estore.core.events.ProductReservationCancelledEvent;
import com.mlorenzo.estore.core.events.ProductReservedEvent;
import com.mlorenzo.estore.core.models.User;
import com.mlorenzo.estore.core.queries.FetchUserPaymentDetailsQuery;
import com.mlorenzo.estore.ordersservice.commandapi.ApproveOrderCommand;
import com.mlorenzo.estore.ordersservice.commandapi.RejectOrderCommand;
import com.mlorenzo.estore.ordersservice.core.events.OrderApprovedEvent;
import com.mlorenzo.estore.ordersservice.core.events.OrderCreatedEvent;
import com.mlorenzo.estore.ordersservice.core.events.OrderRejectedEvent;
import com.mlorenzo.estore.ordersservice.core.models.OrderSummary;
import com.mlorenzo.estore.ordersservice.core.queries.FindOrderQuery;

// Creamos en este microservicio el componente SAGA(modo orquestador en este caso) porque este microservicio es el encargado de iniciar el flujo o transacción

// Anotación para indicar al framework Axon que esta clase es un componente SAGA.
// Esta anotación también incluye la anotación @Componente que hace que esta clase sea un componente de Spring.
@Saga
public class OrderSaga {
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);
	private static final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";
	
	private String scheduleId;
	
	// Como SAGA es un componente serializable, es importante marcar las propiedades que inyectamos como "transient" para que no se serialicen
	@Autowired
	private transient CommandGateway commandGateway;
	
	@Autowired
	private transient QueryGateway queryGateway;
	
	@Autowired
	private transient DeadlineManager deadlineManager;
	
	@Autowired
	private transient QueryUpdateEmitter queryUpdateEmitter;
	
	// Como este evento OrderCreatedEvent es el inicio del flujo SAGA, anotamos este método con la anotación @StartSaga.
	// Esta anotación crea una instancia del componente SAGA(En este caso, de esta clase OrderSaga).
	@StartSaga
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo OrderCreatedEvent) de una instancia del componente SAGA(En este caso, esta clase OrderSaga).
	// Como aquí se crea una instancia del componente SAGA debido al uso de la anotación @StartSaga, la propiedad indicada en el atributo "associationProperty" y su valor se asociarán a dicha instancia para poder localizarla y usarla posteriormente en la ejecución de los siguientes manejadores de eventos de tipo @SagaEventHandler.
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderCreatedEvent orderCreatedEvent) {
		ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
				.orderId(orderCreatedEvent.getOrderId())
				.productId(orderCreatedEvent.getProductId())
				.quantity(orderCreatedEvent.getQuantity())
				.userId(orderCreatedEvent.getUserId())
				.build();
		LOGGER.info("OrderCreatedEvent handled for orderId: {} and productId: {}", reserveProductCommand.getOrderId(), reserveProductCommand.getProductId());
		// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo ReserveProductCommand) al Command Bus.
		// Operación no bloqueante. Existe otro método llamado "sendAndWait" que es bloqueante.
		// El segundor argumento de entrada es opcional y es una implementación de la interfaz CommandCallback, es decir, es una función de Callback que se ejecutará cuando el método "send" finalice su ejecución.
		commandGateway.send(reserveProductCommand, (commandMessage, commandResultMessage) -> {
			// Si el resultado no ha sido satisfactorio, es decir, ha ocurrido alguna excepción, realizamos una transacción de compensación
			if(commandResultMessage.isExceptional()) {
				String reason = commandResultMessage.exceptionResult().getMessage();
				LOGGER.error(reason);
				// Start a compensating transaction
				rejectOrder(orderCreatedEvent.getOrderId(), reason);
				return;
			}
		});
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo ProductReservedEvent) de una instancia del componente SAGA(En este caso, esta clase OrderSaga).
	// Debe indicarse al atributo "associationProperty" de esta anotación el nombre de una propiedad del evento recibido, como argumento de entrada en este método, para poder localizar, mediante su valor, una instancia del componente SAGA creada previamente que maneje este evento.
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(ProductReservedEvent productReservedEvent) {
		LOGGER.info("ProductReservedEvent is called for productId: {} and orderId: {}", productReservedEvent.getProductId(), productReservedEvent.getOrderId());
		FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());
		User userPaymentDetails = null;
		try {
			// Método que hace que el Query Gateway despache o envíe una query(En este caso de tipo FetchUserPaymentDetailsQuery) al Query Bus
			userPaymentDetails =  queryGateway.query(query, ResponseTypes.instanceOf(User.class)).join();
		}
		catch(Exception ex) {
			LOGGER.error(ex.getMessage());
			// Start compensating transaction
			cancelProductReservation(productReservedEvent, ex.getMessage());
			return;
		}
		if(userPaymentDetails == null) {
			// Start compensating transaction
			cancelProductReservation(productReservedEvent, "Could not fetch user payment details");
			return;
		}
		LOGGER.info("Successfully fetched user payment details for user {}", userPaymentDetails.getFirstName());
		// Programa un Deadline asociado al nombre PAYMENT_PROCESSING_TIMEOUT_DEADLINE con un timeout de 120 segundos y se le pasa opcionalmente el evento "productReservedEvent" como datos de payload.
		// Si el proceso de pago no se completa en ese timeout, se ejecutará el manjeador asociado a este Deadline que realizará un proceso de compensación(ver método "handlePaymentDeadline").
		scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS), PAYMENT_PROCESSING_TIMEOUT_DEADLINE, productReservedEvent);
		// Forzamos una salida de este método para que el código de abajo no se ejecute y, de esta manera, no se complete el proceso de pago haciendo que se cumpla el timeout programado en el Deadline anterior
		//if(true) return;
		ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
				.orderId(productReservedEvent.getOrderId())
				.paymentDetails(userPaymentDetails.getPaymentDetails())
				.paymentId(UUID.randomUUID().toString())
				.build();
		String resultProcessPaymentCommand = null;
		try {
			// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo ProcessPaymentCommand) al Command Bus.
			// Operación bloqueante con un timeout establecido. Existe otro método llamado "send" que es no bloqueante.
			// Se comenta porque ahora manejamos el timeout de este proceso mediante un Deadline asociado al nombre PAYMENT_PROCESSING_TIMEOUT_DEADLINE.
			//resultProcessPaymentCommand = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
			resultProcessPaymentCommand = commandGateway.sendAndWait(processPaymentCommand);
		}
		catch(Exception ex) {
			LOGGER.error(ex.getMessage());
			// Start compensating transaction
			cancelProductReservation(productReservedEvent, ex.getMessage());
			return;
		}
		if(resultProcessPaymentCommand == null) {
			LOGGER.info("The ProcessPaymentCommand resulted is NULL. Initiating a compensating transaction");
			// Start compensating transaction
			cancelProductReservation(productReservedEvent, "Could not proccess user payment with provided payment details");
			return;
		}
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo PaymentProcessedEvent) de una instancia del componente SAGA(En este caso, esta clase OrderSaga).
	// Debe indicarse al atributo "associationProperty" de esta anotación el nombre de una propiedad del evento recibido, como argumento de entrada en este método, para poder localizar, mediante su valor, una instancia del componente SAGA creada previamente que maneje este evento.
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(PaymentProcessedEvent paymentProcessedEvent) {
		// Si el proceso de pago se realizó correctamente y el evento de tipo PaymentProcessedEvent se produjo, entonces significa que el timeout del Deadline asociado al proceso de pago y programado previamente no se produjo y lo cancelamos porque ya no es necesario.
		cancelDeadline();
		ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
		// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo ReserveProductCommand) al Command Bus.
		// Operación no bloqueante. Existe otro método llamado "sendAndWait" que es bloqueante.
		commandGateway.send(approveOrderCommand);
	}
	
	// Como este evento OrderApprovedEvent es un evento final del flujo de SAGA, anotamos este método con la anotación @EndSaga.
	// Esta anotación destruye una instancia del componente SAGA(En este caso, de esta clase OrderSaga) creada previamente mediante la anotación @StartSaga y, a partir de aquí, ya no se podrá manejar ningún evento más en esa instancia.
	@EndSaga
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo OrderApprovedEvent) de una instancia del componente SAGA(En este caso, esta clase OrderSaga).
	// Debe indicarse al atributo "associationProperty" de esta anotación el nombre de una propiedad del evento recibido, como argumento de entrada en este método, para poder localizar, mediante su valor, la instancia del componente SAGA creada previamente que va a ser destruida.
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderApprovedEvent orderApprovedEvent) {
		LOGGER.info("Order is approved. Order SAGA is complete for orderId: {}", orderApprovedEvent.getOrderId());
		// Alternativa equivalente a usar la anotación @EndSaga a nivel de método
		//SagaLifecycle.end();
		// Emitimos este OrdenSummary como una actualización de la Subscription Query 
		queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, new OrderSummary(orderApprovedEvent.getOrderId(), orderApprovedEvent.getOrderStatus(), ""));
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo ProductReservationCancelledEvent) de una instancia del componente SAGA(En este caso, esta clase OrderSaga).
	// Debe indicarse al atributo "associationProperty" de esta anotación el nombre de una propiedad del evento recibido, como argumento de entrada en este método, para poder localizar, mediante su valor, una instancia del componente SAGA creada previamente que maneje este evento.
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
		rejectOrder(productReservationCancelledEvent.getOrderId(), productReservationCancelledEvent.getReason());
	}
	
	// Como este evento OrderRejectedEvent es un evento final del flujo de SAGA, anotamos este método con la anotación @EndSaga.
	// Esta anotación destruye una instancia de la clase SAGA(En este caso, de esta clase OrderSaga) creada previamente mediante la anotación @StartSaga y, a partir de aquí, ya no se podrá manejar ningún evento más en esa instancia.
	@EndSaga
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo OrderRejectedEvent) de una instancia del componente SAGA(En este caso, esta clase OrderSaga).
	// Debe indicarse al atributo "associationProperty" de esta anotación el nombre de una propiedad del evento recibido, como argumento de entrada en este método, para poder localizar, mediante su valor, la instancia del componente SAGA creada previamente que va a ser destruida.
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderRejectedEvent orderRejectedEvent) {
		LOGGER.info("Successfully rejected order with id: {}", orderRejectedEvent.getOrderId());
		// Alternativa equivalente a usar la anotación @EndSaga a nivel de método
		//SagaLifecycle.end();
		// Emitimos este OrdenSummary como una actualización de la Subscription Query 
		queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, new OrderSummary(orderRejectedEvent.getOrderId(), orderRejectedEvent.getOrderStatus(), orderRejectedEvent.getReason()));
	}
	
	// Anotación para que este método sea un manejador de Deadlines(En este caso, Deadlines asociados con el nombre PAYMENT_PROCESSING_TIMEOUT_DEADLINE).
	// En este caso, como hay un Deadline programado asociado al nombre PAYMENT_PROCESSING_TIMEOUT_DEADLINE con el evento ProductReservedEvent usado como datos de payload, si queremos usar los datos de ese evento en este manejador, tenemos que pasarlo como argumento de entrada
	// en este método para que se inyecte.
	@DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
	public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
		LOGGER.info("Payment processing  deadline took place. Sending a compensating command to cancel the product reservation");
		cancelProductReservation(productReservedEvent, "Payment timeout");
	}
	
	private void rejectOrder(String orderId, String reason) {
		RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderId, reason);
		// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo RejectOrderCommand) al Command Bus.
		// Operación no bloqueante. Existe otro método llamado "sendAndWait" que es bloqueante.
		commandGateway.send(rejectOrderCommand);
	}
	
	private void cancelProductReservation(ProductReservedEvent event, String reason) {
		// Cancelamos aquí también el Deadline asociado al proceso de pago y programado previamente porque si se ejecuta este método, significa que el proceso de pago falló por alguna excepción pero no por el timeout del Deadline. Por lo tanto, ese Deadline ya no es necesario.
		cancelDeadline();
		CancelProductReservationCommand cancelProductReservationCommand = CancelProductReservationCommand.builder()
				.orderId(event.getOrderId())
				.productId(event.getProductId())
				.userId(event.getUserId())
				.quantity(event.getQuantity())
				.reason(reason)
				.build();
		// Método que hace que el Command Gateway despache o envíe un command(En este caso de tipo CancelProductReservationCommand) al Command Bus.
		// Operación no bloqueante. Existe otro método llamado "sendAndWait" que es bloqueante.
		commandGateway.send(cancelProductReservationCommand);
	}
	
	private void cancelDeadline() {
		// Cancela todos los Deadlines programados previamente asociados al nombre PAYMENT_PROCESSING_TIMEOUT_DEADLINE
		//deadlineManager.cancelAll(PAYMENT_PROCESSING_TIMEOUT_DEADLINE);
		// Cancela un determinado Deadline asociado a un id de programación y al nombre PAYMENT_PROCESSING_TIMEOUT_DEADLINE
		if(scheduleId != null) {
			deadlineManager.cancelSchedule(scheduleId, PAYMENT_PROCESSING_TIMEOUT_DEADLINE);
			scheduleId = null;
		}
	}
}
