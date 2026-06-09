<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Dat xe"/></jsp:include>

<div class="container py-5">
    <h2 class="page-heading mb-4"><i class="bi bi-cart-check"></i>Xac nhan dat xe</h2>

    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card-custom mb-4">
                <div class="card-header"><i class="bi bi-calendar-range me-2"></i>Thoi gian thue</div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="text-muted small">Nhan xe</div>
                            <div class="fw-bold">${pickupAt}</div>
                        </div>
                        <div class="col-md-6">
                            <div class="text-muted small">Tra xe</div>
                            <div class="fw-bold">${returnAt}</div>
                        </div>
                    </div>
                    <div class="mt-2 text-accent">So ngay uoc tinh: <strong>${days}</strong> ngay</div>
                </div>
            </div>

            <div class="card-custom">
                <div class="card-header"><i class="bi bi-car-front me-2"></i>Xe da chon (${selectedCars.size()})</div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/book" id="bookingForm">
                        <input type="hidden" name="pickupAt" value="${pickupAt}">
                        <input type="hidden" name="returnAt" value="${returnAt}">

                        <c:forEach var="car" items="${selectedCars}" varStatus="st">
                            <div class="car-card mb-3">
                                <input type="hidden" name="carId" value="${car.carId}">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <div class="car-brand">${car.brand} ${car.model}</div>
                                        <div class="text-muted small">${car.seatCount} cho | ${car.transmission} | Con ${car.availableQuantity} xe cung mau</div>
                                    </div>
                                    <div class="car-price"><fmt:formatNumber value="${car.dailyRate}" pattern="#,###"/>/ngay</div>
                                </div>
                                <div class="mt-2">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox"
                                               name="requiresDriver" value="${car.carId}"
                                               id="driver-${car.carId}">
                                        <label class="form-check-label" for="driver-${car.carId}">
                                            <i class="bi bi-person-badge me-1"></i>Thue kem tai xe (+300,000 VND/ngay)
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <div class="divider"></div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Dia diem nhan xe</label>
                                <input type="text" name="pickupLocation" class="form-control"
                                       value="${pickupLocation}"
                                       maxlength="255" required
                                       placeholder="VD: 123 Nguyen Hue, Q1, HCM">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Dia diem tra xe</label>
                                <input type="text" name="returnLocation" class="form-control"
                                       value="${returnLocation}"
                                       maxlength="255" required
                                       placeholder="VD: San bay Tan Son Nhat">
                            </div>
                        </div>

                        <div class="divider"></div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="payment-option">
                                    <input class="form-check-input" type="radio" name="paymentMode"
                                           value="DEPOSIT_ONLY"
                                           ${paymentMode != 'FULL_PREPAYMENT' ? 'checked' : ''}>
                                    <span>
                                        <strong>Thanh toan coc</strong>
                                        <small>Giu xe truoc, tien thue thanh toan sau</small>
                                    </span>
                                </label>
                            </div>
                            <div class="col-md-6">
                                <label class="payment-option">
                                    <input class="form-check-input" type="radio" name="paymentMode"
                                           value="FULL_PREPAYMENT"
                                           ${paymentMode == 'FULL_PREPAYMENT' ? 'checked' : ''}>
                                    <span>
                                        <strong>Thanh toan toan bo</strong>
                                        <small>Coc + tien xe + phi tai xe neu co</small>
                                    </span>
                                </label>
                            </div>
                        </div>

                        <div class="text-end mt-4">
                            <a href="${pageContext.request.contextPath}/search" class="btn btn-outline-accent me-2">
                                <i class="bi bi-arrow-left me-1"></i>Quay lai
                            </a>
                            <button type="submit" class="btn btn-accent px-4">
                                <i class="bi bi-credit-card me-1"></i>Tiep tuc thanh toan
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card-custom" style="position:sticky;top:80px">
                <div class="card-header"><i class="bi bi-receipt me-2"></i>Tom tat</div>
                <div class="card-body">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">So xe:</span>
                        <span>${selectedCars.size()}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">So ngay:</span>
                        <span>${days}</span>
                    </div>
                    <div class="divider"></div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Tien thue xe:</span>
                        <span class="fw-bold"><fmt:formatNumber value="${totalRental}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Phi tai xe uoc tinh:</span>
                        <span class="fw-bold"><fmt:formatNumber value="${totalDriverFee}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Dat coc bat buoc:</span>
                        <span class="text-warning fw-bold"><fmt:formatNumber value="${totalDeposit}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="divider"></div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="fw-bold">Can thanh toan de giu xe:</span>
                        <span class="car-price"><fmt:formatNumber value="${totalDeposit}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="d-flex justify-content-between small text-muted">
                        <span>Neu tra toan bo:</span>
                        <span><fmt:formatNumber value="${fullPrepaymentTotal}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="mt-3 p-2 bg-accent-soft rounded text-center small">
                        <i class="bi bi-info-circle me-1 text-accent"></i>
                        He thong chi giu xe sau khi thanh toan coc thanh cong.
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
