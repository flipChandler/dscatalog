package com.devsuperior.dscatalog.tests.factory;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;

public class CategoryFactory {

	public static Category createCategory() {
		Category category = new Category(1L, "Livros");
		return category;
	}

	public static CategoryDTO createCategoryDTO() {
		Category category = createCategory();
		return new CategoryDTO(category);
	}

	public static CategoryDTO createCategoryDTO(Long id) {
		CategoryDTO categoryDTO = createCategoryDTO();
		categoryDTO.setId(id);
		return categoryDTO;
	}
}
