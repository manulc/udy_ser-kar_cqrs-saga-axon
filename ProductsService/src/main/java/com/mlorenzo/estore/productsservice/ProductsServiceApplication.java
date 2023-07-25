package com.mlorenzo.estore.productsservice;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.mlorenzo.estore.productsservice.commandapi.CreateProductCommandInterceptor;
import com.mlorenzo.estore.productsservice.core.errorhandler.ProductServiceEventsErrorHandler;

// Opcional ya que basta con tener la dependencia "spring-cloud-starter-netflix-eureka-client" en el classpath para que este microservicio se registre en el servidor Eureka
@EnableEurekaClient
@SpringBootApplication
public class ProductsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsServiceApplication.class, args);
	}
	
	// Método para registrar nuestro interceptor de Commands CreateProductCommandInterceptor en el Command Bus.
	// Los Commands son despachados a este bus desde el Command Gateway. Por lo tanto, este interceptor se ejecutará antes que los Command Handlers(métodos o construcotres anotados con @CommandHandler).
	// Recordatorio: Este método, como está anotado con @Autowired, será invocado automáticamente por Spring para realizar la inyección de dependencias que se corresponden con los argumentos de entrada de dicho método
	@Autowired
	public void registerCreateProductCommandInterceptor(ApplicationContext context, CommandBus commandBus) {
		commandBus.registerDispatchInterceptor(context.getBean(CreateProductCommandInterceptor.class));
	}
	
	// Nota sobre los procesadores de eventos del framework Axon.
	// Existen 2 tipos principales de procesadores de eventos: El procesador de eventos Tracking y el procesador de eventos Subscribing.
	// El procesador de eventos Tracking es el procesador de eventos por defecto y ejecuta el Aggregate en un hilo distinto al resto de hilos donde se ejecutan los manejadores de eventos de tipo @EventHandler. En este
	// tipo de procesador de eventos, por defecto las ejecuciones de los manejadores de eventos de tipo @EventHandler se agrupan en hilos en función del paquete donde se encuentren las clases que los implementen. Por ejemplo,
	// 2 o más manejadores de eventos, que se implementen en la misma clase, pertenecen al mismo paquete y, por lo tanto, se ejecutan en el mismo hilo porque son del mismo grupo. 2 manejadores de eventos implementados
	// en clases diferentes de paquetes diferentes se ejecutarán en 2 hilos distintos porque pertencen a 2 grupos distintos.
	// En este tipo de procesador de eventos, las excepciones producidas por los manejadores de eventos que no sean capturadas y manejadas harán que este procesador entre en un modo de error reintentado automáticamente la
	// ejecución de aquellos manejadores cuyas excepciones no fueron capturadas y manejadas usando un período de retroceso(back-off) incremental que va desde 1 seg hasta cumplirse los 60 seg.
	// El procesador de eventos Subscribing ejecuta tanto el Aggregate como los manejadores de eventos de tipo @EventHandler en un único y mismo hilo. Por lo tanto, como todo se ejecuta en un mismo hilo, si un manejador de
	// eventos produce una excepción que no es capturada ni manejada, dicha excepción llegará hasta el Aggregate y ese Aggregate podrá tratarla si se desea(En un procesador de tipo Tracking, una excepción no capturada ni
	// manejada jamás llegará hasta el Aggregate porque se encontraría en un hilo distinto al de los manejadores de eventos). En este tipo de procesador de eventos no hay reintentos en caso de excepciones no capturas ni
	// manejadas procesentes de manejadores de eventos.
	// Por defecto, y ésto vale tanto si se está usando un procesador de eventos Tracking como uno de tipo Subscribing, el framework Axon captura las excepciones producidas por los manejadores de eventos que no han sido
	// manejadas previamente y simplemente hace "logging" de ellas y pasa a ejecutar el siguiente manejador de eventos del Event Bus.
	
	// Método para configurar el procesador de eventos del framework Axon.
	// Recordatorio: Este método, como está anotado con @Autowired, será invocado automáticamente por Spring para realizar la inyección de dependencias que se corresponden con los argumentos de entrada de dicho método.
	@Autowired
	public void configure(EventProcessingConfigurer config) {
		// Registramos nuestro manejador personalizado de excepciones ProductServiceEventsErrorHandler para todos los manejadores de eventos de tipo @EventHandler en vez de usar el manejador de excepciones por defecto del framework Axon
		//config.registerDefaultListenerInvocationErrorHandler(conf -> new ProductServiceEventsErrorHandler());
		// En vez de usar el manejador de excepciones por defecto del framework Axon, usamos, mediante su registro, nuestro manejador personalizado de excepciones ProductServiceEventsErrorHandler para aquellos manejadores de eventos de
		// tipo @EventHandler pertenecientes al grupo de procesamiento "product-group"
		config.registerListenerInvocationErrorHandler("product-group", conf -> new ProductServiceEventsErrorHandler());
		
		// En vez de usar el manejador de excepciones por defecto del framework Axon, usamos el manejador de excepciones PropagatingErrorHandler(también del framework Axon) para propagar las excepciones de todos los manejadores de eventos
		// en vez de capturarlas y manejarlas. Ésto provocará reintentos de ejecución de los manejadores de eventos en caso de usarse el procesador de eventos Tracking, o provocará que las excepciones lleguen al Aggregate en caso de usarse
		// el procesador de eventos Subscribing
		//config.registerDefaultListenerInvocationErrorHandler(conf -> PropagatingErrorHandler.instance());
		// En vez de usar el manejador de excepciones por defecto del framework Axon, usamos el manejador de excepciones PropagatingErrorHandler(también del framework Axon) para propagar las excepciones de aquellos manejadores de eventos
		// pertenecientes al grupo de procesamiento "product-group" en vez de capturarlas y manejarlas. Ésto provocará reintentos de ejecución de los manejadores de eventos en caso de usarse el procesador de eventos Tracking, o provocará
		// que las excepciones lleguen al Aggregate en caso de usarse el procesador de eventos Subscribing
		//config.registerListenerInvocationErrorHandler("product-group", conf -> PropagatingErrorHandler.instance());
		config.registerListenerInvocationErrorHandler("com.mlorenzo.estore.productsservice.query", conf -> PropagatingErrorHandler.instance());
		
		// Establece el procesador de eventos Tracking para todos los manejadores de eventos(Procesador de eventos por defecto si no se indica otro)
		//config.usingTrackingEventProcessors();
		// Establece el procesador de eventos Subscribing para todos los manejadores de eventos
		//config.usingSubscribingEventProcessors();
	}
	
	// Este bean de Spring configura la creación de Snapshots de un Aggregate en el Snapshot Store cada vez que se producen 3 eventos asociados a ese Aggregate.
	// Nota: Un Snapshot se considera un evento y, por lo tanot, se tiene en cuenta en el contador de eventos asociados a un Aggreagte para la creación del siguiente Snapshot.
	// Es útil cuando se tienen muchos eventos(cientos o miles) en el Event Store asociados a un Aggregate y se quiere optimizar el proceso de carga de esos eventos para que la recreación del estado actual de ese Aggregate sea más rápida.
	// De esta forma, cuando el framework Axon va a recrear el estado actual de un Aggregate, primero mira en el Snapshot Store si hay Snapshots creados para ese Aggregate y, en caso afirmativo, aplica el último Snapshot asociado a ese
	// Aggregate. Después, si hay eventos en el Event Store asociados a ese Aggregate que son posteriores o más nuevos que ese Snapshot, el framework Axon los aplica a continuación para la recreación. Aquellos eventos del Event Store asociados
	// a ese Aggregate que son previos o más antiguos a ese Snapshot, son ignorados en este proceso de recreación del estado actual del Aggregate.
	@Bean
	public SnapshotTriggerDefinition productSnapshotTriggerDefinition(Snapshotter snapshotter) {
		return new EventCountSnapshotTriggerDefinition(snapshotter, 3);
	}
}
