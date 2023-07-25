package com.mlorenzo.estore.productsservice.commandapi.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLookupRespository extends JpaRepository<ProductLookupEntity, String> {
	Optional<ProductLookupEntity> findByProductIdOrTitle(String productId, String title);
}
