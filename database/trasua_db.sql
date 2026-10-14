-- =====================================================================
--  CSDL QUẢN LÝ QUÁN TRÀ SỮA (trasua_db) - PostgreSQL
--  Bản xuất đầy đủ (cấu trúc + dữ liệu) từ database đang chạy.
-- =====================================================================
--  CÁCH NẠP:
--   * Tự động: chạy chay_web.bat - nếu máy chưa có database trasua_db,
--     file .bat sẽ tạo database và nạp file này.
--   * Thủ công bằng pgAdmin: tạo database trasua_db -> chọn nó ->
--     Tools -> Query Tool -> mở file này -> Execute (F5).
--     (File có DROP ... IF EXISTS ở đầu nên chạy lại nhiều lần cũng được.)
--
--  Tài khoản mẫu (mật khẩu đã mã hoá BCrypt):
--    admin / admin123      - Quản lý   (ADMIN)
--    nhanvien / nv123      - Nhân viên (STAFF)
--    khachhang / kh123     - Khách hàng (CUSTOMER)
--    lan.nguyen, minh.tran / kh123 - khách mẫu (tác giả đánh giá mẫu)
--
--  Xuất lại file này sau khi thay đổi dữ liệu: chạy xuat_database.bat
-- =====================================================================
--
-- PostgreSQL database dump
--


-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.products DROP CONSTRAINT IF EXISTS products_category_id_fkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS orders_staff_id_fkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS orders_customer_id_fkey;
ALTER TABLE IF EXISTS ONLY public.order_items DROP CONSTRAINT IF EXISTS order_items_order_id_fkey;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS fkpl51cejpw4gy5swfar8br9ngi;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS fkcgy7qjc1r99dp117y9en6lxye;
DROP INDEX IF EXISTS public.idx_orders_status;
DROP INDEX IF EXISTS public.idx_orders_customer;
DROP INDEX IF EXISTS public.idx_orders_created;
DROP INDEX IF EXISTS public.idx_order_items_order;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_username_key;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS uk1nv3auyahyyy79hvtrcqgtfo9;
ALTER TABLE IF EXISTS ONLY public.toppings DROP CONSTRAINT IF EXISTS toppings_pkey;
ALTER TABLE IF EXISTS ONLY public.toppings DROP CONSTRAINT IF EXISTS toppings_name_key;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS reviews_pkey;
ALTER TABLE IF EXISTS ONLY public.promotions DROP CONSTRAINT IF EXISTS promotions_pkey;
ALTER TABLE IF EXISTS ONLY public.promotions DROP CONSTRAINT IF EXISTS promotions_code_key;
ALTER TABLE IF EXISTS ONLY public.products DROP CONSTRAINT IF EXISTS products_pkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS orders_pkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS orders_order_code_key;
ALTER TABLE IF EXISTS ONLY public.order_items DROP CONSTRAINT IF EXISTS order_items_pkey;
ALTER TABLE IF EXISTS ONLY public.categories DROP CONSTRAINT IF EXISTS categories_pkey;
ALTER TABLE IF EXISTS ONLY public.categories DROP CONSTRAINT IF EXISTS categories_name_key;
ALTER TABLE IF EXISTS public.users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.toppings ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.promotions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.products ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.orders ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.order_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.categories ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.users_id_seq;
DROP TABLE IF EXISTS public.users;
DROP SEQUENCE IF EXISTS public.toppings_id_seq;
DROP TABLE IF EXISTS public.toppings;
DROP TABLE IF EXISTS public.reviews;
DROP SEQUENCE IF EXISTS public.promotions_id_seq;
DROP TABLE IF EXISTS public.promotions;
DROP SEQUENCE IF EXISTS public.products_id_seq;
DROP TABLE IF EXISTS public.products;
DROP SEQUENCE IF EXISTS public.orders_id_seq;
DROP TABLE IF EXISTS public.orders;
DROP SEQUENCE IF EXISTS public.order_items_id_seq;
DROP TABLE IF EXISTS public.order_items;
DROP SEQUENCE IF EXISTS public.categories_id_seq;
DROP TABLE IF EXISTS public.categories;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255)
);


