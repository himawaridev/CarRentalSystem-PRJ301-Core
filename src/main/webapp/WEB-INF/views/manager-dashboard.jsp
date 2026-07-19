<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Quan ly - Dashboard"/></jsp:include>

<div class="container py-5">
    <h2 class="page-heading mb-4"><i class="bi bi-gear"></i>Quan ly he thong</h2>

    <c:if test="${not empty success}"><div class="alert alert-custom-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-custom-error">${error}</div></c:if>

    <!-- Tabs -->
    <ul class="nav nav-tabs-custom mb-4">
        <li class="nav-item">
            <a class="nav-link ${tab == 'cars' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/dashboard?tab=cars">
                <i class="bi bi-car-front me-1"></i>Quan ly xe
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${tab == 'assign' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/dashboard?tab=assign">
                <i class="bi bi-person-badge me-1"></i>Gan tai xe
            </a>
        </li>
    </ul>

    <!-- Cars Tab -->
    <c:if test="${tab == 'cars'}">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Danh sach xe (${cars.size()})</h5>
            <button class="btn btn-accent btn-sm" data-bs-toggle="modal" data-bs-target="#addCarModal">
                <i class="bi bi-plus-lg me-1"></i>Them xe
            </button>
        </div>

        <div class="table-responsive">
            <table class="table table-custom">
                <thead>
                    <tr>
                        <th>ID</th><th>Bien so</th><th>Hang</th><th>Model</th><th>Loai</th><th>Con</th>
                        <th>Gia/ngay</th><th>Dat coc</th><th>Trang thai</th><th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="car" items="${cars}">
                    <tr>
                        <td>${car.carId}</td>
                        <td class="fw-bold">${car.licensePlate}</td>
                        <td>${car.brand}</td>
                        <td>${car.model}</td>
                        <td><span class="car-tag">${car.seatCount} cho</span></td>
                        <td><span class="car-tag">${car.availableQuantity} xe</span></td>
                        <td><fmt:formatNumber value="${car.dailyRate}" pattern="#,###"/></td>
                        <td><fmt:formatNumber value="${car.depositAmount}" pattern="#,###"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${car.booked}"><span class="badge-status badge-deposit">DA DUOC DAT</span></c:when>
                                <c:when test="${car.status == 'AVAILABLE'}"><span class="badge-status badge-accepted">AVAILABLE</span></c:when>
                                <c:when test="${car.status == 'MAINTENANCE'}"><span class="badge-status badge-pending">MAINTENANCE</span></c:when>
                                <c:otherwise><span class="badge-status badge-cancelled">${car.status}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <button class="btn btn-sm btn-outline-accent" data-bs-toggle="modal"
                                    data-bs-target="#editCarModal-${car.carId}"><i class="bi bi-pencil"></i></button>
                        </td>
                    </tr>

                    <!-- Edit Modal -->
                    <div class="modal fade" id="editCarModal-${car.carId}" tabindex="-1">
                        <div class="modal-dialog modal-lg">
                            <div class="modal-content">
                                <form method="post" action="${pageContext.request.contextPath}/manager/dashboard">
                                    <input type="hidden" name="action" value="editCar">
                                    <input type="hidden" name="carId" value="${car.carId}">
                                    <div class="modal-header"><h5 class="modal-title">Sua xe: ${car.licensePlate}</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body">
                                        <div class="row g-3">
                                            <div class="col-md-4">
                                                <label class="form-label">Loai xe</label>
                                                <select name="carTypeId" class="form-select">
                                                    <c:forEach var="ct" items="${carTypes}">
                                                        <option value="${ct.carTypeId}" ${ct.carTypeId == car.carTypeId ? 'selected' : ''}>${ct.typeName}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-4"><label class="form-label">Bien so</label><input type="text" name="licensePlate" class="form-control" value="${car.licensePlate}" required></div>
                                            <div class="col-md-4"><label class="form-label">Hang xe</label><input type="text" name="brand" class="form-control" value="${car.brand}" required></div>
                                            <div class="col-md-4"><label class="form-label">Model</label><input type="text" name="model" class="form-control" value="${car.model}" required></div>
                                            <div class="col-md-4"><label class="form-label">Nam SX</label><input type="number" name="manufactureYear" class="form-control" value="${car.manufactureYear}"></div>
                                            <div class="col-md-4"><label class="form-label">Mau</label><input type="text" name="color" class="form-control" value="${car.color}"></div>
                                            <div class="col-md-4">
                                                <label class="form-label">Hop so</label>
                                                <select name="transmission" class="form-select">
                                                    <option value="">--</option>
                                                    <option value="AUTOMATIC" ${car.transmission == 'AUTOMATIC' ? 'selected' : ''}>AUTOMATIC</option>
                                                    <option value="MANUAL" ${car.transmission == 'MANUAL' ? 'selected' : ''}>MANUAL</option>
                                                </select>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Nhien lieu</label>
                                                <select name="fuelType" class="form-select">
                                                    <option value="">--</option>
                                                    <option value="GASOLINE" ${car.fuelType == 'GASOLINE' ? 'selected' : ''}>GASOLINE</option>
                                                    <option value="DIESEL" ${car.fuelType == 'DIESEL' ? 'selected' : ''}>DIESEL</option>
                                                    <option value="HYBRID" ${car.fuelType == 'HYBRID' ? 'selected' : ''}>HYBRID</option>
                                                    <option value="ELECTRIC" ${car.fuelType == 'ELECTRIC' ? 'selected' : ''}>ELECTRIC</option>
                                                </select>
                                            </div>
                                            <div class="col-md-4"><label class="form-label">ODO (km)</label><input type="number" name="mileage" class="form-control" value="${car.mileage}" required></div>
                                            <div class="col-md-4"><label class="form-label">Gia/ngay</label><input type="number" name="dailyRate" class="form-control" value="${car.dailyRate}" required></div>
                                            <div class="col-md-4"><label class="form-label">Dat coc</label><input type="number" name="depositAmount" class="form-control" value="${car.depositAmount}" required></div>
                                            <div class="col-md-4">
                                                <label class="form-label">Trang thai</label>
                                                <select name="status" class="form-select">
                                                    <option value="AVAILABLE" ${car.status == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                                                    <option value="MAINTENANCE" ${car.status == 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                                    <option value="INACTIVE" ${car.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                                    <option value="RETIRED" ${car.status == 'RETIRED' ? 'selected' : ''}>RETIRED</option>
                                                </select>
                                            </div>
                                            <div class="col-md-6"><label class="form-label">Image URL</label><input type="text" name="imageUrl" class="form-control" value="${car.imageUrl}"></div>
                                            <div class="col-md-6"><label class="form-label">Mo ta</label><input type="text" name="description" class="form-control" value="${car.description}"></div>
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-outline-accent" data-bs-dismiss="modal">Huy</button>
                                        <button type="submit" class="btn btn-accent">Luu</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <!-- Add Car Modal -->
        <div class="modal fade" id="addCarModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/manager/dashboard">
                        <input type="hidden" name="action" value="addCar">
                        <div class="modal-header"><h5 class="modal-title">Them xe moi</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                        <div class="modal-body">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label">Loai xe</label>
                                    <select name="carTypeId" class="form-select" required>
                                        <c:forEach var="ct" items="${carTypes}">
                                            <option value="${ct.carTypeId}">${ct.typeName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4"><label class="form-label">Bien so</label><input type="text" name="licensePlate" class="form-control" required></div>
                                <div class="col-md-4"><label class="form-label">Hang xe</label><input type="text" name="brand" class="form-control" required></div>
                                <div class="col-md-4"><label class="form-label">Model</label><input type="text" name="model" class="form-control" required></div>
                                <div class="col-md-4"><label class="form-label">Nam SX</label><input type="number" name="manufactureYear" class="form-control"></div>
                                <div class="col-md-4"><label class="form-label">Mau</label><input type="text" name="color" class="form-control"></div>
                                <div class="col-md-4">
                                    <label class="form-label">Hop so</label>
                                    <select name="transmission" class="form-select"><option value="">--</option><option value="AUTOMATIC">AUTOMATIC</option><option value="MANUAL">MANUAL</option></select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Nhien lieu</label>
                                    <select name="fuelType" class="form-select"><option value="">--</option><option value="GASOLINE">GASOLINE</option><option value="DIESEL">DIESEL</option><option value="HYBRID">HYBRID</option><option value="ELECTRIC">ELECTRIC</option></select>
                                </div>
                                <div class="col-md-4"><label class="form-label">ODO</label><input type="number" name="mileage" class="form-control" value="0" required></div>
                                <div class="col-md-4"><label class="form-label">Gia/ngay (VND)</label><input type="number" name="dailyRate" class="form-control" required></div>
                                <div class="col-md-4"><label class="form-label">Dat coc (VND)</label><input type="number" name="depositAmount" class="form-control" value="0" required></div>
                                <div class="col-md-4">
                                    <label class="form-label">Trang thai</label>
                                    <select name="status" class="form-select"><option value="AVAILABLE">AVAILABLE</option><option value="MAINTENANCE">MAINTENANCE</option></select>
                                </div>
                                <div class="col-md-6"><label class="form-label">Image URL</label><input type="text" name="imageUrl" class="form-control"></div>
                                <div class="col-md-6"><label class="form-label">Mo ta</label><input type="text" name="description" class="form-control"></div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-accent" data-bs-dismiss="modal">Huy</button>
                            <button type="submit" class="btn btn-accent">Them xe</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </c:if>

    <!-- Assign Driver Tab -->
    <c:if test="${tab == 'assign'}">
        <h5 class="mb-3">Gan tai xe cho hop dong</h5>

        <c:if test="${not empty acceptedContracts}">
        <div class="mb-4">
            <label class="form-label">Chon hop dong da xac nhan:</label>
            <div class="d-flex flex-wrap gap-2">
                <c:forEach var="ac" items="${acceptedContracts}">
                    <a href="${pageContext.request.contextPath}/manager/dashboard?tab=assign&contractId=${ac.contractId}"
                       class="btn btn-sm ${contract != null and contract.contractId == ac.contractId ? 'btn-accent' : 'btn-outline-accent'}">
                        ${ac.contractCode}
                    </a>
                </c:forEach>
            </div>
        </div>
        </c:if>

        <c:if test="${not empty details}">
        <div class="card-custom mb-4">
            <div class="card-header">Hop dong: ${contract.contractCode} | ${contract.customerName}</div>
            <div class="card-body">
                <c:forEach var="d" items="${details}">
                    <div class="car-card mb-2">
                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <div>
                                <strong>${d.carBrand} ${d.carModel}</strong> - ${d.licensePlate}
                                <c:if test="${d.requiresDriver}">
                                    <span class="car-tag"><i class="bi bi-person-badge me-1"></i>Can tai xe</span>
                                </c:if>
                                <c:if test="${not d.requiresDriver}">
                                    <span class="car-tag text-muted">Tu lai</span>
                                </c:if>
                            </div>
                            <c:if test="${d.requiresDriver}">
                                <c:set var="existingA" value="${existingAssignments[d.contractDetailId]}" />
                                <c:choose>
                                    <c:when test="${not empty existingA}">
                                        <!-- Already has driver -->
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="badge-status badge-accepted">
                                                <i class="bi bi-person-check me-1"></i>Da gan: ${existingA.driverName}
                                            </span>
                                            <span class="text-muted small">(${existingA.assignmentStatus})</span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- Assign form -->
                                        <form method="post" action="${pageContext.request.contextPath}/manager/dashboard" class="d-flex gap-2 align-items-center"
                                              onsubmit="return validateDriverSelect(this)">
                                            <input type="hidden" name="action" value="assignDriver">
                                            <input type="hidden" name="contractDetailId" value="${d.contractDetailId}">
                                            <input type="hidden" name="contractId" value="${contract.contractId}">
                                            <select name="driverId" class="form-select form-select-sm" style="width:260px" required>
                                                <option value="">-- Chon tai xe --</option>
                                                <c:forEach var="dr" items="${drivers}">
                                                    <c:choose>
                                                        <c:when test="${dr.busy}">
                                                            <option value="${dr.driverId}" data-busy="true" style="color:#dc2626">
                                                                ${dr.fullName} (${dr.licenseClass}) - DANG BAN
                                                            </option>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <option value="${dr.driverId}" data-busy="false" style="color:#059669">
                                                                ${dr.fullName} (${dr.licenseClass}) - Ranh
                                                            </option>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </select>
                                            <button class="btn btn-sm btn-accent">Gan</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
        </c:if>

        <!-- Active assignments table -->
        <c:if test="${not empty activeAssignments}">
        <h5 class="mt-4 mb-3"><i class="bi bi-people me-2"></i>Danh sach tai xe dang hoat dong</h5>
        <div class="table-responsive">
            <table class="table table-custom">
                <thead>
                    <tr>
                        <th>Tai xe</th>
                        <th>Xe</th>
                        <th>Bien so</th>
                        <th>Hop dong</th>
                        <th>Khach hang</th>
                        <th>Nhan xe</th>
                        <th>Tra xe</th>
                        <th>Trang thai</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="a" items="${activeAssignments}">
                        <tr>
                            <td><strong>${a.driverName}</strong></td>
                            <td>${a.carBrand} ${a.carModel}</td>
                            <td><span class="fw-bold">${a.licensePlate}</span></td>
                            <td><span class="text-accent">${a.contractCode}</span></td>
                            <td>${a.customerName}</td>
                            <td class="small">${a.pickupAt}</td>
                            <td class="small">${a.returnAt}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${a.assignmentStatus == 'ASSIGNED'}">
                                        <span class="badge-status badge-pending">Cho nhan xe</span>
                                    </c:when>
                                    <c:when test="${a.assignmentStatus == 'HANDOVER_RECEIVED'}">
                                        <span class="badge-status badge-picked">Dang lai</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-status badge-deposit">${a.assignmentStatus}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        </c:if>

        <c:if test="${empty activeAssignments}">
            <div class="text-center py-4 text-muted">
                <i class="bi bi-person-x" style="font-size:2.5rem;opacity:.3"></i>
                <p class="mt-2">Chua co tai xe nao duoc gan.</p>
            </div>
        </c:if>
    </c:if>
</div>

<script>
function validateDriverSelect(form) {
    var select = form.querySelector('select[name="driverId"]');
    var selected = select.options[select.selectedIndex];
    if (!selected || !selected.value) return false;
    if (selected.getAttribute('data-busy') === 'true') {
        return confirm('Tai xe nay DANG BAN (da nhan xe khac).\n\nBan co chac chan muon gan tai xe nay khong?\nNeu tai xe bi trung lich, he thong se tu choi.');
    }
    return true;
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
