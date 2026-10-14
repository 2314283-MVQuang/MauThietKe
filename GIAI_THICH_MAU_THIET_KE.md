# Giải thích 3 mẫu thiết kế trong đồ án Quản lý quán trà sữa

Toàn bộ code của 3 mẫu nằm trong package `com.trasua.pattern` và được viết bằng **Java thuần**
(không phụ thuộc Spring) để dễ đọc, dễ kiểm thử, dễ trình bày. Mỗi lớp đều có chú thích tiếng Việt
ghi rõ **vai trò** của nó trong mẫu (Component, Decorator, Builder, Director, Strategy, Context...).

| Mẫu | Nhóm (GoF) | Bài toán trong quán trà sữa | Package |
|---|---|---|---|
| **Builder** | Creational (khởi tạo) | Tạo ly nước & đơn hàng có rất nhiều tuỳ chọn, nhiều ràng buộc | `pattern.builder` |
| **Decorator** | Structural (cấu trúc) | Ly nước được "thêm thắt" size, topping, đường/đá, giá cộng dồn | `pattern.decorator` |
| **Strategy** | Behavioral (hành vi) | Nhiều cách giảm giá & nhiều cách thanh toán, đổi được lúc chạy | `pattern.strategy` |

---

## 1. DECORATOR PATTERN – tính giá ly nước

### Vấn đề
Một ly trà sữa = đồ uống gốc + size + 0..n topping + mức đường/đá. Nếu dùng **kế thừa** ta phải tạo
lớp cho mọi tổ hợp: `TraSuaSizeL`, `TraSuaSizeLTranChau`, `TraSuaSizeLTranChauPudding`... → **bùng nổ lớp con**,
và thêm 1 topping mới thì phải viết thêm hàng chục lớp.

### Ý tưởng
Gắn thêm trách nhiệm (giá, mô tả) cho đối tượng **lúc chạy** bằng cách **bọc** nó trong một đối tượng
khác có **cùng giao diện**. Có thể bọc nhiều lớp lồng nhau theo thứ tự tuỳ ý.

### Ánh xạ vai trò → lớp

| Vai trò GoF | Lớp | Ghi chú |
|---|---|---|
| Component | `Beverage` (interface) | `getDescription()`, `getCost()` |
| ConcreteComponent | `BaseBeverage` | Ly trơn: tên + giá size M |
| Decorator | `BeverageDecorator` (abstract) | **implements** `Beverage` **và chứa** một `Beverage wrapped` |
| ConcreteDecorator | `SizeDecorator` | + chênh lệch giá size L |
| ConcreteDecorator | `ToppingDecorator` | + giá topping (mỗi topping 1 lớp bọc) |
| ConcreteDecorator | `SugarIceDecorator` | chỉ thêm mô tả, **không đổi giá** |

### Sơ đồ lớp
```
        «interface»
         Beverage  ◄──────────────────────────────┐
     + getDescription()                           │ wrapped (HAS-A)
     + getCost()                                  │
         ▲                     ▲                  │
         │ implements          │ implements       │
   BaseBeverage        BeverageDecorator (abstract)
                               ▲
              ┌────────────────┼────────────────┐
        SizeDecorator   ToppingDecorator   SugarIceDecorator
```

### Code cốt lõi (rút gọn)
```java
public abstract class BeverageDecorator implements Beverage {   // IS-A Beverage
    protected final Beverage wrapped;                           // HAS-A Beverage
    protected BeverageDecorator(Beverage wrapped) { this.wrapped = wrapped; }
}

public class ToppingDecorator extends BeverageDecorator {
    public BigDecimal getCost() {
        return wrapped.getCost().add(toppingPrice);      // giá bên trong + giá topping
    }
    public String getDescription() {
        return wrapped.getDescription() + " + " + toppingName;
    }
}
```

### Ví dụ chạy
```java
Beverage ly = new BaseBeverage("Trà sữa truyền thống", 25000);   // 25.000
ly = new SizeDecorator(ly, DrinkSize.L, 7000);                     // 32.000
ly = new ToppingDecorator(ly, "Trân châu đen", 5000);              // 37.000
ly = new ToppingDecorator(ly, "Kem cheese", 10000);                // 47.000
ly = new SugarIceDecorator(ly, 50, 70);                            // 47.000
ly.getDescription();
// "Trà sữa truyền thống (Size L) + Trân châu đen + Kem cheese [Đường 50%, Đá 70%]"
```
Gọi `getCost()` ở lớp ngoài cùng sẽ **gọi đệ quy vào trong** cho tới lõi rồi cộng dồn ra ngoài.

