package com.mlorenzo.estore.productsservice.queryapi;

import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mlorenzo.estore.productsservice.queryapi.data.ProductRepository;
import com.mlorenzo.estore.productsservice.queryapi.rest.ProductRestModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ProductsQueryHandler {
	private final ProductRepository productRepository;
	
	// Nota sobre los manejadores de Queries.
	// Estos manejadores consumen Queries, en función de su tipo, que son despachados o publicados en el Query Bus por el Query Gateway.
	
	// Anotación para que este método sea un manejador de Queries(En este caso, de tipo FindProductsQuery)
	@QueryHandler
	public List<ProductRestModel> findProducts(FindProductsQuery findProductsQuery) {
		return productRepository.findAll().stream()
				.map(productEntity -> {
					ProductRestModel productRestModel = new ProductRestModel();
					BeanUtils.copyProperties(productEntity, productRestModel);
					return productRestModel;
				})
				.collect(Collectors.toList());
	}
}
