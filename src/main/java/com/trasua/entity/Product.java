package com.trasua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Đồ uống trong menu. Có 2 giá: size M (giá gốc) và size L. */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "price_m", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceM;

    @Column(name = "price_l", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceL;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Điểm sao trung bình & số lượt đánh giá (chỉ tính đánh giá đang hiển thị).
     * Lưu sẵn trong bảng để menu hiển thị sao mà không phải tính lại mỗi lần;
     * ReviewService cập nhật 2 cột này mỗi khi có đánh giá thêm / sửa / xoá / ẩn.
     */
    @Column(name = "rating_avg", nullable = false, columnDefinition = "double precision default 0")
    private double ratingAvg;

    @Column(name = "rating_count", nullable = false, columnDefinition = "integer default 0")
    private int ratingCount;

    // ===================== GIỚI THIỆU MÓN =====================

    /** Món signature của quán - hiển thị nổi bật ở trang chủ. */
    @Column(name = "signature", nullable = false, columnDefinition = "boolean default false")
    private boolean signature;

    /** Một câu giới thiệu ngắn, hiện trên thẻ món. */
    @Column(name = "tagline", length = 200)
    private String tagline;

    /** Câu chuyện / cách pha chế, hiện ở trang chi tiết. */
    @Column(name = "story", length = 2000)
    private String story;

    /** Thành phần, cách nhau bởi dấu phẩy. */
    @Column(name = "ingredients", length = 500)
    private String ingredients;

    /** Thang vị 0..5: độ ngọt, độ đậm (trà / cà phê), độ béo. */
    @Column(name = "taste_sweet", nullable = false, columnDefinition = "integer default 0")
    private int tasteSweet;

    @Column(name = "taste_strength", nullable = false, columnDefinition = "integer default 0")
    private int tasteStrength;

    @Column(name = "taste_creamy", nullable = false, columnDefinition = "integer default 0")
    private int tasteCreamy;

    /** Gợi ý cách gọi, ví dụ "50% đường + trân châu đen". */
    @Column(name = "suggestion", length = 255)
    private String suggestion;

    /** false = tạm hết / ẩn khỏi menu. */
    @Column(name = "available", nullable = false)
    private boolean available = true;

    public Product() {
    }

    public Product(String name, String description, Category category,
                   BigDecimal priceM, BigDecimal priceL) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceM = priceM;
        this.priceL = priceL;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getPriceM() { return priceM; }
    public void setPriceM(BigDecimal priceM) { this.priceM = priceM; }
    public BigDecimal getPriceL() { return priceL; }
    public void setPriceL(BigDecimal priceL) { this.priceL = priceL; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public boolean isSignature() { return signature; }
    public void setSignature(boolean signature) { this.signature = signature; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public int getTasteSweet() { return tasteSweet; }
    public void setTasteSweet(int tasteSweet) { this.tasteSweet = tasteSweet; }
    public int getTasteStrength() { return tasteStrength; }
    public void setTasteStrength(int tasteStrength) { this.tasteStrength = tasteStrength; }
    public int getTasteCreamy() { return tasteCreamy; }
    public void setTasteCreamy(int tasteCreamy) { this.tasteCreamy = tasteCreamy; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    /** Danh sách thành phần đã tách (dùng hiển thị dạng thẻ). */
    public java.util.List<String> getIngredientList() {
        if (ingredients == null || ingredients.isBlank()) {
            return java.util.List.of();
        }
        return java.util.Arrays.stream(ingredients.split(","))
                .map(String::trim).filter(x -> !x.isEmpty()).toList();
    }

    /**
     * Tông màu của món (theo nguyên liệu chính) - giao diện dùng làm màu nền / điểm nhấn:
     * berry (dâu, vải), peach (đào, cam), taro (khoai môn), leaf (matcha), honey (còn lại).
     */
    public String getTone() {
        String n = name == null ? "" : name.toLowerCase();
        if (n.contains("dâu") || n.contains("vải")) return "berry";
        if (n.contains("đào") || n.contains("cam")) return "peach";
        if (n.contains("khoai")) return "taro";
        if (n.contains("matcha")) return "leaf";
        return "honey";
    }

    public boolean hasIntro() {
        return (story != null && !story.isBlank()) || !getIngredientList().isEmpty()
                || tasteSweet + tasteStrength + tasteCreamy > 0;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
