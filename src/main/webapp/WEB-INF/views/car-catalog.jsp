<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Danh sach xe"/></jsp:include>

<div class="catalog-hero">
    <div class="container">
        <h1 class="catalog-hero-title">Kham pha doi xe <span class="text-gradient">cua chung toi</span></h1>
        <p class="catalog-hero-sub">
            Tong cong <strong>${allCars.size()}</strong> xe da dang tu sedan, SUV den xe sang — san sang phuc vu ban
        </p>
    </div>
</div>

<div class="container py-4">

    <!-- Filters -->
    <div class="catalog-filter-bar mb-4">
        <form method="get" action="${pageContext.request.contextPath}/cars" class="d-flex flex-wrap gap-3 align-items-end">
            <div>
                <label class="form-label small fw-bold text-muted">LOAI XE</label>
                <select name="seats" class="form-select form-select-sm" style="width:160px">
                    <option value="">Tat ca</option>
                    <option value="4" ${seatFilter == '4' ? 'selected' : ''}>Sedan - 4 cho</option>
                    <option value="5" ${seatFilter == '5' ? 'selected' : ''}>SUV/Sedan - 5 cho</option>
                    <option value="7" ${seatFilter == '7' ? 'selected' : ''}>SUV/MPV - 7 cho</option>
                </select>
            </div>
            <div>
                <label class="form-label small fw-bold text-muted">HANG XE</label>
                <select name="brand" class="form-select form-select-sm" style="width:160px">
                    <option value="">Tat ca</option>
                    <c:forEach var="b" items="${brands}">
                        <option value="${b}" ${brandFilter == b ? 'selected' : ''}>${b}</option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label class="form-label small fw-bold text-muted">TRANG THAI</label>
                <select name="status" class="form-select form-select-sm" style="width:160px">
                    <option value="">Tat ca</option>
                    <option value="AVAILABLE" ${statusFilter == 'AVAILABLE' ? 'selected' : ''}>San sang</option>
                    <option value="MAINTENANCE" ${statusFilter == 'MAINTENANCE' ? 'selected' : ''}>Bao tri</option>
                </select>
            </div>
            <div>
                <button type="submit" class="btn btn-accent btn-sm px-4">
                    <i class="bi bi-funnel me-1"></i>Loc
                </button>
                <a href="${pageContext.request.contextPath}/cars" class="btn btn-outline-accent btn-sm px-3">
                    <i class="bi bi-arrow-counterclockwise"></i>
                </a>
            </div>
            <div class="ms-auto">
                <span class="text-muted small"><i class="bi bi-car-front me-1"></i>Hien thi <strong>${cars.size()}</strong> xe</span>
            </div>
        </form>
    </div>

    <!-- Car grid -->
    <c:if test="${empty cars}">
        <div class="text-center py-5">
            <i class="bi bi-emoji-frown" style="font-size:3.5rem;color:var(--gray-300)"></i>
            <h4 class="text-muted mt-3">Khong co xe nao phu hop voi bo loc</h4>
            <a href="${pageContext.request.contextPath}/cars" class="btn btn-accent btn-sm mt-2">Xem tat ca</a>
        </div>
    </c:if>

    <div class="row g-4">
        <c:forEach var="car" items="${cars}" varStatus="loop">
            <div class="col-md-6 col-lg-4">
                <div class="catalog-card">
                    <!-- Card header with car icon -->
                    <div class="catalog-card-visual">
                        <i class="bi bi-car-front-fill"></i>
                        <c:choose>
                            <c:when test="${car.status == 'AVAILABLE'}">
                                <span class="catalog-status catalog-status-available">
                                    <i class="bi bi-check-circle-fill me-1"></i>San sang
                                </span>
                            </c:when>
                            <c:when test="${car.status == 'MAINTENANCE'}">
                                <span class="catalog-status catalog-status-maintenance">
                                    <i class="bi bi-wrench me-1"></i>Bao tri
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="catalog-status catalog-status-other">
                                    ${car.status}
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Card body -->
                    <div class="catalog-card-body">
                        <h5 class="catalog-car-name">${car.brand} ${car.model}</h5>
                        <div class="catalog-plate">${car.licensePlate}</div>

                        <!-- Specs grid -->
                        <div class="catalog-specs">
                            <div class="catalog-spec">
                                <i class="bi bi-people-fill"></i>
                                <span>${car.seatCount} cho</span>
                            </div>
                            <c:if test="${not empty car.transmission}">
                                <div class="catalog-spec">
                                    <i class="bi bi-gear-wide-connected"></i>
                                    <span>${car.transmission == 'AUTOMATIC' ? 'Tu dong' : 'So san'}</span>
                                </div>
                            </c:if>
                            <c:if test="${not empty car.fuelType}">
                                <div class="catalog-spec">
                                    <i class="bi bi-fuel-pump-fill"></i>
                                    <span>${car.fuelType}</span>
                                </div>
                            </c:if>
                            <c:if test="${car.manufactureYear != null}">
                                <div class="catalog-spec">
                                    <i class="bi bi-calendar3"></i>
                                    <span>${car.manufactureYear}</span>
                                </div>
                            </c:if>
                            <c:if test="${not empty car.color}">
                                <div class="catalog-spec">
                                    <i class="bi bi-palette-fill"></i>
                                    <span>${car.color}</span>
                                </div>
                            </c:if>
                            <div class="catalog-spec">
                                <i class="bi bi-speedometer2"></i>
                                <span><fmt:formatNumber value="${car.mileage}" pattern="#,###"/> km</span>
                            </div>
                        </div>

                        <c:if test="${not empty car.description}">
                            <p class="catalog-desc">${car.description}</p>
                        </c:if>

                        <!-- Price footer -->
                        <div class="catalog-price-bar">
                            <div>
                                <div class="catalog-price">
                                    <fmt:formatNumber value="${car.dailyRate}" pattern="#,###"/> <small>VND/ngay</small>
                                </div>
                                <div class="catalog-deposit">
                                    Dat coc: <fmt:formatNumber value="${car.depositAmount}" pattern="#,###"/> VND
                                </div>
                            </div>
                            <c:if test="${car.status == 'AVAILABLE'}">
                                <a href="${pageContext.request.contextPath}/search" class="btn btn-accent btn-sm">
                                    <i class="bi bi-calendar-check me-1"></i>Dat xe
                                </a>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
