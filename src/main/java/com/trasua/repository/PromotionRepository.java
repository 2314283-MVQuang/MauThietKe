package com.trasua.repository;

import com.trasua.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Promotion> findAllByOrderByIdDesc();

    List<Promotion> findByActiveTrueOrderByIdDesc();
}
