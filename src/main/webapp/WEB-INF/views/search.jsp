<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Trang chu"/></jsp:include>

<!-- ===== HERO BANNER ===== -->
<section class="hero-banner">
    <div class="hero-overlay"></div>
    <div class="container position-relative">
        <div class="row align-items-center" style="min-height:420px">
            <div class="col-lg-7">
                <div class="hero-badge"><i class="bi bi-trophy me-1"></i> #1 Dich vu cho thue xe tai Viet Nam</div>
                <h1 class="hero-banner-title">
                    Di chuyen <span class="text-gradient">thoai mai</span>,<br>
                    gia ca <span class="text-gradient">hop ly</span>.
                </h1>
                <p class="hero-subtitle">
                    Hon 100+ xe cao cap san sang cho ban. Tu sedan, SUV den xe sang
                    — tu lai hoac co tai xe, linh hoat theo nhu cau cua ban.
                </p>
                <div class="hero-stats">
                    <div class="hero-stat-item">
                        <div class="hero-stat-value">100<span>+</span></div>
                        <div class="hero-stat-label">Xe cho thue</div>
                    </div>
                    <div class="hero-stat-divider"></div>
                    <div class="hero-stat-item">
                        <div class="hero-stat-value">5K<span>+</span></div>
                        <div class="hero-stat-label">Khach hang</div>
                    </div>
                    <div class="hero-stat-divider"></div>
                    <div class="hero-stat-item">
                        <div class="hero-stat-value">4.9<span><i class="bi bi-star-fill" style="font-size:0.8rem"></i></span></div>
                        <div class="hero-stat-label">Danh gia</div>
                    </div>
                </div>
            </div>
            <div class="col-lg-5 d-none d-lg-block">
                <div class="hero-car-visual">
                    <i class="bi bi-car-front-fill"></i>
                    <div class="hero-car-glow"></div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ===== SEARCH FORM ===== -->
<section class="search-section">
    <div class="container">
        <div class="search-card mx-auto">
            <div class="search-card-header">
                <i class="bi bi-search me-2"></i>Tim xe phu hop voi ban
            </div>
            <div class="search-card-body">
                <c:if test="${not empty error}">
                    <div class="alert alert-custom-error mb-3">${error}</div>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/search" id="searchForm">
                    <div class="row g-3 align-items-end">
                        <div class="col-md-3">
                            <label class="form-label">Loai xe</label>
                            <select name="seatCount" class="form-select">
                                <option value="">Tat ca loai xe</option>
                                <option value="4" ${seatCount == '4' ? 'selected' : ''}>Sedan - 4 cho ngoi</option>
                                <option value="5" ${seatCount == '5' ? 'selected' : ''}>SUV/Sedan - 5 cho ngoi</option>
                                <option value="7" ${seatCount == '7' ? 'selected' : ''}>SUV/MPV - 7 cho ngoi</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Ngay gio nhan xe</label>
                            <input type="datetime-local" name="pickupAt" class="form-control"
                                   value="${pickupAt}" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Ngay gio tra xe</label>
                            <input type="datetime-local" name="returnAt" class="form-control"
                                   value="${returnAt}" required>
                        </div>
                        <div class="col-md-3">
                            <button type="submit" class="btn btn-accent w-100">
                                <i class="bi bi-search me-1"></i>Tim kiem
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</section>

<!-- ===== WHY CHOOSE US ===== -->
<c:if test="${empty cars and empty pickupAt}">
<section class="why-section">
    <div class="container">
        <h2 class="section-title text-center">Tai sao chon <span class="text-gradient">CarRental</span>?</h2>
        <p class="section-subtitle text-center">Chung toi mang den trai nghiem thue xe tot nhat cho ban</p>
        <div class="row g-4 mt-2">
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="bi bi-shield-check"></i>
                    </div>
                    <h5>An toan & Bao hiem</h5>
                    <p>Tat ca xe deu duoc bao hiem toan dien. Bao tri dinh ky, dam bao an toan tuyet doi.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="feature-card featured">
                    <div class="feature-icon">
                        <i class="bi bi-cash-coin"></i>
                    </div>
                    <h5>Gia tot nhat</h5>
                    <p>Cam ket gia thue canh tranh nhat thi truong. Khong phat sinh chi phi an.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="bi bi-clock-history"></i>
                    </div>
                    <h5>Ho tro 24/7</h5>
                    <p>Doi ngu ho tro khach hang san sang phuc vu ban bat cu luc nao, moi noi.</p>
                </div>
            </div>
        </div>

        <!-- Car brands showcase -->
        <div class="brands-section mt-5">
            <p class="text-muted text-center small mb-3" style="letter-spacing:0.1em;text-transform:uppercase;font-weight:600">Doi xe da dang cac hang</p>
            <div class="d-flex justify-content-center flex-wrap gap-4">
                <span class="brand-tag">Toyota</span>
                <span class="brand-tag">Hyundai</span>
                <span class="brand-tag">Honda</span>
                <span class="brand-tag">Mazda</span>
                <span class="brand-tag">Ford</span>
                <span class="brand-tag">Kia</span>
                <span class="brand-tag">Mitsubishi</span>
                <span class="brand-tag">VinFast</span>
            </div>
        </div>
    </div>
