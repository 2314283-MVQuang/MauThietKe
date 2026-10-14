package com.trasua.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Ảnh vượt quá giới hạn upload bị từ chối TRƯỚC khi vào controller, nên cần
 * một ControllerAdvice không giới hạn theo controller để bắt lỗi này.
 */
@ControllerAdvice
public class UploadErrorAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleTooLarge(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Ảnh quá lớn (tối đa 10MB)");
        return "redirect:/admin/products";
    }
}
