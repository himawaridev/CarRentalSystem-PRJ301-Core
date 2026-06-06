<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<footer class="site-footer">
    <div class="container">
        <div class="row g-4">
            <!-- Brand -->
            <div class="col-lg-4 col-md-6">
                <div class="footer-brand">
                    <i class="bi bi-car-front-fill"></i> CarRental
                </div>
                <p class="footer-desc">
                    Dich vu cho thue xe o to hang dau Viet Nam. Uy tin, chat luong, gia ca hop ly voi hon 100+ xe da dang phuc vu moi nhu cau di chuyen cua ban.
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
                        <i class="bi bi-geo-alt"></i>
                        <span>Khu CNC Hoa Lac, Thach That, Ha Noi</span>
                    </li>
                    <li>
                        <i class="bi bi-telephone"></i>
                        <span>1900 6868 (24/7)</span>
                    </li>
                    <li>
                        <i class="bi bi-envelope"></i>
                        <span>support@carrental.vn</span>
                    </li>
                    <li>
                        <i class="bi bi-clock"></i>
                        <span>Mon - Sun: 06:00 - 22:00</span>
                    </li>
                </ul>
            </div>
        </div>

        <div class="footer-bottom">
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
