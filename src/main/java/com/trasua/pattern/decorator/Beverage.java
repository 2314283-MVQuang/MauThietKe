package com.trasua.pattern.decorator;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: COMPONENT (giao diện chung)
 * ============================================================================
 *
 * VẤN ĐỀ:
 *   Một ly trà sữa có thể được "thêm thắt" rất nhiều thứ: lên size L, thêm
 *   trân châu đen, thêm pudding, thêm kem cheese, chỉnh đường/đá...
 *   Nếu dùng KẾ THỪA, ta phải tạo ra hàng trăm lớp con kiểu
 *   TraSuaSizeLTranChauPudding, TraSuaSizeMKemCheese... => "bùng nổ lớp con".
 *
 * GIẢI PHÁP (Decorator):
 *   - Mọi thứ đều là một {@code Beverage} (đồ uống).
 *   - Ly gốc là {@link BaseBeverage} (ConcreteComponent).
 *   - Mỗi phần thêm là một "lớp bọc" ({@link BeverageDecorator}), nó cũng là
 *     Beverage, bên trong chứa một Beverage khác. Khi gọi getCost() nó lấy giá
 *     của ly bên trong rồi cộng thêm giá của phần mình.
 *   - Có thể bọc nhiều lớp lồng nhau theo bất kỳ thứ tự/số lượng nào khi CHẠY
 *     (runtime), không cần tạo lớp mới.
 *
 *   Ví dụ:  new ToppingDecorator(
 *               new SizeDecorator(new BaseBeverage("Trà sữa", 30000), L, 8000),
 *               "Trân châu đen", 5000)
 *   => getCost() = 30000 + 8000 + 5000 = 43000
 */
public interface Beverage {

    /** Mô tả ly nước (được các lớp bọc nối thêm thông tin). */
    String getDescription();

    /** Giá của ly nước (được các lớp bọc cộng thêm tiền). */
    BigDecimal getCost();
}
