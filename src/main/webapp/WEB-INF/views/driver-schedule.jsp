<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Lich lai xe"/></jsp:include>

<div class="container py-5">
    <h2 class="page-heading mb-4"><i class="bi bi-calendar-check"></i>Lich lai xe cua toi</h2>

    <c:if test="${not empty success}">
        <div class="alert alert-custom-success">${success}</div>
    </c:if>

    <c:if test="${empty schedule}">
        <div class="text-center py-5">
            <i class="bi bi-calendar-x" style="font-size:4rem;color:var(--gray-300)"></i>
            <h4 class="text-muted mt-3">Chua co lich lai nao</h4>
        </div>
    </c:if>

    <c:if test="${not empty schedule}">
    <div class="row g-4">
        <c:forEach var="a" items="${schedule}">
            <div class="col-md-6 col-lg-4">
                <div class="card-custom">
                    <div class="card-header d-flex justify-content-between">
                        <span>${a.contractCode}</span>
                        <c:choose>
                            <c:when test="${a.assignmentStatus == 'ASSIGNED'}"><span class="badge-status badge-pending">Cho nhan xe</span></c:when>
                            <c:when test="${a.assignmentStatus == 'HANDOVER_RECEIVED'}"><span class="badge-status badge-deposit">Da nhan xe</span></c:when>
                            <c:when test="${a.assignmentStatus == 'TRIP_IN_PROGRESS'}"><span class="badge-status badge-picked">Dang chay</span></c:when>
                            <c:when test="${a.assignmentStatus == 'TRIP_COMPLETED'}"><span class="badge-status badge-completed">Hoan tat</span></c:when>
                            <c:otherwise><span class="badge bg-secondary">${a.assignmentStatus}</span></c:otherwise>
                        </c:choose>
                    </div>
                    <div class="card-body">
                        <div class="car-brand mb-1">${a.carBrand} ${a.carModel}</div>
                        <div class="text-muted small mb-2">${a.licensePlate}</div>
                        <div class="small mb-1"><i class="bi bi-person me-1"></i>Khach: ${a.customerName}</div>
                        <div class="small mb-1"><i class="bi bi-geo-alt me-1 text-accent"></i>Don: ${a.pickupLocation}</div>
                        <div class="small mb-1"><i class="bi bi-geo me-1 text-accent"></i>Tra: ${a.returnLocation}</div>
                        <div class="small mb-1"><i class="bi bi-clock me-1"></i>${a.pickupAt} - ${a.returnAt}</div>

                        <div class="divider"></div>

                        <c:if test="${a.assignmentStatus == 'ASSIGNED'}">
                            <form method="post" action="${pageContext.request.contextPath}/driver/schedule">
                                <input type="hidden" name="assignmentId" value="${a.assignmentId}">
                                <input type="hidden" name="status" value="HANDOVER_RECEIVED">
                                <button class="btn btn-accent btn-sm w-100"><i class="bi bi-check-circle me-1"></i>Xac nhan nhan xe</button>
                            </form>
                        </c:if>
                        <c:if test="${a.assignmentStatus == 'HANDOVER_RECEIVED'}">
                            <form method="post" action="${pageContext.request.contextPath}/driver/schedule">
                                <input type="hidden" name="assignmentId" value="${a.assignmentId}">
                                <input type="hidden" name="status" value="TRIP_IN_PROGRESS">
                                <button class="btn btn-accent btn-sm w-100"><i class="bi bi-play-fill me-1"></i>Bat dau chuyen di</button>
                            </form>
                        </c:if>
                        <c:if test="${a.assignmentStatus == 'TRIP_IN_PROGRESS'}">
                            <form method="post" action="${pageContext.request.contextPath}/driver/schedule">
                                <input type="hidden" name="assignmentId" value="${a.assignmentId}">
                                <input type="hidden" name="status" value="TRIP_COMPLETED">
                                <button class="btn btn-accent btn-sm w-100"><i class="bi bi-flag-fill me-1"></i>Hoan tat chuyen di</button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
