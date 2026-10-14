package com.trasua.controller.admin;

import com.trasua.entity.Category;
import com.trasua.entity.Product;
import com.trasua.entity.Topping;
import com.trasua.service.CatalogService;
import com.trasua.service.ImageStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quản lý danh mục, đồ uống và topping (chỉ ADMIN). */
@Controller
@RequestMapping("/admin")
public class AdminCatalogController {

    private final CatalogService catalogService;
    private final ImageStorageService imageStorageService;

    public AdminCatalogController(CatalogService catalogService, ImageStorageService imageStorageService) {
        this.catalogService = catalogService;
        this.imageStorageService = imageStorageService;
    }

    // ============================ DANH MỤC ============================

    @GetMapping("/categories")
    public String categories(@RequestParam(value = "edit", required = false) Long editId, Model model) {
        model.addAttribute("categories", catalogService.allCategories());
        model.addAttribute("form", editId == null ? new Category() : catalogService.getCategory(editId));
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("form") Category form, RedirectAttributes ra) {
        try {
            catalogService.saveCategory(form);
            ra.addFlashAttribute("success", "Đã lưu danh mục");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            catalogService.deleteCategory(id);
            ra.addFlashAttribute("success", "Đã xoá danh mục");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ============================ ĐỒ UỐNG ============================

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", catalogService.allProducts());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        return productForm(new Product(), null, model);
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable("id") Long id, Model model) {
        Product product = catalogService.getProduct(id);
        return productForm(product, product.getCategory().getId(), model);
    }

    private String productForm(Product product, Long categoryId, Model model) {
        model.addAttribute("product", product);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", catalogService.allCategories());
        return "admin/product-form";
    }

    /**
     * Lưu đồ uống. Ảnh món có thể đến từ 3 nguồn:
     *   1. imageFile  - file ảnh chọn từ máy HOẶC ảnh chụp bằng camera (JS đưa ảnh chụp vào cùng ô này)
     *   2. imageUrl   - link ảnh trên mạng (tuỳ chọn)
     *   3. removeImage - tick để xoá ảnh hiện tại
     */
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("product") Product form,
                              @RequestParam(value = "categoryId", required = false) Long categoryId,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
                              Model model, RedirectAttributes ra) {
        String oldImage = form.getId() == null ? null : catalogService.getProduct(form.getId()).getImageUrl();
        String newUploaded = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                newUploaded = imageStorageService.storeProductImage(imageFile);
                form.setImageUrl(newUploaded);
            } else if (removeImage) {
                form.setImageUrl(null);
            }
            catalogService.saveProduct(form, categoryId);
            // Ảnh cũ do hệ thống lưu mà không còn dùng nữa thì xoá khỏi ổ đĩa
            if (oldImage != null && !oldImage.equals(form.getImageUrl())) {
                imageStorageService.deleteIfLocal(oldImage);
            }
            ra.addFlashAttribute("success", "Đã lưu đồ uống " + form.getName());
            return "redirect:/admin/products";
        } catch (IllegalArgumentException e) {
            imageStorageService.deleteIfLocal(newUploaded);
            form.setImageUrl(oldImage);
            model.addAttribute("error", e.getMessage());
            return productForm(form, categoryId, model);
        }
    }

    @PostMapping("/products/{id}/toggle")
    public String toggleProduct(@PathVariable("id") Long id, RedirectAttributes ra) {
        catalogService.toggleProduct(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái bán");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes ra) {
        String image = catalogService.getProduct(id).getImageUrl();
        catalogService.deleteProduct(id);
        imageStorageService.deleteIfLocal(image);
        ra.addFlashAttribute("success", "Đã xoá đồ uống");
        return "redirect:/admin/products";
    }

    // ============================ TOPPING ============================

    @GetMapping("/toppings")
    public String toppings(@RequestParam(value = "edit", required = false) Long editId, Model model) {
        model.addAttribute("toppings", catalogService.allToppings());
        model.addAttribute("form", editId == null ? new Topping() : catalogService.getTopping(editId));
        return "admin/toppings";
    }

    @PostMapping("/toppings/save")
    public String saveTopping(@ModelAttribute("form") Topping form, RedirectAttributes ra) {
        try {
            catalogService.saveTopping(form);
            ra.addFlashAttribute("success", "Đã lưu topping");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/toppings";
    }

    @PostMapping("/toppings/{id}/delete")
    public String deleteTopping(@PathVariable("id") Long id, RedirectAttributes ra) {
        catalogService.deleteTopping(id);
        ra.addFlashAttribute("success", "Đã xoá topping");
        return "redirect:/admin/toppings";
    }
}
