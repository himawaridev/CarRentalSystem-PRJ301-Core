<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Danh sach xe"/></jsp:include>
<c:url var="placeholderImage" value="/images/car-placeholder.svg" />

<div class="catalog-hero">
    <div class="container">
        <h1 class="catalog-hero-title">Kham pha doi xe <span class="text-gradient">cua chung toi</span></h1>
        <p class="catalog-hero-sub">
            Tong cong <strong>${allCars.size()}</strong> xe da dang tu sedan, SUV den xe sang — san sang phuc vu ban
        </p>
    </div>
</div>

<div class="container py-4 ${not empty cars && canBook ? 'catalog-content-has-fixed-selection' : ''}">

    <!-- Filters -->
    <div class="search-card catalog-search-card mx-auto mb-4">
        <div class="search-card-header">
            <div class="search-card-heading">
                <span class="search-heading-icon"><i class="bi bi-funnel"></i></span>
                <div>
                    <div class="search-heading-title">Loc danh sach xe</div>
                    <div class="search-heading-note">Tim nhanh theo dac diem va muc gia</div>
                </div>
            </div>
        </div>
        <div class="search-card-body">
            <c:if test="${not empty bookingRoleMessage}">
                <div class="alert alert-custom-error mb-3">${bookingRoleMessage}</div>
            </c:if>
            <c:if test="${not empty catalogError}">
                <div class="alert alert-custom-error mb-3">${catalogError}</div>
            </c:if>
            <form method="get" action="${pageContext.request.contextPath}/cars" id="catalogFilterForm">
                <fieldset class="search-filter-group">
                    <legend class="search-filter-title">
                        <span class="search-filter-icon vehicle"><i class="bi bi-car-front"></i></span>
                        Xe va ngan sach
                    </legend>
                    <div class="catalog-filter-grid">
                        <div class="search-field">
                            <label class="form-label" for="catalogBrand">Thuong hieu</label>
                            <select name="brand" id="catalogBrand" class="form-select">
                                <option value="">Tat ca hang xe</option>
                                <c:forEach var="b" items="${brands}">
                                    <option value="${b}" ${brandFilter == b ? 'selected' : ''}>${b}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="catalogSeats">So cho</label>
                            <select name="seats" id="catalogSeats" class="form-select">
                                <option value="">Tat ca so cho</option>
                                <c:forEach var="s" items="${seatCounts}">
                                    <option value="${s}" ${seatFilter == s ? 'selected' : ''}>${s} cho ngoi</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="catalogStatus">Trang thai</label>
                            <select name="status" id="catalogStatus" class="form-select">
                                <option value="">Tat ca trang thai</option>
                                <option value="AVAILABLE" ${statusFilter == 'AVAILABLE' ? 'selected' : ''}>San sang</option>
                                <option value="MAINTENANCE" ${statusFilter == 'MAINTENANCE' ? 'selected' : ''}>Bao tri</option>
                            </select>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="catalogMinPrice">Gia toi thieu</label>
                            <div class="search-input-affix">
                                <input type="number" min="0" step="1000" name="minPrice" id="catalogMinPrice"
                                       value="${minPrice}" class="form-control" placeholder="500.000">
                                <span>VND</span>
                            </div>
                        </div>
                        <div class="search-field">
                            <label class="form-label" for="catalogMaxPrice">Gia toi da</label>
                            <div class="search-input-affix">
                                <input type="number" min="0" step="1000" name="maxPrice" id="catalogMaxPrice"
                                       value="${maxPrice}" class="form-control" placeholder="1.500.000">
                                <span>VND</span>
                            </div>
                        </div>
                    </div>
                </fieldset>
                <div class="catalog-filter-actions">
                    <div class="catalog-result-count">
                        <i class="bi bi-car-front"></i>
                        <span>Hien thi <strong>${cars.size()}</strong> xe</span>
                    </div>
                    <div class="catalog-filter-buttons">
                        <a href="${pageContext.request.contextPath}/cars" class="btn btn-outline-accent catalog-reset-btn"
                           title="Dat lai bo loc" aria-label="Dat lai bo loc">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </a>
                        <button type="submit" class="btn btn-accent btn-action-nowrap catalog-filter-submit">
                            <i class="bi bi-funnel"></i>
                            <span>Ap dung bo loc</span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <c:if test="${not empty cars && canBook}">
        <div class="catalog-selection-bar catalog-selection-fixed">
            <div>
                <span class="text-muted small">Xe da chon</span>
                <strong class="ms-2"><span id="selectedCatalogCount">0</span></strong>
            </div>
            <button type="button" class="btn btn-accent btn-action-nowrap btn-sm px-4" id="catalogBookBtn"
                    onclick="openBookingModal()" disabled>
                <i class="bi bi-calendar-check me-1"></i>Dat xe da chon
            </button>
        </div>
    </c:if>

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
                <div class="catalog-card" id="catalogCard-${car.carId}">
                    <div class="catalog-card-visual">
                        <c:set var="catalogImageSrc" value="${empty car.imageUrl ? placeholderImage : car.imageUrl}" />
                        <img src="${catalogImageSrc}"
                             alt="${car.brand} ${car.model} ${car.licensePlate}"
                             class="car-card-image"
                             loading="lazy"
                             decoding="async"
                             onerror="this.onerror=null;this.src='${placeholderImage}';">
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
                        <div class="d-flex justify-content-between align-items-start gap-3">
                            <div>
                                <h5 class="catalog-car-name">${car.brand} ${car.model}</h5>
                                <div class="catalog-plate">
                                    <i class="bi bi-upc-scan me-1"></i>${car.licensePlate}
                                </div>
                            </div>
                            <c:if test="${car.status == 'AVAILABLE' && canBook}">
                                <div class="form-check m-0">
                                    <input class="form-check-input catalog-car-checkbox" type="checkbox"
                                           id="selectCar-${car.carId}" value="${car.carId}"
                                           onchange="updateCatalogSelection()">
                                </div>
                            </c:if>
                        </div>

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
                            <div class="catalog-spec">
                                <i class="bi bi-speedometer2"></i>
                                <span><fmt:formatNumber value="${car.mileage}" pattern="#,###"/> km</span>
                            </div>
                            <c:if test="${not empty car.color}">
                                <div class="catalog-spec">
                                    <i class="bi bi-palette-fill"></i>
                                    <span>${car.color}</span>
                                </div>
                            </c:if>
                            <div class="catalog-spec">
                                <i class="bi bi-upc"></i>
                                <span>${car.licensePlate}</span>
                            </div>
                        </div>

                        <c:if test="${not empty car.description}">
                            <p class="catalog-desc">${car.description}</p>
                        </c:if>

                        <!-- Price footer -->
                        <div class="catalog-price-bar">
                            <div class="catalog-price-info">
                                <div class="catalog-price">
                                    <fmt:formatNumber value="${car.dailyRate}" pattern="#,###"/> <small>VND/ngay</small>
                                </div>
                                <div class="catalog-deposit">
                                    Dat coc: <fmt:formatNumber value="${car.depositAmount}" pattern="#,###"/> VND
                                </div>
                            </div>
                            <c:if test="${car.status == 'AVAILABLE' && canBook}">
                                <button type="button" class="btn btn-accent btn-action-nowrap btn-sm catalog-select-btn"
                                        id="selectBtn-${car.carId}" onclick="toggleCatalogSelection(${car.carId})">
                                    <i class="bi bi-check2-square"></i><span>Chon</span>
                                </button>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<c:if test="${canBook}">
