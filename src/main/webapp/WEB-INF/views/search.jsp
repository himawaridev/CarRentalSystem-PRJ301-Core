<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Trang chu"/></jsp:include>
<c:url var="placeholderImage" value="/images/car-placeholder.svg" />

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
                <div class="search-card-heading">
                    <span class="search-heading-icon"><i class="bi bi-search"></i></span>
                    <div>
                        <div class="search-heading-title">Tim xe phu hop</div>
                        <div class="search-heading-note">Gia thue duoc tinh theo chu ky 24 gio</div>
                    </div>
                </div>
            </div>
            <div class="search-card-body">
                <c:if test="${not empty error}">
                    <div class="alert alert-custom-error mb-3">${error}</div>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/search" id="searchForm">
                    <fieldset class="search-filter-group">
                        <legend class="search-filter-title">
                            <span class="search-filter-icon vehicle"><i class="bi bi-car-front"></i></span>
                            Xe va ngan sach
                        </legend>
                        <div class="vehicle-filter-grid">
                        <div class="search-field">
                            <label class="form-label" for="searchBrand">Thuong hieu</label>
                            <select name="brand" id="searchBrand" class="form-select">
                                <option value="">Tat ca hang xe</option>
                                <c:forEach var="b" items="${brands}">
                                    <option value="${b}" ${brandFilter == b ? 'selected' : ''}>${b}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="searchSeatCount">So cho</label>
                            <select name="seatCount" id="searchSeatCount" class="form-select">
                                <option value="">Tat ca so cho</option>
                                <c:forEach var="s" items="${seatCounts}">
                                    <option value="${s}" ${seatCount == s ? 'selected' : ''}>${s} cho ngoi</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="searchMinPrice">Gia toi thieu</label>
                            <div class="search-input-affix">
                                <input type="number" min="0" step="1000" name="minPrice" id="searchMinPrice" class="form-control"
                                       value="${minPrice}" placeholder="500.000">
                                <span>VND</span>
                            </div>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="searchMaxPrice">Gia toi da</label>
                            <div class="search-input-affix">
                                <input type="number" min="0" step="1000" name="maxPrice" id="searchMaxPrice" class="form-control"
                                       value="${maxPrice}" placeholder="1.500.000">
                                <span>VND</span>
                            </div>
                        </div>
                        </div>
                    </fieldset>

                    <fieldset class="search-filter-group time-group">
                        <legend class="search-filter-title">
                            <span class="search-filter-icon time"><i class="bi bi-calendar3"></i></span>
                            Thoi gian thue
                        </legend>
                        <div class="rental-time-grid">
                        <div class="search-field">
                            <label class="form-label" for="searchPickupAt">Ngay gio nhan xe</label>
                            <input type="datetime-local" name="pickupAt" id="searchPickupAt" class="form-control"
                                   value="${pickupAt}" min="${minimumPickupAt}" required>
                        </div>
                        <div class="rental-time-arrow" aria-hidden="true"><i class="bi bi-arrow-right"></i></div>
                        <div class="search-field">
                            <label class="form-label" for="searchReturnAt">Ngay gio tra xe</label>
                            <input type="datetime-local" name="returnAt" id="searchReturnAt" class="form-control"
                                   value="${returnAt}" required>
                        </div>
                        <div class="search-submit-wrap">
                            <button type="submit" class="btn btn-accent btn-action-nowrap search-submit-btn">
                                <i class="bi bi-search"></i>
                                <span>Tim xe ngay</span>
                            </button>
                        </div>
                        </div>
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
</section>

<!-- ===== WHY CHOOSE US ===== -->
<c:if test="${empty carGroups and empty pickupAt}">
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
<c:if test="${not empty carGroups}">
<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
        <h4 class="page-heading mb-0"><i class="bi bi-car-front"></i>Ket qua: ${fn:length(carGroups)} dong xe kha dung</h4>
        <span class="text-muted small">Chon dong xe de xem bien so cu the</span>
    </div>

    <div class="row g-4">
        <c:forEach var="group" items="${carGroups}">
            <div class="col-md-6 col-lg-4">
                <div class="car-card car-group-card">
                    <c:set var="searchImageSrc" value="${empty group.imageUrl ? placeholderImage : group.imageUrl}" />
                    <div class="search-car-visual mb-3">
                        <img src="${searchImageSrc}"
                             alt="${group.brand} ${group.model}"
                             class="search-car-image"
                             loading="lazy"
                             decoding="async"
                             onerror="this.onerror=null;this.src='${placeholderImage}';">
                    </div>
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <div class="car-brand">${group.brand} ${group.model}</div>
                            <div class="text-muted small">Co ${group.availableCount} xe kha dung trong thoi gian nay</div>
                        </div>
                        <span class="brand-count-badge">${group.availableCount}</span>
                    </div>
                    <div class="mb-2">
                        <span class="car-tag"><i class="bi bi-people me-1"></i>${group.seatCount} cho</span>
                        <span class="car-tag"><i class="bi bi-stack me-1"></i>${group.availableCount} xe</span>
                    </div>
                    <div class="divider"></div>
                    <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap">
                        <div>
                            <div class="car-price"><fmt:formatNumber value="${group.dailyRate}" pattern="#,###"/> VND<span class="text-muted small">/ngay</span></div>
                            <div class="text-muted small">Gia hien thi la muc thap nhat cua nhom</div>
                        </div>
                        <c:url var="selectCarUrl" value="/select-car">
                            <c:param name="brand" value="${group.brand}" />
                            <c:param name="model" value="${group.model}" />
                            <c:param name="pickupAt" value="${pickupAt}" />
                            <c:param name="returnAt" value="${returnAt}" />
                            <c:if test="${not empty seatCount}">
                                <c:param name="seatCount" value="${seatCount}" />
                            </c:if>
                            <c:if test="${not empty minPrice}">
                                <c:param name="minPrice" value="${minPrice}" />
                            </c:if>
                            <c:if test="${not empty maxPrice}">
                                <c:param name="maxPrice" value="${maxPrice}" />
                            </c:if>
                        </c:url>
                        <a href="${selectCarUrl}" class="btn btn-accent btn-action-nowrap">
                            <i class="bi bi-list-check me-1"></i>View Available Cars
                        </a>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
</c:if>

<c:if test="${not empty pickupAt and empty carGroups}">
<div class="container mt-5 text-center" style="padding-bottom:60px">
    <i class="bi bi-emoji-frown" style="font-size:4rem;color:var(--gray-300)"></i>
    <h4 class="text-muted mt-3">Khong tim thay xe phu hop</h4>
    <p class="text-muted">Vui long thu lai voi ngay khac, thuong hieu khac hoac khoang gia rong hon.</p>
</div>
</c:if>

<script>
document.addEventListener('DOMContentLoaded', function() {
    var pickupInput = document.getElementById('searchPickupAt');
    var returnInput = document.getElementById('searchReturnAt');
    if (!pickupInput || !returnInput) return;

    function addOneMinute(value) {
        var date = new Date(value);
        date.setMinutes(date.getMinutes() + 1);
        date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
        return date.toISOString().slice(0, 16);
    }

    function updateReturnMinimum() {
        var pickupValue = pickupInput.value || pickupInput.min;
        if (pickupValue) {
            returnInput.min = addOneMinute(pickupValue);
        }
    }

    pickupInput.addEventListener('change', updateReturnMinimum);
    updateReturnMinimum();
});
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
