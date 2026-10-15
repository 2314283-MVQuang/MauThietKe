# 📋 SPEC – Đặc tả & phân công công việc (giai đoạn 2)

Dự án: **Web quản lý quán trà sữa "Trà Sữa Thôi"** – Spring Boot 3.5 + Thymeleaf + PostgreSQL
Môn: **Mẫu thiết kế** – bắt buộc thể hiện **Builder**, **Decorator**, **Strategy**.
Repo: https://github.com/2314283-MVQuang/MauThietKe

---

## 1. Hiện trạng (giai đoạn 1 – đã xong)

| Đã có | Vị trí trong code |
|---|---|
| Phân quyền 3 vai trò ADMIN / STAFF / CUSTOMER, đăng nhập thường + Google | `config/SecurityConfig.java`, `config/GoogleLogin*.java` |
| Menu, chi tiết món, tuỳ chỉnh ly (size, đường, đá, topping), giỏ hàng, đặt hàng | `MenuController`, `CartController`, `OrderController` |
| Thanh toán tiền mặt / QR Vietcombank / QR MoMo | `pattern/strategy/payment/*` |
| Mã khuyến mãi | `pattern/strategy/discount/*` |
| Nhân viên xử lý đơn, bán tại quầy | `StaffController` → `/staff/orders` |
| Quản lý: thống kê, đồ uống, danh mục, topping, khuyến mãi, đánh giá, tài khoản | `controller/admin/*` |
| Đánh giá sao + nhận xét | `ReviewController`, `ReviewService` |
| 3 mẫu thiết kế + unit test | `pattern/builder`, `pattern/decorator`, `pattern/strategy`, `src/test/java/com/trasua/pattern/` |

Cách chạy: xem `README.md` (bấm đúp `chay_web.bat`).

---

## 2. Bảng phân công

| Thành viên | Phụ trách | Vai trò liên quan | Mẫu thiết kế phải áp dụng | Nhánh Git |
|---|---|---|---|---|
| **Trung** | Chatbox tư vấn chọn nước | Khách (mọi trang) | Strategy (+ Builder cho câu trả lời) | `feature/chatbox` |
| **Tuấn** | Chấm công & tính lương cho nhân viên | STAFF | Strategy (+ Decorator cho phụ cấp/thưởng) | `feature/tinh-luong` |
| **Thuận** | Tách *Quản lý khách hàng* và *Quản lý nhân viên* (lương phải trả, thống kê giờ làm) | ADMIN | Strategy (hạng khách hàng) + dùng lại phần lương của Tuấn | `feature/quan-ly-kh-nv` |
| **Tiến** | Thiết kế lại giao diện cho đẹp, tích hợp code của cả nhóm, **push lên GitHub** | Tất cả | – (đảm bảo giao diện các phần mới) | `feature/giao-dien` → `main` |

**Phụ thuộc giữa các phần**

```
Tuấn  (entity WorkShift + PayrollService)  ──►  Thuận (màn hình Quản lý nhân viên dùng PayrollService)
Trung (chatbox)                              ──►  độc lập
Tiến  (giao diện)                            ──►  làm song song, cuối cùng gộp tất cả vào main và push
```
➡ **Tuấn push phần entity + `PayrollService` sớm nhất** (kể cả khi chưa có giao diện) để Thuận dùng.

---

## 3. Quy ước chung cho cả nhóm

1. **Git**
   - Lấy code mới nhất trước khi làm: `git pull origin main`.
   - Mỗi người làm trên **nhánh riêng** (cột "Nhánh Git" ở trên), commit nhỏ, lời commit rõ ràng (VD: `Tuan: them trang cham cong`).
   - Xong thì `git push origin <ten-nhanh>` và tạo **Pull Request** vào `main`. **Tiến** review và merge.
   - **Không bao giờ** commit file `cau_hinh_rieng.properties` (mật khẩu DB, Google secret). File này đã nằm trong `.gitignore`.
