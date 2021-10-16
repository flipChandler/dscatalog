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

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Transactional(readOnly = true) // readOnly evita o lock no BD | não trava o BD pra fazer essa query
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = repository.findAll(pageRequest); // Page já é um stream
		
		return list.map(product -> new ProductDTO(product));
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
		// entity.setName(dto.getName());
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}
	
	@Transactional
	public ProductDTO update(ProductDTO dto) {
		try {
			Product entity = repository.getById(dto.getId()); // getById é lazy loading?
			// entity.setName(dto.getName());
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
			throw new DatabaseException("Integerity Violation!");
		}
	}
}
