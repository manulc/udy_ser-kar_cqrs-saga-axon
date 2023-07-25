package com.mlorenzo.estore.productsservice.commandapi;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.productsservice.commandapi.data.ProductLookupEntity;
import com.mlorenzo.estore.productsservice.commandapi.data.ProductLookupRespository;

import lombok.RequiredArgsConstructor;

// Clase que intercepta cualquier tipo de Command para realizar alguna lógica de negocio, como por ejemplo, validaciones, logging, etc...
// Este interceptor se registrará en el Command Bus(ver clase principal ProductsServiceApplication) para que se ejecute cada vez que llega un Command a ese bus
// Los Commands son despachados a este bus desde el Command Gateway.
// Por lo tanto, estos interceptores se ejecutarán antes que los Command Handlers(métodos o constructores anotados con @CommandHandler).

@RequiredArgsConstructor
@Component
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);
	
	private final ProductLookupRespository productLookupRespository;
	
	// Este método implementa la interfaz funcional BiFunction que representa una función que recibe 2 tipos de argumentos de entrada y devuelve un tipo de dato.
	// En este caso, la función que representa esta interfaz recibe 2 argumentos de entrada de tipo Integer y CommandMessage<?> y devuelve un dato de tipo CommandMessage<?>
	@Override
	public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
			List<? extends CommandMessage<?>> messages) {
		return (index, command) -> {
			LOGGER.info("Intercepted command: " + command.getPayloadType());
			// Verficamos que el Command interceptado es del tipo que nos interesa, es decir, de tipo CreateProductCommand
			if(CreateProductCommand.class.equals(command.getPayloadType())) {
				CreateProductCommand createProductCommand = (CreateProductCommand)command.getPayload();
				// Si ya existe en la base de datos un producto con el mismo id o con el mismo título, lanzamos la siguiente excepción
				Optional<ProductLookupEntity> oProductLookupEntity = productLookupRespository.findByProductIdOrTitle(createProductCommand.getProductId(),createProductCommand.getTitle());
				if(oProductLookupEntity.isPresent())
					throw new IllegalStateException(String.format("Product with productId %s or title %s already exist", createProductCommand.getProductId(), createProductCommand.getTitle()));
			}
			return command;
		};
	}
}