2. **Database**: không sửa bảng bằng tay trong pgAdmin. Thêm/sửa lớp `@Entity` → Hibernate tự tạo cột (`ddl-auto=update`).
   Dữ liệu mẫu mới thì thêm vào `config/DataSeeder.java`. Khi gộp xong, Tiến chạy `xuat_database.bat` để cập nhật `database/trasua_db.sql`.
3. **Mẫu thiết kế**: mỗi phần mới phải có ít nhất 1 mẫu, đặt trong `src/main/java/com/trasua/pattern/...`, có chú thích tiếng Việt
   giải thích vai trò từng lớp (giống các file pattern hiện có) và có unit test trong `src/test/java/com/trasua/pattern/`.
   Thêm mục giải thích vào `GIAI_THICH_MAU_THIET_KE.md`.
4. **Giao diện**: dùng `fragments/layout.html` (head, navbar, alerts, footer) và các biến màu trong `static/css/style.css`
   (`--leaf`, `--peach`, `--berry`, `--taro`, `--honey`, `--ink`…). Không viết màu cứng trong HTML.
   CSS của ai thì viết trong **khối riêng** có tiêu đề, VD `/* ===== CHATBOX (Trung) ===== */`, để tránh xung đột khi gộp.
5. **File dễ xung đột**: `fragments/layout.html`, `style.css`, `SecurityConfig.java`, `DataSeeder.java`.
   Chỉ thêm đúng dòng cần thiết, không định dạng lại cả file.
6. **Form POST** phải có CSRF (Thymeleaf `th:action` tự thêm). Gọi `fetch` POST bằng JavaScript thì gửi kèm token
   (thêm `<meta name="_csrf" th:content="${_csrf.token}">` và `<meta name="_csrf_header" th:content="${_csrf.headerName}">` vào `head`).

---

## 4. Chi tiết từng phần

### 4.1 🤖 Trung – Chatbox tư vấn nước

**Mục tiêu:** khách hỏi bằng tiếng Việt → chatbox gợi ý món phù hợp từ menu thật của quán.

**Giao diện**
- Nút tròn nổi ở góc phải dưới trên **mọi trang** (tạo `templates/fragments/chatbox.html`, nhúng vào `layout.html` phần footer).
- Bấm → mở khung chat: lời chào, lịch sử tin nhắn (lưu trong `sessionStorage`), ô nhập, nút gửi.
- Các nút gợi ý nhanh: *Món ít ngọt* · *Món đậm trà* · *Món béo* · *Best seller* · *Món signature* · *Dưới 30.000đ*.
- Câu trả lời có thẻ món: ảnh, tên, giá size M, số sao, nút **Xem món** (`/products/{id}`).

**Backend**
- `POST /api/chat` (đã nằm trong `/api/**` được phép truy cập) — nhận `{ "message": "..." }`, trả:
  ```json
  { "text": "Bạn thích ít ngọt thì thử mấy món này nhé:",
    "products": [ { "id": 3, "name": "...", "price": 30000, "imageUrl": "...", "rating": 4.6 } ],
    "suggestions": ["Món đậm trà", "Dưới 30.000đ"] }
  ```
- Dữ liệu dùng để tư vấn có sẵn trong `Product`: `tasteSweet`, `tasteStrength`, `tasteCreamy` (1–5), `category`, `priceM`, `ratingAvg`, `signature`, `available`.
- Chỉ gợi ý món đang bán (`available = true`), tối đa 3 món mỗi câu, sắp theo điểm sao.
- Nhận diện câu hỏi **không phân biệt dấu** ("it ngot" = "ít ngọt").
- Không cần AI bên ngoài (chạy offline, không cần API key).

**Mẫu thiết kế**
- **Strategy**: interface `ChatReplyStrategy { boolean supports(ChatContext ctx); ChatReply reply(ChatContext ctx); }`
  với các chiến lược: `ChaoHoiStrategy`, `GoiYTheoKhauViStrategy` (ngọt/đậm/béo), `GoiYTheoGiaStrategy`, `BestSellerStrategy`,
  `SignatureStrategy`, `MacDinhStrategy` (không hiểu → gợi ý các nút).
  Lớp Context `ChatAdvisor` duyệt danh sách chiến lược và chọn cái đầu tiên `supports()` → giống `PaymentProcessor` hiện có.
