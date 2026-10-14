package com.trasua.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Đánh giá sao (1-5) và nhận xét của khách hàng cho một đồ uống.
 * Mỗi khách chỉ có 1 đánh giá cho mỗi món (sửa lại thì ghi đè).
 */
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Số sao 1..5. */
    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", length = 1000)
    private String comment;

    /** Quản lý có thể ẩn nhận xét không phù hợp; nhận xét ẩn không tính vào điểm trung bình. */
    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Review() {
    }

    public Review(Product product, User user, int rating, String comment) {
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
    }

    /** Đã sửa sau khi đăng? (dùng để hiện chữ "đã chỉnh sửa"). */
    public boolean isEdited() {
        return updatedAt != null && createdAt != null && updatedAt.isAfter(createdAt.plusSeconds(1));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
