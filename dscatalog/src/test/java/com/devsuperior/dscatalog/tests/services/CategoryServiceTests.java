package com.devsuperior.dscatalog.tests.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repository.CategoryRepository;
import com.devsuperior.dscatalog.services.CategoryService;
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

	@BeforeEach
	void setup() {
		existingId = 1L;
		nonExistingId = 1000L;
		this.startCategory();
	}
	
	@Test
	public void findAllPaged_ShouldReturnReturnPage_whenPage0Size10() {
		when(categoryRepository.findAllPaged(any())).thenReturn(page);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		
		Page<CategoryDTO> result = categoryService.findAllPaged(pageRequest);
		
		assertNotNull(result);
		assertFalse(result.isEmpty());
		verify(categoryRepository, times(1)).findAllPaged(any());
	}
	
	
	void startCategory() {
		category = CategoryFactory.createCategory();
		categoryDTO = CategoryFactory.createCategoryDTO();
		page = new PageImpl<>(List.of(category));
	}
}
