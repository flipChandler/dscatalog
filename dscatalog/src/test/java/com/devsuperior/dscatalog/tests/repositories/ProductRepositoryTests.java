package com.devsuperior.dscatalog.tests.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;

@DataJpaTest							// teste de repository precisa somente dessa annotation
public class ProductRepositoryTests {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Test
	public void deleteShouldDeleteObject_whenIdExists() {
		
		productRepository.deleteById(1L);
		
		Optional<Product> result = productRepository.findById(1L);
		
		assertFalse(result.isPresent());		
	}
	
}
