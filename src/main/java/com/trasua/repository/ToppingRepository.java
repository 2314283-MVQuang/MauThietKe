package com.trasua.repository;

import com.trasua.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findAllByOrderByNameAsc();

    List<Topping> findByAvailableTrueOrderByNameAsc();
}