<!-- Booking Modal -->
<div class="modal fade" id="bookingModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="directBookForm" method="get" action="${pageContext.request.contextPath}/book">
                <input type="hidden" name="selectionMode" value="specific">
                <div id="selectedCatalogCarIds"></div>
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-calendar-check me-2"></i>Dat xe da chon: <span id="modalSelectedCount">0</span>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Ngay nhan xe</label>
                        <input type="datetime-local" name="pickupAt" id="modalPickup" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Ngay tra xe</label>
                        <input type="datetime-local" name="returnAt" id="modalReturn" class="form-control" required>
                    </div>
                    <div class="text-muted small">
                        <i class="bi bi-info-circle me-1"></i>Sau khi chon ngay, ban se duoc chuyen den trang xac nhan dat xe.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-accent btn-action-nowrap" data-bs-dismiss="modal">Huy</button>
                    <button type="submit" class="btn btn-accent btn-action-nowrap">
                        <i class="bi bi-arrow-right me-1"></i>Tiep tuc dat xe
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function updateCatalogSelection() {
    var checked = document.querySelectorAll('.catalog-car-checkbox:checked');
    var count = checked.length;
    var countLabel = document.getElementById('selectedCatalogCount');
    var bookButton = document.getElementById('catalogBookBtn');
    if (countLabel) {
        countLabel.textContent = count;
    }
    if (bookButton) {
        bookButton.disabled = count === 0;
    }

    document.querySelectorAll('.catalog-card').forEach(function(card) {
        card.classList.remove('catalog-card-selected');
    });
    document.querySelectorAll('.catalog-select-btn').forEach(function(button) {
        button.classList.remove('catalog-select-btn-active');
        button.innerHTML = '<i class="bi bi-check2-square"></i><span>Chon</span>';
    });

    checked.forEach(function(cb) {
        var card = document.getElementById('catalogCard-' + cb.value);
        var button = document.getElementById('selectBtn-' + cb.value);
        if (card) {
            card.classList.add('catalog-card-selected');
        }
        if (button) {
            button.classList.add('catalog-select-btn-active');
            button.innerHTML = '<i class="bi bi-check-circle"></i><span>Da chon</span>';
        }
    });
}

