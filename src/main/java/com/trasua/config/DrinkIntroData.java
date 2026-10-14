package com.trasua.config;

import java.util.Map;

/**
 * Nội dung giới thiệu mặc định cho 11 món của quán (dùng khi khởi tạo dữ liệu).
 * Quản lý có thể sửa lại từng món trong trang "Đồ uống".
 */
final class DrinkIntroData {

    /**
     * @param sweet    độ ngọt 0..5
     * @param strength độ đậm của trà / cà phê 0..5
     * @param creamy   độ béo 0..5
     */
    record Intro(boolean signature, String tagline, String story, String ingredients,
                 int sweet, int strength, int creamy, String suggestion) {
    }

    private DrinkIntroData() {
    }

    static final Map<String, Intro> INTROS = Map.ofEntries(
            Map.entry("Trà sữa truyền thống", new Intro(false,
                    "Hồng trà đậm, sữa béo vừa phải – vị quen mà không nhàm.",
                    "Hồng trà Assam ủ lạnh qua đêm cho vị chát dịu, hoà cùng sữa tươi và một chút sữa đặc. "
                            + "Đây là ly trà sữa \"chuẩn bài\" để bạn tự phối topping theo ý thích.",
                    "Hồng trà Assam, Sữa tươi, Sữa đặc, Đá viên",
                    3, 4, 3, "70% đường + trân châu đen")),
            Map.entry("Trà sữa trân châu đường đen", new Intro(true,
                    "Vệt đường đen sóng sánh quanh ly, trân châu dẻo nóng.",
                    "Trân châu được nấu với đường đen mỗi sáng, múc nóng vào ly để tạo những vệt hổ vằn "
                            + "trên thành ly. Rót sữa tươi lạnh lên trên, lắc nhẹ trước khi uống để vị caramel lan đều.",
                    "Sữa tươi, Đường đen, Trân châu đen, Kem sữa",
                    4, 1, 5, "Không cần thêm đường, chọn size L để nhiều trân châu hơn")),
            Map.entry("Trà sữa matcha", new Intro(false,
                    "Matcha xay mịn, đậu đỏ bùi bùi.",
                    "Bột matcha được đánh tay với nước ấm cho bọt mịn rồi hoà sữa tươi, phủ đậu đỏ ninh mềm. "
                            + "Vị đắng nhẹ đầu lưỡi, hậu ngọt thanh.",
                    "Matcha, Sữa tươi, Đậu đỏ",
                    2, 4, 3, "50% đường + kem cheese")),
            Map.entry("Trà sữa khoai môn", new Intro(false,
                    "Khoai môn Đà Lạt nghiền tay, tím mộng mơ.",
                    "Khoai môn tím Đà Lạt hấp chín, nghiền thô để còn những miếng bùi trong ly, "
                            + "hoà cùng trà sữa và thạch dừa giòn mát.",
                    "Khoai môn Đà Lạt, Trà sữa, Thạch dừa",
                    3, 2, 4, "50% đường + pudding trứng")),
            Map.entry("Trà đào cam sả", new Intro(true,
                    "Đào giòn, cam tươi, sả thơm – mát lạnh tức thì.",
                    "Trà đen ủ cùng sả đập dập, thêm nước cam vắt và những miếng đào ngâm giòn sần sật. "
                            + "Món giải nhiệt được gọi nhiều nhất vào những buổi trưa nắng.",
                    "Trà đen, Đào ngâm, Cam tươi, Sả, Bạc hà",
                    3, 3, 0, "70% đường, ít đá để giữ trọn vị cam")),
            Map.entry("Trà vải hoa hồng", new Intro(false,
                    "Vải thiều ngọt lịm quyện hương hoa hồng.",
                    "Trà xanh nhài pha nhẹ, thêm syrup hoa hồng và vải thiều nguyên quả, "
                            + "viền ly rắc cánh hồng khô thơm dịu.",
                    "Trà xanh nhài, Vải thiều, Syrup hoa hồng, Cánh hồng khô",
                    3, 2, 0, "50% đường + thạch dừa")),
            Map.entry("Trà dâu tây Đà Lạt", new Intro(true,
                    "Dâu tây Đà Lạt dầm tay, chua ngọt tự nhiên.",
                    "Dâu tây Đà Lạt tươi được dầm tay cùng chút đường để giữ thớ quả, rồi hoà vào trà xanh lạnh. "
                            + "Màu đỏ đến từ chính trái dâu, không dùng siro tạo màu.",
                    "Dâu tây Đà Lạt, Trà xanh, Lá bạc hà",
                    3, 2, 0, "50% đường + thạch phô mai")),
            Map.entry("Cà phê sữa đá", new Intro(false,
                    "Phin Robusta Lâm Đồng đậm đà, sữa đặc béo ngậy.",
                    "Cà phê Robusta Lâm Đồng rang mộc, pha phin chậm từng giọt rồi khuấy cùng sữa đặc và đá. "
                            + "Đậm, đắng, thơm – đúng chất cà phê Việt.",
                    "Cà phê Robusta, Sữa đặc, Đá viên",
                    3, 5, 3, "Size M, 70% đường")),
            Map.entry("Bạc xỉu", new Intro(false,
                    "Nhiều sữa, ít cà phê – dịu nhẹ cho người mới uống.",
                    "Lớp sữa tươi và sữa đặc béo ngậy, rót nhẹ cà phê phin lên trên tạo hai tầng màu. "
                            + "Vị cà phê thoang thoảng, hợp để nhâm nhi cả buổi chiều.",
                    "Sữa tươi, Sữa đặc, Cà phê phin",
                    4, 2, 5, "Khuấy đều trước khi uống")),
            Map.entry("Matcha đá xay", new Intro(false,
                    "Matcha xay mịn mát lạnh, phủ kem bồng bềnh.",
                    "Matcha, sữa và đá xay nhuyễn thành lớp sánh mịn như kem, phủ kem tươi vị matcha phía trên.",
                    "Matcha, Sữa tươi, Đá xay, Kem tươi",
                    3, 3, 4, "Thêm trân châu trắng cho vui miệng")),
            Map.entry("Cookie đá xay", new Intro(false,
                    "Bánh quy socola giòn tan trong lớp đá xay mát lạnh.",
                    "Bánh quy socola nghiền vụn xay cùng sữa và đá, rắc thêm bánh vụn lên mặt. "
                            + "Ngọt vừa, béo thơm – món tráng miệng dạng uống.",
                    "Bánh quy socola, Sữa tươi, Đá xay, Kem tươi",
                    4, 1, 5, "Size L để chia sẻ cùng bạn"))
    );
}