- **Builder**: `ChatReplyBuilder` dựng câu trả lời (`text`, `products`, `suggestions`).

**File dự kiến:** `pattern/strategy/chat/*`, `service/ChatService.java`, `controller/ChatApiController.java`,
`templates/fragments/chatbox.html`, `static/js/chatbox.js`, CSS trong khối `/* ===== CHATBOX (Trung) ===== */`.

**Nghiệm thu**
- [ ] "xin chào" → lời chào + nút gợi ý.
- [ ] "món nào ít ngọt" → 3 món có `tasteSweet` thấp nhất.
- [ ] "dưới 30k" → chỉ món có giá M ≤ 30.000đ.
- [ ] "món đặc trưng của quán" → các món signature.
- [ ] Câu lạ ("abc xyz") → trả lời mặc định, không lỗi.
- [ ] Unit test cho từng Strategy; chatbox dùng được trên điện thoại (màn hình 375px).

---

### 4.2 ⏱️ Tuấn – Chấm công & tính lương (role NHÂN VIÊN)

**Mục tiêu:** nhân viên biết **mình làm bao nhiêu giờ** và **được bao nhiêu tiền** trong tháng.

**Dữ liệu (Tuấn tạo, Thuận dùng lại)**
- Thêm vào `User`:
  - `employmentType`: enum `EmploymentType { PART_TIME, FULL_TIME }` (mặc định `PART_TIME`)
  - `hourlyRate`: `BigDecimal` – lương theo giờ (VD 25.000đ/giờ)
  - `baseSalary`: `BigDecimal` – lương cứng/tháng (chỉ dùng cho FULL_TIME, có thể để trống)
- Entity mới `WorkShift` (bảng `work_shifts`): `id`, `staff` (→ `User`), `checkIn`, `checkOut` (null khi đang làm),
  `hourlyRate` (chụp lại đơn giá **tại thời điểm làm** để sau này đổi lương không làm sai số cũ), `note`, `editedBy`, `editReason`.
- `WorkShiftRepository`: tìm ca theo nhân viên + tháng, tìm ca đang mở.

**Chức năng cho nhân viên** – trang `/staff/timesheet` (thêm link "Chấm công" trên navbar cho STAFF)
- Nút **Vào ca** / **Kết ca** (mỗi lúc chỉ có 1 ca đang mở; không kết ca khi chưa vào ca).
- Chọn tháng (mặc định tháng hiện tại) → bảng các ca: ngày, giờ vào, giờ ra, số giờ, thành tiền.
- Ô tổng: **tổng số giờ**, **số ca**, **lương tạm tính** của tháng.
- Ca dài hơn 12 tiếng hoặc quên kết ca → đánh dấu cảnh báo (quản lý sửa ở phần của Thuận).
- Số giờ tính theo phút, làm tròn hiển thị 2 chữ số (VD 4,25 giờ).

**Tính lương – `PayrollService` (Thuận gọi lại, KHÔNG viết lại)**
```java
PayrollSummary calculate(User staff, YearMonth month);           // 1 nhân viên
List<PayrollSummary> calculateAll(YearMonth month);              // tất cả nhân viên (cho Thuận)
// PayrollSummary: staff, month, shiftCount, totalMinutes, totalHours, amount, List<WorkShift> shifts
```

**Mẫu thiết kế**
- **Strategy**: `SalaryStrategy` – `PartTimeHourlyStrategy` (tổng giờ × đơn giá từng ca),
  `FullTimeMonthlyStrategy` (lương cứng + giờ vượt 208 giờ/tháng × đơn giá × 1,5). Chọn chiến lược theo `employmentType`.
- **Decorator** (khuyến khích): cộng thêm vào lương gốc, VD `PhuCapCuoiTuanDecorator` (giờ làm Thứ 7/CN × 1,2),
  `ThuongChuyenCanDecorator` (đủ 20 ca/tháng +200.000đ) – giống cách `ToppingDecorator` cộng giá vào ly.

