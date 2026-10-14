package com.trasua.repository;

import com.trasua.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndVisibleTrueOrderByCreatedAtDesc(Long productId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    List<Review> findByUserId(Long userId);

    List<Review> findAllByOrderByCreatedAtDesc();

    void deleteByProductId(Long productId);

    /** Nhận xét mới nhất có nội dung, từ minRating sao trở lên - cho mục "Khách nói gì" ở trang chủ. */
    List<Review> findTop3ByVisibleTrueAndRatingGreaterThanEqualAndCommentIsNotNullOrderByCreatedAtDesc(int minRating);

    /** [số sao, số lượt] của các đánh giá đang hiển thị - để vẽ biểu đồ phân bố sao. */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.visible = true GROUP BY r.rating")
    List<Object[]> countByStars(@Param("productId") Long productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0), COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.visible = true")
    List<Object[]> averageAndCount(@Param("productId") Long productId);
}
