package com.trasua.repository;

import com.trasua.entity.DailyClosing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyClosingRepository extends JpaRepository<DailyClosing, Long> {

    Optional<DailyClosing> findByBusinessDate(LocalDate businessDate);

    List<DailyClosing> findByBusinessDateBetweenOrderByBusinessDateDesc(LocalDate from, LocalDate to);
}
