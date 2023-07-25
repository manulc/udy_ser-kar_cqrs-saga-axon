package com.mlorenzo.estore.productsservice.core.errorhandler;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;

// Manejador personalizado para las excepciones producidas en los manejadores de eventos de tipo @EventHandler.
// En vez de usar el manejador de excepciones por defecto del framework Axon, que simplemente hace "logging" de la excepción y después continua con la ejecución del siguiente manejador de eventos,
// podemos establecer nuestro manejador personalizado de excepciones para que se use en todos los manejadores de eventos o en aquellos de determinados grupos de procesamiento.
// Ver método "public void configure(EventProcessingConfigurer config)" en la clase principal de la aplicación "ProductsServiceApplication".
// En este caso en concreto, la funcionalidad de este manejador personalizado de excepciones es equivalente a la funcionalidad del manejador de excepciones "PropagatingErrorHandler" del framework Axon,
// es decir, simplemente propaga las excepciones hacia arriba sin manejarlas. Ésto es útil cuando queremos que el framework Axon no haga nada con las excepciones de los manejadores de eventos para que
// éstas lleguén hasta el Aggregate(siempre y cuando el manejador de eventos que produjo la excepción se ejecute en el mismo hilo que el Aggregate) y, de esta forma, poder manjerlas allí(Por ejemplo,
// usando un manejador centralzado de excepciones de Spring como en nuestro caso).

public class ProductServiceEventsErrorHandler implements ListenerInvocationErrorHandler {

	@Override
	public void onError(Exception exception, EventMessage<?> event, EventMessageHandler eventHandler) throws Exception {
		throw exception;
	}
}
