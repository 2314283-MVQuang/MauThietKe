package com.trasua.service;

import com.trasua.entity.Promotion;
import com.trasua.entity.PromotionType;
import com.trasua.pattern.strategy.discount.DiscountStrategy;
import com.trasua.pattern.strategy.discount.DiscountStrategyFactory;
import com.trasua.pattern.strategy.discount.NoDiscountStrategy;
import com.trasua.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<Promotion> findAll() {
        return promotionRepository.findAllByOrderByIdDesc();
    }

    /** Các mã đang chạy - hiển thị gợi ý cho khách ở giỏ hàng. */
    public List<Promotion> findUsable() {
        return promotionRepository.findByActiveTrueOrderByIdDesc().stream()
                .filter(Promotion::isUsableToday)
                .toList();
    }

    public Promotion getById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khuyến mãi #" + id));
    }

    /** Tìm mã còn hiệu lực, ném lỗi kèm lý do nếu không dùng được. */
    public Promotion getUsableByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã khuyến mãi");
        }
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không tồn tại"));
        if (!promotion.isUsableToday()) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn hoặc tạm ngưng");
        }
        return promotion;
    }

    /**
     * STRATEGY: trả về chiến lược giảm giá ứng với mã khuyến mãi.
     * Mã rỗng / không còn hiệu lực -> NoDiscountStrategy.
     */
    public DiscountStrategy strategyForCode(String code) {
        if (code == null || code.isBlank()) {
            return new NoDiscountStrategy();
        }
        try {
            return DiscountStrategyFactory.fromPromotion(getUsableByCode(code));
        } catch (IllegalArgumentException e) {
            return new NoDiscountStrategy();
        }
    }

    @Transactional
    public Promotion save(Promotion form) {
        if (form.getCode() == null || form.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống");
        }
        String code = form.getCode().trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_]{3,30}")) {
            throw new IllegalArgumentException("Mã chỉ gồm chữ in hoa không dấu, số, dấu _ (3-30 ký tự)");
        }
        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("Tên chương trình không được để trống");
        }
        if (form.getStartDate() != null && form.getEndDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (form.getType() == PromotionType.MUA_X_TANG_1) {
            if (form.getBuyQuantity() == null || form.getBuyQuantity() < 1) {
                throw new IllegalArgumentException("Số ly phải mua tối thiểu là 1");
            }
            form.setValue(BigDecimal.ZERO);
        } else if (form.getValue() == null || form.getValue().signum() <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        }
        if (form.getType() == PromotionType.PHAN_TRAM && form.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm không vượt quá 100");
        }

        Promotion promotion = form.getId() == null ? new Promotion() : getById(form.getId());
        if (!code.equalsIgnoreCase(promotion.getCode()) && promotionRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Mã " + code + " đã tồn tại");
        }
        promotion.setCode(code);
        promotion.setName(form.getName().trim());
        promotion.setType(form.getType());
        promotion.setValue(form.getValue());
        promotion.setMaxDiscount(form.getMaxDiscount());
        promotion.setMinOrderAmount(form.getMinOrderAmount() == null ? BigDecimal.ZERO : form.getMinOrderAmount());
        promotion.setBuyQuantity(form.getBuyQuantity());
        promotion.setActive(form.isActive());
        promotion.setStartDate(form.getStartDate());
        promotion.setEndDate(form.getEndDate());

        // Thử tạo strategy để chắc chắn cấu hình hợp lệ trước khi lưu
        DiscountStrategyFactory.fromPromotion(promotion);
        return promotionRepository.save(promotion);
    }

    @Transactional
    public void toggle(Long id) {
        Promotion promotion = getById(id);
        promotion.setActive(!promotion.isActive());
    }

    @Transactional
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }
}
