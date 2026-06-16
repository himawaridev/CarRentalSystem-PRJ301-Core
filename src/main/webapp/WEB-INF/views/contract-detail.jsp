<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Chi tiet hop dong"/></jsp:include>

<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="page-heading mb-0"><i class="bi bi-file-earmark-text"></i>Chi tiet hop dong</h2>
        <a href="${pageContext.request.contextPath}/my-contracts" class="btn btn-outline-accent btn-sm">
            <i class="bi bi-arrow-left me-1"></i>Quay lai
        </a>
    </div>

    <!-- Contract header card -->
    <div class="card-custom mb-4">
        <div class="card-body">
            <div class="row g-4">
                <div class="col-md-6">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <h4 class="mb-0 text-accent fw-bold">${contract.contractCode}</h4>
                        <c:choose>
                            <c:when test="${contract.status == 'PENDING_PAYMENT'}"><span class="badge-status badge-pending">Cho thanh toan</span></c:when>
                            <c:when test="${contract.status == 'PAYMENT_EXPIRED'}"><span class="badge-status badge-rejected">Het han</span></c:when>
                            <c:when test="${contract.status == 'RESERVED'}"><span class="badge-status badge-deposit">Da giu xe</span></c:when>
                            <c:when test="${contract.status == 'CONFIRMED'}"><span class="badge-status badge-accepted">Da xac nhan</span></c:when>
                            <c:when test="${contract.status == 'CAR_PICKED_UP'}"><span class="badge-status badge-picked">Da nhan xe</span></c:when>
                            <c:when test="${contract.status == 'CAR_RETURNED'}"><span class="badge-status badge-returned">Da tra xe</span></c:when>
                            <c:when test="${contract.status == 'SETTLEMENT_PENDING'}"><span class="badge-status badge-pending">Dang quyet toan</span></c:when>
                            <c:when test="${contract.status == 'COMPLETED'}"><span class="badge-status badge-completed">Hoan tat</span></c:when>
                            <c:when test="${contract.status == 'CANCELLED'}"><span class="badge-status badge-cancelled">Da huy</span></c:when>
                            <c:otherwise><span class="badge bg-secondary">${contract.status}</span></c:otherwise>
                        </c:choose>
                        <c:if test="${refundCompleted}">
                            <span class="badge-status badge-completed">Da hoan tien</span>
                        </c:if>
                        <c:if test="${refundPending}">
                            <span class="badge-status badge-pending">Dang cho hoan tien</span>
                        </c:if>
                    </div>
                    <table class="table table-borderless mb-0" style="font-size:.9rem">
                        <tr>
                            <td class="text-muted" style="width:140px"><i class="bi bi-calendar-check me-2"></i>Ngay nhan xe</td>
                            <td class="fw-bold">${contract.pickupAt}</td>
                        </tr>
                        <tr>
                            <td class="text-muted"><i class="bi bi-calendar-x me-2"></i>Ngay tra xe</td>
                            <td class="fw-bold">${contract.returnAt}</td>
                        </tr>
                        <tr>
                            <td class="text-muted"><i class="bi bi-geo-alt me-2"></i>Noi nhan</td>
                            <td>${contract.pickupLocation}</td>
                        </tr>
                        <tr>
                            <td class="text-muted"><i class="bi bi-geo me-2"></i>Noi tra</td>
                            <td>${contract.returnLocation}</td>
                        </tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <div class="p-3 rounded-3" style="background:var(--gray-50);border:1px solid var(--border)">
                        <h6 class="fw-bold mb-3"><i class="bi bi-cash-stack me-2"></i>Thong tin thanh toan</h6>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">Tien dat coc:</span>
                            <span class="fw-bold"><fmt:formatNumber value="${contract.depositAmountDue}" pattern="#,###"/> VND</span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">Tong thanh toan:</span>
                            <span class="fw-bold text-accent" style="font-size:1.1rem">
                                <fmt:formatNumber value="${contract.finalAmountDue}" pattern="#,###"/> VND
                            </span>
                        </div>
                        <hr style="border-color:var(--border)">
                        <div class="d-flex justify-content-between small">
                            <span class="text-muted">Ngay tao hop dong:</span>
                            <span>${contract.createdAt}</span>
                        </div>
                        <c:if test="${latestRefund != null}">
                            <hr style="border-color:var(--border)">
                            <div class="refund-summary-box ${refundCompleted ? 'completed' : 'pending'}">
                                <div class="d-flex justify-content-between align-items-center gap-3 mb-2">
                                    <span class="fw-bold">
                                        <i class="bi ${refundCompleted ? 'bi-check2-circle' : 'bi-arrow-counterclockwise'} me-1"></i>
                                        ${refundCompleted ? 'Da hoan tien' : 'Dang cho hoan tien'}
                                    </span>
                                    <strong>${latestRefundAmountText} VND</strong>
                                </div>
                                <div class="small text-muted">
                                    Phuong thuc: ${latestRefund.refundMethod}
                                </div>
                                <c:if test="${not empty latestRefund.providerRefundRef}">
                                    <div class="small text-muted">
                                        Ma giao dich: ${latestRefund.providerRefundRef}
                                    </div>
                                </c:if>
                                <c:if test="${not empty latestRefund.proofOfRefund}">
                                    <div class="small text-muted">
                                        Chung tu: ${latestRefund.proofOfRefund}
                                    </div>
                                </c:if>
                                <c:if test="${latestRefund.completedAt != null}">
                                    <div class="small text-muted">
                                        Thoi gian hoan: ${latestRefund.completedAt}
                                    </div>
                                </c:if>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Car details -->
    <h5 class="mb-3"><i class="bi bi-car-front me-2"></i>Chi tiet xe thue (${details.size()} xe)</h5>
    <div class="row g-3 mb-4">
        <c:forEach var="d" items="${details}">
            <div class="col-md-6">
                <div class="card-custom h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h5 class="fw-bold mb-1">${d.carBrand} ${d.carModel}</h5>
                                <span class="text-muted small">${d.licensePlate} | ${d.seatCount} cho</span>
                            </div>
                            <c:choose>
                                <c:when test="${d.requiresDriver}">
                                    <span class="car-tag"><i class="bi bi-person-badge me-1"></i>Co tai xe</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="car-tag text-muted">Tu lai</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <table class="table table-sm table-borderless mb-0" style="font-size:.85rem">
                            <tr>
                                <td class="text-muted" style="width:140px">Gia thue/ngay</td>
                                <td><fmt:formatNumber value="${d.rentalDailyRate}" pattern="#,###"/> VND</td>
                            </tr>
                            <tr>
                                <td class="text-muted">So ngay thue</td>
                                <td>${d.estimatedDays} ngay</td>
                            </tr>
                            <tr>
                                <td class="text-muted">Tien thue xe</td>
                                <td class="fw-bold"><fmt:formatNumber value="${d.rentalAmount}" pattern="#,###"/> VND</td>
                            </tr>
                            <c:if test="${d.requiresDriver}">
                                <tr>
                                    <td class="text-muted">Phi tai xe/ngay</td>
                                    <td><fmt:formatNumber value="${d.driverDailyRate}" pattern="#,###"/> VND</td>
                                </tr>
                                <tr>
                                    <td class="text-muted">Tien tai xe</td>
                                    <td class="fw-bold"><fmt:formatNumber value="${d.driverAmount}" pattern="#,###"/> VND</td>
                                </tr>
                            </c:if>
                            <tr style="border-top:1px solid var(--border)">
                                <td class="text-muted">Tong cho xe nay</td>
                                <td class="fw-bold text-accent"><fmt:formatNumber value="${d.lineTotal}" pattern="#,###"/> VND</td>
                            </tr>
                        </table>

                        <!-- Driver assignment info -->
                        <c:set var="assignment" value="${driverAssignments[d.contractDetailId]}" />
                        <c:if test="${d.requiresDriver}">
                            <div class="mt-3 p-2 rounded" style="background:var(--gray-50);border:1px solid var(--border)">
                                <c:choose>
                                    <c:when test="${not empty assignment}">
                                        <div class="d-flex align-items-center gap-2">
                                            <i class="bi bi-person-check-fill text-success"></i>
                                            <span class="small">
                                                <strong>Tai xe: ${assignment.driverName}</strong>
                                                <span class="text-muted ms-1">(${assignment.assignmentStatus})</span>
                                            </span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="d-flex align-items-center gap-2">
                                            <i class="bi bi-hourglass-split text-warning"></i>
                                            <span class="small text-muted">Chua duoc gan tai xe</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- Status timeline -->
    <h5 class="mb-3"><i class="bi bi-clock-history me-2"></i>Trang thai hop dong</h5>
    <div class="card-custom">
        <div class="card-body">
            <div class="d-flex flex-wrap gap-2 align-items-center">
                <span class="badge ${contract.status == 'PENDING_PAYMENT' || contract.status == 'RESERVED' || contract.status == 'CONFIRMED' || contract.status == 'CAR_PICKED_UP' || contract.status == 'CAR_RETURNED' || contract.status == 'SETTLEMENT_PENDING' || contract.status == 'COMPLETED' ? 'bg-primary' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-send me-1"></i>Tao don
                </span>
                <i class="bi bi-chevron-right text-muted"></i>
                <span class="badge ${contract.status == 'RESERVED' || contract.status == 'CONFIRMED' || contract.status == 'CAR_PICKED_UP' || contract.status == 'CAR_RETURNED' || contract.status == 'SETTLEMENT_PENDING' || contract.status == 'COMPLETED' ? 'bg-primary' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-credit-card me-1"></i>Thanh toan coc
                </span>
                <i class="bi bi-chevron-right text-muted"></i>
                <span class="badge ${contract.status == 'CONFIRMED' || contract.status == 'CAR_PICKED_UP' || contract.status == 'CAR_RETURNED' || contract.status == 'SETTLEMENT_PENDING' || contract.status == 'COMPLETED' ? 'bg-primary' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-check-circle me-1"></i>Xac nhan
                </span>
                <i class="bi bi-chevron-right text-muted"></i>
                <span class="badge ${contract.status == 'CAR_PICKED_UP' || contract.status == 'CAR_RETURNED' || contract.status == 'SETTLEMENT_PENDING' || contract.status == 'COMPLETED' ? 'bg-primary' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-car-front me-1"></i>Nhan xe
                </span>
                <i class="bi bi-chevron-right text-muted"></i>
                <span class="badge ${contract.status == 'CAR_RETURNED' || contract.status == 'SETTLEMENT_PENDING' || contract.status == 'COMPLETED' ? 'bg-primary' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-arrow-return-left me-1"></i>Tra xe
                </span>
                <i class="bi bi-chevron-right text-muted"></i>
                <span class="badge ${contract.status == 'SETTLEMENT_PENDING' || contract.status == 'COMPLETED' ? 'bg-primary' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-cash-stack me-1"></i>Quyet toan
                </span>
                <i class="bi bi-chevron-right text-muted"></i>
                <span class="badge ${contract.status == 'COMPLETED' ? 'bg-success' : 'bg-secondary'} rounded-pill px-3 py-2">
                    <i class="bi bi-check-all me-1"></i>Hoan tat
                </span>
            </div>
            <c:if test="${contract.status == 'CANCELLED'}">
                <div class="mt-3 text-danger">
                    <i class="bi bi-x-circle-fill me-1"></i>Hop dong nay da bi huy.
                </div>
            </c:if>
            <c:if test="${refundCompleted}">
                <div class="mt-3 text-success">
                    <i class="bi bi-check2-circle me-1"></i>
                    Da hoan tien <strong>${latestRefundAmountText} VND</strong>
                    cho hop dong nay.
                </div>
            </c:if>
            <c:if test="${refundPending}">
                <div class="mt-3 text-warning">
                    <i class="bi bi-arrow-counterclockwise me-1"></i>
                    Yeu cau hoan tien <strong>${latestRefundAmountText} VND</strong>
                    dang cho nhan vien xu ly.
                </div>
            </c:if>
            <c:if test="${contract.status == 'PAYMENT_EXPIRED'}">
                <div class="mt-3 text-danger">
                    <i class="bi bi-x-circle-fill me-1"></i>Giao dich thanh toan da het han.
                </div>
            </c:if>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
