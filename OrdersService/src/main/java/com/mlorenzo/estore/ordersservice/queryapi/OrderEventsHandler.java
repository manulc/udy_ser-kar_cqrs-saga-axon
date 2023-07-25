package com.mlorenzo.estore.ordersservice.queryapi;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.ordersservice.core.events.OrderApprovedEvent;
import com.mlorenzo.estore.ordersservice.core.events.OrderCreatedEvent;
import com.mlorenzo.estore.ordersservice.core.events.OrderRejectedEvent;
import com.mlorenzo.estore.ordersservice.queryapi.data.OrderEntity;
import com.mlorenzo.estore.ordersservice.queryapi.data.OrderRepository;

import lombok.RequiredArgsConstructor;

// Nota: En caso de usar el procesador de eventos Tracking(procesador por defecto si no se indica otro), por defecto, 2 o más manejadores de eventos de tipo @EventHandler se agrupan para ejecutarse en un mismo hilo si pertenecen a la misma clase(mismo paquete)
// o a clases distintas que pertenecen al mismo paquete, es decir, se agrupan por nombre de paquete. Si queremos que un conjunto de manejadores de eventos, que pertenecen a clases de distintos paquetes, se agrupen en un mismo grupo para que se ejecuten en un
// mismo hilo, tenemos que usar la anotación @ProcessingGroup a nivel de clase como en este caso.

@RequiredArgsConstructor
@Component
public class OrderEventsHandler {
	private final OrderRepository orderRepository;
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo OrderCreatedEvent)
	@EventHandler
	public void on(OrderCreatedEvent event) {
		OrderEntity orderEntity = new OrderEntity();
		BeanUtils.copyProperties(event, orderEntity);
		orderRepository.save(orderEntity);
	}
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo OrderApprovedEvent)
	@EventHandler
	public void on(OrderApprovedEvent event) {
		orderRepository.findById(event.getOrderId())
			.ifPresent(orderEntity -> {
				orderEntity.setOrderStatus(event.getOrderStatus());
				orderRepository.save(orderEntity);
			});
	}
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo OrderRejectedEvent)
	@EventHandler
	public void on(OrderRejectedEvent event) {
		orderRepository.findById(event.getOrderId())
			.ifPresent(orderEntity -> {
				orderEntity.setOrderStatus(event.getOrderStatus());
				orderRepository.save(orderEntity);
			});
	}
}
