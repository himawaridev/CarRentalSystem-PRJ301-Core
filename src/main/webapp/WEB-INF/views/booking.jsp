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
                        <input type="hidden" name="selectionMode" value="${selectionMode}">

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
                                        <input class="form-check-input booking-driver-checkbox" type="checkbox"
                                               name="requiresDriver" value="${car.carId}"
                                               id="driver-${car.carId}"
                                               data-driver-fee="${driverFeePerCar}"
                                               ${selectedDriverCarIdNumbers.contains(car.carId) ? 'checked' : ''}>
                                        <label class="form-check-label" for="driver-${car.carId}">
                                            <i class="bi bi-person-badge me-1"></i>Thue kem tai xe (+300,000 VND/ngay)
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <div class="divider"></div>
                        <div class="row g-3">
                            <div class="col-lg-7">
                                <label class="form-label">Dia diem nhan xe</label>
                                <input type="hidden" name="pickupLocation" id="pickupLocation" value="${pickupLocation}">
                                <div class="row g-2">
                                    <div class="col-12">
                                        <input type="text" name="pickupSpecificAddress" id="pickupSpecificAddress"
                                               class="form-control"
                                               value="${pickupSpecificAddress}"
                                               maxlength="120" required
                                               placeholder="So nha, ten duong, toa nha">
                                    </div>
                                    <div class="col-md-4">
                                        <select name="pickupProvince" id="pickupProvince" class="form-select"
                                                data-selected="${pickupProvince}" required>
                                            <option value="">Tinh/TP</option>
                                        </select>
                                    </div>
                                    <div class="col-md-4">
                                        <select name="pickupDistrict" id="pickupDistrict" class="form-select"
                                                data-selected="${pickupDistrict}" required>
                                            <option value="">Quan/Huyen</option>
                                        </select>
                                    </div>
                                    <div class="col-md-4">
                                        <select name="pickupWard" id="pickupWard" class="form-select"
                                                data-selected="${pickupWard}" required>
                                            <option value="">Phuong/Xa</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="text-muted small mt-1">Dia chi se duoc luu theo thu tu: dia chi cu the, phuong/xa, quan/huyen, tinh/thanh.</div>
                            </div>
                            <div class="col-lg-5">
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

                        <div class="btn-action-group justify-content-end mt-4">
                            <a href="${pageContext.request.contextPath}/search" class="btn btn-outline-accent btn-action-nowrap">
                                <i class="bi bi-arrow-left me-1"></i>Quay lai
                            </a>
                            <button type="submit" class="btn btn-accent btn-action-nowrap px-4">
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
                        <span class="fw-bold" id="summaryDriverFee"><fmt:formatNumber value="${totalDriverFee}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Dat coc bat buoc:</span>
                        <span class="text-warning fw-bold"><fmt:formatNumber value="${totalDeposit}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="divider"></div>
                    <div class="d-flex justify-content-between mb-2"
                         id="bookingSummary"
                         data-deposit="${totalDeposit}"
                         data-rental="${totalRental}"
                         data-driver-fee-per-car="${driverFeePerCar}">
                        <span class="fw-bold" id="summaryRequiredPaymentLabel">Can thanh toan de giu xe:</span>
                        <span class="car-price" id="summaryRequiredPayment"><fmt:formatNumber value="${totalDeposit}" pattern="#,###"/> VND</span>
                    </div>
                    <div class="d-flex justify-content-between small text-muted">
                        <span>Neu tra toan bo:</span>
                        <span id="summaryFullPrepayment"><fmt:formatNumber value="${fullPrepaymentTotal}" pattern="#,###"/> VND</span>
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

