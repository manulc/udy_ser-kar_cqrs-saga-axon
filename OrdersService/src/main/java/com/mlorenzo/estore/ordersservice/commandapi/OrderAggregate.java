package com.mlorenzo.estore.ordersservice.commandapi;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.mlorenzo.estore.ordersservice.core.events.OrderApprovedEvent;
import com.mlorenzo.estore.ordersservice.core.events.OrderCreatedEvent;
import com.mlorenzo.estore.ordersservice.core.events.OrderRejectedEvent;
import com.mlorenzo.estore.ordersservice.core.models.OrderStatus;

//Anotación que hace que esta clase sea un Aggregate
@Aggregate
public class OrderAggregate {
	
	// Esta anotación es para asociar Commands con Aggregates usando la anotación @TargetAggregateIdentifier que se establece en las clases de los Commands.
	// El framework Axon usa esta anotación junto con la anotación @TargetAggregateIdentifier de los Commands para poder despachar correctamente los Commands
	// a sus objetos Aggregates asociados
	@AggregateIdentifier
	private String orderId;
	
	private String productId;
	private String userId;
	private int quantity;
	private String addressId;
	private OrderStatus orderStatus;
	
	// El framework Axon necesita un constructor vacío para recrear o reconstruir el estado actual de un Aggregate a partir de los eventos almacenados en el Event Store asociados a ese Aggegate.
	// Este proceso ocurre cada vez que el framework Axon recibe un Command asociado con ese Aggregate que es manejado por un método(no por el manejador de Commands del constructor, ya que, en ese caso, es cuando se crea el Aggregate por primera vez) mediante la anotación @CommandHandler.
    // Para ello, el framework Axon utiliza el valor de la propiedad anotada con @TargetAggregateIdentifier del Command recivido para identificar los eventos del Aggregate en el Event Store.
	public OrderAggregate() {	
	}
	
	// Nota sobre los manejadores de Commands de un Aggregate.
	// Estos manejadores consumen Commands, en función de su tipo, que son despachados o publicados en el Command Bus por el Command Gateway.
		
	// Anotación para que este constructor sea un manejador de Commands del Aggregate(En este caso, Commands de tipo CreateOrderCommand).
	// En este caso, este manejador de Commands crea instancias del Aggregate de esta clase OrderAggregate usando como identificador de cada instancia, mediante la anotación @AggregateIdentifier, el id de una orden.
	// No puede haber más de una instancia de un Aggregate con el mismo identificador.
	@CommandHandler
	public OrderAggregate(CreateOrderCommand createOrderCommand) {
		// Aquí podemos validar opcionalmente el Command recibido como argumento de entrada
		OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
				.orderId(createOrderCommand.getOrderId())
				.productId(createOrderCommand.getProductId())
				.quantity(createOrderCommand.getQuantity())
				.userId(createOrderCommand.getUserId())
				.addressId(createOrderCommand.getAddressId())
				.build();
		// Con este método, un Aggregate aplica y publica un evento(En este caso, de tipo OrderCreatedEvent). Ésto significa que primero se invocará dentro del Aggregate al método manejador de eventos de tipo @EventSourcingHandler asociado a ese evento(Dentro de un Aggregate sólo puede haber un método de este tipo asociado a un determinado tipo de evento)
		// para actualizar el estado del Aggregate a partir de los datos contenidos en el evento y, después, se inicia una transacción para poder persistir dicho evento en el Event Store(La finalización de la transacción dependerá del tipo de procesador de eventos que se use, ya que dependerá de si los manejadores de eventos se ejecutan en el mismo hilo que el
		// Aggregate o en hilos distintos a éste. Por defecto, el framework Axon utiliza el procesador de eventos de tipo Tracking). Después de todo ésto, el evento se despacha o se publica en el Event Bus para que pueda ser consumido por los métodos manejadores de eventos de tipo @EventHandler o @SagaEventHandler asociados al tipo de evento publicado.
		AggregateLifecycle.apply(orderCreatedEvent);
	}
	