### Dùng ở đâu trong ứng dụng
- `OrderItemBuilder.buildBeverage()` ráp chuỗi decorator cho mỗi ly khách chọn.
- Trang **chi tiết món** (`/products/{id}`): khung "Cách tính giá từng lớp" gọi API `/api/price-preview`,
  dùng `DecoratorChainTracer` bóc từng lớp bọc → **demo trực quan cho thầy**.
- Cột `description` của bảng `order_items` chính là `getDescription()` của chuỗi decorator.

### Vì sao `ToppingDecorator` dùng chung cho mọi topping?
Topping do quản lý thêm trong CSDL. Nếu viết `TranChauDecorator`, `PuddingDecorator`... thì mỗi lần
thêm topping phải sửa code. Truyền tên + giá vào một `ToppingDecorator` giúp hệ thống **mở để mở rộng,
đóng để sửa đổi** (Open/Closed Principle).

---

## 2. BUILDER PATTERN – tạo ly nước và đơn hàng

### Vấn đề
- `OrderItem` có nhiều tham số tuỳ chọn: size, đường, đá, topping, số lượng, ghi chú.
  Constructor kiểu `new OrderItem(p, L, 50, 70, toppings, 2, "...")` khó đọc (50 là đường hay đá?),
  dễ truyền nhầm thứ tự → anti-pattern **telescoping constructor**.
- `Order` có ~20 thuộc tính và **ràng buộc** giữa chúng (đơn giao hàng phải có địa chỉ + SĐT, đơn tại quầy
  phải do nhân viên tạo, tổng tiền phải = tạm tính − giảm giá, mã đơn tự sinh...). Dùng setter rải rác
  thì rất dễ tạo ra một đơn hàng "nửa vời", sai dữ liệu.

### Ý tưởng
Tách **quá trình xây dựng** một đối tượng phức tạp ra khỏi bản thân đối tượng. Client gọi từng bước
(theo nhu cầu, theo thứ tự tuỳ ý), cuối cùng gọi `build()` – nơi **kiểm tra hợp lệ tập trung** và
tính các trường dẫn xuất.

### Ánh xạ vai trò → lớp

| Vai trò GoF | Lớp | Ghi chú |
|---|---|---|
| Builder / ConcreteBuilder | `OrderItemBuilder` | Dựng 1 ly nước (`OrderItem`) |
| Builder / ConcreteBuilder | `OrderBuilder` | Dựng 1 đơn hàng (`Order`) |
| Director | `DrinkDirector` | Biết "công thức" chuẩn: Mặc định, Ít ngọt, Đặc biệt |
| Product | `OrderItem`, `Order` | Entity JPA |
| Client | `MenuController`, `OrderService` | |

### Code cốt lõi
```java
// Client dùng Builder – đọc như câu văn, chỉ gọi bước cần thiết
OrderItem item = OrderItemBuilder.forProduct(traSua)
        .size(DrinkSize.L)
        .sugar(50)
        .ice(70)
        .addTopping(tranChauDen)
        .quantity(2)
        .build();                // validate + ráp Decorator + tính giá

Order order = OrderBuilder.newOrder()
        .forCustomer(khach)                       // tự lấy tên, SĐT, địa chỉ
        .orderType(OrderType.GIAO_HANG)
        .addItems(gioHang)
        .discountStrategy(strategy, "GIAM10")     // <- Strategy
        .paymentMethod(PaymentMethod.VI_MOMO)
        .build();                                 // validate + tính tiền + sinh mã đơn
```
```java
// Director đóng gói công thức
public OrderItem makeLessSweet(Product product) {
    return OrderItemBuilder.forProduct(product)
            .size(DrinkSize.M).sugar(30).ice(50).quantity(1).note("Ít ngọt")
            .build();
}
```

