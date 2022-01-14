package com.devsuperior.dscatalog.tests.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.devsuperior.dscatalog.entities.Category;
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
	private long countPCGamerProducts;
	private PageRequest pageRequest;
	private List<Category> categories = new ArrayList<>();
	private long countCategory1And2;
	
	@BeforeEach
	public void setup() throws Exception {
		existingId = 1L;
		nonExistingId = 1000;
		countTotalProducts = 25L;
		countPCGamerProducts = 21L; 							// data.sql tem 21 PC GAMER
		pageRequest = PageRequest.of(0, 10);
		categories.add(new Category(1L, "Livros"));		 		// 1 produto com essa categoria
		categories.add(new Category(2L, "Eletrônicos")); 		// 2 produtos com essa categoria
		countCategory1And2 = 3L;
	}
	
	@Test
	public void findProductsWithCategories_ShouldReturnProductsOnlySelectedFromInformedCategories() {
		String name = "";
		
		Page<Product> result = productRepository.findProductsWithCategories(categories, name, pageRequest);
		
		assertFalse(result.isEmpty());
		assertEquals(countCategory1And2, result.getTotalElements());
	}
	
	@Test
	public void findProductsWithCategories_ShouldReturnAnEmptyList_whenCategoriesIsIncorrect() {
		String name = "";
		categories.clear();
		categories.add(new Category(4L, "Garden"));
		
		Page<Product> result = productRepository.findProductsWithCategories(categories, name, pageRequest);
		
		assertTrue(result.isEmpty());
		assertEquals(0, result.getTotalElements());
	}
	
	@Test
	public void findProductsWithCategories_ShouldReturnNothing_whenNameDoesNotExist() {
		String name = "Camera";
		
		Page<Product> result = productRepository.findProductsWithCategories(null, name, pageRequest);
		
		assertTrue(result.isEmpty());
	}	
	
	@Test
	public void findProductsWithCategories_ShouldReturnAllProducts_whenNameIsEmpty() {
		String name = "";
		categories = null;
		
		Page<Product> result = productRepository.findProductsWithCategories(categories, name, pageRequest);
		
		assertFalse(result.isEmpty());
		assertEquals(countTotalProducts, result.getTotalElements());
	}
	
	@Test
	public void findProductsWithCategories_ShouldReturnProducts_whenNameExists() {
		String name = "PC Gamer";
		
		Page<Product> result = productRepository.findProductsWithCategories(null, name, pageRequest);
		
		assertFalse(result.isEmpty());
		assertEquals(countPCGamerProducts, result.getTotalElements());
	}
	
	@Test
	public void findProductsWithCategories_ShouldReturnProducts_whenNameExistsIgnoringCase() {
		String name = "pc gAMer";
		
		Page<Product> result = productRepository.findProductsWithCategories(null, name, pageRequest);
		
		assertFalse(result.isEmpty());
		assertEquals(countPCGamerProducts, result.getTotalElements());
	}
	
	@Test
	public void save_ShouldPersistWithAutoincrement_whenIdIsNull() {
		Product product = ProductFactory.createProduct();
		product.setId(null);
		
		product = productRepository.save(product);
		Optional<Product> result = productRepository.findById(product.getId());
		
		assertNotNull(product.getId());
		assertEquals(countTotalProducts + 1L, product.getId());
		assertTrue(result.isPresent());
		assertSame(result.get(), product);								// se é o mesmo objeto (cache da JPA não precisou ir ao banco de dados)
	}
	
	@Test
	public void delete_ShouldDeleteObject_whenIdExists() {
		
		productRepository.deleteById(existingId);
		
		Optional<Product> result = productRepository.findById(existingId);
		
		assertFalse(result.isPresent());		
	}
	
	@Test
	public void delete_ShouldThrowEmptyResultDataAccessException_whenIdDoesNotExist() {
		
		assertThrows(EmptyResultDataAccessException.class, () -> {
			productRepository.deleteById(nonExistingId);
		});		
	}	
}
