package com.devsuperior.dscatalog.tests.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.tests.factory.ProductFactory;

@DataJpaTest							// teste de repository precisa somente dessa annotation
public class ProductRepositoryTests {
	
	@Autowired
	private ProductRepository productRepository;	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;
	
	@BeforeEach
	public void setup() throws Exception {
		existingId = 1L;
		nonExistingId = 1000;
		countTotalProducts = 25L;
	}
	
	@Test
	public void saveShouldPersistWithAutoincrement_whenIdIsNull() {
		Product product = ProductFactory.createProduct();
		product.setId(null);
		
		product = productRepository.save(product);
		Optional<Product> result = productRepository.findById(product.getId());
		
		assertNotNull(product.getId());
		assertEquals(countTotalProducts + 1L, product.getId());
		assertTrue(result.isPresent());
		assertSame(result.get(), product);							// se é o mesmo objeto (cache da JPA não precisou ir ao banco de dados)
	}
	
	@Test
	public void deleteShouldDeleteObject_whenIdExists() {
		
		productRepository.deleteById(existingId);
		
		Optional<Product> result = productRepository.findById(existingId);
		
		assertFalse(result.isPresent());		
	}
	
	@Test
	public void deleteShouldThrowEmptyResultDataAccessException_whenIdDoesNotExist() {
		
		assertThrows(EmptyResultDataAccessException.class, () -> {
			productRepository.deleteById(nonExistingId);
		});		
	}	
}
