<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Chon xe cu the"/></jsp:include>
<c:url var="placeholderImage" value="/images/car-placeholder.svg" />

<div class="container py-5">
    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
        <div>
            <h2 class="page-heading mb-1"><i class="bi bi-list-check"></i>Chon xe cu the</h2>
            <div class="text-muted">
                ${brand} ${model}
                <c:if test="${not empty pickupAt and not empty returnAt}">
                    | ${pickupAt} - ${returnAt}
                </c:if>
            </div>
        </div>
        <div class="btn-action-group">
            <a href="${pageContext.request.contextPath}/search" class="btn btn-outline-accent btn-action-nowrap">
                <i class="bi bi-arrow-left me-1"></i>Quay lai tim kiem
            </a>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <c:if test="${empty error and empty cars}">
        <div class="text-center py-5">
            <i class="bi bi-emoji-frown" style="font-size:4rem;color:var(--gray-300)"></i>
            <h4 class="text-muted mt-3">Khong con xe kha dung</h4>
            <p class="text-muted">Xe co the vua duoc dat hoac khong phu hop voi thoi gian ban chon.</p>
        </div>
    </c:if>

    <c:if test="${not empty cars}">
        <div class="row g-4">
            <c:forEach var="car" items="${cars}">
                <div class="col-md-6 col-xl-4">
                    <div class="car-card h-100">
                        <c:set var="carImageSrc" value="${empty car.imageUrl ? placeholderImage : car.imageUrl}" />
                        <div class="search-car-visual mb-3">
                            <img src="${carImageSrc}"
                                 alt="${car.brand} ${car.model} ${car.licensePlate}"
                                 class="search-car-image"
                                 loading="lazy"
                                 decoding="async"
                                 onerror="this.onerror=null;this.src='${placeholderImage}';">
                        </div>

                        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                            <div>
                                <div class="car-brand">${car.brand} ${car.model}</div>
                                <div class="catalog-plate">${car.licensePlate}</div>
                            </div>
                            <span class="badge-status badge-accepted">AVAILABLE</span>
                        </div>

                        <div class="mb-3">
                            <span class="car-tag"><i class="bi bi-palette me-1"></i>${car.color}</span>
                            <span class="car-tag"><i class="bi bi-speedometer2 me-1"></i><fmt:formatNumber value="${car.mileage}" pattern="#,###"/> km</span>
                            <span class="car-tag"><i class="bi bi-people me-1"></i>${car.seatCount} cho</span>
                            <c:if test="${not empty car.transmission}">
                                <span class="car-tag"><i class="bi bi-gear me-1"></i>${car.transmission}</span>
                            </c:if>
                        </div>

                        <div class="divider"></div>
                        <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap">
                            <div>
                                <div class="car-price">
                                    <fmt:formatNumber value="${car.dailyRate}" pattern="#,###"/> VND
                                    <span class="text-muted small">/ngay</span>
                                </div>
                                <div class="text-muted small">
                                    Dat coc: <fmt:formatNumber value="${car.depositAmount}" pattern="#,###"/> VND
                                </div>
                            </div>
                            <c:url var="bookUrl" value="/book">
                                <c:param name="carId" value="${car.carId}" />
                                <c:param name="pickupAt" value="${pickupAt}" />
                                <c:param name="returnAt" value="${returnAt}" />
                                <c:param name="selectionMode" value="specific" />
                            </c:url>
                            <a href="${bookUrl}" class="btn btn-accent btn-action-nowrap">
                                <i class="bi bi-cart-check me-1"></i>Book Now
                            </a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
