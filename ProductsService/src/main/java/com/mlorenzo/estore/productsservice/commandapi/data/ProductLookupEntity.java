package com.mlorenzo.estore.productsservice.commandapi.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Una aplicación basada en CQRS y Event Sourcing trae consigo un problema llamado Set Based Consistency Validation que, de forma resumida, consiste en ¿cómo validar datos desde el lado del API Command teniendo la información(base de datos de lectura) en el lado del API
// Query?. Desde el lado del API Commando no podemos realizar una consulta al API Query para realizar la validación porque en una aplicación basada en CQRS y Event Sourcing la sincronización entre ámbos lados(API Commando y API Query) toma algún tiempo y, por lo tanto,
// la consistencia de datos es eventual y no inmediata. Para solucionar este problema, creamos una pequeña tabla de consultas(En este caso y a modo de ejemplo, estamos usando la misma base de datos para esta tabla y la tabla de lecturas del API Query, pero lo suyo sería
// que estuvieran en bases de datos distintas) en el lado del API Command que contenga únicamente los datos que se van a validar.

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "productslookup")
public class ProductLookupEntity {
	
	@Id
	private String productId;
	
	@Column(unique = true)
	private String title;
}
