package com.trasua.config;

import com.trasua.entity.*;
import com.trasua.repository.*;
import com.trasua.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tạo dữ liệu mẫu khi khởi động lần đầu (bảng users đang trống).
 * Nếu bạn đã chạy file database/trasua_db.sql trong pgAdmin thì lớp này bỏ qua.
 *
 * Tài khoản mẫu:
 *   admin     / admin123   (Quản lý)
 *   nhanvien  / nv123      (Nhân viên)
 *   khachhang / kh123      (Khách hàng)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public DataSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
                      ProductRepository productRepository, ToppingRepository toppingRepository,
                      PromotionRepository promotionRepository, PasswordEncoder passwordEncoder,
                      ReviewRepository reviewRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.toppingRepository = toppingRepository;
        this.promotionRepository = promotionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("CSDL trống - đang tạo dữ liệu mẫu...");
            seedUsers();
            seedMenu();
            seedToppings();
            seedPromotions();
            log.info("Đã tạo dữ liệu mẫu. Đăng nhập: admin/admin123, nhanvien/nv123, khachhang/kh123");
        }
        if (reviewRepository.count() == 0) {
            seedReviews();
        }
        assignDefaultImages();
        assignDefaultIntros();
    }

    /** Điền phần giới thiệu cho món CHƯA có giới thiệu (không ghi đè nội dung quản lý đã sửa). */
    private void assignDefaultIntros() {
        int count = 0;
        for (Product p : productRepository.findAll()) {
            DrinkIntroData.Intro intro = DrinkIntroData.INTROS.get(p.getName());
            if (intro == null || (p.getStory() != null && !p.getStory().isBlank())) {
                continue;
            }
            p.setSignature(intro.signature());
            p.setTagline(intro.tagline());
            p.setStory(intro.story());
            p.setIngredients(intro.ingredients());
            p.setTasteSweet(intro.sweet());
            p.setTasteStrength(intro.strength());
            p.setTasteCreamy(intro.creamy());
            p.setSuggestion(intro.suggestion());
            productRepository.save(p);
            count++;
        }
        if (count > 0) {
            log.info("Đã thêm phần giới thiệu cho {} món", count);
        }
    }

    /** Ảnh có sẵn trong project (static/images/products) cho các món mặc định. */
    private static final java.util.Map<String, String> DEFAULT_IMAGES = java.util.Map.ofEntries(
            java.util.Map.entry("Trà sữa truyền thống", "01-tra-sua-truyen-thong.jpg"),
            java.util.Map.entry("Trà sữa trân châu đường đen", "02-tra-sua-tran-chau-duong-den.jpg"),
            java.util.Map.entry("Trà sữa matcha", "03-tra-sua-matcha.jpg"),
            java.util.Map.entry("Trà sữa khoai môn", "04-tra-sua-khoai-mon.jpg"),
            java.util.Map.entry("Trà đào cam sả", "05-tra-dao-cam-sa.jpg"),
            java.util.Map.entry("Trà vải hoa hồng", "06-tra-vai-hoa-hong.jpg"),
            java.util.Map.entry("Trà dâu tây Đà Lạt", "07-tra-dau-tay-da-lat.jpg"),
            java.util.Map.entry("Cà phê sữa đá", "08-ca-phe-sua-da.jpg"),
            java.util.Map.entry("Bạc xỉu", "09-bac-xiu.jpg"),
            java.util.Map.entry("Matcha đá xay", "10-matcha-da-xay.jpg"),
            java.util.Map.entry("Cookie đá xay", "11-cookie-da-xay.jpg"));

    /**
     * Gắn ảnh mặc định cho những món CHƯA có ảnh (không ghi đè ảnh quản lý đã upload).
     */
    private void assignDefaultImages() {
        int count = 0;
        for (Product p : productRepository.findAll()) {
            String file = DEFAULT_IMAGES.get(p.getName());
            if (file != null && (p.getImageUrl() == null || p.getImageUrl().isBlank())) {
                p.setImageUrl("/images/products/" + file);
                productRepository.save(p);
                count++;
            }
        }
        if (count > 0) {
            log.info("Đã gắn ảnh mặc định cho {} món", count);
        }
    }

    /** Vài đánh giá mẫu để menu có sao ngay khi demo. */
    private void seedReviews() {
        User kh = userRepository.findByUsername("khachhang").orElse(null);
        User lan = demoCustomer("lan.nguyen", "Nguyễn Thị Lan", "0901000004");
        User minh = demoCustomer("minh.tran", "Trần Văn Minh", "0901000005");
        if (kh == null) {
            return;
        }
        review("Trà sữa trân châu đường đen", kh, 5, "Trân châu dẻo, đường đen thơm, uống size L đã luôn!");
        review("Trà sữa trân châu đường đen", lan, 5, "Món best seller của quán, lần nào ghé cũng gọi.");
        review("Trà sữa trân châu đường đen", minh, 4, "Ngon nhưng hơi ngọt, lần sau chọn 50% đường.");
        review("Cà phê sữa đá", kh, 4, "Đậm vị, giá sinh viên.");
        review("Cà phê sữa đá", minh, 5, "Cà phê Lâm Đồng chuẩn vị.");
        review("Trà đào cam sả", lan, 5, "Thơm sả, đào giòn, rất mát.");
        review("Trà đào cam sả", minh, 4, null);
        review("Trà sữa matcha", lan, 4, "Matcha đậm, thêm kem cheese là hợp.");
        review("Trà dâu tây Đà Lạt", minh, 5, "Dâu tươi thật, chua ngọt vừa phải.");
        review("Trà sữa truyền thống", kh, 4, "Vị trà rõ, không bị gắt.");
        review("Trà sữa truyền thống", lan, 3, "Ổn, hơi nhiều đá.");
        review("Matcha đá xay", minh, 5, "Mịn, béo, ngon.");
        log.info("Đã tạo đánh giá mẫu cho menu");
    }

    private User demoCustomer(String username, String fullName, String phone) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User(username, passwordEncoder.encode("kh123"), fullName, Role.CUSTOMER);
            u.setPhone(phone);
            return userRepository.save(u);
        });
    }

    private void review(String productName, User user, int rating, String comment) {
        productRepository.findAllByOrderByCategoryNameAscNameAsc().stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst()
                .ifPresent(p -> {
                    reviewRepository.save(new Review(p, user, rating, comment));
                    reviewService.recalculate(p);
                });
    }

    private void seedUsers() {
        User admin = new User("admin", passwordEncoder.encode("admin123"), "Quản lý quán", Role.ADMIN);
        admin.setPhone("0901000001");
        admin.setEmail("admin@trasua.vn");

        User staff = new User("nhanvien", passwordEncoder.encode("nv123"), "Nguyễn Văn Nhân", Role.STAFF);
        staff.setPhone("0901000002");

        User customer = new User("khachhang", passwordEncoder.encode("kh123"), "Trần Thị Khách", Role.CUSTOMER);
        customer.setPhone("0901000003");
        customer.setAddress("01 Phù Đổng Thiên Vương, Phường 8, Đà Lạt");
        customer.setEmail("khachhang@gmail.com");

        userRepository.save(admin);
        userRepository.save(staff);
        userRepository.save(customer);
    }

    private void seedMenu() {
        Category traSua = categoryRepository.save(new Category("Trà sữa", "Trà sữa pha từ trà nguyên lá"));
        Category traTraiCay = categoryRepository.save(new Category("Trà trái cây", "Trà tươi mát với trái cây thật"));
        Category caPhe = categoryRepository.save(new Category("Cà phê", "Cà phê Robusta & Arabica Lâm Đồng"));
        Category daXay = categoryRepository.save(new Category("Đá xay", "Đồ uống đá xay mát lạnh"));

        product("Trà sữa truyền thống", "Hồng trà sữa đậm vị", traSua, 25000, 32000);
        product("Trà sữa trân châu đường đen", "Trân châu nấu đường đen, sữa tươi", traSua, 35000, 42000);
        product("Trà sữa matcha", "Matcha Nhật Bản và sữa tươi", traSua, 32000, 39000);
        product("Trà sữa khoai môn", "Khoai môn Đà Lạt nghiền", traSua, 30000, 37000);
        product("Trà đào cam sả", "Trà đen, đào miếng, cam tươi, sả", traTraiCay, 30000, 37000);
        product("Trà vải hoa hồng", "Trà xanh nhài, vải thiều, hoa hồng", traTraiCay, 30000, 37000);
        product("Trà dâu tây Đà Lạt", "Dâu tây tươi Đà Lạt", traTraiCay, 32000, 39000);
        product("Cà phê sữa đá", "Phin truyền thống", caPhe, 20000, 25000);
        product("Bạc xỉu", "Nhiều sữa, ít cà phê", caPhe, 25000, 30000);
        product("Matcha đá xay", "Matcha đá xay phủ kem", daXay, 38000, 45000);
        product("Cookie đá xay", "Bánh quy Oreo đá xay", daXay, 38000, 45000);
    }

    private void product(String name, String desc, Category category, long priceM, long priceL) {
        productRepository.save(new Product(name, desc, category, BigDecimal.valueOf(priceM), BigDecimal.valueOf(priceL)));
    }

    private void seedToppings() {
        toppingRepository.save(new Topping("Trân châu đen", BigDecimal.valueOf(5000)));
        toppingRepository.save(new Topping("Trân châu trắng", BigDecimal.valueOf(7000)));
        toppingRepository.save(new Topping("Thạch dừa", BigDecimal.valueOf(5000)));
        toppingRepository.save(new Topping("Pudding trứng", BigDecimal.valueOf(8000)));
        toppingRepository.save(new Topping("Kem cheese", BigDecimal.valueOf(10000)));
        toppingRepository.save(new Topping("Thạch phô mai", BigDecimal.valueOf(8000)));
    }

    private void seedPromotions() {
        Promotion giam10 = new Promotion();
        giam10.setCode("GIAM10");
        giam10.setName("Giảm 10% cho đơn từ 50.000đ");
        giam10.setType(PromotionType.PHAN_TRAM);
        giam10.setValue(BigDecimal.valueOf(10));
        giam10.setMaxDiscount(BigDecimal.valueOf(20000));
        giam10.setMinOrderAmount(BigDecimal.valueOf(50000));
        giam10.setStartDate(LocalDate.now().minusDays(1));
        giam10.setEndDate(LocalDate.now().plusMonths(3));
        promotionRepository.save(giam10);

        Promotion giam15k = new Promotion();
        giam15k.setCode("GIAM15K");
        giam15k.setName("Giảm 15.000đ cho đơn từ 80.000đ");
        giam15k.setType(PromotionType.SO_TIEN);
        giam15k.setValue(BigDecimal.valueOf(15000));
        giam15k.setMinOrderAmount(BigDecimal.valueOf(80000));
        promotionRepository.save(giam15k);

        Promotion mua2tang1 = new Promotion();
        mua2tang1.setCode("MUA2TANG1");
        mua2tang1.setName("Mua 2 tặng 1 ly rẻ nhất");
        mua2tang1.setType(PromotionType.MUA_X_TANG_1);
        mua2tang1.setValue(BigDecimal.ZERO);
        mua2tang1.setBuyQuantity(2);
        mua2tang1.setMinOrderAmount(BigDecimal.ZERO);
        promotionRepository.save(mua2tang1);
    }
}
