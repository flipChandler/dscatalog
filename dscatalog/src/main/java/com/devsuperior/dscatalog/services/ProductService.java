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
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.CategoryRepository;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true) 													// readOnly evita o lock no BD | não trava o BD pra fazer essa query
	public Page<ProductDTO> findAllPaged(Long categoryId, PageRequest pageRequest) {
		Category category = (categoryId == 0) ? null : categoryRepository.getOne(categoryId);
		Page<Product> list = repository.findProductsWithCategories(category, pageRequest); 							// Page já é um stream
		
		return list.map(product -> new ProductDTO(product, product.getCategories()));
	}
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {		
		Optional<Product> optional = repository.findById(id);
		Product product = optional.orElseThrow(() -> new ResourceNotFoundException("Entity Not Found"));
		return new ProductDTO(product, product.getCategories());
	}
	
	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}	

	@Transactional
	public ProductDTO update(ProductDTO dto) {
		try {
			Product entity = repository.getOne(dto.getId()); 			// getById é lazy loading?
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
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
			throw new DatabaseException("Integrity Violation!");
		}
	}
	
	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();   // limpar qlq category que possa ter aqui
		
		for (CategoryDTO categoryDTO : dto.getCategories()) {
			Category category = categoryRepository.getOne(categoryDTO.getId());
			entity.getCategories().add(category);
		}
	}
}