### Dùng ở đâu trong ứng dụng
- Nút **"Tuỳ chỉnh & đặt"** → `MenuController.addToCart()` dùng `OrderItemBuilder`.
- Ô chọn **"Mặc định / Ít ngọt / Đặc biệt" + nút (+)** ở menu → `MenuController.quickAdd()` dùng `DrinkDirector`.
- **Đặt hàng** (khách online hoặc nhân viên tại quầy) → `OrderService.checkout()` dùng `OrderBuilder`.
  Cùng một Builder nhưng khách gọi `.forCustomer(...)`, nhân viên gọi `.createdByStaff(...).customerName(...)`.

### Các ràng buộc được kiểm tra trong `build()`
- Ly: mức đường/đá ∈ {0, 30, 50, 70, 100}, tối đa 5 topping, số lượng 1–50, món & topping đang bán.
- Đơn: ít nhất 1 món; giao hàng phải có địa chỉ và SĐT hợp lệ; "uống tại quán" chỉ nhân viên tạo;
  đơn tại quầy → `DA_XAC_NHAN`, đơn online → `CHO_XAC_NHAN`.

---

## 3. STRATEGY PATTERN – khuyến mãi & thanh toán

### Vấn đề
Quán có nhiều cách giảm giá (giảm %, giảm tiền cố định, mua X tặng 1...) và nhiều cách thanh toán
(tiền mặt, chuyển khoản, MoMo). Viết bằng `if/else` / `switch` trong một hàm thì mỗi lần thêm kiểu mới
phải sửa hàm cũ → dễ lỗi, vi phạm Open/Closed.

### Ý tưởng
Định nghĩa **một họ thuật toán**, đóng gói mỗi thuật toán vào **một lớp riêng** có chung giao diện, và
cho phép **hoán đổi** chúng lúc chạy. Lớp sử dụng (Context) chỉ biết giao diện.

### Họ #1 – Khuyến mãi

| Vai trò GoF | Lớp |
|---|---|
| Strategy | `DiscountStrategy` (interface) – `calculate(DiscountInput)` |
| ConcreteStrategy | `NoDiscountStrategy` (Null Object), `PercentageDiscountStrategy`, `FixedAmountDiscountStrategy`, `BuyXGetOneFreeStrategy` |
| Context | `PriceCalculator` – giữ 1 strategy, `setDiscountStrategy(...)` để đổi |
| (chọn strategy) | `DiscountStrategyFactory.fromPromotion(promotion)` – dựa vào cột `type` trong bảng `promotions` |

```java
PriceCalculator calculator = new PriceCalculator();            // Context
calculator.setDiscountStrategy(new PercentageDiscountStrategy(10, 20000, 50000));
calculator.calculate(items);   // -> giảm 10%, tối đa 20.000đ

calculator.setDiscountStrategy(new BuyXGetOneFreeStrategy(2));
calculator.calculate(items);   // -> cùng lời gọi, thuật toán khác hẳn (tặng ly rẻ nhất)
```
Bên trong `PriceCalculator.calculate()` **không có một câu if nào** theo loại khuyến mãi:
```java
DiscountResult discount = discountStrategy.calculate(input);   // ủy quyền
```

### Họ #2 – Thanh toán

| Vai trò GoF | Lớp |
|---|---|
| Strategy | `PaymentStrategy` (interface) – `pay(PaymentRequest)` |
| ConcreteStrategy | `CashPaymentStrategy` (tính tiền thối), `BankTransferPaymentStrategy` (QR Vietcombank + nội dung CK), `MomoPaymentStrategy` (QR MoMo) |
| Context | `PaymentProcessor` – bảng tra `PaymentMethod → PaymentStrategy` |

Mỗi chiến lược còn tự cung cấp ảnh QR của mình qua `getQrImageUrl()` (tiền mặt trả về `null`).
Giỏ hàng và trang chi tiết đơn chỉ hỏi chiến lược "QR của bạn là gì?" – không có `if (method == MOMO)` nào trong giao diện.

Spring tự gom tất cả bean `PaymentStrategy` (khai báo trong `config/PatternConfig.java`) truyền vào
`PaymentProcessor`. Thêm ZaloPay chỉ cần: viết `ZaloPayPaymentStrategy` + khai báo 1 `@Bean`.

