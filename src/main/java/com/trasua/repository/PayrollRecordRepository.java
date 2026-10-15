package com.trasua.repository;

import com.trasua.entity.PayrollRecord;
import com.trasua.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {

    Optional<PayrollRecord> findByStaffAndPayrollMonth(User staff, String payrollMonth);

    List<PayrollRecord> findByPayrollMonth(String payrollMonth);
}
