/* =====================================================================
   HIỆU ỨNG ĐỘNG CHO TOÀN SITE
   - Hiện dần khi cuộn (.reveal)
   - Thanh menu đổ bóng khi cuộn
   - Thông báo dạng toast trượt vào / tự ẩn
   - Số trên giỏ hàng nảy lên khi vừa thêm món
   - Trang chủ: lọc menu tại chỗ (View Transitions), tìm kiếm tức thì, parallax hero
   Tất cả đều tắt khi người dùng bật "giảm chuyển động".
   ===================================================================== */
(function () {
    const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* ---------- 1. Hiện dần khi cuộn ---------- */
    const items = document.querySelectorAll('.reveal');
    if (items.length) {
        if (!('IntersectionObserver' in window) || reduce) {
            items.forEach(el => el.classList.add('is-visible'));
        } else {
            const io = new IntersectionObserver(entries => {
                entries.forEach(e => {
                    if (e.isIntersecting) {
                        e.target.classList.add('is-visible');
                        io.unobserve(e.target);
                    }
                });
            }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
            items.forEach(el => io.observe(el));
        }
    }

    /* ---------- 2. Thanh menu đổ bóng khi cuộn ---------- */
    const nav = document.querySelector('.navbar-ts');
    if (nav) {
        const onScroll = () => nav.classList.toggle('scrolled', window.scrollY > 8);
        onScroll();
        window.addEventListener('scroll', onScroll, { passive: true });
    }

    /* ---------- 3. Toast thông báo ---------- */
    document.querySelectorAll('.ts-toast').forEach(t => {
        const close = () => {
            t.classList.add('leaving');
            t.addEventListener('animationend', () => t.remove(), { once: true });
            if (reduce) t.remove();
        };
        t.querySelector('.ts-toast-close')?.addEventListener('click', close);
        const ms = Number(t.dataset.autohide || 0);
        if (ms) setTimeout(close, ms);
    });

    /* ---------- 4. Giỏ hàng nảy lên khi vừa thêm món ---------- */
    const okToast = document.querySelector('.ts-toast-success');
    const badge = document.querySelector('.cart-badge');
    if (okToast && badge && /thêm/i.test(okToast.textContent)) {
        badge.classList.add('bump');
    }

    /* ---------- 5. Trang chủ: lọc menu tại chỗ + tìm kiếm tức thì ---------- */
    const grid = document.getElementById('menuGrid');
    if (grid) {
        const cards = [...grid.querySelectorAll('.menu-item')];
        const tabs = [...document.querySelectorAll('.cat-tabs [data-cat]')];
        const search = document.querySelector('#menuSearch input');
        const empty = document.getElementById('menuEmpty');
        // Bỏ dấu để gõ "tra sua" vẫn ra "Trà sữa"
        const norm = s => (s || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd');

        let cat = (tabs.find(t => t.classList.contains('active')) || {}).dataset?.cat || 'all';

        const apply = () => {
            const q = norm(search ? search.value.trim() : '');
            let shown = 0;
            cards.forEach(c => {
                const ok = (cat === 'all' || c.dataset.cat === cat) && (!q || norm(c.dataset.name).includes(q));
                c.hidden = !ok;
                if (ok) {
                    shown++;
                    c.querySelector('.reveal')?.classList.add('is-visible');
                }
            });
            if (empty) empty.hidden = shown > 0;
        };

        // Dùng View Transitions để các thẻ trượt về vị trí mới; trình duyệt cũ thì nháy mờ nhẹ
        const animate = fn => {
            if (!reduce && document.startViewTransition) {
                document.startViewTransition(fn);
            } else {
                fn();
                if (!reduce) {
                    grid.classList.remove('flash');
                    void grid.offsetWidth;
                    grid.classList.add('flash');
                }
            }
        };

        const choose = value => {
            cat = value;
            tabs.forEach(t => t.classList.toggle('active', t.dataset.cat === value));
            const url = new URL(location.href);
            url.searchParams.delete('q');
            if (value === 'all') url.searchParams.delete('category'); else url.searchParams.set('category', value);
            url.hash = 'menu';
            history.replaceState(null, '', url);
            animate(apply);
        };

        document.querySelectorAll('[data-cat]').forEach(el => {
            if (el.closest('#menuGrid')) return;
            el.addEventListener('click', e => {
                e.preventDefault();
                if (search && el.dataset.cat === 'all') search.value = '';
                choose(el.dataset.cat);
            });
        });

        if (search) {
            let timer;
            search.addEventListener('input', () => {
                clearTimeout(timer);
                timer = setTimeout(() => animate(apply), 120);
            });
            search.form.addEventListener('submit', e => { e.preventDefault(); animate(apply); });
        }
        apply();
    }

    /* ---------- 6. Hero: các ly nghiêng theo chuột (parallax) ---------- */
    const hero = document.getElementById('hero');
    if (hero && !reduce && matchMedia('(pointer: fine)').matches) {
        const layers = [...hero.querySelectorAll('.plx')];
        let raf = 0;
        hero.addEventListener('pointermove', e => {
            const r = hero.getBoundingClientRect();
            const x = (e.clientX - r.left) / r.width - 0.5;
            const y = (e.clientY - r.top) / r.height - 0.5;
            cancelAnimationFrame(raf);
            raf = requestAnimationFrame(() => layers.forEach(l => {
                const d = Number(l.dataset.depth || 10);
                l.style.transform = `translate3d(${(-x * d).toFixed(1)}px, ${(-y * d).toFixed(1)}px, 0)`;
            }));
        });
        hero.addEventListener('pointerleave', () => layers.forEach(l => l.style.transform = ''));
    }
})();
