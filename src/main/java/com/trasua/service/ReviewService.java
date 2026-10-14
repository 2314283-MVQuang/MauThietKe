package com.trasua.service;

import com.trasua.entity.OrderStatus;
import com.trasua.entity.Product;
import com.trasua.entity.Review;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.repository.OrderRepository;
import com.trasua.repository.ProductRepository;
import com.trasua.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Nghiệp vụ đánh giá sao & nhận xét món.
 * Quy tắc:
 *   - Chỉ KHÁCH HÀNG đã nhận một đơn HOÀN THÀNH có món đó mới được đánh giá.
 *   - Mỗi khách 1 đánh giá / món; gửi lại = sửa đánh giá cũ.
 *   - Quản lý có thể ẩn / xoá đánh giá; đánh giá ẩn không tính vào điểm trung bình.
 */
@Service
public class ReviewService {

    public static final int MAX_COMMENT = 1000;

    /** Một dòng trong biểu đồ phân bố sao: 5★ - 12 lượt - 60%. */
    public record StarBar(int stars, long count, int percent) {
    }

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // ============================ ĐỌC ============================

    public List<Review> visibleReviews(Long productId) {
        return reviewRepository.findByProductIdAndVisibleTrueOrderByCreatedAtDesc(productId);
    }

    public List<Review> highlightReviews() {
        return reviewRepository.findTop3ByVisibleTrueAndRatingGreaterThanEqualAndCommentIsNotNullOrderByCreatedAtDesc(4);
    }

    public List<Review> allReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Review> findMine(Long productId, User user) {
        if (user == null) {
            return Optional.empty();
        }
        return reviewRepository.findByProductIdAndUserId(productId, user.getId());
    }

    /** Id các món mà khách này đã đánh giá (để trang đơn hàng hiện "Sửa đánh giá"). */
    public Set<Long> reviewedProductIds(User user) {
        return reviewRepository.findByUserId(user.getId()).stream()
                .map(r -> r.getProduct().getId())
                .collect(Collectors.toSet());
    }

    /** Khách có quyền đánh giá món này không? */
    public boolean canReview(User user, Long productId) {
        return user != null
                && user.getRole() == Role.CUSTOMER
                && orderRepository.hasPurchased(user.getId(), productId, OrderStatus.HOAN_THANH);
    }

    /** Phân bố số lượt theo từng mức sao 5 → 1. */
    public List<StarBar> starBars(Long productId) {
        long[] counts = new long[6];
        long total = 0;
        for (Object[] row : reviewRepository.countByStars(productId)) {
            int stars = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            if (stars >= 1 && stars <= 5) {
                counts[stars] = count;
                total += count;
            }
        }
        List<StarBar> bars = new ArrayList<>();
        for (int s = 5; s >= 1; s--) {
            int percent = total == 0 ? 0 : (int) Math.round(counts[s] * 100.0 / total);
            bars.add(new StarBar(s, counts[s], percent));
        }
        return bars;
    }

    // ============================ GHI ============================

    /** Thêm mới hoặc sửa đánh giá của khách cho một món. */
    @Transactional
    public Review saveReview(User user, Long productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Vui lòng chọn từ 1 đến 5 sao");
        }
        String text = (comment == null || comment.isBlank()) ? null : comment.trim();
        if (text != null && text.length() > MAX_COMMENT) {
            throw new IllegalArgumentException("Nhận xét tối đa " + MAX_COMMENT + " ký tự");
        }
        if (!canReview(user, productId)) {
            throw new IllegalArgumentException("Bạn chỉ có thể đánh giá món đã mua (đơn hàng đã hoàn thành)");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đồ uống #" + productId));

        Review review = reviewRepository.findByProductIdAndUserId(productId, user.getId())
                .orElseGet(() -> new Review(product, user, rating, text));
        review.setRating(rating);
        review.setComment(text);
        reviewRepository.save(review);
        recalculate(product);
        return review;
    }

    @Transactional
    public void deleteMine(User user, Long productId) {
        reviewRepository.findByProductIdAndUserId(productId, user.getId()).ifPresent(review -> {
            Product product = review.getProduct();
            reviewRepository.delete(review);
            reviewRepository.flush();
            recalculate(product);
        });
    }

    /** Quản lý: ẩn / hiện một đánh giá. */
    @Transactional
    public void toggleVisible(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá #" + reviewId));
        review.setVisible(!review.isVisible());
        reviewRepository.flush();
        recalculate(review.getProduct());
    }

    /** Quản lý: xoá hẳn một đánh giá. */
    @Transactional
    public void deleteByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá #" + reviewId));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        reviewRepository.flush();
        recalculate(product);
    }

    /** Tính lại điểm trung bình & số lượt (chỉ đánh giá đang hiển thị) rồi lưu vào món. */
    @Transactional
    public void recalculate(Product product) {
        reviewRepository.flush();
        Object[] row = reviewRepository.averageAndCount(product.getId()).get(0);
        double avg = row[0] == null ? 0 : ((Number) row[0]).doubleValue();
        int count = row[1] == null ? 0 : ((Number) row[1]).intValue();
        product.setRatingAvg(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
        product.setRatingCount(count);
        productRepository.save(product);
    }
}
