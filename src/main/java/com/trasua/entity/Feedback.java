package com.trasua.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Góp ý của khách để quán CẢI THIỆN món (khác với đánh giá sao công khai):
 * khách gửi -> quản lý đọc, trả lời, cập nhật tiến trình -> khách xem được phản hồi.
 */
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /** null = góp ý chung cho quán. */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic", nullable = false, length = 20)
    private FeedbackTopic topic = FeedbackTopic.HUONG_VI;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.MOI;

    @Column(name = "admin_reply", length = 1000)
    private String adminReply;

    @Column(name = "replied_by", length = 50)
    private String repliedBy;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** "Nguyễn Thị Lan" -> "Lan" (hiển thị công khai, không lộ họ tên đầy đủ). */
    public String getCustomerShortName() {
        String name = customer.getFullName() == null ? customer.getUsername() : customer.getFullName().trim();
        String[] parts = name.split("\\s+");
        return parts[parts.length - 1];
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public FeedbackTopic getTopic() { return topic; }
    public void setTopic(FeedbackTopic topic) { this.topic = topic; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public String getRepliedBy() { return repliedBy; }
    public void setRepliedBy(String repliedBy) { this.repliedBy = repliedBy; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