**File dự kiến:** `entity/WorkShift.java`, `entity/EmploymentType.java`, `repository/WorkShiftRepository.java`,
`service/PayrollService.java`, `pattern/strategy/salary/*`, (`pattern/decorator/salary/*`), `controller/TimesheetController.java`,
`templates/staff/timesheet.html`; thêm dữ liệu ca làm mẫu cho `nhanvien` trong `DataSeeder`.

**Nghiệm thu**
- [ ] Ví dụ: part-time 25.000đ/giờ, 3 ca × 4 giờ → hiển thị **12 giờ – 300.000đ**.
- [ ] Đổi `hourlyRate` sau đó không làm thay đổi tiền của các ca đã làm.
- [ ] Nhân viên chỉ xem được ca của **chính mình**; khách hàng không vào được `/staff/timesheet`.
- [ ] Unit test cho từng `SalaryStrategy` (và Decorator nếu có).

---

### 4.3 👥 Thuận – Quản lý khách hàng & Quản lý nhân viên (role QUẢN LÝ)

**Mục tiêu:** tách trang "Tài khoản" (`/admin/users`) hiện tại thành **2 phần riêng**. Phần nhân viên cho quản lý biết
**tháng này phải trả bao nhiêu tiền lương** (nhân viên part-time) và **thống kê giờ làm**.

**Navbar quản lý:** thay mục "Tài khoản" bằng 2 mục **Khách hàng** và **Nhân viên**
(`/admin/users` vẫn giữ để đổi vai trò / khoá tài khoản, hoặc chuyển hướng sang 2 trang mới).

**A. Quản lý khách hàng – `/admin/customers`**
- Danh sách khách hàng (role CUSTOMER): họ tên, SĐT, email, ngày tham gia, **số đơn**, **tổng chi tiêu**, đơn gần nhất, trạng thái.
- Tìm theo tên/SĐT/email; sắp theo tổng chi tiêu; khoá / mở khoá.
- Trang chi tiết khách: thông tin, lịch sử đơn hàng, các đánh giá đã viết.
- Hạng khách hàng **Đồng / Bạc / Vàng** theo tổng chi tiêu → **Strategy** `CustomerTierStrategy`
  (có thể đổi cách xếp hạng: theo tổng tiền hoặc theo số đơn).

**B. Quản lý nhân viên – `/admin/staff`**
- Danh sách nhân viên: họ tên, SĐT, loại (part-time/full-time), lương/giờ, trạng thái; thêm/sửa nhân viên
  (sửa được `employmentType`, `hourlyRate`, `baseSalary` – các trường do Tuấn thêm).
- **Bảng lương tháng** – chọn tháng → mỗi nhân viên: số ca, tổng giờ, đơn giá, **lương phải trả**, trạng thái *Chưa trả / Đã trả*.
  Dòng tổng ở trên cùng: **Tổng tiền lương phải trả tháng này**. Số liệu lấy từ `PayrollService.calculateAll(month)` của Tuấn.
- Nút **Đã trả lương** → lưu entity `PayrollRecord` (`staff`, `month`, `totalHours`, `amount`, `paidAt`, `paidBy`)
  để chốt số liệu (không bị đổi khi sửa ca sau đó).
- Chi tiết nhân viên: danh sách ca trong tháng; quản lý **sửa giờ vào/ra** của ca bị quên kết ca (bắt buộc nhập lý do).
- **Thống kê giờ làm**: tổng giờ theo nhân viên trong tháng, số giờ theo từng ngày; ai làm nhiều/ít nhất.
- Trang Thống kê (`/admin`) thêm thẻ **"Lương phải trả tháng này"**.
- (Tuỳ chọn) Xuất bảng lương ra file CSV.

