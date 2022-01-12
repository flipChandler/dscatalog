package com.devsuperior.dscatalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
	
	@Query("SELECT DISTINCT p FROM Product p INNER JOIN p.categories c WHERE "
			+ "(:category IS NULL OR :category IN c) AND "
			+ "(LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) )")									// SE NULL, findAll
	Page<Product> findProductsWithCategories(Category category, String name, Pageable pageable); 

}