function toggleCatalogSelection(carId) {
    var checkbox = document.getElementById('selectCar-' + carId);
    if (!checkbox) {
        return;
    }
    checkbox.checked = !checkbox.checked;
    updateCatalogSelection();
}

function openBookingModal() {
    var checked = document.querySelectorAll('.catalog-car-checkbox:checked');
    if (checked.length === 0) {
        return;
    }

    var selectedContainer = document.getElementById('selectedCatalogCarIds');
    selectedContainer.innerHTML = '';
    checked.forEach(function(cb) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'carId';
        input.value = cb.value;
        selectedContainer.appendChild(input);
    });
    document.getElementById('modalSelectedCount').textContent = checked.length + ' xe';

    // Set default dates: pickup = tomorrow 8AM, return = day after tomorrow 8AM
    var now = new Date();
    var pickup = new Date(now.getTime() + 24*60*60*1000);
    pickup.setHours(8, 0, 0, 0);
    var ret = new Date(pickup.getTime() + 24*60*60*1000);

    var pickupInput = document.getElementById('modalPickup');
    var returnInput = document.getElementById('modalReturn');
    pickupInput.min = formatDT(now);
    pickupInput.value = formatDT(pickup);
    returnInput.min = formatDT(new Date(pickup.getTime() + 60*1000));
    returnInput.value = formatDT(ret);

    var modal = new bootstrap.Modal(document.getElementById('bookingModal'));
    modal.show();
}

function formatDT(d) {
    return d.getFullYear() + '-' +
        String(d.getMonth()+1).padStart(2,'0') + '-' +
        String(d.getDate()).padStart(2,'0') + 'T' +
        String(d.getHours()).padStart(2,'0') + ':' +
        String(d.getMinutes()).padStart(2,'0');
}

// Validate return > pickup
document.getElementById('directBookForm').addEventListener('submit', function(e) {
    var now = new Date();
    now.setSeconds(0, 0);
    var pickupInput = document.getElementById('modalPickup');
    var p = new Date(pickupInput.value);
    var r = new Date(document.getElementById('modalReturn').value);
    if (p < now) {
        e.preventDefault();
        alert('Thoi gian nhan xe khong duoc nam trong qua khu!');
        return;
    }
    if (r <= p) {
        e.preventDefault();
        alert('Ngay tra xe phai sau ngay nhan xe!');
    }
});

document.getElementById('modalPickup').addEventListener('change', function() {
    var pickup = new Date(this.value);
    document.getElementById('modalReturn').min = formatDT(new Date(pickup.getTime() + 60*1000));
});

updateCatalogSelection();
</script>
</c:if>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
