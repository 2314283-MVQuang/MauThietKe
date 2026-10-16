package com.trasua.controller;

import com.trasua.entity.OrderItem;
import com.trasua.entity.Product;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.pattern.builder.DrinkDirector;
import com.trasua.pattern.builder.OrderItemBuilder;
import com.trasua.service.CatalogService;
import com.trasua.service.ReviewService;
import com.trasua.service.UserService;
import com.trasua.web.Cart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/** Menu đồ uống, trang tuỳ chỉnh ly nước và thêm vào giỏ. */
@Controller
public class MenuController {

    private final CatalogService catalogService;
    private final Cart cart;
    private final ReviewService reviewService;
    private final UserService userService;

    public MenuController(CatalogService catalogService, Cart cart,
                          ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.catalogService = catalogService;
        this.cart = cart;
    }

    /**
     * Menu dạng bảng (in A4 / tải ảnh PNG). Dữ liệu lấy trực tiếp từ CSDL nên luôn đúng giá.
     * Chia 2 trang: trang 1 = trà sữa + cà phê, trang 2 = trà trái cây + đá xay + topping.
     */
    @GetMapping("/menu-board")
    public String menuBoard(Model model) {
        List<Product> products = catalogService.menuProducts(null, null);
        java.util.Map<com.trasua.entity.Category, List<Product>> byCategory = new java.util.LinkedHashMap<>();
        for (com.trasua.entity.Category c : catalogService.allCategories()) {
            List<Product> items = products.stream().filter(p -> p.getCategory().getId().equals(c.getId())).toList();
            if (!items.isEmpty()) {
                byCategory.put(c, items);
            }
        }
        // Chia danh mục thành 2 trang cho cân: trang trái nhận các danh mục có chữ "sữa" / "cà phê" trước
        List<java.util.Map.Entry<com.trasua.entity.Category, List<Product>>> entries = new java.util.ArrayList<>(byCategory.entrySet());
        entries.sort(java.util.Comparator.comparingInt(e -> {
            String n = e.getKey().getName().toLowerCase();
            return n.contains("sữa") ? 0 : n.contains("cà phê") ? 1 : n.contains("trái cây") ? 2 : 3;
        }));
        int half = (entries.size() + 1) / 2;
        model.addAttribute("pageLeft", entries.subList(0, half));
        model.addAttribute("pageRight", entries.subList(half, entries.size()));
        model.addAttribute("toppings", catalogService.availableToppings());
        // Ảnh tròn mỗi trang lấy từ chính các món trên trang đó (tối đa 3, ưu tiên món signature)
        java.util.function.Function<List<java.util.Map.Entry<com.trasua.entity.Category, List<Product>>>, List<Product>> photosOf =
                page -> page.stream().flatMap(e -> e.getValue().stream())
                        .filter(p -> p.getImageUrl() != null)
                        .sorted(java.util.Comparator.comparing(Product::isSignature).reversed())
                        .limit(3).toList();
        model.addAttribute("pagePhotos", List.of(photosOf.apply(entries.subList(0, half)),
                photosOf.apply(entries.subList(half, entries.size()))));
        return "menu/board";
    }

    @GetMapping("/")
    public String menu(@RequestParam(value = "category", required = false) Long category,
                       @RequestParam(value = "q", required = false) String q,
                       Model model) {
        model.addAttribute("categories", catalogService.allCategories());
        // Luôn trả đủ món: tab danh mục & ô tìm kiếm lọc ngay trên trình duyệt (có hiệu ứng)
        // Món nhân viên đánh dấu "hết" vẫn hiện nhưng mờ + nhãn "Tạm hết"
        List<Product> products = catalogService.menuProductsWithSoldOut();
        model.addAttribute("products", products);
        // ---- Trang chủ: món signature, ảnh hero, dải hương vị, nhận xét nổi bật ----
        List<Product> signatures = catalogService.signatureProducts();
        model.addAttribute("signatures", signatures.stream().limit(3).toList());
        List<Product> heroCups = new java.util.ArrayList<>(signatures.stream().filter(p -> p.getImageUrl() != null).toList());
        catalogService.menuProducts(null, null).stream()
                .filter(p -> p.getImageUrl() != null && !heroCups.contains(p))
                .forEach(heroCups::add);
        model.addAttribute("heroCups", heroCups.stream().limit(3).toList());
        model.addAttribute("flavors", catalogService.menuProducts(null, null).stream().map(Product::getName).toList());
        model.addAttribute("highlightReviews", reviewService.highlightReviews());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("q", q);
        return "menu/index";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id, Principal principal, Model model) {
        model.addAttribute("product", catalogService.getProduct(id));
        // ---- Đánh giá sao & nhận xét ----
        User user = principal == null ? null : userService.getByUsername(principal.getName());
        model.addAttribute("reviews", reviewService.visibleReviews(id));
        model.addAttribute("starBars", reviewService.starBars(id));
        model.addAttribute("myReview", reviewService.findMine(id, user).orElse(null));
        model.addAttribute("canReview", reviewService.canReview(user, id));
        model.addAttribute("isCustomer", user != null && user.getRole() == Role.CUSTOMER);
        model.addAttribute("toppings", catalogService.availableToppings());
        model.addAttribute("levels", OrderItemBuilder.LEVELS);
        return "menu/product";
    }

    /**
     * Thêm ly nước đã tuỳ chỉnh vào giỏ.
     * BUILDER: dựng OrderItem từng bước theo lựa chọn của khách;
     * bên trong build() dùng DECORATOR để tính giá.
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "size", defaultValue = "M") String size,
                            @RequestParam(value = "sugar", defaultValue = "70") int sugar,
                            @RequestParam(value = "ice", defaultValue = "70") int ice,
                            @RequestParam(value = "toppingIds", required = false) List<Long> toppingIds,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "note", required = false) String note,
                            RedirectAttributes ra) {
        try {
            Product product = catalogService.getProduct(productId);
            OrderItem item = OrderItemBuilder.forProduct(product)
                    .size(size)
                    .sugar(sugar)
                    .ice(ice)
                    .toppings(catalogService.toppingsByIds(toppingIds))
                    .quantity(quantity)
                    .note(note)
                    .build();
            cart.add(item);
            ra.addFlashAttribute("success", "Đã thêm " + item.getQuantity() + " x " + item.getDescription() + " vào giỏ");
            return "redirect:/";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    /**
     * "Thêm nhanh" theo công thức có sẵn.
     * BUILDER - DIRECTOR: Director giữ một Builder (DrinkBuilder) và gọi các bước
     * theo công thức chuẩn. Mỗi ly tạo một Director mới với Builder cho đúng món.
     */
    @PostMapping("/cart/quick-add")
    public String quickAdd(@RequestParam("productId") Long productId,
                           @RequestParam(value = "recipe", defaultValue = "default") String recipe,
                           RedirectAttributes ra) {
        try {
            Product product = catalogService.getProduct(productId);
            DrinkDirector director = new DrinkDirector(OrderItemBuilder.forProduct(product));
            OrderItem item = switch (recipe) {
                case "less-sweet" -> director.makeLessSweet();
                case "special" -> director.makeSpecial(catalogService.availableToppings());
                default -> director.makeDefault();
            };
            cart.add(item);
            ra.addFlashAttribute("success", "Đã thêm nhanh: " + item.getDescription());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }
}
