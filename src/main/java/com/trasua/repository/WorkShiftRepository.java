package com.trasua.repository;

import com.trasua.entity.User;
import com.trasua.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {

    /** Các ca của 1 nhân viên bắt đầu trong khoảng [from, to). */
    List<WorkShift> findByStaffAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInAsc(
            User staff, LocalDateTime from, LocalDateTime to);

    /** Ca đang mở (chưa kết ca) của nhân viên. */
    Optional<WorkShift> findFirstByStaffAndCheckOutIsNullOrderByCheckInDesc(User staff);

    boolean existsByStaff(User staff);
}
