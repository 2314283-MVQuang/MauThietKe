package com.trasua.controller;

import com.trasua.entity.Product;
import com.trasua.pattern.builder.OrderItemBuilder;
import com.trasua.pattern.decorator.Beverage;
import com.trasua.pattern.decorator.DecoratorChainTracer;
import com.trasua.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * API xem trước giá khi khách đang chọn size/topping (gọi bằng JavaScript).
 * Trả về cả từng lớp DECORATOR để hiển thị "bóc tách giá" trên giao diện.
 */
@RestController
public class PriceApiController {

    public record PricePreview(boolean ok, String error, String description, BigDecimal unitPrice,
                               BigDecimal lineTotal, List<DecoratorChainTracer.Step> steps) {
    }

    private final CatalogService catalogService;

    public PriceApiController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/price-preview")
    public PricePreview preview(@RequestParam("productId") Long productId,
                                @RequestParam(value = "size", defaultValue = "M") String size,
                                @RequestParam(value = "sugar", defaultValue = "70") int sugar,
                                @RequestParam(value = "ice", defaultValue = "70") int ice,
                                @RequestParam(value = "toppingIds", required = false) List<Long> toppingIds,
                                @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        try {
            Product product = catalogService.getProduct(productId);
            Beverage beverage = OrderItemBuilder.forProduct(product)
                    .size(size)
                    .sugar(sugar)
                    .ice(ice)
                    .toppings(catalogService.toppingsByIds(toppingIds))
                    .quantity(quantity)
                    .buildBeverage();
            BigDecimal unit = beverage.getCost();
            return new PricePreview(true, null, beverage.getDescription(), unit,
                    unit.multiply(BigDecimal.valueOf(Math.max(quantity, 1))),
                    DecoratorChainTracer.trace(beverage));
        } catch (RuntimeException e) {
            return new PricePreview(false, e.getMessage(), null, null, null, List.of());
        }
    }
}
