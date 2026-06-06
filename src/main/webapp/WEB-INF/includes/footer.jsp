<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<footer class="site-footer">
    <!-- CTA Strip -->
    <div class="footer-cta">
        <div class="container">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
                <div>
                    <p class="footer-cta-text">San sang cho chuyen di tiep theo?</p>
                    <p class="footer-cta-sub">Dat xe trong 2 phut - Giao xe tan noi - Huy mien phi</p>
                </div>
                <a href="${ctx}/search" class="btn">
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
                    <p class="footer-copyright">&copy; 2026 CarRental System. All rights reserved.</p>
                </div>
                <div class="col-md-6 text-md-end">
                    <p class="footer-copyright">PRJ301 - FPT University</p>
                </div>
            </div>
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
