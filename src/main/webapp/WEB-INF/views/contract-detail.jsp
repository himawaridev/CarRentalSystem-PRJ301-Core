<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Chi tiet hop dong"/></jsp:include>

<div class="container py-5">
    <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline-accent mb-3">
        <i class="bi bi-arrow-left me-1"></i>Quay lai
    </a>

    <c:if test="${not empty contract}">
    <div class="row g-4">
        <div class="col-lg-6">
            <div class="card-custom">
                <div class="card-header"><i class="bi bi-file-text me-2"></i>Hop dong: ${contract.contractCode}</div>
                <div class="card-body">
                    <div class="mb-2"><strong>Khach hang:</strong> ${contract.customerName}</div>
                    <div class="mb-2"><strong>SDT:</strong> ${contract.customerPhone}</div>
                    <div class="mb-2"><strong>Nhan xe:</strong> ${contract.pickupAt}</div>
                    <div class="mb-2"><strong>Tra xe:</strong> ${contract.returnAt}</div>
                    <div class="mb-2"><strong>Dia diem nhan:</strong> ${contract.pickupLocation}</div>
                    <div class="mb-2"><strong>Dia diem tra:</strong> ${contract.returnLocation}</div>
                    <div class="divider"></div>
                    <div class="mb-2"><strong>Dat coc:</strong> <fmt:formatNumber value="${contract.depositAmountDue}" pattern="#,###"/> VND</div>
                    <div class="mb-2"><strong>Tong tien:</strong> <span class="car-price"><fmt:formatNumber value="${contract.finalAmountDue}" pattern="#,###"/> VND</span></div>
                    <div class="mb-2"><strong>Trang thai:</strong>
                        <c:choose>
                            <c:when test="${contract.status == 'PENDING_REVIEW'}"><span class="badge-status badge-pending">Cho duyet</span></c:when>
                            <c:when test="${contract.status == 'ACCEPTED'}"><span class="badge-status badge-accepted">Da duyet</span></c:when>
                            <c:when test="${contract.status == 'REJECTED'}"><span class="badge-status badge-rejected">Tu choi</span></c:when>
                            <c:when test="${contract.status == 'DEPOSIT_PAID'}"><span class="badge-status badge-deposit">Da dat coc</span></c:when>
                            <c:when test="${contract.status == 'CAR_PICKED_UP'}"><span class="badge-status badge-picked">Da nhan xe</span></c:when>
                            <c:when test="${contract.status == 'CAR_RETURNED'}"><span class="badge-status badge-returned">Da tra xe</span></c:when>
                            <c:when test="${contract.status == 'FINAL_PAYMENT_COMPLETED'}"><span class="badge-status badge-completed">Hoan tat</span></c:when>
                            <c:otherwise><span class="badge bg-secondary">${contract.status}</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card-custom">
                <div class="card-header"><i class="bi bi-car-front me-2"></i>Danh sach xe</div>
                <div class="card-body">
                    <c:forEach var="d" items="${details}">
                        <div class="car-card mb-2">
                            <div class="car-brand">${d.carBrand} ${d.carModel}</div>
                            <div class="text-muted small">${d.licensePlate} | ${d.seatCount} cho</div>
                            <div class="mt-1">
                                <span class="car-tag">Gia: <fmt:formatNumber value="${d.rentalDailyRate}" pattern="#,###"/>/ngay</span>
                                <c:if test="${d.requiresDriver}">
                                    <span class="car-tag"><i class="bi bi-person-badge me-1"></i>Co tai xe</span>
                                </c:if>
                            </div>
                            <div class="text-accent small mt-1">Tong: <fmt:formatNumber value="${d.lineTotal}" pattern="#,###"/> VND</div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
