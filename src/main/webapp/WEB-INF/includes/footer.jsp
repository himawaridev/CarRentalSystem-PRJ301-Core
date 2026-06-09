<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<footer class="site-footer" id="siteFooter">
    <!-- CTA Strip -->
    <div class="footer-cta">
        <div class="container">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
                <div>
                    <p class="footer-cta-text">San sang cho chuyen di tiep theo?</p>
                    <p class="footer-cta-sub">Dat xe trong 2 phut - Giao xe tan noi - Huy mien phi</p>
                </div>
                <a href="${ctx}/search" class="btn footer-cta-btn">
                    <i class="bi bi-arrow-right-circle me-1"></i>Dat xe ngay
                </a>
            </div>
        </div>
    </div>

    <!-- Main Footer -->
    <div class="footer-main">
        <div class="container">
            <div class="row g-4 g-lg-5">
                <!-- Brand -->
                <div class="col-lg-4 col-md-6">
                    <div class="footer-brand">
                        <i class="bi bi-car-front-fill"></i> CarRental
                    </div>
                    <p class="footer-desc">
                        Dich vu cho thue xe o to hang dau Viet Nam. Uy tin, chat luong, gia ca hop ly voi hon 100+ xe da dang phuc vu moi nhu cau cua ban.
                    </p>
                    <div class="footer-socials">
                        <a href="#" title="Facebook"><i class="bi bi-facebook"></i></a>
                        <a href="#" title="Instagram"><i class="bi bi-instagram"></i></a>
                        <a href="#" title="Youtube"><i class="bi bi-youtube"></i></a>
                        <a href="#" title="Tiktok"><i class="bi bi-tiktok"></i></a>
                    </div>
                </div>

                <!-- Quick Links -->
                <div class="col-lg-2 col-md-6">
                    <h6 class="footer-heading">Dich vu</h6>
                    <ul class="footer-links">
                        <li><a href="${ctx}/search">Tim xe cho thue</a></li>
                        <li><a href="${ctx}/search">Thue xe tu lai</a></li>
                        <li><a href="${ctx}/search">Thue xe co tai xe</a></li>
                        <li><a href="${ctx}/register">Dang ky tai khoan</a></li>
                    </ul>
                </div>

                <!-- Support -->
                <div class="col-lg-2 col-md-6">
                    <h6 class="footer-heading">Ho tro</h6>
                    <ul class="footer-links">
                        <li><a href="#">Huong dan thue xe</a></li>
                        <li><a href="#">Chinh sach bao hiem</a></li>
                        <li><a href="#">Dieu khoan su dung</a></li>
                        <li><a href="#">Cau hoi thuong gap</a></li>
                    </ul>
                </div>

                <!-- Contact -->
                <div class="col-lg-4 col-md-6">
                    <h6 class="footer-heading">Lien he</h6>
                    <ul class="footer-contact">
                        <li>
                            <i class="bi bi-geo-alt-fill"></i>
                            <span>Khu CNC Hoa Lac, Thach That, Ha Noi</span>
                        </li>
                        <li>
                            <i class="bi bi-telephone-fill"></i>
                            <span>1900 6868 (24/7)</span>
                        </li>
                        <li>
                            <i class="bi bi-envelope-fill"></i>
                            <span>support@carrental.vn</span>
                        </li>
                        <li>
                            <i class="bi bi-clock-fill"></i>
                            <span>T2 - CN: 06:00 - 22:00</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <!-- Bottom bar -->
    <div class="footer-bottom">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-6">
                    <p class="mb-0" style="font-size:.8rem;color:#475569">&copy; 2026 CarRental System. All rights reserved.</p>
                </div>
                <div class="col-md-6 text-md-end">
                    <p class="mb-0" style="font-size:.8rem;color:#475569">PRJ301 - FPT University</p>
                </div>
            </div>
        </div>
    </div>
</footer>

