package com.mlorenzo.estore.productsservice.commandapi;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.productsservice.commandapi.data.ProductLookupEntity;
import com.mlorenzo.estore.productsservice.commandapi.data.ProductLookupRespository;
import com.mlorenzo.estore.productsservice.core.events.ProductCreatedEvent;

import lombok.RequiredArgsConstructor;

// Nota: En caso de usar el procesador de eventos Tracking(procesador por defecto si no se indica otro), por defecto, 2 o más manejadores de eventos de tipo @EventHandler se agrupan para ejecutarse en un mismo hilo si pertenecen a la misma clase(mismo paquete)
// o a clases distintas que pertenecen al mismo paquete, es decir, se agrupan por nombre de paquete. Si queremos que un conjunto de manejadores de eventos, que pertenecen a clases de distintos paquetes, se agrupen en un mismo grupo para que se ejecuten en un
// mismo hilo, tenemos que usar la anotación @ProcessingGroup a nivel de clase como en este caso.

// En el archivo de propiedades se ha establecido la propiedad "axon.eventhandling.processors.product-group.mode" en "subscribing" para que el manejador de eventos de esta clase, que se encarga de actualizar la pequeña tabla de consultas para validaciones(tabla Look-up),
// y el Aggregate se ejecuten en el mismo hilo y así poder mantener la consistencia de datos entre esta tabla y el Event Store. De esta forma, si ocurre alguna excepción a la hora de persistir datos en alguna de estas tablas y no se captura ni se maneja, la excepción llegará
// hasta el Aggregate y se ejecutará un rollback de la transacción iniciada(En este caso, la transacción abarca a ámbas tablas debido a que sus procesos de persistencia se ejecutan en el mismo hilo).

// Anotación para establecer un grupo de procesamiento(En este caso, el grupo "product-group") a los manejadores de eventos de esta clase
@ProcessingGroup("product-group")
@RequiredArgsConstructor
@Component
public class ProductLookupEventsHandler {
	private final ProductLookupRespository repository;
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo ProductCreatedEvent)
	@EventHandler
	public void on(ProductCreatedEvent event) {
		ProductLookupEntity productLookupEntity = new ProductLookupEntity(event.getProductId(), event.getTitle());
		repository.save(productLookupEntity);
		//if(true) throw new RuntimeException("Forcing exception in the Event Handler class");
	}
}
