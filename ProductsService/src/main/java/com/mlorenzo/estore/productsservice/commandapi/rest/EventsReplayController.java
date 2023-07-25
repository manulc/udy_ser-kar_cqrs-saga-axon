package com.mlorenzo.estore.productsservice.commandapi.rest;

import java.util.Optional;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

// Nota: La recreación de eventos sólo funciona en los procesadores de eventos de tipo Tracking.
// Nota: La recreación de eventos no funciona en los componentes SAGA.

@AllArgsConstructor
@RestController
@RequestMapping("/management")
public class EventsReplayController {
	private final EventProcessingConfiguration eventProcessingConfiguration;

	@PostMapping("/event-processor/{processorName}/reset")
	public ResponseEntity<String> replayEvents(@PathVariable String processorName) {
		Optional<TrackingEventProcessor> optionalProcessor = eventProcessingConfiguration.eventProcessor(processorName, TrackingEventProcessor.class);
		if(optionalProcessor.isPresent()) {
			TrackingEventProcessor trackingEventProcessor = optionalProcessor.get();
			// Antes de recrear los eventos asociados a un determinado procesador de eventos de tipo Tracking, tenemos que parar dicho procesador de eventos y reiniciar sus tokens
			trackingEventProcessor.shutDown();
			trackingEventProcessor.resetTokens();
			// Iniciamos la recreación de eventos
			trackingEventProcessor.start();
			return ResponseEntity.ok(String.format("The event processor with name [%s] has been reset", processorName));
		}
		return new ResponseEntity<>(String.format("The event processor with name [%s] is not exists", processorName), HttpStatus.NOT_FOUND);
	}
}
