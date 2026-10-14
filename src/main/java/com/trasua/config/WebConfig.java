package com.trasua.config;

import com.trasua.service.ImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Phục vụ ảnh món đã upload: /uploads/** -> thư mục uploads trên ổ đĩa. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImageStorageService imageStorageService;

    public WebConfig(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(imageStorageService.getRootDir().toUri().toString());
    }
}
