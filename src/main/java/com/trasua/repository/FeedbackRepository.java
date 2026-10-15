package com.trasua.repository;

import com.trasua.entity.Feedback;
import com.trasua.entity.FeedbackStatus;
import com.trasua.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByCustomerOrderByCreatedAtDesc(User customer);

    List<Feedback> findAllByOrderByCreatedAtDesc();

    List<Feedback> findTop6ByStatusOrderByRepliedAtDesc(FeedbackStatus status);

    long countByStatus(FeedbackStatus status);

    /** Xoá món -> giữ lại góp ý nhưng bỏ liên kết món (thành góp ý chung). */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Feedback f SET f.product = null WHERE f.product.id = :productId")
    void detachProduct(@org.springframework.data.repository.query.Param("productId") Long productId);
}
