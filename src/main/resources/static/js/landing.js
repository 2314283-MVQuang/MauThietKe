/* =====================================================================
   TRANG CHỦ (Trung)
   - Hàng neo trong navbar: đánh dấu mục đang xem (.active + aria-current)
   - Nút "Đặt món ngay" cố định trên điện thoại: ẩn khi còn ở hero hoặc đang xem menu
   - Khối "Hỏi nhanh trợ lý": chỉ hiện khi trang đã có chatbox (nút #chatbox-toggle)
   Cuộn mượt do CSS (html { scroll-behavior: smooth }) lo, tự tắt khi người dùng bật "giảm chuyển động".
   ===================================================================== */
(function () {
    const header = document.querySelector('.navbar-ts');
    const root = document.documentElement;

    // Chiều cao navbar (gồm hàng neo) → CSS dùng làm scroll-margin-top cho các mục
    const setOffset = () => root.style.setProperty('--landing-offset', (header ? header.offsetHeight : 0) + 'px');
    setOffset();
    window.addEventListener('resize', setOffset);

    /* ---------- 1. Đánh dấu mục đang xem ---------- */
    const list = document.querySelector('.landing-nav-list');
    const links = list ? [...list.querySelectorAll('a[href^="#"]')] : [];
    const sections = links.map(a => document.getElementById(a.hash.slice(1))).filter(Boolean);
    let current = null;

    const mark = id => {
        if (id === current) return;
        current = id;
        links.forEach(a => {
            const on = a.hash === '#' + id;
            a.classList.toggle('active', on);
            if (on) {
                a.setAttribute('aria-current', 'true');
                // Điện thoại: hàng neo cuộn ngang, đưa mục đang xem vào giữa (chỉ cuộn hàng neo, không cuộn trang)
                if (list.scrollWidth > list.clientWidth) {
                    list.scrollLeft = a.offsetLeft - (list.clientWidth - a.offsetWidth) / 2;
                }
            } else {
                a.removeAttribute('aria-current');
            }
        });
    };

    /* ---------- 2. Nút "Đặt món ngay" trên điện thoại ---------- */
    const fab = document.querySelector('[data-landing-fab]');
    const hero = document.getElementById('hero');
    const menu = document.getElementById('menu');

    let ticking = false;
    const update = () => {
        ticking = false;
        const line = (header ? header.offsetHeight : 0) + window.innerHeight * 0.3;
        // Mục đang xem = mục cuối cùng có mép trên đã qua "vạch" ngay dưới navbar
        let id = null;
        sections.forEach(s => { if (s.getBoundingClientRect().top <= line) id = s.id; });
        // Cuộn chạm đáy trang thì chọn mục cuối (mục Liên hệ ngắn, có thể không chạm vạch)
        if (window.innerHeight + window.scrollY >= root.scrollHeight - 2 && sections.length) {
            id = sections[sections.length - 1].id;
        }
        mark(id);

        if (fab) {
            const heroVisible = hero && hero.getBoundingClientRect().bottom > (header ? header.offsetHeight : 0);
            const r = menu && menu.getBoundingClientRect();
            const menuVisible = r && r.top < window.innerHeight && r.bottom > 0;
            fab.classList.toggle('is-hidden', Boolean(heroVisible || menuVisible));
        }
    };
    const onScroll = () => {
        if (!ticking) { ticking = true; requestAnimationFrame(update); }
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener('resize', onScroll);
    update();

    /* ---------- 3. Mở chatbox (chỉ khi đã có chatbox trên trang) ---------- */
    const ask = document.querySelector('[data-chatbox-cta]');
    const chatToggle = document.getElementById('chatbox-toggle');
    if (ask && chatToggle) {
        ask.hidden = false;
        ask.querySelector('[data-chatbox-open]')?.addEventListener('click', () => chatToggle.click());
    }
})();
