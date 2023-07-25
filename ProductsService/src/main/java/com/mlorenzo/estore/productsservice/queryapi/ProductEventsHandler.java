package com.mlorenzo.estore.productsservice.queryapi;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.core.events.ProductReservationCancelledEvent;
import com.mlorenzo.estore.core.events.ProductReservedEvent;
import com.mlorenzo.estore.productsservice.core.events.ProductCreatedEvent;
import com.mlorenzo.estore.productsservice.queryapi.data.ProductEntity;
import com.mlorenzo.estore.productsservice.queryapi.data.ProductRepository;

import lombok.RequiredArgsConstructor;

// Nota: En caso de usar el procesador de eventos Tracking(procesador por defecto si no se indica otro), por defecto, 2 o más manejadores de eventos de tipo @EventHandler se agrupan para ejecutarse en un mismo hilo si pertenecen a la misma clase(mismo paquete)
// o a clases distintas que pertenecen al mismo paquete, es decir, se agrupan por nombre de paquete. Si queremos que un conjunto de manejadores de eventos, que pertenecen a clases de distintos paquetes, se agrupen en un mismo grupo para que se ejecuten en un
// mismo hilo, tenemos que usar la anotación @ProcessingGroup a nivel de clase como en este caso.

@RequiredArgsConstructor
@Component
public class ProductEventsHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventsHandler.class);
	
	private final ProductRepository productRepository;
	
	// Nota: El manejo de excepciones en un Event Handler es diferente al manejo de excepciones en un Command Handler, es decir, si queremos realizar el manejo de excepciones en un sitio centralizado(ProductServiceErrorHandling) como es nuestro caso,
	// desde un Event Handler, como éste, tenemos que ir propagando las excepciones usando, para ello, el propagador de excepciones de Axon PropagatingErrorHandler o usando un propagador personalizado(ProductServiceEventsErrorHandler) como es nuestro caso

	// Podemos implementar un manejador común de excepciones concretas(En este caso, IllegalArgumentException) para las excepciones que puedan producirse en los Event Handlers de esta clase
	@ExceptionHandler(resultType = IllegalArgumentException.class)
	public void handle(IllegalArgumentException ex) {
		// Log error message
	}
	
	// Podemos implementar un manejador común de excepciones genéricas para las excepciones que puedan producirse en los Event Handlers de esta clase
	@ExceptionHandler(resultType = Exception.class)
	public void handle(Exception ex) throws Exception {
		// Relanzamos la excepción para que sea manejada por nuestro ListenerInvocationErrorHandler ProductServiceEventsErrorHandler
		throw ex;
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo ProductCreatedEvent)
	@EventHandler
	public void on(ProductCreatedEvent event) {
		ProductEntity productEntity = new ProductEntity();
		BeanUtils.copyProperties(event, productEntity);
		productRepository.save(productEntity);
		// En este punto, la transacción que se encarga de persistir los datos en la tabla de Lookup y en la tabla de productos aún sigue en el aire y, debido a esta excepción, Axon realizará un rollback sin que llegue a completarse esa transacción
		// Se comenta porque es para pruebas
		//if(true) throw new RuntimeException("Forcing exception in the Event Handler class");
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo ProductCreatedEvent)
	@EventHandler
	public void on(ProductReservedEvent event) {
		productRepository.findById(event.getProductId())
			.ifPresent(productEntity -> {
				LOGGER.debug("ProductReservedEvent: Current product quantity {}", productEntity.getQuantity());
				productEntity.setQuantity(productEntity.getQuantity() - event.getQuantity());
				productRepository.save(productEntity);
				LOGGER.debug("ProductReservedEvent: New product quantity {}", productEntity.getQuantity());
			});
		LOGGER.info("ProductReservedEvent is called for productId: {} and orderId: {}", event.getProductId(), event.getOrderId());
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo ProductReservationCancelledEvent)
	@EventHandler
	public void on(ProductReservationCancelledEvent event) {
		productRepository.findById(event.getProductId())
			.ifPresent(productEntity -> {
				LOGGER.debug("ProductReservationCancelledEvent: Current product quantity {}", productEntity.getQuantity());
				productEntity.setQuantity(productEntity.getQuantity() + event.getQuantity());
				productRepository.save(productEntity);
				LOGGER.debug("ProductReservationCancelledEvent: New product quantity {}", productEntity.getQuantity());
			});
	}
	
	// Anotación para que este método se ejecute justo antes de que comience la recreación de eventos asociados a un determinado Tracking Event Processor
	// (En este caso, el Tracking Event Processor del grupo "com.mlorenzo.estore.productsservice.queryapi", que es el paquete donde se encuentra esta clase).
	// En la recreación de eventos, se invocará de forma automática a aquellos manejadores de eventos de tipo @EventHandler asociados a un determinado
	// Tracking Event Processor. Si queremos excluir de la recreación a alguno de estos manejadores de eventos, tenemos que anotar a sus métodos con la anotación @DisallowReplay.
	// En este caso en concreto, limpiamos la tabla de Productos de la base de datos para que se recreé de nuevo de forma automática a partir
	// de los eventos almacenados en el Event Store asociados al procesador de eventos de tipo Tracking del grupo "com.mlorenzo.estore.productsservice.queryapi".
	@ResetHandler
	public void reset() {
		productRepository.deleteAll();
	}
}
