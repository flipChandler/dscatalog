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

import javax.persistence.EntityNotFoundException;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.CategoryRepository;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.ProductFactory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService productService;

	@Mock 											// não carrega o contexto da aplicação | MockBean carrega o contexto
	private ProductRepository productRepository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private Product product, product2;
	private ProductDTO productDTO;
	private PageImpl<Product> page;
	private Category category;

	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;
		category = new Category(1L, "Eletrônicos");
		this.startProduct();
	}
	
	@Test
	public void findAllPaged_ShouldReturnPage_whenPage0Size10() {
		when(productRepository.findProductsWithCategories(any(), anyString(), any())).thenReturn(page);
		
		Long categoryId = 0L;
		String name = "";
		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<ProductDTO> result = productService.findAllPaged(categoryId, name, pageRequest);
		
		assertNotNull(result);
		assertFalse(result.isEmpty()); 													// esse resultado tem que ser falso
		assertEquals(0, result.getNumber()); 												// a página é realmente a 0?
		assertEquals(3, result.getSize());												// o tamanho da página é 10?
		assertEquals(3, result.getTotalElements());										// tem 1 produto dentro do Mockito
		verify(productRepository, times(1)).findProductsWithCategories(null, name, pageRequest);
	}

	@Test
	public void findById_ShouldReturnProductDTO_whenIdExists() {
		when(productRepository.findById(existingId)).thenReturn(Optional.of(product));

		ProductDTO result = productService.findById(existingId);

		assertNotNull(result);
		assertEquals(ProductDTO.class, result.getClass());
		assertEquals(product.getId(), result.getId());
		assertEquals(product.getName(), result.getName());
		verify(productRepository, times(1)).findById(any());
	}

	@Test
	public void findById_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		when(productRepository.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.findById(nonExistingId);
		});
		verify(productRepository, times(1)).findById(any());
	}
	
	@Test
	public void insert_ShouldReturnProductDTO() {
		when(productRepository.save(any())).thenReturn(product);
		when(categoryRepository.getOne(any())).thenReturn(category);
		
		ProductDTO response = productService.insert(productDTO);
		
		assertEquals(productDTO.getDescription(), response.getDescription());
		assertEquals(productDTO.getClass(), response.getClass());
		assertEquals(productDTO.getPrice(), response.getPrice());
		verify(productRepository, times(1)).save(any());
	}	
	
	@Test
	public void update_ShouldReturnProductDTOInstance_whenIdExists() {
		when(productRepository.getOne(existingId)).thenReturn(product);
		when(productRepository.save(any())).thenReturn(product);

		ProductDTO dto = new ProductDTO();
		ProductDTO response = productService.update(existingId, dto);
		
		assertEquals(ProductDTO.class, response.getClass());
		assertEquals(product.getId(), response.getId());
		verify(productRepository, times(1)).save(any());
	}
	
	@Test
	public void update_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		doThrow(EntityNotFoundException.class).when(productRepository).getOne(nonExistingId);
		assertThrows(ResourceNotFoundException.class, () -> {
			productService.update(nonExistingId, productDTO);
		});		
		verify(productRepository, times(0)).save(product);
	}

	@Test
	public void delete_ShouldDoNothing_whenIdExists() {
		doNothing().when(productRepository).deleteById(existingId);

		assertDoesNotThrow(() -> {
			productService.delete(existingId);
		});

		verify(productRepository, times(1)).deleteById(existingId);
	}

	@Test
	public void delete_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.delete(nonExistingId);
		});
		verify(productRepository, times(1)).deleteById(nonExistingId);
	}

	@Test
	public void delete_ShouldThrowDatabaseException_whenIdIsDependent() {
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
		productDTO = ProductFactory.createProductDTO();
		page = new PageImpl<>(List.of(product, product2, new Product()));
	}
}
