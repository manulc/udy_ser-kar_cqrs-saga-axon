package com.mlorenzo.estore.paymentsservice.queryapi;

import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.core.events.PaymentProcessedEvent;
import com.mlorenzo.estore.paymentsservice.queryapi.data.PaymentEntity;
import com.mlorenzo.estore.paymentsservice.queryapi.data.PaymentRepository;

import lombok.RequiredArgsConstructor;

// Nota: En caso de usar el procesador de eventos Tracking(procesador por defecto si no se indica otro), por defecto, 2 o más manejadores de eventos de tipo @EventHandler se agrupan para ejecutarse en un mismo hilo si pertenecen a la misma clase(mismo paquete)
// o a clases distintas que pertenecen al mismo paquete, es decir, se agrupan por nombre de paquete. Si queremos que un conjunto de manejadores de eventos, que pertenecen a clases de distintos paquetes, se agrupen en un mismo grupo para que se ejecuten en un
// mismo hilo, tenemos que usar la anotación @ProcessingGroup a nivel de clase como en este caso.

@RequiredArgsConstructor
@Component
public class PaymentEventsHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(PaymentEventsHandler.class);
	
	private final PaymentRepository paymentRepository;
	
	// Anotación para que este método sea un manejador de eventos(En este caso, de tipo PaymentProcessedEvent)
	@EventHandler
	public void on(PaymentProcessedEvent event) {
		LOGGER.info("PaymentProcessedEvent is called for orderId: " + event.getOrderId());
		PaymentEntity paymentEntity = new PaymentEntity();
		BeanUtils.copyProperties(event, paymentEntity);
		paymentRepository.save(paymentEntity);
	}
}
