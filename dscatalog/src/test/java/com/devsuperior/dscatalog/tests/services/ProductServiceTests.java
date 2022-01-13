package com.devsuperior.dscatalog.tests.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.ProductFactory;

@ExtendWith(SpringExtension.class)
// @Transactional								// faz o rollback do banco em cada test 
public class ProductServiceTests {

	@InjectMocks
	private ProductService productService;

	@Mock // não carrega o contexto da aplicação | MockBean carrega o contexto
	private ProductRepository productRepository;

	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private Product product, product2;
	private PageImpl<Product> page;


	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;		
		this.startProduct();

		when(productRepository.save(any())).thenReturn(product);
	}
	
	@Test
	public void findAllPagedShouldReturnPage_whenPage0Size10() {
		PageRequest pageRequest = PageRequest.of(0, 10);
		// TODO: fix logic test
		when(productRepository.findProductsWithCategories(any(), anyString(), (Pageable)any())).thenReturn(page);

		Page<ProductDTO> response = productService.findAllPaged(0L, "", pageRequest);

		assertFalse(response.isEmpty()); 													// esse resultado tem que ser falso
		assertEquals(0, response.getNumber()); 												// a página é realmente a 0?
		assertEquals(3, response.getSize());												// o tamanho da página é 10?
		assertEquals(3, response.getTotalElements());										// tem 1 produto dentro do Mockito
	}

	@Test
	public void findByIdShouldReturnProductDTOInstance_whenIdExists() {
		when(productRepository.findById(existingId)).thenReturn(Optional.of(product));

		ProductDTO response = productService.findById(existingId);

		assertNotNull(response);
		assertEquals(ProductDTO.class, response.getClass());
		assertEquals(1L, response.getId());
		assertEquals("Phone", response.getName());
	}

	@Test
	public void findByIdShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		when(productRepository.findById(any())).thenThrow(new ResourceNotFoundException("Entity Not Found"));

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.findById(existingId);
		});
	}
	
	@Test
	public void updateShouldReturnProductDTOInstance_whenIdExists() {
		// TODO: update return a ProductDTO
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		// TODO: update throw exception
	}

	@Test
	public void deleteShouldDoNothing_whenIdExists() {
		doNothing().when(productRepository).deleteById(existingId);

		assertDoesNotThrow(() -> {
			productService.delete(existingId);
		});

		verify(productRepository, times(1)).deleteById(existingId);
	}

	@Test
	public void deleteShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.delete(nonExistingId);
		});

		verify(productRepository, times(1)).deleteById(nonExistingId);
	}

	@Test
	public void deleteShouldThrowDatabaseException_whenIdIsDependent() {
		doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

		assertThrows(DatabaseException.class, () -> {
			productService.delete(dependentId);
		});

		verify(productRepository, times(1)).deleteById(dependentId); // se um produto tiver um pedido no futuro, não
																		// poderá deletar um produto
	}
	
	void startProduct() {
		product = ProductFactory.createProduct();
		product2 = ProductFactory.createProduct();
		page = new PageImpl<>(List.of(product, product2, new Product()));
	}
}