**File dự kiến:** `controller/admin/AdminCustomerController.java`, `controller/admin/AdminStaffController.java`,
`service/CustomerService.java`, `entity/PayrollRecord.java`, `repository/PayrollRecordRepository.java`,
`pattern/strategy/tier/*`, `templates/admin/customers.html`, `customer-detail.html`, `staff.html`, `staff-detail.html`, `payroll.html`.

**Nghiệm thu**
- [ ] Trang khách hàng không hiện nhân viên/quản lý và ngược lại.
- [ ] Tổng lương tháng = tổng cột "lương phải trả" = số tính ở trang `/staff/timesheet` của từng nhân viên.
- [ ] Đã bấm "Đã trả lương" thì số tiền tháng đó không đổi dù sửa đơn giá.
- [ ] Chỉ ADMIN vào được `/admin/customers`, `/admin/staff` (đã có sẵn quy tắc `/admin/**` trong `SecurityConfig`).

---

### 4.4 🎨 Tiến – Giao diện & đưa lên GitHub

**Mục tiêu:** toàn bộ web đẹp, đồng bộ với trang chủ mới; gộp code cả nhóm và **push lên GitHub**.

**Giao diện**
- Làm đẹp các trang chưa được thiết kế lại: **quản lý** (`admin/*`: thống kê, đồ uống, danh mục, topping, khuyến mãi, đánh giá, tài khoản),
  **nhân viên** (`staff/orders`), **giỏ hàng**, **chi tiết đơn**, **đơn của tôi**, **hồ sơ**, **trang lỗi** (`error.html`, `403.html`).
- Khu quản lý dùng bố cục có **sidebar** bên trái, bảng/thẻ/badge/nút cùng một kiểu, có trạng thái trống ("Chưa có dữ liệu").
- Trang Thống kê: thêm biểu đồ doanh thu 7 ngày, top món (VD Chart.js từ cdnjs).
- Hiển thị tốt trên điện thoại (375px) và máy tính; chữ đủ tương phản; ảnh có `alt`.
- Áp cùng phong cách cho các trang mới của **Trung** (chatbox), **Tuấn** (chấm công), **Thuận** (khách hàng, nhân viên, bảng lương).
- Giữ hiệu ứng chuyển trang và `prefers-reduced-motion` đang có trong `style.css` / `motion.js`.

**Tích hợp & push GitHub**
1. Review Pull Request của từng bạn; merge theo thứ tự: **Tuấn → Thuận → Trung → Giao diện (Tiến)**.
2. Gỡ xung đột ở các file dùng chung (mục 3.5).
3. Chạy `chay_web.bat`, đi hết checklist ở mục 5 với cả 3 tài khoản demo.
4. Chạy `xuat_database.bat` để cập nhật `database/trasua_db.sql` (dữ liệu mẫu mới: ca làm, bảng lương…).
5. Cập nhật `README.md` (chức năng mới, ảnh chụp màn hình trong `docs/screenshots/`).
6. Push lên `main` (`day_len_github.bat` hoặc `git push origin main`) và gắn tag `v2.0`.
7. Kiểm tra trên GitHub: **không có** `cau_hinh_rieng.properties`, `target/`, file `.zip`, `.log`.

**Nghiệm thu**
- [ ] Clone repo về một máy khác → chạy `chay_web.bat` → web lên đầy đủ dữ liệu.
- [ ] Có ảnh chụp trước/sau cho các trang đã làm lại.

---

## 5. Checklist kiểm thử chung (trước khi nộp)

| Tài khoản | Kiểm tra |
|---|---|
| Khách vãng lai | Xem menu, chi tiết món, chatbox gợi ý đúng món |
| `khachhang` / `kh123` | Đặt món, thanh toán QR, đánh giá; **không** vào được `/staff/**`, `/admin/**` |
| `nhanvien` / `nv123` | Xử lý đơn; Vào ca → Kết ca; xem đúng số giờ & lương tháng |
| `admin` / `admin123` | Quản lý khách hàng, nhân viên, bảng lương tháng, đánh dấu đã trả; thống kê |
| Tất cả | Giao diện đẹp trên máy tính & điện thoại; `mvn test` chạy qua hết |
