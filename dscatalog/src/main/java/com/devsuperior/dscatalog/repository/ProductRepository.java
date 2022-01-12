package com.devsuperior.dscatalog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
	
	@Query("SELECT DISTINCT p FROM Product p INNER JOIN p.categories c WHERE "							// COALESCE, por causa do PostreSQL
			+ "(COALESCE(:categories) IS NULL OR c IN :categories) AND "
			+ "(LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) )")									// SE NULL, findAll
	Page<Product> findProductsWithCategories(List<Category> categories, String name, Pageable pageable); 

}
