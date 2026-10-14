package com.trasua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Topping thêm vào ly: trân châu, thạch, pudding, kem cheese... */
@Entity
@Table(name = "toppings")
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private boolean available = true;

    public Topping() {
    }

    public Topping(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