</section>
</c:if>

<!-- ===== SEARCH RESULTS ===== -->
<c:if test="${not empty cars}">
<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="page-heading mb-0"><i class="bi bi-car-front"></i>Ket qua: ${cars.size()} xe kha dung</h4>
        <form id="bookForm" method="get" action="${pageContext.request.contextPath}/book">
            <input type="hidden" name="pickupAt" value="${pickupAt}">
            <input type="hidden" name="returnAt" value="${returnAt}">
            <div id="selectedCarIds"></div>
            <button type="submit" class="btn btn-accent" id="bookBtn" disabled>
                <i class="bi bi-cart-plus me-1"></i>Dat xe da chon (<span id="selectedCount">0</span>)
            </button>
        </form>
    </div>

    <div class="row g-4">
        <c:forEach var="car" items="${cars}">
            <div class="col-md-6 col-lg-4">
                <div class="car-card" id="carCard-${car.carId}">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <div class="car-brand">${car.brand} ${car.model}</div>
                            <div class="text-muted small">${car.licensePlate}</div>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input car-checkbox" type="checkbox"
                                   value="${car.carId}" data-car-id="${car.carId}"
                                   onchange="updateSelection()">
                        </div>
                    </div>
                    <div class="mb-2">
                        <span class="car-tag"><i class="bi bi-people me-1"></i>${car.seatCount} cho</span>
                        <c:if test="${not empty car.transmission}">
                            <span class="car-tag"><i class="bi bi-gear me-1"></i>${car.transmission}</span>
                        </c:if>
                        <c:if test="${not empty car.fuelType}">
                            <span class="car-tag"><i class="bi bi-fuel-pump me-1"></i>${car.fuelType}</span>
                        </c:if>
                        <c:if test="${not empty car.color}">
                            <span class="car-tag">${car.color}</span>
                        </c:if>
                    </div>
                    <c:if test="${car.manufactureYear != null}">
                        <div class="text-muted small mb-1"><i class="bi bi-calendar3 me-1"></i>Nam SX: ${car.manufactureYear}</div>
                    </c:if>
                    <div class="text-muted small mb-2"><i class="bi bi-speedometer me-1"></i>ODO: <fmt:formatNumber value="${car.mileage}" pattern="#,###"/> km</div>
                    <div class="divider"></div>
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="car-price"><fmt:formatNumber value="${car.dailyRate}" pattern="#,###"/> VND<span class="text-muted small">/ngay</span></div>
                            <div class="text-muted small">Dat coc: <fmt:formatNumber value="${car.depositAmount}" pattern="#,###"/> VND</div>
                        </div>
                        <i class="bi bi-car-front car-icon"></i>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
</c:if>

<c:if test="${not empty pickupAt and empty cars}">
<div class="container mt-5 text-center" style="padding-bottom:60px">
    <i class="bi bi-emoji-frown" style="font-size:4rem;color:var(--gray-300)"></i>
    <h4 class="text-muted mt-3">Khong tim thay xe phu hop</h4>
    <p class="text-muted">Vui long thu lai voi ngay khac hoac loai xe khac.</p>
</div>
</c:if>

<script>
function updateSelection() {
    const checked = document.querySelectorAll('.car-checkbox:checked');
    document.getElementById('selectedCount').textContent = checked.length;
    document.getElementById('bookBtn').disabled = checked.length === 0;

    const container = document.getElementById('selectedCarIds');
    container.innerHTML = '';
    checked.forEach(cb => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'carId';
        input.value = cb.value;
        container.appendChild(input);
    });

    document.querySelectorAll('.car-card').forEach(card => card.style.borderColor = 'var(--border)');
    checked.forEach(cb => {
        document.getElementById('carCard-' + cb.dataset.carId).style.borderColor = 'var(--primary)';
    });
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
