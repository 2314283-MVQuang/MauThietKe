package com.trasua.repository;

import com.trasua.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCategoryNameAscNameAsc();

    List<Product> findByAvailableTrueOrderByCategoryNameAscNameAsc();

    List<Product> findByAvailableTrueAndNameContainingIgnoreCaseOrderByNameAsc(String keyword);

    long countByCategoryId(Long categoryId);

    List<Product> findBySignatureTrueAndAvailableTrueOrderByRatingAvgDesc();
}
