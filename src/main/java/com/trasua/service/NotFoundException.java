package com.trasua.service;

/** Ném ra khi không tìm thấy dữ liệu theo id. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
