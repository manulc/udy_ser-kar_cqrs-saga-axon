package com.mlorenzo.estore.productsservice.commandapi;

import java.math.BigDecimal;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.mlorenzo.estore.core.commands.CancelProductReservationCommand;
import com.mlorenzo.estore.core.commands.ReserveProductCommand;
import com.mlorenzo.estore.core.events.ProductReservationCancelledEvent;
import com.mlorenzo.estore.core.events.ProductReservedEvent;
import com.mlorenzo.estore.productsservice.core.events.ProductCreatedEvent;


// Anotación que hace que esta clase sea un Aggregate.
// Mediante el atributo "snapshotTriggerDefinition" de esta anotación, asociamos el bean de Spring "productSnapshotTriggerDefinition" con este Aggregate para la creación de sus Snapshots en el Snapshot Store.
@Aggregate(snapshotTriggerDefinition = "productSnapshotTriggerDefinition")
public class ProductAggregate {
	
	// Esta anotación es para asociar Commands con Aggregates usando la anotación @TargetAggregateIdentifier que se establece en las clases de los Commands.
	// El framework Axon usa esta anotación junto con la anotación @TargetAggregateIdentifier de los Commands para poder despachar correctamente los Commands
	// a sus objetos Aggregates asociados
	@AggregateIdentifier
	private String productId;
	
	private String title;
	private BigDecimal price;
	private Integer quantity;
	
	// El framework Axon necesita un constructor vacío para recrear o reconstruir el estado actual de un Aggregate a partir de los eventos almacenados en el Event Store asociados a ese Aggegate.
	// Este proceso ocurre cada vez que el framework Axon recibe un Command asociado con ese Aggregate que es manejado por un método(no por el manejador de Commands del constructor, ya que, en ese caso, es cuando se crea el Aggregate por primera vez) mediante la anotación @CommandHandler.
	// Para ello, el framework Axon utiliza el valor de la propiedad anotada con @TargetAggregateIdentifier del Command recibido para identificar los eventos del Aggregate en el Event Store.
	public ProductAggregate() {
	}
	
	// Nota sobre los manejadores de Commands de un Aggregate.
	// Estos manejadores consumen Commands, en función de su tipo, que son despachados o publicados en el Command Bus por el Command Gateway.
	
	// Anotación para que este constructor sea un manejador de Commands del Aggregate(En este caso, Commands de tipo CreateProductCommand).
	// En este caso, este manejador de Commands crea instancias del Aggregate de esta clase ProductAggregate usando como identificador de cada instancia, mediante la anotación @AggregateIdentifier, el id de un producto.
	// No puede haber más de una instancia de un Aggregate con el mismo identificador.
	@CommandHandler
	public ProductAggregate(CreateProductCommand createProductCommand) {
		// Aquí podemos validar opcionalmente el Command recibido como argumento de entrada
		// Se comenta porque estas validaciones ya se verifican usando anotaciones JPA en la clase modelo CreateProductRestModel
		/*if(createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException("Price cannot be less o equal then zero");
		if(createProductCommand.getTitle() == null || createProductCommand.getTitle().isBlank())
			throw new IllegalArgumentException("Title cannot be empty");*/
		ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
				.productId(createProductCommand.getProductId())
				.title(createProductCommand.getTitle())
				.price(createProductCommand.getPrice())
				.quantity(createProductCommand.getQuantity())
				.build();
		// Con este método, un Aggregate aplica y publica un evento(En este caso, de tipo ProductCreatedEvent). Ésto significa que primero se invocará dentro del Aggregate al método manejador de eventos de tipo @EventSourcingHandler asociado a ese evento(Dentro de un Aggregate sólo puede haber un método de este tipo asociado a un determinado tipo de evento)
		// para actualizar el estado del Aggregate a partir de los datos contenidos en el evento y, después, se inicia una transacción para poder persistir dicho evento en el Event Store(La finalización de la transacción dependerá del tipo de procesador de eventos que se use, ya que dependerá de si los manejadores de eventos se ejecutan en el mismo hilo que el
		// Aggregate o en hilos distintos a éste. Por defecto, el framework Axon utiliza el procesador de eventos de tipo Tracking). Después de todo ésto, el evento se despacha o se publica en el Event Bus para que pueda ser consumido por los métodos manejadores de eventos de tipo @EventHandler o @SagaEventHandler asociados al tipo de evento publicado.
		AggregateLifecycle.apply(productCreatedEvent);
		// Aunque esta excepción se produce después de aplicar y publicar el evento, ese evento no llega a persistirse en el Event Store porque la transacción no finaliza inmediatamente y se realiza un rollback debido a la excepción ocurrida. Por lo tanto, ese evento tampoco llega a despacharse o publicarse en el Event Bus para que pueda ser consumido por los
		// métodos manejadores de eventos de tipo @EventHandler asociados a dicho evento
		// Nota: Debido a que estamos en un método Command Handler, esta excepción es traducida por el framework Axon por una excepción de tipo CommandExecutionException
		// Se comenta porque es para pruebas
		//if(true) throw new RuntimeException("An error took place in the CreateProductCommand @CommandHandler method");
	}
	
