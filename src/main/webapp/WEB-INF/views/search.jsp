<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Tim xe"/></jsp:include>

<div class="hero-section">
    <div class="container position-relative">
        <div class="text-center mb-4">
            <h1 class="hero-title">Tim xe <span>cho thue</span></h1>
            <p class="text-muted">Chon loai xe, ngay gio nhan va tra xe de xem cac xe kha dung</p>
        </div>
        <div class="card-custom mx-auto" style="max-width:800px">
            <div class="card-body">
                <c:if test="${not empty error}">
                    <div class="alert alert-custom-error mb-3">${error}</div>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/search" id="searchForm">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label">Loai xe (so cho)</label>
                            <select name="seatCount" class="form-select">
                                <option value="">Tat ca</option>
                                <option value="4" ${seatCount == '4' ? 'selected' : ''}>4 cho</option>
                                <option value="5" ${seatCount == '5' ? 'selected' : ''}>5 cho</option>
                                <option value="7" ${seatCount == '7' ? 'selected' : ''}>7 cho</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Ngay gio nhan xe</label>
                            <input type="datetime-local" name="pickupAt" class="form-control"
                                   value="${pickupAt}" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Ngay gio tra xe</label>
                            <input type="datetime-local" name="returnAt" class="form-control"
                                   value="${returnAt}" required>
                        </div>
                    </div>
                    <div class="text-center mt-3">
                        <button type="submit" class="btn btn-accent px-5">
                            <i class="bi bi-search me-1"></i>Tim kiem
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty cars}">
<div class="container mt-4">
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
<div class="container mt-5 text-center">
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
