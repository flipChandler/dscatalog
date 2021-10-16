package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repository.CategoryRepository;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository repository;
	
	@Transactional(readOnly = true) // readOnly evita o lock no BD | não trava o BD pra fazer essa query
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		
		return list.stream()
				.map(category -> new CategoryDTO(category))
				.collect(Collectors.toList());
	}
}