	// Anotación para que este método sea un manejador de Commands del Aggregate(En este caso, Commands de tipo ReserveProductCommand)
	@CommandHandler
	public void handle(ReserveProductCommand reserveProductCommand) {
		// Aquí podemos validar opcionalmente el Command recibido como argumento de entrada
		if(quantity < reserveProductCommand.getQuantity())
			throw new IllegalArgumentException("Insufficient number of items in stock");
		ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
				.orderId(reserveProductCommand.getOrderId())
				.productId(reserveProductCommand.getProductId())
				.quantity(reserveProductCommand.getQuantity())
				.userId(reserveProductCommand.getUserId())
				.build();
		// Con este método, un Aggregate aplica y publica un evento(En este caso, de tipo ProductCreatedEvent). Ésto significa que primero se invocará dentro del Aggregate al método manejador de eventos de tipo @EventSourcingHandler asociado a ese evento(Dentro de un Aggregate sólo puede haber un método de este tipo asociado a un determinado tipo de evento)
		// para actualizar el estado del Aggregate a partir de los datos contenidos en el evento y, después, se inicia una transacción para poder persistir dicho evento en el Event Store(La finalización de la transacción dependerá del tipo de procesador de eventos que se use, ya que dependerá de si los manejadores de eventos se ejecutan en el mismo hilo que el
		// Aggregate o en hilos distintos a éste. Por defecto, el framework Axon utiliza el procesador de eventos de tipo Tracking). Después de todo ésto, el evento se despacha o se publica en el Event Bus para que pueda ser consumido por los métodos manejadores de eventos de tipo @EventHandler o @SagaEventHandler asociados al tipo de evento publicado.
		AggregateLifecycle.apply(productReservedEvent);
	}
	
	// Anotación para que este método sea un manejador de Commands del Aggregate(En este caso, Commands de tipo CancelProductReservationCommand)
	@CommandHandler
	public void handle(CancelProductReservationCommand cancelProductReservationCommand) {
		// Aquí podemos validar opcionalmente el Command recibido como argumento de entrada
		ProductReservationCancelledEvent productReservationCancelledEvent = ProductReservationCancelledEvent.builder()
				.orderId(cancelProductReservationCommand.getOrderId())
				.productId(cancelProductReservationCommand.getProductId())
				.userId(cancelProductReservationCommand.getUserId())
				.quantity(cancelProductReservationCommand.getQuantity())
				.reason(cancelProductReservationCommand.getReason())
				.build();
		// Con este método, un Aggregate aplica y publica un evento(En este caso, de tipo ProductReservationCancelledEvent). Ésto significa que primero se invocará dentro del Aggregate al método manejador de eventos de tipo @EventSourcingHandler asociado a ese evento(Dentro de un Aggregate sólo puede haber un método de este tipo asociado a un determinado tipo de evento)
		// para actualizar el estado del Aggregate a partir de los datos contenidos en el evento y, después, se inicia una transacción para poder persistir dicho evento en el Event Store(La finalización de la transacción dependerá del tipo de procesador de eventos que se use, ya que dependerá de si los manejadores de eventos se ejecutan en el mismo hilo que el
		// Aggregate o en hilos distintos a éste. Por defecto, el framework Axon utiliza el procesador de eventos de tipo Tracking). Después de todo ésto, el evento se despacha o se publica en el Event Bus para que pueda ser consumido por los métodos manejadores de eventos de tipo @EventHandler o @SagaEventHandler asociados al tipo de evento publicado.
		AggregateLifecycle.apply(productReservationCancelledEvent);
	}
	
	// Nota sobre los métodos que son manejadores de eventos de un Aggregate.
	// Este tipo de métodos no deben incluir lógica de negocio y simplemente deben actualizar el estado del Aggregate a partir de los datos de los eventos.
	// Justo después de ejecutarse un método de este tipo, el evento es publicado en el Event Bus y la transacción iniciada para persistir el evento en el Event Store finaliza(El evento queda persistido definitivamente en esa base de datos)
	// sólo si el procesador de eventos usado es de tipo Tracking(procesador de eventos por defecto). Ésto es debido a que dicho procesador ejecuta el Aggregate en un hilo distinto a otros hilos donde se ejecutarán posteriormente los manejadores de eventos de
	// tipo @EventHandler asociados a ese evento. Si el procesador de eventos es de tipo Subscribing, la transacción permanecerá abierta hasta que todos estos manejadores de eventos de tipo @EventHandler asociados al evento finalicen su ejecución y ésto es
	// debido a que dicho procesador de eventos ejecuta tanto al Aggregate como a estos métodos manejadores de eventos en un mismo hilo.
	
	// Anotación para que este método sea un manejador de eventos del Aggregate(En este caso, de tipo ProductCreatedEvent)
	@EventSourcingHandler
	public void on(ProductCreatedEvent productCreatedEvent) {
		this.productId = productCreatedEvent.getProductId();
		this.title = productCreatedEvent.getTitle();
		this.price = productCreatedEvent.getPrice();
		this.quantity = productCreatedEvent.getQuantity();
	}
	
	// Anotación para que este método sea un manejador de eventos del Aggregate(En este caso, de tipo ProductReservedEvent)
	@EventSourcingHandler
	public void on(ProductReservedEvent productReservedEvent) {
		this.quantity -= productReservedEvent.getQuantity();
	}
	
	// Anotación para que este método sea un manejador de eventos del Aggregate(En este caso, de tipo ProductReservationCancelledEvent)
	@EventSourcingHandler
	public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
		this.quantity += productReservationCancelledEvent.getQuantity();
	}
}
