package com.trasua.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Nâng cấp CSDL cũ khi khởi động:
 * bảng order_items cũ có ràng buộc CHECK chỉ cho size 'M','L' -> bỏ đi để lưu được size quản lý tự thêm (S, XL...).
 */
@Component
public class SchemaUpgrade {

    private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade.class);

    private final JdbcTemplate jdbc;

    public SchemaUpgrade(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void upgrade() {
        try {
            jdbc.execute("ALTER TABLE order_items DROP CONSTRAINT IF EXISTS order_items_size_check");
        } catch (RuntimeException e) {
            log.warn("Không bỏ được ràng buộc size cũ: {}", e.getMessage());
        }
    }
}
