package com.devsuperior.dscatalog.tests.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
	
	@InjectMocks
	private ProductService productService;
	
	@Mock // não carrega o contexto da aplicação | MockBean carrega o contexto
	private ProductRepository productRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	
	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;

		doNothing().when(productRepository).deleteById(existingId);
		doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);
		doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);
	}
	
	@Test
	public void deleteShouldDoNothing_whenIdExists() {
		
		assertDoesNotThrow(() -> {
			productService.delete(existingId);
		});
		
		verify(productRepository, times(1)).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		
		assertThrows(ResourceNotFoundException.class, () -> {
			productService.delete(nonExistingId);
		});
		
		verify(productRepository, times(1)).deleteById(nonExistingId);
	}
	
	@Test
	public void deleteShouldThrowDatabaseException_whenIdDoesNotExist() {
		
		assertThrows(DatabaseException.class, () -> {
			productService.delete(dependentId);
		});
		
		verify(productRepository, times(1)).deleteById(dependentId);			// se um produto tiver um pedido no futuro, não poderá deletar um produto
	}
}