### Dùng ở đâu trong ứng dụng
- **Giỏ hàng**: nhập mã `GIAM10`, `GIAM15K`, `MUA2TANG1` → `OrderService.previewCart()` hiển thị số tiền giảm.
- **Đặt hàng**: `OrderBuilder` nhận `DiscountStrategy`; sau đó `PaymentProcessor.process(method, ...)`.
- **Quản lý khuyến mãi** (`/admin/promotions`): cột "Chiến lược áp dụng" hiển thị `strategy.describe()`.

---

## 4. Ba mẫu phối hợp trong một lần đặt hàng

```
Khách chọn ly ──► OrderItemBuilder (BUILDER)
                     └─ ráp BaseBeverage → SizeDecorator → ToppingDecorator… (DECORATOR) → đơn giá
                  ──► Giỏ hàng (session)
Bấm "Đặt hàng" ─► OrderService.checkout()
                     ├─ DiscountStrategyFactory → DiscountStrategy (STRATEGY - khuyến mãi)
                     ├─ OrderBuilder.build()  (BUILDER) → PriceCalculator dùng DiscountStrategy
                     ├─ PaymentProcessor → PaymentStrategy (STRATEGY - thanh toán)
                     └─ lưu PostgreSQL
```

## 5. Kịch bản demo cho thầy (≈ 5 phút)

1. Đăng nhập `khachhang / kh123` → bấm **Tuỳ chỉnh & đặt** món "Trà sữa truyền thống".
   Đổi size L, tick 2 topping → khung bên phải hiện từng lớp bọc **(Decorator)**.
2. Ở menu, chọn "Ít ngọt" rồi bấm (+) → thêm nhanh bằng **Director (Builder)**.
3. Vào giỏ: áp mã `GIAM10`, rồi đổi sang `MUA2TANG1` → cùng giỏ hàng, số tiền giảm khác nhau **(Strategy)**.
4. Đăng nhập `nhanvien / nv123` → thêm món, vào "Đơn tại quầy", chọn hình thức "Giao hàng" nhưng để trống
   địa chỉ → bấm Đặt hàng → bị chặn bởi kiểm tra trong `OrderBuilder.build()` **(Builder)**.
5. Đổi sang "Tại quán", thanh toán tiền mặt, nhập tiền khách đưa → tính tiền thối **(CashPaymentStrategy)**;
   vào "Xử lý đơn" chuyển đơn qua các trạng thái.
6. Đăng nhập `admin / admin123` → xem thống kê, thêm topping mới → quay lại menu thấy topping dùng được ngay
   mà không sửa code **(Decorator + Open/Closed)**.
7. Mở `src/test/java/com/trasua/pattern/*Test.java` và chạy `mvn test` – 18 test cho 3 mẫu.

## 6. Câu hỏi thầy có thể hỏi

**Decorator khác kế thừa thế nào?** Kế thừa thêm hành vi lúc *biên dịch* cho cả lớp; Decorator thêm lúc
*chạy* cho từng đối tượng, kết hợp tuỳ ý, tránh bùng nổ lớp con.

**Decorator khác Strategy thế nào?** Decorator thay đổi "vỏ ngoài" (bọc thêm, cộng dồn nhiều lớp);
Strategy thay đổi "ruột" (thay hẳn thuật toán, mỗi lúc chỉ một chiến lược).

**Builder khác Factory thế nào?** Factory tạo đối tượng *trong một bước* và quyết định *tạo lớp nào*;
Builder tạo đối tượng *qua nhiều bước*, cùng một lớp sản phẩm nhưng cấu hình khác nhau.
(Trong đồ án, `DiscountStrategyFactory` là factory đơn giản để *chọn* strategy.)

**Director có bắt buộc không?** Không. Director chỉ hữu ích khi có các "công thức" lặp lại –
ở đây là các nút thêm nhanh.

**Nếu thêm khuyến mãi "giảm 20% sau 21h"?** Viết `HappyHourDiscountStrategy implements DiscountStrategy`,
thêm 1 giá trị enum và 1 nhánh trong `DiscountStrategyFactory`. `PriceCalculator`, `OrderBuilder`,
giỏ hàng **không phải sửa**.
