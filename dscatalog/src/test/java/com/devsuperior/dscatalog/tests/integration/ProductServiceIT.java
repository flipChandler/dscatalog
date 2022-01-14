package com.devsuperior.dscatalog.tests.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@Transactional													// rollback no banco a cada teste
public class ProductServiceIT {									// teste de integração

	@Autowired
	private ProductService productService;
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;
	private long countPCGamerProducts;
	private PageRequest pageRequest;
	
	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
		countPCGamerProducts = 21L; 	// data.sql tem 21 PC GAMER
		pageRequest = PageRequest.of(0, 10);
	}
	
	@Test
	public void deleteShouldDoNothing_whenIdExists() {
		assertDoesNotThrow(() -> {
			productService.delete(existingId);
		});
	}

	@Test
	public void deleteShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> {
			productService.delete(nonExistingId);
		});
	}
	
	@Test
	public void findAllPagedShouldReturnNothing_whenNameDoesNotExist() {
		String name = "Camera";
		
		Page<ProductDTO> result = productService.findAllPaged(0L, name, pageRequest);
		
		assertTrue(result.isEmpty());
	}	
	
	@Test
	public void findAllPagedShouldReturnProducts_whenNameIsEmpty() {
		String name = "";
		
		Page<ProductDTO> result = productService.findAllPaged(0L, name, pageRequest);
		
		assertFalse(result.isEmpty());
		assertEquals(countTotalProducts, result.getTotalElements());
	}
	
	@Test
	public void findAllPagedShouldReturnProducts_whenNameExistsIgnoringCase() {
		String name = "pc gAMer";
		
		Page<ProductDTO> result = productService.findAllPaged(0L, name, pageRequest);
		
		assertFalse(result.isEmpty());
		assertEquals(countPCGamerProducts, result.getTotalElements());
	}
}