<script>
(function() {
    const provinceSelect = document.getElementById('pickupProvince');
    const districtSelect = document.getElementById('pickupDistrict');
    const wardSelect = document.getElementById('pickupWard');
    const specificAddressInput = document.getElementById('pickupSpecificAddress');
    const pickupLocationInput = document.getElementById('pickupLocation');

    const fallbackLocations = [
        { name: 'TP Ho Chi Minh', districts: [
            { name: 'Quan 1', wards: ['Ben Nghe', 'Ben Thanh', 'Da Kao', 'Nguyen Thai Binh'] },
            { name: 'Quan 3', wards: ['Vo Thi Sau', 'Phuong 9', 'Phuong 11'] },
            { name: 'Quan Binh Thanh', wards: ['Phuong 1', 'Phuong 3', 'Phuong 15', 'Phuong 22'] }
        ] },
        { name: 'Ha Noi', districts: [
            { name: 'Quan Hoan Kiem', wards: ['Hang Bac', 'Hang Bong', 'Trang Tien'] },
            { name: 'Quan Ba Dinh', wards: ['Dien Bien', 'Kim Ma', 'Ngoc Ha'] },
            { name: 'Quan Cau Giay', wards: ['Dich Vong', 'Mai Dich', 'Yen Hoa'] }
        ] },
        { name: 'Da Nang', districts: [
            { name: 'Quan Hai Chau', wards: ['Hai Chau I', 'Hai Chau II', 'Thach Thang'] },
            { name: 'Quan Son Tra', wards: ['An Hai Bac', 'An Hai Dong', 'Man Thai'] }
        ] },
        { name: 'Khac', districts: [{ name: 'Khac', wards: ['Khac'] }] }
    ];

    function selectedValue(select) {
        return select ? select.getAttribute('data-selected') || '' : '';
    }

    function normalizeLocations(data) {
        return (data || []).map(function(province) {
            return {
                name: province.name,
                districts: (province.districts || []).map(function(district) {
                    return {
                        name: district.name,
                        wards: (district.wards || []).map(function(ward) { return ward.name; })
                    };
                })
            };
        }).filter(function(province) { return province.name; });
    }

    function ensureOther(locations) {
        if (!locations.some(function(item) { return item.name === 'Khac'; })) {
            locations.push({ name: 'Khac', districts: [{ name: 'Khac', wards: ['Khac'] }] });
        }
        return locations;
    }

    function setOptions(select, values, placeholder, selected) {
        select.innerHTML = '';
        const empty = document.createElement('option');
        empty.value = '';
        empty.textContent = placeholder;
        select.appendChild(empty);
        values.forEach(function(value) {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = value;
            if (value === selected) option.selected = true;
            select.appendChild(option);
        });
    }

    function findByName(items, name) {
        return (items || []).find(function(item) { return item.name === name; });
    }

    function updatePickupLocationValue() {
        if (!pickupLocationInput) return;
        const parts = [
            specificAddressInput ? specificAddressInput.value.trim() : '',
            wardSelect ? wardSelect.value : '',
            districtSelect ? districtSelect.value : '',
            provinceSelect ? provinceSelect.value : ''
        ].filter(Boolean);
        pickupLocationInput.value = parts.join(', ');
    }

    function initLocationSelectors(locations) {
        locations = ensureOther(locations && locations.length ? locations : fallbackLocations);
        const initialProvince = selectedValue(provinceSelect);
        const initialDistrict = selectedValue(districtSelect);
        const initialWard = selectedValue(wardSelect);

        setOptions(provinceSelect, locations.map(function(item) { return item.name; }), 'Tinh/TP', initialProvince);

        function refreshDistricts(selectedDistrict) {
            const province = findByName(locations, provinceSelect.value);
            const districts = province ? province.districts : [];
            setOptions(districtSelect, districts.map(function(item) { return item.name; }), 'Quan/Huyen', selectedDistrict || '');
            refreshWards(initialWard);
        }

        function refreshWards(selectedWard) {
            const province = findByName(locations, provinceSelect.value);
            const district = province ? findByName(province.districts, districtSelect.value) : null;
            setOptions(wardSelect, district ? district.wards : [], 'Phuong/Xa', selectedWard || '');
            updatePickupLocationValue();
        }

        provinceSelect.addEventListener('change', function() {
            provinceSelect.setAttribute('data-selected', provinceSelect.value);
            districtSelect.setAttribute('data-selected', '');
            wardSelect.setAttribute('data-selected', '');
            refreshDistricts('');
        });
        districtSelect.addEventListener('change', function() {
            districtSelect.setAttribute('data-selected', districtSelect.value);
            wardSelect.setAttribute('data-selected', '');
            refreshWards('');
        });
        wardSelect.addEventListener('change', updatePickupLocationValue);
        if (specificAddressInput) specificAddressInput.addEventListener('input', updatePickupLocationValue);

        refreshDistricts(initialDistrict);
        refreshWards(initialWard);
    }

    if (provinceSelect && districtSelect && wardSelect) {
        fetch('https://provinces.open-api.vn/api/?depth=3')
            .then(function(response) {
                if (!response.ok) throw new Error('Cannot load province data');
                return response.json();
            })
            .then(function(data) { initLocationSelectors(normalizeLocations(data)); })
            .catch(function() { initLocationSelectors(fallbackLocations); });
    }

    const summary = document.getElementById('bookingSummary');
    const driverFeeText = document.getElementById('summaryDriverFee');
    const requiredPaymentText = document.getElementById('summaryRequiredPayment');
    const requiredPaymentLabel = document.getElementById('summaryRequiredPaymentLabel');
    const fullPrepaymentText = document.getElementById('summaryFullPrepayment');
    const driverCheckboxes = Array.from(document.querySelectorAll('.booking-driver-checkbox'));
    const paymentModeRadios = Array.from(document.querySelectorAll('input[name="paymentMode"]'));

    function parseAmount(value) {
        const number = Number.parseFloat(value || '0');
        return Number.isFinite(number) ? number : 0;
    }

    function formatVnd(value) {
        return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(value) + ' VND';
    }

    function updateBookingSummary() {
        if (!summary) return;
        const deposit = parseAmount(summary.dataset.deposit);
        const rental = parseAmount(summary.dataset.rental);
        const driverFeePerCar = parseAmount(summary.dataset.driverFeePerCar);
        const selectedDriverCount = driverCheckboxes.filter(function(checkbox) { return checkbox.checked; }).length;
        const driverFee = selectedDriverCount * driverFeePerCar;
        const fullTotal = deposit + rental + driverFee;
        const paymentMode = (document.querySelector('input[name="paymentMode"]:checked') || {}).value || 'DEPOSIT_ONLY';
        const requiredPayment = paymentMode === 'FULL_PREPAYMENT' ? fullTotal : deposit;

        if (driverFeeText) driverFeeText.textContent = formatVnd(driverFee);
        if (fullPrepaymentText) fullPrepaymentText.textContent = formatVnd(fullTotal);
        if (requiredPaymentText) requiredPaymentText.textContent = formatVnd(requiredPayment);
        if (requiredPaymentLabel) {
            requiredPaymentLabel.textContent = paymentMode === 'FULL_PREPAYMENT'
                    ? 'Can thanh toan toan bo:'
                    : 'Can thanh toan de giu xe:';
        }
    }

    driverCheckboxes.forEach(function(checkbox) {
        checkbox.addEventListener('change', updateBookingSummary);
    });
    paymentModeRadios.forEach(function(radio) {
        radio.addEventListener('change', updateBookingSummary);
    });
    updateBookingSummary();
})();
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
