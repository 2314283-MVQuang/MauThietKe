# 🧋 Quản lý quán trà sữa – Spring Boot + PostgreSQL

Đồ án môn **Mẫu thiết kế**: web quản lý quán trà sữa có phân quyền, áp dụng
**Builder Pattern**, **Decorator Pattern** và **Strategy Pattern**.

- Sinh viên: Mai Văn Quang – MSSV 2314283 – Lớp CTK47C – Trường Đại học Đà Lạt
- Công nghệ: Java 17+, Spring Boot 3.5, Spring MVC + Thymeleaf, Spring Data JPA, Spring Security, PostgreSQL

> Giải thích chi tiết 3 mẫu thiết kế: xem file **`GIAI_THICH_MAU_THIET_KE.md`**.

---

## 1. Chức năng theo vai trò

| Vai trò | Chức năng |
|---|---|
| Khách vãng lai | Xem menu (kèm điểm sao của từng món), xem nhận xét, tìm kiếm, xem chi tiết & xem trước giá, đăng ký |
| **Khách hàng** (CUSTOMER) | Tuỳ chỉnh ly (size, đường, đá, topping), "thêm nhanh" theo công thức, giỏ hàng, nhập mã khuyến mãi, đặt hàng (mang đi / giao hàng), chọn thanh toán (tiền mặt / quét QR Vietcombank / quét QR MoMo), xem lịch sử & chi tiết đơn, huỷ đơn khi chưa xác nhận, **đánh giá 1–5 sao & nhận xét món đã mua** (sửa / xoá được), sửa hồ sơ, đổi mật khẩu |
| **Nhân viên** (STAFF) | Bán hàng tại quầy (tại quán / mang đi / giao), nhập tiền khách đưa → tính tiền thối, xử lý đơn: xác nhận → pha chế → giao → hoàn thành, huỷ đơn, xác nhận đã thanh toán, in hoá đơn |
| **Quản lý** (ADMIN) | Tất cả quyền nhân viên + thống kê doanh thu (hôm nay, tháng, 7 ngày, top món bán chạy), CRUD danh mục / đồ uống (kèm ảnh: chọn từ máy hoặc chụp bằng webcam) / topping / mã khuyến mãi, duyệt đánh giá (ẩn / xoá nhận xét), quản lý tài khoản & phân quyền, khoá/mở tài khoản |

Phân quyền thực hiện ở `config/SecurityConfig.java` (theo URL) và trong giao diện bằng `sec:authorize`.

## 2. Cài đặt & chạy (clone từ GitHub)

### Cần cài trước
- **Java 17+** (JDK, ví dụ Eclipse Temurin)
- **PostgreSQL** (kèm pgAdmin 4) – nhớ mật khẩu tài khoản `postgres` đặt lúc cài
- *(Không cần cài Maven – `chay_web.bat` tự tải)*

### Bước 1 – Lấy code
```bash
git clone https://github.com/2314283-MVQuang/MauThietKe.git
cd MauThietKe
```

### Bước 2 – Điền mật khẩu PostgreSQL của máy bạn
Copy file **`cau_hinh_rieng.properties.example`** thành **`cau_hinh_rieng.properties`** (cùng thư mục gốc),
rồi sửa dòng:
```properties
spring.datasource.password=mat_khau_postgres_cua_ban
```
> `chay_web.bat` cũng tự tạo file này (giá trị mặc định `123456`) nếu bạn chưa tạo.
> File `cau_hinh_rieng.properties` **không được đưa lên GitHub** (đã có trong `.gitignore`)
> và sẽ ghi đè các giá trị trong `src/main/resources/application.properties`.

### Bước 3 – Chạy
- **Cách nhanh nhất (Windows):** bấm đúp **`chay_web.bat`**. File này:
  1. tự tìm PostgreSQL, **tạo database `trasua_db` và nạp toàn bộ dữ liệu từ `database/trasua_db.sql`** nếu máy chưa có;
  2. tự tải Maven nếu máy chưa có;
  3. chạy web. Đóng cửa sổ đen = tắt web.
- **IntelliJ IDEA**: File → Open → chọn thư mục project (có `pom.xml`) → chạy `TraSuaApplication`
  (khi đó tự tạo database `trasua_db` trong pgAdmin và nạp `database/trasua_db.sql` bằng Query Tool).
- **Dòng lệnh**: `mvn spring-boot:run`

Mở trình duyệt: **http://localhost:8080**

### Database
- `database/trasua_db.sql` là bản xuất đầy đủ (cấu trúc + dữ liệu: menu, ảnh, topping, khuyến mãi, đơn hàng, đánh giá, tài khoản demo).
- Nạp thủ công: pgAdmin → tạo database `trasua_db` → **Tools → Query Tool** → mở file → **Execute (F5)**.
- Muốn cập nhật file này sau khi thay đổi dữ liệu: bấm đúp **`xuat_database.bat`** (dùng `pg_dump`).

