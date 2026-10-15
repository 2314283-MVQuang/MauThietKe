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
    private final com.trasua.repository.FeedbackRepository feedbackRepository;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository,
                          ToppingRepository toppingRepository, ReviewRepository reviewRepository,
                          com.trasua.repository.FeedbackRepository feedbackRepository) {
        this.reviewRepository = reviewRepository;
        this.feedbackRepository = feedbackRepository;
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
        return saveProduct(form, categoryId, null, null);
    }

    /**
     * Lưu đồ uống kèm các SIZE THÊM (ngoài M, L): mỗi size có tên ngắn (S, XL...) và giá riêng.
     * sizeNames / sizePrices là 2 danh sách song song lấy từ form; dòng để trống tên sẽ bỏ qua.
     */
    @Transactional
    public Product saveProduct(Product form, Long categoryId, List<String> sizeNames, List<BigDecimal> sizePrices) {
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
        if (sizeNames != null) {
            applyExtraSizes(product, sizeNames, sizePrices);
        }
        return productRepository.save(product);
    }

    /** Kiểm tra & ghi đè danh sách size thêm của món. */
    private void applyExtraSizes(Product product, List<String> names, List<BigDecimal> prices) {
        java.util.Map<String, BigDecimal> wanted = new java.util.LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i) == null ? "" : names.get(i).trim().toUpperCase();
            BigDecimal price = prices != null && i < prices.size() ? prices.get(i) : null;
            if (name.isEmpty() && price == null) {
                continue;                                   // dòng trống
            }
            if (!name.matches("[A-Z0-9+]{1,5}")) {
                throw new IllegalArgumentException("Tên size \"" + name + "\" không hợp lệ (1-5 ký tự chữ/số, VD: S, XL, XXL)");
            }
            if (name.equals("M") || name.equals("L")) {
                throw new IllegalArgumentException("Size M và L đã có sẵn - chỉnh giá ở ô Giá size M / L");
            }
            if (wanted.containsKey(name)) {
                throw new IllegalArgumentException("Size " + name + " bị nhập trùng");
            }
            requirePositive(price, "Giá size " + name);
            wanted.put(name, price);
        }
        // Cập nhật tại chỗ (orphanRemoval xoá size bị bỏ đi)
        product.getExtraSizes().removeIf(s -> !wanted.containsKey(s.getName()));
        for (var e : wanted.entrySet()) {
            product.getExtraSizes().stream().filter(s -> s.getName().equals(e.getKey())).findFirst()
                    .ifPresentOrElse(s -> s.setPrice(e.getValue()),
                            () -> product.getExtraSizes().add(new com.trasua.entity.ProductSize(product, e.getKey(), e.getValue())));
        }
    }

    @Transactional
    public void toggleProduct(Long id) {
        Product product = getProduct(id);
        product.setAvailable(!product.isAvailable());
    }

    /** Nhân viên đánh dấu món CÒN / HẾT (ghi lại ai đánh dấu, lúc nào). */
    @Transactional
    public Product setProductAvailable(Long id, boolean available, String actor) {
        Product product = getProduct(id);
        product.setAvailable(available);
        product.setStockUpdatedBy(actor);
        product.setStockUpdatedAt(java.time.LocalDateTime.now());
        return product;
    }

    /** Đầu ca: mở bán lại tất cả món đang hết. Trả về số món được mở lại. */
    @Transactional
    public int reopenAllProducts(String actor) {
        int count = 0;
        for (Product p : productRepository.findAll()) {
            if (!p.isAvailable()) {
                setProductAvailable(p.getId(), true, actor);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public Topping setToppingAvailable(Long id, boolean available) {
        Topping topping = getTopping(id);
        topping.setAvailable(available);
        return topping;
    }

    /** Menu cho khách: món còn bán trước, món tạm hết hiện mờ ở cuối mỗi danh mục. */
    public List<Product> menuProductsWithSoldOut() {
        return productRepository.findAllByOrderByCategoryNameAscNameAsc().stream()
                .sorted(java.util.Comparator.comparing((Product p) -> p.getCategory().getName())
                        .thenComparing(p -> !p.isAvailable()))
                .toList();
    }

    @Transactional
    public void deleteProduct(Long id) {
        // Đơn hàng chỉ lưu "ảnh chụp" tên + giá nên xoá đồ uống không ảnh hưởng lịch sử đơn
        reviewRepository.deleteByProductId(id);   // xoá đánh giá của món trước (khoá ngoại)
        feedbackRepository.detachProduct(id);     // góp ý của món giữ lại, chuyển thành góp ý chung
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
        // Giữ đúng thứ tự khách chọn; một id xuất hiện 2 lần = 2 phần topping đó
        java.util.Map<Long, Topping> cache = new java.util.HashMap<>();
        return ids.stream().map(id -> cache.computeIfAbsent(id, this::getTopping)).toList();
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
