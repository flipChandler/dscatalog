package com.devsuperior.dscatalog.tests.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repository.CategoryRepository;
import com.devsuperior.dscatalog.services.CategoryService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.CategoryFactory;

@ExtendWith(SpringExtension.class)
public class CategoryServiceTests {

	@InjectMocks
	private CategoryService categoryService;

	@Mock
	private CategoryRepository categoryRepository;

	private long existingId;
	private long nonExistingId;
	private CategoryDTO categoryDTO;
	private PageImpl<Category> page;
	private Category category;
	private long dependentId;

	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;
		this.startCategory();
	}
	
	@Test
	public void findAllPaged_ShouldReturnReturnPage_whenPage0Size10() {
		when(categoryRepository.findAllPaged(any())).thenReturn(page);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		
		Page<CategoryDTO> result = categoryService.findAllPaged(pageRequest);
		
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(0, result.getNumber()); 											// a página é realmente a 0?
		assertEquals(1, result.getSize());												// o tamanho da página é 1?
		assertEquals(1, result.getTotalElements());
		verify(categoryRepository, times(1)).findAllPaged(any());
	}
	
	@Test
	public void findById_ShouldReturnCategoryDTO_whenIdExists() {
		when(categoryRepository.findById(existingId)).thenReturn(Optional.of(category));

		CategoryDTO result = categoryService.findById(existingId);

		assertNotNull(result);
		assertEquals(CategoryDTO.class, result.getClass());
		assertEquals(category.getId(), result.getId());
		assertEquals(category.getName(), result.getName());
		verify(categoryRepository, times(1)).findById(any());
	}
	
	@Test
	public void findById_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		when(categoryRepository.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

		assertThrows(ResourceNotFoundException.class, () -> {
			categoryService.findById(nonExistingId);
		});
		verify(categoryRepository, times(1)).findById(any());
	}
	
	@Test
	public void insert_ShouldReturnCategoryDTO() {
		when(categoryRepository.save(any())).thenReturn(category);
		when(categoryRepository.getOne(any())).thenReturn(category);
		
		CategoryDTO response = categoryService.insert(categoryDTO);
		
		assertEquals(categoryDTO.getClass(), response.getClass());
		assertEquals(categoryDTO.getName(), response.getName());
		verify(categoryRepository, times(1)).save(any());
	}
	
	@Test
	public void update_ShouldReturnCategoryDTOInstance_whenIdExists() {
		when(categoryRepository.getOne(existingId)).thenReturn(category);
		when(categoryRepository.save(any())).thenReturn(category);

		CategoryDTO dto = new CategoryDTO();
		CategoryDTO response = categoryService.update(existingId, dto);
		
		assertEquals(CategoryDTO.class, response.getClass());
		assertEquals(category.getId(), response.getId());
		verify(categoryRepository, times(1)).save(any());
	}
	
	@Test
	public void update_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		doThrow(EntityNotFoundException.class).when(categoryRepository).getOne(nonExistingId);
		assertThrows(ResourceNotFoundException.class, () -> {
			categoryService.update(nonExistingId, categoryDTO);
		});		
		verify(categoryRepository, times(0)).save(category);
	}
	
	@Test
	public void delete_ShouldDoNothing_whenIdExists() {
		doNothing().when(categoryRepository).deleteById(existingId);

		assertDoesNotThrow(() -> {
			categoryService.delete(existingId);
		});

		verify(categoryRepository, times(1)).deleteById(existingId);
	}

	@Test
	public void delete_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() {
		doThrow(EmptyResultDataAccessException.class).when(categoryRepository).deleteById(nonExistingId);

		assertThrows(ResourceNotFoundException.class, () -> {
			categoryService.delete(nonExistingId);
		});
		verify(categoryRepository, times(1)).deleteById(nonExistingId);
	}

	@Test
	public void delete_ShouldThrowDatabaseException_whenIdIsDependent() {
		doThrow(DataIntegrityViolationException.class).when(categoryRepository).deleteById(dependentId);

		assertThrows(DatabaseException.class, () -> {
			categoryService.delete(dependentId);
		});
		
		// se uma categoria tiver um produto, não poderá deletar a categoria
		verify(categoryRepository, times(1)).deleteById(dependentId); 
	}
	
	void startCategory() {
		category = CategoryFactory.createCategory();
		categoryDTO = CategoryFactory.createCategoryDTO();
		page = new PageImpl<>(List.of(category));
	}	
}