### Đăng nhập bằng Google (tuỳ chọn)
Không có Client ID thì nút Google chỉ báo "chưa cấu hình", các chức năng khác vẫn chạy bình thường.
1. Vào https://console.cloud.google.com/ → tạo (hoặc chọn) một project.
2. **APIs & Services → OAuth consent screen**: chọn *External*, điền tên ứng dụng + email, thêm email của bạn vào *Test users*.
3. **APIs & Services → Credentials → Create credentials → OAuth client ID** → loại *Web application*.
   - *Authorized redirect URIs*: `http://localhost:8080/login/oauth2/code/google`
4. Chép **Client ID** và **Client secret** vào **`cau_hinh_rieng.properties`** (KHÔNG điền vào `application.properties` vì file đó lên GitHub):
   ```properties
   app.google.client-id=xxxxxxxx.apps.googleusercontent.com
   app.google.client-secret=GOCSPX-xxxxxxxx
   ```
5. Chạy lại web. Nút **"Tiếp tục với Google"** ở trang đăng nhập/đăng ký sẽ hoạt động:
   email đã có trong hệ thống → vào đúng tài khoản đó; email mới → tự tạo tài khoản Khách hàng.

### Tài khoản demo
| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản lý | `admin` | `admin123` |
| Nhân viên | `nhanvien` | `nv123` |
| Khách hàng | `khachhang` | `kh123` |
| Khách mẫu (tác giả đánh giá mẫu) | `lan.nguyen`, `minh.tran` | `kh123` |

### Chạy kiểm thử 3 mẫu thiết kế
```bash
mvn test
```
Các test nằm trong `src/test/java/com/trasua/pattern/` (không cần database).

## 3. Cấu trúc thư mục

```
src/main/java/com/trasua/
├── TraSuaApplication.java
├── pattern/                         <== 3 MẪU THIẾT KẾ (Java thuần, có chú thích)
│   ├── builder/     OrderItemBuilder, OrderBuilder, DrinkDirector
│   ├── decorator/   Beverage, BaseBeverage, BeverageDecorator,
│   │                SizeDecorator, ToppingDecorator, SugarIceDecorator, DecoratorChainTracer
│   └── strategy/
│       ├── discount/  DiscountStrategy + 4 chiến lược, PriceCalculator (Context), DiscountStrategyFactory
│       └── payment/   PaymentStrategy + 3 chiến lược, PaymentProcessor (Context)
├── config/          SecurityConfig, PatternConfig, CustomUserDetailsService, DataSeeder
├── entity/          User, Category, Product, Topping, Promotion, Order, OrderItem + enum
├── repository/      Spring Data JPA
├── service/         UserService, CatalogService, PromotionService, OrderService, StatisticsService
├── controller/      Menu, Cart, Order, Staff, Profile, Auth, PriceApi, admin/*
├── dto/             RegisterForm, CheckoutForm, UserForm
└── web/             Cart (giỏ hàng session), GlobalModelAdvice
src/main/resources/
├── templates/       giao diện Thymeleaf (Bootstrap 5)
├── static/css/      style.css
├── static/images/payment/  mã QR Vietcombank & MoMo
├── static/images/products/ ảnh 11 món mặc định
└── application.properties
database/trasua_db.sql   bản xuất đầy đủ CSDL (cấu trúc + dữ liệu)
uploads/products/        ảnh món do quản lý upload (tự tạo khi chạy)
chay_web.bat             bấm đúp để chạy web trên Windows (tự tạo + nạp database)
xuat_database.bat        xuất lại database ra database/trasua_db.sql
day_len_github.bat       đẩy dự án lên GitHub (cần cài Git)
cau_hinh_rieng.properties.example  mẫu cấu hình riêng (mật khẩu DB, Google) – không lên GitHub
```

## 4. Sơ đồ CSDL

```
users (id, username, password, full_name, phone, email, address, role, enabled, created_at)
categories (id, name, description)
products (id, name, description, category_id → categories, price_m, price_l, image_url, rating_avg, rating_count, available)
toppings (id, name, price, available)
promotions (id, code, name, type, discount_value, max_discount, min_order_amount, buy_quantity, active, start_date, end_date)
orders (id, order_code, customer_id → users, staff_id → users, customer_name, phone, order_type,
        delivery_address, note, status, subtotal, promotion_code, discount_description, discount_amount,
        total, payment_method, payment_status, payment_info, cash_received, change_amount, created_at, updated_at)
reviews (id, product_id → products, user_id → users, rating 1-5, comment, visible, created_at, updated_at)
        UNIQUE (product_id, user_id) – mỗi khách 1 đánh giá / món
order_items (id, order_id → orders, product_id, product_name, size, sugar_level, ice_level, toppings,
             description, quantity, unit_price, line_total, note)
```

## 5. Lỗi thường gặp

| Lỗi | Cách sửa |
|---|---|
| `password authentication failed for user "postgres"` | Sửa `spring.datasource.password` trong `cau_hinh_rieng.properties` cho đúng mật khẩu PostgreSQL |
| `database "trasua_db" does not exist` | Chạy `chay_web.bat`, hoặc tạo database `trasua_db` trong pgAdmin rồi nạp `database/trasua_db.sql` |
| `Port 8080 was already in use` | Thêm `server.port=8081` vào `application.properties` |
| Muốn làm lại dữ liệu từ đầu | Chạy lại `database/trasua_db.sql` (file tự xoá bảng cũ rồi tạo lại) |