<style>
/* ===== FOOTER INLINE STYLES (guaranteed to load) ===== */
#siteFooter {
    background: linear-gradient(180deg, #0f172a 0%, #0c1322 100%);
    color: #94a3b8;
    padding: 0;
    margin-top: auto;
    position: relative;
    border-top: 3px solid;
    border-image: linear-gradient(90deg, #2563eb, #818cf8, #2563eb) 1;
}
#siteFooter .footer-cta {
    background: linear-gradient(135deg, #1d4ed8, #1e40af);
    padding: 28px 0;
}
#siteFooter .footer-cta-text {
    color: #fff;
    font-size: 1.15rem;
    font-weight: 700;
    margin: 0;
}
#siteFooter .footer-cta-sub {
    color: rgba(255,255,255,.65);
    font-size: .85rem;
    margin: 4px 0 0;
}
#siteFooter .footer-cta-btn {
    background: #fff;
    color: #1d4ed8;
    border: none;
    font-weight: 700;
    padding: 10px 28px;
    border-radius: 8px;
    font-size: .9rem;
    text-decoration: none;
    transition: all .2s ease;
}
#siteFooter .footer-cta-btn:hover {
    background: #f1f5f9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0,0,0,.2);
}
#siteFooter .footer-main {
    padding: 44px 0 12px;
}
#siteFooter .footer-brand {
    font-size: 1.5rem;
    font-weight: 800;
    color: #fff;
    margin-bottom: 14px;
}
#siteFooter .footer-brand i {
    color: #3b82f6;
}
#siteFooter .footer-desc {
    font-size: .85rem;
    line-height: 1.75;
    color: #64748b;
    margin-bottom: 20px;
    max-width: 320px;
}
#siteFooter .footer-socials {
    display: flex;
    gap: 8px;
}
#siteFooter .footer-socials a {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    background: rgba(255,255,255,.05);
    border: 1px solid rgba(255,255,255,.1);
    color: #94a3b8;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    transition: all .25s ease;
    text-decoration: none;
}
#siteFooter .footer-socials a:hover {
    background: #2563eb;
    border-color: #2563eb;
    color: #fff;
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(37,99,235,.4);
}
#siteFooter .footer-heading {
    font-size: .8rem;
    font-weight: 700;
    color: #fff;
    text-transform: uppercase;
    letter-spacing: .1em;
    margin-bottom: 20px;
    padding-bottom: 12px;
    position: relative;
}
#siteFooter .footer-heading::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 24px;
    height: 2px;
    background: #2563eb;
    border-radius: 2px;
}
#siteFooter .footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
}
#siteFooter .footer-links li {
    margin-bottom: 11px;
}
#siteFooter .footer-links a {
    color: #64748b;
    font-size: .875rem;
    text-decoration: none;
    transition: all .2s ease;
}
#siteFooter .footer-links a:hover {
    color: #e2e8f0;
    padding-left: 6px;
}
#siteFooter .footer-contact {
    list-style: none;
    padding: 0;
    margin: 0;
}
#siteFooter .footer-contact li {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
    font-size: .875rem;
}
#siteFooter .footer-contact li i {
    color: #3b82f6;
    font-size: 1rem;
    margin-top: 1px;
    flex-shrink: 0;
    width: 18px;
    text-align: center;
}
#siteFooter .footer-contact li span {
    color: #94a3b8;
    line-height: 1.5;
}
#siteFooter .footer-bottom {
    margin-top: 24px;
    padding: 20px 0;
    border-top: 1px solid rgba(255,255,255,.06);
}
@media (max-width: 768px) {
    #siteFooter .footer-main { padding: 32px 0 8px; }
    #siteFooter .footer-bottom { text-align: center; }
    #siteFooter .footer-bottom .text-md-end { text-align: center !important; }
    #siteFooter .footer-cta { text-align: center; }
    #siteFooter .footer-cta-btn { margin-top: 12px; }
}
</style>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.js"></script>
<script>
document.addEventListener('DOMContentLoaded', function() {
    window.__carRentalSwipers = window.__carRentalSwipers || [];

    document.querySelectorAll('[data-slider="car-images"], [data-slider="brand-images"]').forEach(function(slider) {
        if (slider.dataset.initialized === 'true') {
            return;
        }
        slider.dataset.initialized = 'true';

        if (typeof Swiper === 'undefined') {
            startFallbackSlider(slider);
            return;
        }

        var instance = new Swiper(slider, {
            slidesPerView: 1,
            loop: true,
            allowTouchMove: true,
            grabCursor: true,
            watchOverflow: true,
            speed: 600,
            autoplay: {
                delay: 3000,
                disableOnInteraction: false
            },
            pagination: {
                el: slider.querySelector('.swiper-pagination'),
                clickable: true
            }
        });
        slider.swiperInstance = instance;
        window.__carRentalSwipers.push(instance);

        window.setTimeout(function() {
            if (!instance || slider.querySelectorAll('.swiper-slide').length <= 1) {
                return;
            }
            if (instance.autoplay && instance.autoplay.running) {
                return;
            }
            window.setInterval(function() {
                if (!document.hidden && typeof instance.slideNext === 'function') {
                    instance.slideNext();
                }
            }, 3000);
        }, 3200);
    });

    function startFallbackSlider(slider) {
        var slides = Array.from(slider.querySelectorAll('.swiper-slide'));
        if (slides.length <= 1) {
            return;
        }
        var index = 0;
        var touchStartX = null;
        function showSlide(nextIndex) {
            slides[index].style.display = 'none';
            index = (nextIndex + slides.length) % slides.length;
            slides[index].style.display = 'block';
        }
        slides.forEach(function(slide, slideIndex) {
            slide.style.display = slideIndex === 0 ? 'block' : 'none';
        });
        window.setInterval(function() {
            showSlide(index + 1);
        }, 3000);
        slider.addEventListener('touchstart', function(event) {
            touchStartX = event.touches && event.touches.length ? event.touches[0].clientX : null;
        }, { passive: true });
        slider.addEventListener('touchend', function(event) {
            if (touchStartX === null || !event.changedTouches || !event.changedTouches.length) {
                return;
            }
            var deltaX = event.changedTouches[0].clientX - touchStartX;
            if (Math.abs(deltaX) > 32) {
                showSlide(deltaX < 0 ? index + 1 : index - 1);
            }
            touchStartX = null;
        }, { passive: true });
    }
});
</script>
</body>
</html>
