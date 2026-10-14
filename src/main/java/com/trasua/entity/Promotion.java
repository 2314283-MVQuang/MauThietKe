package com.trasua.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mã khuyến mãi do quản lý tạo.
 * Trường {@code type} quyết định chiến lược giảm giá (Strategy Pattern) được dùng.
 */
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PromotionType type = PromotionType.PHAN_TRAM;

    /** PHAN_TRAM: số % ; SO_TIEN: số tiền giảm ; MUA_X_TANG_1: không dùng. */
    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    /** Giảm tối đa (chỉ cho loại %). Null = không giới hạn. */
    @Column(name = "max_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    /** Giá trị đơn tối thiểu để áp dụng. */
    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /** Số ly phải mua (cho loại MUA_X_TANG_1). */
    @Column(name = "buy_quantity")
    private Integer buyQuantity;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "start_date")
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "end_date")
    private LocalDate endDate;

    public Promotion() {
    }

    /** Mã còn hiệu lực vào ngày hôm nay? */
    public boolean isUsableToday() {
        LocalDate today = LocalDate.now();
        return active
                && (startDate == null || !today.isBefore(startDate))
                && (endDate == null || !today.isAfter(endDate));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PromotionType getType() { return type; }
    public void setType(PromotionType type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public Integer getBuyQuantity() { return buyQuantity; }
    public void setBuyQuantity(Integer buyQuantity) { this.buyQuantity = buyQuantity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
