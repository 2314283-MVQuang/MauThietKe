package com.trasua.web;

import com.trasua.entity.OrderItem;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Giỏ hàng lưu trong session (mỗi người dùng đăng nhập có một giỏ riêng).
 * Các phần tử là OrderItem đã được dựng bởi OrderItemBuilder (Builder + Decorator).
 */
@Component
@SessionScope
public class Cart {

    private final List<OrderItem> items = new ArrayList<>();
    private String promotionCode;

    /** Thêm món; nếu đã có món giống hệt (cùng mô tả + ghi chú) thì cộng dồn số lượng. */
    public void add(OrderItem newItem) {
        for (OrderItem item : items) {
            if (Objects.equals(item.getDescription(), newItem.getDescription())
                    && Objects.equals(item.getNote(), newItem.getNote())) {
                setQuantity(item, item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        items.add(newItem);
    }

    public void updateQuantity(int index, int quantity) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        if (quantity <= 0) {
            items.remove(index);
        } else {
            setQuantity(items.get(index), Math.min(quantity, 50));
        }
    }

    public void remove(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public void clear() {
        items.clear();
        promotionCode = null;
    }

    private static void setQuantity(OrderItem item, int quantity) {
        item.setQuantity(quantity);
        item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }
}
