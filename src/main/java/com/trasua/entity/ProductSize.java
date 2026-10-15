package com.trasua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Size THÊM của một món (ngoài M và L có sẵn), VD size S rẻ hơn hoặc size XL to hơn.
 * Mỗi size có giá riêng; SizeDecorator cộng (hoặc trừ) phần chênh lệch so với giá size M.
 */
@Entity
@Table(name = "product_sizes",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_size_name", columnNames = {"product_id", "name"}))
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Tên size ngắn: S, XL, XXL... (tối đa 5 ký tự). */
    @Column(name = "name", nullable = false, length = 5)
    private String name;

    /** Giá bán của size này (giá trọn ly). */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public ProductSize() {
    }

    public ProductSize(Product product, String name, BigDecimal price) {
        this.product = product;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