--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    product_id bigint,
    product_name character varying(150) NOT NULL,
    size character varying(5) NOT NULL,
    sugar_level integer NOT NULL,
    ice_level integer NOT NULL,
    toppings character varying(500),
    description character varying(1000),
    quantity integer NOT NULL,
    unit_price numeric(12,2) NOT NULL,
    line_total numeric(12,2) NOT NULL,
    note character varying(255),
    CONSTRAINT order_items_quantity_check CHECK ((quantity > 0)),
    CONSTRAINT order_items_size_check CHECK (((size)::text = ANY ((ARRAY['M'::character varying, 'L'::character varying])::text[])))
);


--
-- Name: order_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.order_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: order_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.order_items_id_seq OWNED BY public.order_items.id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    id bigint NOT NULL,
    order_code character varying(30) NOT NULL,
    customer_id bigint,
    staff_id bigint,
    customer_name character varying(100) NOT NULL,
    phone character varying(20),
    order_type character varying(20) NOT NULL,
    delivery_address character varying(255),
    note character varying(500),
    status character varying(20) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    promotion_code character varying(30),
    discount_description character varying(255),
    discount_amount numeric(12,2) DEFAULT 0 NOT NULL,
    total numeric(12,2) NOT NULL,
    payment_method character varying(20) NOT NULL,
    payment_status character varying(20) NOT NULL,
    payment_info character varying(500),
    cash_received numeric(12,2),
    change_amount numeric(12,2),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT orders_order_type_check CHECK (((order_type)::text = ANY ((ARRAY['TAI_QUAN'::character varying, 'MANG_DI'::character varying, 'GIAO_HANG'::character varying])::text[]))),
    CONSTRAINT orders_payment_method_check CHECK (((payment_method)::text = ANY ((ARRAY['TIEN_MAT'::character varying, 'CHUYEN_KHOAN'::character varying, 'VI_MOMO'::character varying])::text[]))),
    CONSTRAINT orders_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['CHUA_THANH_TOAN'::character varying, 'DA_THANH_TOAN'::character varying])::text[]))),
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['CHO_XAC_NHAN'::character varying, 'DA_XAC_NHAN'::character varying, 'DANG_PHA_CHE'::character varying, 'DANG_GIAO'::character varying, 'HOAN_THANH'::character varying, 'DA_HUY'::character varying])::text[])))
);


--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.orders_id_seq OWNED BY public.orders.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id bigint NOT NULL,
    name character varying(150) NOT NULL,
    description character varying(500),
    category_id bigint NOT NULL,
    price_m numeric(12,2) NOT NULL,
    price_l numeric(12,2) NOT NULL,
    image_url character varying(500),
    available boolean DEFAULT true NOT NULL,
    rating_avg double precision DEFAULT 0 NOT NULL,
    rating_count integer DEFAULT 0 NOT NULL,
    ingredients character varying(500),
    signature boolean DEFAULT false NOT NULL,
    story character varying(2000),
    suggestion character varying(255),
    tagline character varying(200),
    taste_creamy integer DEFAULT 0 NOT NULL,
    taste_strength integer DEFAULT 0 NOT NULL,
    taste_sweet integer DEFAULT 0 NOT NULL,
    CONSTRAINT products_price_l_check CHECK ((price_l > (0)::numeric)),
    CONSTRAINT products_price_m_check CHECK ((price_m > (0)::numeric))
);


--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: promotions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.promotions (
    id bigint NOT NULL,
    code character varying(30) NOT NULL,
    name character varying(150) NOT NULL,
    type character varying(20) NOT NULL,
    discount_value numeric(12,2),
    max_discount numeric(12,2),
    min_order_amount numeric(12,2),
    buy_quantity integer,
    active boolean DEFAULT true NOT NULL,
    start_date date,
    end_date date,
    CONSTRAINT promotions_type_check CHECK (((type)::text = ANY ((ARRAY['PHAN_TRAM'::character varying, 'SO_TIEN'::character varying, 'MUA_X_TANG_1'::character varying])::text[])))
);


--
-- Name: promotions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.promotions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: promotions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.promotions_id_seq OWNED BY public.promotions.id;


