package com.devsuperior.dscatalog.tests.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;

@DataJpaTest							// teste de repository precisa somente dessa annotation
public class ProductRepositoryTests {
	
	@Autowired
	private ProductRepository productRepository;	
	private long existingId;
	private long nonExistingId;
	
	@BeforeEach
	public void setup() throws Exception {
		existingId = 1L;
		nonExistingId = 1000;
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
