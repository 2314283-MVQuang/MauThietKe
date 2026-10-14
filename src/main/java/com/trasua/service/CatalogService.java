package com.trasua.service;

import com.trasua.entity.Category;
import com.trasua.entity.Product;
import com.trasua.entity.Topping;
import com.trasua.repository.CategoryRepository;
import com.trasua.repository.ProductRepository;
import com.trasua.repository.ReviewRepository;
import com.trasua.repository.ToppingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Quản lý danh mục, đồ uống và topping. */
@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final ReviewRepository reviewRepository;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository,
                          ToppingRepository toppingRepository, ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.toppingRepository = toppingRepository;
    }

    // ============================ DANH MỤC ============================

    public List<Category> allCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục #" + id));
    }

    @Transactional
    public Category saveCategory(Category form) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        Category category = form.getId() == null ? new Category() : getCategory(form.getId());
        String newName = form.getName().trim();
        if (!newName.equalsIgnoreCase(category.getName()) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new IllegalArgumentException("Danh mục \"" + newName + "\" đã tồn tại");
        }
        category.setName(newName);
        category.setDescription(form.getDescription());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (productRepository.countByCategoryId(id) > 0) {
            throw new IllegalArgumentException("Không thể xoá: danh mục vẫn còn đồ uống");
        }
        categoryRepository.deleteById(id);
    }

    // ============================ ĐỒ UỐNG ============================

    public List<Product> allProducts() {
        return productRepository.findAllByOrderByCategoryNameAscNameAsc();
    }

    public List<Product> menuProducts(Long categoryId, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByAvailableTrueAndNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
        }
        List<Product> products = productRepository.findByAvailableTrueOrderByCategoryNameAscNameAsc();
        if (categoryId != null) {
            products = products.stream().filter(p -> p.getCategory().getId().equals(categoryId)).toList();
        }
        return products;
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đồ uống #" + id));
    }

    @Transactional
    public Product saveProduct(Product form, Long categoryId) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("Tên đồ uống không được để trống");
        }
        requirePositive(form.getPriceM(), "Giá size M");
        requirePositive(form.getPriceL(), "Giá size L");
        if (form.getPriceL().compareTo(form.getPriceM()) < 0) {
            throw new IllegalArgumentException("Giá size L không được nhỏ hơn giá size M");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục");
        }
        Product product = form.getId() == null ? new Product() : getProduct(form.getId());
        product.setName(form.getName().trim());
        product.setDescription(form.getDescription());
        product.setCategory(getCategory(categoryId));
        product.setPriceM(form.getPriceM());
        product.setPriceL(form.getPriceL());
        product.setImageUrl(form.getImageUrl() == null || form.getImageUrl().isBlank() ? null : form.getImageUrl().trim());
        product.setAvailable(form.isAvailable());
        // Giới thiệu món
        product.setSignature(form.isSignature());
        product.setTagline(blankToNull(form.getTagline()));
        product.setStory(blankToNull(form.getStory()));
        product.setIngredients(blankToNull(form.getIngredients()));
        product.setSuggestion(blankToNull(form.getSuggestion()));
        product.setTasteSweet(level(form.getTasteSweet(), "Độ ngọt"));
        product.setTasteStrength(level(form.getTasteStrength(), "Độ đậm"));
        product.setTasteCreamy(level(form.getTasteCreamy(), "Độ béo"));
        return productRepository.save(product);
    }

    @Transactional
    public void toggleProduct(Long id) {
        Product product = getProduct(id);
        product.setAvailable(!product.isAvailable());
    }

    @Transactional
    public void deleteProduct(Long id) {
        // Đơn hàng chỉ lưu "ảnh chụp" tên + giá nên xoá đồ uống không ảnh hưởng lịch sử đơn
        reviewRepository.deleteByProductId(id);   // xoá đánh giá của món trước (khoá ngoại)
        productRepository.deleteById(id);
    }

    // ============================ TOPPING ============================

    public List<Topping> allToppings() {
        return toppingRepository.findAllByOrderByNameAsc();
    }

    public List<Topping> availableToppings() {
        return toppingRepository.findByAvailableTrueOrderByNameAsc();
    }

    public List<Topping> toppingsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        // Giữ đúng thứ tự khách chọn
        return ids.stream().distinct().map(this::getTopping).toList();
    }

    public Topping getTopping(Long id) {
        return toppingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topping #" + id));
    }

    @Transactional
    public Topping saveTopping(Topping form) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("Tên topping không được để trống");
        }
        if (form.getPrice() == null || form.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Giá topping không hợp lệ");
        }
        Topping topping = form.getId() == null ? new Topping() : getTopping(form.getId());
        topping.setName(form.getName().trim());
        topping.setPrice(form.getPrice());
        topping.setAvailable(form.isAvailable());
        return toppingRepository.save(topping);
    }

    @Transactional
    public void deleteTopping(Long id) {
        toppingRepository.deleteById(id);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static int level(int value, String field) {
        if (value < 0 || value > 5) {
            throw new IllegalArgumentException(field + " phải từ 0 đến 5");
        }
        return value;
    }

    public List<Product> signatureProducts() {
        return productRepository.findBySignatureTrueAndAvailableTrueOrderByRatingAvgDesc();
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " phải lớn hơn 0");
        }
    }
}
