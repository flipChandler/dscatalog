package com.devsuperior.dscatalog.tests.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.ProductService;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
	
	@InjectMocks
	private ProductService productService;
	
	@Mock // não carrega o contexto da aplicação | MockBean carrega o contexto
	private ProductRepository productRepository;
	
	private long existingId;
	private long nonExistingId;
	
	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;

		doNothing().when(productRepository).deleteById(existingId);
		doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);
	}
	
	@Test
	public void deleteShouldDoNothing_whenIdExists() {
		
		assertDoesNotThrow(() -> {
			productService.delete(existingId);
		});
		
		verify(productRepository, times(1)).deleteById(existingId);
	}
}