--
-- Name: reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reviews (
    id bigint NOT NULL,
    comment character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    rating integer NOT NULL,
    updated_at timestamp(6) without time zone,
    visible boolean NOT NULL,
    product_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: reviews_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.reviews ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.reviews_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: toppings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.toppings (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    price numeric(12,2) NOT NULL,
    available boolean DEFAULT true NOT NULL,
    CONSTRAINT toppings_price_check CHECK ((price >= (0)::numeric))
);


--
-- Name: toppings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.toppings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: toppings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.toppings_id_seq OWNED BY public.toppings.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(100) NOT NULL,
    full_name character varying(100) NOT NULL,
    phone character varying(20),
    email character varying(100),
    address character varying(255),
    role character varying(20) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'STAFF'::character varying, 'CUSTOMER'::character varying])::text[])))
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: order_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items ALTER COLUMN id SET DEFAULT nextval('public.order_items_id_seq'::regclass);


--
-- Name: orders id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders ALTER COLUMN id SET DEFAULT nextval('public.orders_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: promotions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promotions ALTER COLUMN id SET DEFAULT nextval('public.promotions_id_seq'::regclass);


--
-- Name: toppings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.toppings ALTER COLUMN id SET DEFAULT nextval('public.toppings_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.categories (id, name, description) FROM stdin;
1	Trà sữa	Trà sữa pha từ trà nguyên lá
2	Trà trái cây	Trà tươi mát với trái cây thật
3	Cà phê	Cà phê Robusta & Arabica Lâm Đồng
4	Đá xay	Đồ uống đá xay mát lạnh
\.


--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.order_items (id, order_id, product_id, product_name, size, sugar_level, ice_level, toppings, description, quantity, unit_price, line_total, note) FROM stdin;
1	1	2	Trà sữa trân châu đường đen	L	50	70	Kem cheese	Trà sữa trân châu đường đen (Size L) + Kem cheese [Đường 50%, Đá 70%]	1	52000.00	52000.00	\N
2	1	8	Cà phê sữa đá	M	70	70	\N	Cà phê sữa đá (Size M) [Đường 70%, Đá 70%]	1	20000.00	20000.00	\N
3	2	1	Trà sữa truyền thống	M	70	70	\N	Trà sữa truyền thống (Size M) [Đường 70%, Đá 70%]	2	25000.00	50000.00	\N
4	3	9	Bạc xỉu	M	70	70	\N	Bạc xỉu (Size M) [Đường 70%, Đá 70%]	1	25000.00	25000.00	\N
5	4	9	Bạc xỉu	L	50	100	Thạch phô mai	Bạc xỉu (Size L) + Thạch phô mai [Đường 50%, Đá 100%]	1	38000.00	38000.00	\N
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.orders (id, order_code, customer_id, staff_id, customer_name, phone, order_type, delivery_address, note, status, subtotal, promotion_code, discount_description, discount_amount, total, payment_method, payment_status, payment_info, cash_received, change_amount, created_at, updated_at) FROM stdin;
1	TS-MAU-001	3	\N	Trần Thị Khách	0901000003	MANG_DI	\N	\N	HOAN_THANH	72000.00	GIAM10	Giảm 10% (tối đa 20.000đ)	7200.00	64800.00	VI_MOMO	DA_THANH_TOAN	Đã thanh toán qua MoMo (giả lập)	\N	\N	2026-10-13 12:16:13.138052	2026-10-13 12:16:13.138052
2	TS-MAU-002	\N	2	Khách lẻ	\N	TAI_QUAN	\N	\N	HOAN_THANH	50000.00	\N	\N	0.00	50000.00	TIEN_MAT	DA_THANH_TOAN	Khách đưa 100.000đ, thối lại 50.000đ	\N	\N	2026-10-14 10:16:13.138052	2026-10-14 10:16:13.138052
3	TS261014122450685	3	\N	Trần Thị Khách	0901000003	MANG_DI	\N	\N	HOAN_THANH	25000.00	\N	\N	0.00	25000.00	TIEN_MAT	DA_THANH_TOAN	Thanh toán tiền mặt khi nhận hàng: 25.000đ	\N	\N	2026-10-14 12:24:50.916487	2026-10-14 12:25:52.146621
4	TS261014122750587	\N	2	Khách lẻ	\N	TAI_QUAN	\N	\N	HOAN_THANH	38000.00	\N	\N	0.00	38000.00	CHUYEN_KHOAN	DA_THANH_TOAN	Chuyển 38.000đ tới Vietcombank - STK 0123456789 (QUAN TRA SUA DLU) - Nội dung: TS261014122750587	\N	\N	2026-10-14 12:27:50.185744	2026-10-14 12:30:53.19823
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.products (id, name, description, category_id, price_m, price_l, image_url, available, rating_avg, rating_count, ingredients, signature, story, suggestion, tagline, taste_creamy, taste_strength, taste_sweet) FROM stdin;
5	Trà đào cam sả	Trà đen, đào miếng, cam tươi, sả	2	30000.00	37000.00	/images/products/05-tra-dao-cam-sa.jpg	t	4.5	2	Trà đen, Đào ngâm, Cam tươi, Sả, Bạc hà	t	Trà đen ủ cùng sả đập dập, thêm nước cam vắt và những miếng đào ngâm giòn sần sật. Món giải nhiệt được gọi nhiều nhất vào những buổi trưa nắng.	70% đường, ít đá để giữ trọn vị cam	Đào giòn, cam tươi, sả thơm – mát lạnh tức thì.	0	3	3
3	Trà sữa matcha	Matcha Nhật Bản và sữa tươi	1	32000.00	39000.00	/images/products/03-tra-sua-matcha.jpg	t	4	1	Matcha, Sữa tươi, Đậu đỏ	f	Bột matcha được đánh tay với nước ấm cho bọt mịn rồi hoà sữa tươi, phủ đậu đỏ ninh mềm. Vị đắng nhẹ đầu lưỡi, hậu ngọt thanh.	50% đường + kem cheese	Matcha xay mịn, đậu đỏ bùi bùi.	3	4	2
7	Trà dâu tây Đà Lạt	Dâu tây tươi Đà Lạt	2	32000.00	39000.00	/images/products/07-tra-dau-tay-da-lat.jpg	t	5	1	Dâu tây Đà Lạt, Trà xanh, Lá bạc hà	t	Dâu tây Đà Lạt tươi được dầm tay cùng chút đường để giữ thớ quả, rồi hoà vào trà xanh lạnh. Màu đỏ đến từ chính trái dâu, không dùng siro tạo màu.	50% đường + thạch phô mai	Dâu tây Đà Lạt dầm tay, chua ngọt tự nhiên.	0	2	3
1	Trà sữa truyền thống	Hồng trà sữa đậm vị	1	25000.00	32000.00	/images/products/01-tra-sua-truyen-thong.jpg	t	3.5	2	Hồng trà Assam, Sữa tươi, Sữa đặc, Đá viên	f	Hồng trà Assam ủ lạnh qua đêm cho vị chát dịu, hoà cùng sữa tươi và một chút sữa đặc. Đây là ly trà sữa "chuẩn bài" để bạn tự phối topping theo ý thích.	70% đường + trân châu đen	Hồng trà đậm, sữa béo vừa phải – vị quen mà không nhàm.	3	4	3
10	Matcha đá xay	Matcha đá xay phủ kem	4	38000.00	45000.00	/images/products/10-matcha-da-xay.jpg	t	5	1	Matcha, Sữa tươi, Đá xay, Kem tươi	f	Matcha, sữa và đá xay nhuyễn thành lớp sánh mịn như kem, phủ kem tươi vị matcha phía trên.	Thêm trân châu trắng cho vui miệng	Matcha xay mịn mát lạnh, phủ kem bồng bềnh.	4	3	3
2	Trà sữa trân châu đường đen	Trân châu nấu đường đen, sữa tươi	1	35000.00	42000.00	/images/products/02-tra-sua-tran-chau-duong-den.jpg	t	4.7	3	Sữa tươi, Đường đen, Trân châu đen, Kem sữa	t	Trân châu được nấu với đường đen mỗi sáng, múc nóng vào ly để tạo những vệt hổ vằn trên thành ly. Rót sữa tươi lạnh lên trên, lắc nhẹ trước khi uống để vị caramel lan đều.	Không cần thêm đường, chọn size L để nhiều trân châu hơn	Vệt đường đen sóng sánh quanh ly, trân châu dẻo nóng.	5	1	4
9	Bạc xỉu	Nhiều sữa, ít cà phê	3	25000.00	30000.00	/images/products/09-bac-xiu.jpg	t	0	0	Sữa tươi, Sữa đặc, Cà phê phin	f	Lớp sữa tươi và sữa đặc béo ngậy, rót nhẹ cà phê phin lên trên tạo hai tầng màu. Vị cà phê thoang thoảng, hợp để nhâm nhi cả buổi chiều.	Khuấy đều trước khi uống	Nhiều sữa, ít cà phê – dịu nhẹ cho người mới uống.	5	2	4
4	Trà sữa khoai môn	Khoai môn Đà Lạt nghiền	1	30000.00	37000.00	/images/products/04-tra-sua-khoai-mon.jpg	t	0	0	Khoai môn Đà Lạt, Trà sữa, Thạch dừa	f	Khoai môn tím Đà Lạt hấp chín, nghiền thô để còn những miếng bùi trong ly, hoà cùng trà sữa và thạch dừa giòn mát.	50% đường + pudding trứng	Khoai môn Đà Lạt nghiền tay, tím mộng mơ.	4	2	3
6	Trà vải hoa hồng	Trà xanh nhài, vải thiều, hoa hồng	2	30000.00	37000.00	/images/products/06-tra-vai-hoa-hong.jpg	t	0	0	Trà xanh nhài, Vải thiều, Syrup hoa hồng, Cánh hồng khô	f	Trà xanh nhài pha nhẹ, thêm syrup hoa hồng và vải thiều nguyên quả, viền ly rắc cánh hồng khô thơm dịu.	50% đường + thạch dừa	Vải thiều ngọt lịm quyện hương hoa hồng.	0	2	3
11	Cookie đá xay	Bánh quy Oreo đá xay	4	38000.00	45000.00	/images/products/11-cookie-da-xay.jpg	t	0	0	Bánh quy socola, Sữa tươi, Đá xay, Kem tươi	f	Bánh quy socola nghiền vụn xay cùng sữa và đá, rắc thêm bánh vụn lên mặt. Ngọt vừa, béo thơm – món tráng miệng dạng uống.	Size L để chia sẻ cùng bạn	Bánh quy socola giòn tan trong lớp đá xay mát lạnh.	5	1	4
8	Cà phê sữa đá	Phin truyền thống	3	20000.00	25000.00	/images/products/08-ca-phe-sua-da.jpg	t	4.5	2	Cà phê Robusta, Sữa đặc, Đá viên	f	Cà phê Robusta Lâm Đồng rang mộc, pha phin chậm từng giọt rồi khuấy cùng sữa đặc và đá. Đậm, đắng, thơm – đúng chất cà phê Việt.	Size M, 70% đường	Phin Robusta Lâm Đồng đậm đà, sữa đặc béo ngậy.	3	5	3
\.


--
-- Data for Name: promotions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.promotions (id, code, name, type, discount_value, max_discount, min_order_amount, buy_quantity, active, start_date, end_date) FROM stdin;
1	GIAM10	Giảm 10% cho đơn từ 50.000đ	PHAN_TRAM	10.00	20000.00	50000.00	\N	t	2026-10-13	2027-01-12
2	GIAM15K	Giảm 15.000đ cho đơn từ 80.000đ	SO_TIEN	15000.00	\N	80000.00	\N	t	\N	\N
3	MUA2TANG1	Mua 2 tặng 1 ly rẻ nhất	MUA_X_TANG_1	0.00	\N	0.00	2	t	\N	\N
\.


--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.reviews (id, comment, created_at, rating, updated_at, visible, product_id, user_id) FROM stdin;
2	Món best seller của quán, lần nào ghé cũng gọi.	2026-10-14 13:07:42.314866	5	2026-10-14 13:07:42.314866	t	2	4
3	Ngon nhưng hơi ngọt, lần sau chọn 50% đường.	2026-10-14 13:07:42.330867	4	2026-10-14 13:07:42.330867	t	2	5
4	Đậm vị, giá sinh viên.	2026-10-14 13:07:42.345868	4	2026-10-14 13:07:42.345868	t	8	3
5	Cà phê Lâm Đồng chuẩn vị.	2026-10-14 13:07:42.35887	5	2026-10-14 13:07:42.35887	t	8	5
6	Thơm sả, đào giòn, rất mát.	2026-10-14 13:07:42.369865	5	2026-10-14 13:07:42.369865	t	5	4
7	\N	2026-10-14 13:07:42.380866	4	2026-10-14 13:07:42.380866	t	5	5
8	Matcha đậm, thêm kem cheese là hợp.	2026-10-14 13:07:42.387866	4	2026-10-14 13:07:42.387866	t	3	4
9	Dâu tươi thật, chua ngọt vừa phải.	2026-10-14 13:07:42.392865	5	2026-10-14 13:07:42.392865	t	7	5
10	Vị trà rõ, không bị gắt.	2026-10-14 13:07:42.397891	4	2026-10-14 13:07:42.397891	t	1	3
11	Ổn, hơi nhiều đá.	2026-10-14 13:07:42.403881	3	2026-10-14 13:07:42.403881	t	1	4
12	Mịn, béo, ngon.	2026-10-14 13:07:42.41299	5	2026-10-14 13:07:42.41299	t	10	5
1	Trân châu dẻo, đường đen thơm, uống size L đã luôn!	2026-10-14 13:07:42.265468	5	2026-10-14 13:08:54.233864	t	2	3
\.


--
-- Data for Name: toppings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.toppings (id, name, price, available) FROM stdin;
1	Trân châu đen	5000.00	t
2	Trân châu trắng	7000.00	t
3	Thạch dừa	5000.00	t
4	Pudding trứng	8000.00	t
5	Kem cheese	10000.00	t
6	Thạch phô mai	8000.00	t
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, username, password, full_name, phone, email, address, role, enabled, created_at) FROM stdin;
1	admin	$2a$10$6JNy2BTNAbRbIna7kuW1uedxB9dojUpFOAkDK1glg.vGZFe.Fxfb.	Quản lý quán	0901000001	admin@trasua.vn	\N	ADMIN	t	2026-10-14 12:16:13.123504
2	nhanvien	$2a$10$hXq8dPX6V9ou1hhv7fMXVePEhz5sIALg5/83gAhqXiz2MefYo25nq	Nguyễn Văn Nhân	0901000002	\N	\N	STAFF	t	2026-10-14 12:16:13.123504
3	khachhang	$2a$10$nT33f1ElpVPMvLcCbhsdw.xqt.MpAWGHyxAccM4bCwN4UjtFuKmbO	Trần Thị Khách	0901000003	khachhang@gmail.com	01 Phù Đổng Thiên Vương, Phường 8, Đà Lạt	CUSTOMER	t	2026-10-14 12:16:13.123504
4	lan.nguyen	$2a$10$Dqh.TIC6tbf1ES3MkfGFB.zMaFSAkLp3dHtVdJnyGPuApTUWL3lhK	Nguyễn Thị Lan	0901000004	\N	\N	CUSTOMER	t	2026-10-14 13:07:42.112687
5	minh.tran	$2a$10$Hi00viYcL8c8TmxYIFo/lOB9bwdRckHT3tiucQmSrSBZ41LE3cqAO	Trần Văn Minh	0901000005	\N	\N	CUSTOMER	t	2026-10-14 13:07:42.231084
\.


--
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.categories_id_seq', 4, true);


--
-- Name: order_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.order_items_id_seq', 5, true);


--
-- Name: orders_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.orders_id_seq', 4, true);


--
-- Name: products_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.products_id_seq', 11, true);


--
-- Name: promotions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.promotions_id_seq', 3, true);


--
-- Name: reviews_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.reviews_id_seq', 13, true);


--
-- Name: toppings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.toppings_id_seq', 6, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 6, true);


--
-- Name: categories categories_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_name_key UNIQUE (name);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: orders orders_order_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_code_key UNIQUE (order_code);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: promotions promotions_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_code_key UNIQUE (code);


--
-- Name: promotions promotions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_pkey PRIMARY KEY (id);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- Name: toppings toppings_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.toppings
    ADD CONSTRAINT toppings_name_key UNIQUE (name);


--
-- Name: toppings toppings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.toppings
    ADD CONSTRAINT toppings_pkey PRIMARY KEY (id);


--
-- Name: reviews uk1nv3auyahyyy79hvtrcqgtfo9; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT uk1nv3auyahyyy79hvtrcqgtfo9 UNIQUE (product_id, user_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_order_items_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_items_order ON public.order_items USING btree (order_id);


--
-- Name: idx_orders_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_created ON public.orders USING btree (created_at);


--
-- Name: idx_orders_customer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_customer ON public.orders USING btree (customer_id);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: reviews fkcgy7qjc1r99dp117y9en6lxye; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fkcgy7qjc1r99dp117y9en6lxye FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reviews fkpl51cejpw4gy5swfar8br9ngi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fkpl51cejpw4gy5swfar8br9ngi FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: order_items order_items_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- Name: orders orders_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.users(id);


--
-- Name: orders orders_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.users(id);


--
-- Name: products products_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id);


--
-- PostgreSQL database dump complete
--


