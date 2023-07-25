package com.mlorenzo.estore.productsservice.queryapi.rest;

import java.util.List;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlorenzo.estore.productsservice.queryapi.FindProductsQuery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("products")
public class ProductsQueryController {
	private final QueryGateway queryGateway;
	private final Environment env;
	
	@GetMapping("status/check")
	public String getProduct() {
		return "Working on port: " + env.getProperty("local.server.port");
	}
	
	@GetMapping
	public List<ProductRestModel> getProducts() {
		FindProductsQuery findProductsQuery = new FindProductsQuery();
		// Método que hace que el Query Gateway despache o envíe una query(En este caso de tipo FindProductsQuery) al Query Bus
		List<ProductRestModel> products = queryGateway
				.query(findProductsQuery, ResponseTypes.multipleInstancesOf(ProductRestModel.class))
				.join();
		return products;
	}
}
