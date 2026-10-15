package com.trasua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Một dòng trong đơn hàng (một loại ly với cấu hình size/đường/đá/topping).
 * Giá {@code unitPrice} và mô tả {@code description} được tính bởi chuỗi
 * Decorator (xem package pattern.decorator) và tạo qua OrderItemBuilder.
 * Lưu "ảnh chụp" tên và giá tại thời điểm đặt để sau này đổi giá menu
 * cũng không làm sai lịch sử đơn.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    /** Mã size: M, L hoặc size thêm của món (S, XL...). */
    @Column(name = "size", nullable = false, length = 5)
    private String size;

    @Column(name = "sugar_level", nullable = false)
    private int sugarLevel;

    @Column(name = "ice_level", nullable = false)
    private int iceLevel;

    /** Tên các topping, cách nhau bởi dấu phẩy. */
    @Column(name = "toppings", length = 500)
    private String toppings;

    /** Mô tả đầy đủ do Decorator sinh ra, ví dụ "Trà sữa (Size L) + Trân châu đen". */
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "note", length = 255)
    private String note;

    public OrderItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public void setSize(DrinkSize size) { this.size = size == null ? null : size.name(); }
    public int getSugarLevel() { return sugarLevel; }
    public void setSugarLevel(int sugarLevel) { this.sugarLevel = sugarLevel; }
    public int getIceLevel() { return iceLevel; }
    public void setIceLevel(int iceLevel) { this.iceLevel = iceLevel; }
    public String getToppings() { return toppings; }
    public void setToppings(String toppings) { this.toppings = toppings; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