	// Anotación para que este método sea un manejador de Commands del Aggregate(En este caso, Commands de tipo ApproveOrderCommand)
	@CommandHandler
	public void handle(ApproveOrderCommand approveOrderCommand) {
		// Aquí podemos validar opcionalmente el Command recibido como argumento de entrada
		OrderApprovedEvent orderApprovedEvent = new OrderApprovedEvent(approveOrderCommand.getOrderId());
		// Con este método, un Aggregate aplica y publica un evento(En este caso, de tipo OrderApprovedEvent). Ésto significa que primero se invocará dentro del Aggregate al método manejador de eventos de tipo @EventSourcingHandler asociado a ese evento(Dentro de un Aggregate sólo puede haber un método de este tipo asociado a un determinado tipo de evento)
		// para actualizar el estado del Aggregate a partir de los datos contenidos en el evento y, después, se inicia una transacción para poder persistir dicho evento en el Event Store(La finalización de la transacción dependerá del tipo de procesador de eventos que se use, ya que dependerá de si los manejadores de eventos se ejecutan en el mismo hilo que el
		// Aggregate o en hilos distintos a éste. Por defecto, el framework Axon utiliza el procesador de eventos de tipo Tracking). Después de todo ésto, el evento se despacha o se publica en el Event Bus para que pueda ser consumido por los métodos manejadores de eventos de tipo @EventHandler o @SagaEventHandler asociados al tipo de evento publicado.
		AggregateLifecycle.apply(orderApprovedEvent);
	}
	// Anotación para que este método sea un manejador de Commands del Aggregate(En este caso, Commands de tipo RejectOrderCommand)
	@CommandHandler
	public void handle(RejectOrderCommand rejectOrderCommand) {
		// Aquí podemos validar opcionalmente el Command recibido como argumento de entrada
		OrderRejectedEvent orderRejectedEvent = new OrderRejectedEvent(rejectOrderCommand.getOrderId(), rejectOrderCommand.getReason());
		// Con este método, un Aggregate aplica y publica un evento(En este caso, de tipo OrderRejectedEvent). Ésto significa que primero se invocará dentro del Aggregate al método manejador de eventos de tipo @EventSourcingHandler asociado a ese evento(Dentro de un Aggregate sólo puede haber un método de este tipo asociado a un determinado tipo de evento)
		// para actualizar el estado del Aggregate a partir de los datos contenidos en el evento y, después, se inicia una transacción para poder persistir dicho evento en el Event Store(La finalización de la transacción dependerá del tipo de procesador de eventos que se use, ya que dependerá de si los manejadores de eventos se ejecutan en el mismo hilo que el
		// Aggregate o en hilos distintos a éste. Por defecto, el framework Axon utiliza el procesador de eventos de tipo Tracking). Después de todo ésto, el evento se despacha o se publica en el Event Bus para que pueda ser consumido por los métodos manejadores de eventos de tipo @EventHandler o @SagaEventHandler asociados al tipo de evento publicado.
		AggregateLifecycle.apply(orderRejectedEvent);
	}
	
	// Nota sobre los métodos que son manejadores de eventos de un Aggregate.
	// Este tipo de métodos no deben incluir lógica de negocio y simplemente deben actualizar el estado del Aggregate a partir de los datos de los eventos.
	// Justo después de ejecutarse un método de este tipo, el evento es publicado en el Event Bus y la transacción iniciada para persistir el evento en el Event Store finaliza(El evento queda persistido definitivamente en esa base de datos)
	// sólo si el procesador de eventos usado es de tipo Tracking(procesador de eventos por defecto). Ésto es debido a que dicho procesador ejecuta el Aggregate en un hilo distinto a otros hilos donde se ejecutarán posteriormente los manejadores de eventos de
	// tipo @EventHandler asociados a ese evento. Si el procesador de eventos es de tipo Subscribing, la transacción permanecerá abierta hasta que todos estos manejadores de eventos de tipo @EventHandler asociados al evento finalicen su ejecución y ésto es
	// debido a que dicho procesador de eventos ejecuta tanto al Aggregate como a estos métodos manejadores de eventos en un mismo hilo.
		
	// Anotación para que este método sea un manejador de eventos del Aggregate(En este caso, de tipo OrderCreatedEvent)
	@EventSourcingHandler
	public void on(OrderCreatedEvent orderCreatedEvent) {
		this.orderId = orderCreatedEvent.getOrderId();
		this.userId = orderCreatedEvent.getOrderId();
		this.productId = orderCreatedEvent.getProductId();
		this.quantity = orderCreatedEvent.getQuantity();
		this.addressId = orderCreatedEvent.getAddressId();
		this.orderStatus = orderCreatedEvent.getOrderStatus();
	}
	
	// Anotación para que este método sea un manejador de eventos del Aggregate(En este caso, de tipo OrderApprovedEvent)
	@EventSourcingHandler
	public void on(OrderApprovedEvent orderApprovedEvent) {
		this.orderStatus = orderApprovedEvent.getOrderStatus();
	}
	
	// Anotación para que este método sea un manejador de eventos del Aggregate(En este caso, de tipo OrderRejectedEvent)
	@EventSourcingHandler
	public void on(OrderRejectedEvent orderRejectedEvent) {
		this.orderStatus = orderRejectedEvent.getOrderStatus();
	}
}
