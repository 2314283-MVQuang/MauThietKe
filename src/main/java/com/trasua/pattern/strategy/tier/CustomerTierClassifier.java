package com.trasua.pattern.strategy.tier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: CONTEXT (xếp hạng khách hàng)
 * ============================================================================
 * Giữ danh sách các chiến lược xếp hạng, chọn 1 chiến lược theo khoá (lấy từ URL)
 * rồi ủy quyền việc xếp hạng. Thêm tiêu chí mới = thêm 1 lớp Strategy, không sửa lớp này.
 */
public class CustomerTierClassifier {

    private final Map<String, CustomerTierStrategy> strategies = new LinkedHashMap<>();
    private CustomerTierStrategy current;

    public CustomerTierClassifier(List<CustomerTierStrategy> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất 1 chiến lược xếp hạng");
        }
        list.forEach(s -> strategies.put(s.getKey(), s));
        current = list.get(0);
    }

    /** Mặc định: chi tiêu, rồi số đơn. */
    public static CustomerTierClassifier withDefaults() {
        return new CustomerTierClassifier(List.of(new SpendingTierStrategy(), new OrderCountTierStrategy()));
    }

    /** Đổi chiến lược lúc chạy; khoá không tồn tại thì giữ nguyên chiến lược hiện tại. */
    public void use(String key) {
        if (key != null && strategies.containsKey(key)) {
            current = strategies.get(key);
        }
    }

    public CustomerTierStrategy getCurrent() {
        return current;
    }

    public List<CustomerTierStrategy> getAll() {
        return List.copyOf(strategies.values());
    }

    public CustomerTier classify(CustomerStats stats) {
        return current.classify(stats);
    }
}
