package com.devsuperior.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repository.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository repository;
	
	@Transactional(readOnly = true) // readOnly evita o lock no BD | não trava o BD pra fazer essa query
	public Page<CategoryDTO> findAllPaged(PageRequest pageRequest) {
		Page<Category> list = repository.findAll(pageRequest); // Page já é um stream
		
		return list.map(category -> new CategoryDTO(category));
	}
	
	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {		
		Optional<Category> optional = repository.findById(id);
		Category category = optional.orElseThrow(() -> new ResourceNotFoundException("Entity Not Found"));
		return new CategoryDTO(category);
	}
	
	@Transactional
	public CategoryDTO insert(CategoryDTO dto) {
		Category entity = new Category();
		entity.setName(dto.getName());
		entity = repository.save(entity);
		return new CategoryDTO(entity);
	}
	
	@Transactional
	public CategoryDTO update(CategoryDTO dto) {
		try {
			Category entity = repository.getOne(dto.getId()); // getById é lazy loading?
			entity.setName(dto.getName());
			entity = repository.save(entity);
			return new CategoryDTO(entity);
		} catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not Found: " + dto.getId());
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not Found " + id);
		} catch(DataIntegrityViolationException e) {
			throw new DatabaseException("Integerity Violation!");
		}
	}
}
