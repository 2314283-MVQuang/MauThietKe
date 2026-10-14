package com.trasua.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Lưu ảnh món (tải từ máy hoặc chụp bằng camera) vào thư mục uploads/products
 * và trả về đường dẫn web dạng /uploads/products/xxx.jpg để lưu vào cột image_url.
 */
@Service
public class ImageStorageService {

    public static final String URL_PREFIX = "/uploads/";
    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path rootDir;

    public ImageStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Path getRootDir() {
        return rootDir;
    }

    /** Lưu ảnh món, trả về URL. Ném IllegalArgumentException nếu file không hợp lệ. */
    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn ảnh");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Ảnh quá lớn (tối đa 10MB)");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String ext = ALLOWED.get(contentType);
        if (ext == null) {
            throw new IllegalArgumentException("Chỉ nhận ảnh JPG, PNG, WEBP hoặc GIF");
        }
        try (InputStream in = file.getInputStream()) {
            if (!looksLikeImage(file)) {
                throw new IllegalArgumentException("File không phải ảnh hợp lệ");
            }
            Path dir = rootDir.resolve("products");
            Files.createDirectories(dir);
            String fileName = UUID.randomUUID() + ext;
            Files.copy(in, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return URL_PREFIX + "products/" + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("Không lưu được ảnh: " + e.getMessage());
        }
    }

    /** Xoá ảnh cũ do hệ thống upload (bỏ qua link ảnh bên ngoài). */
    public void deleteIfLocal(String url) {
        if (url == null || !url.startsWith(URL_PREFIX + "products/")) {
            return;
        }
        Path file = rootDir.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!file.startsWith(rootDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // không xoá được thì bỏ qua, không ảnh hưởng nghiệp vụ
        }
    }

    /** Kiểm tra vài byte đầu (magic number) để chặn file giả đuôi ảnh. */
    private static boolean looksLikeImage(MultipartFile file) throws IOException {
        byte[] head = new byte[12];
        int n;
        try (InputStream in = file.getInputStream()) {
            n = in.readNBytes(head, 0, head.length);
        }
        if (n < 4) {
            return false;
        }
        boolean jpg = (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8;
        boolean png = (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G';
        boolean gif = head[0] == 'G' && head[1] == 'I' && head[2] == 'F';
        boolean webp = n >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
        return jpg || png || gif || webp;
    }
}
