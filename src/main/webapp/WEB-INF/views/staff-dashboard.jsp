<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Nhan vien - Quan ly hop dong"/></jsp:include>

<div class="container py-5">
    <h2 class="page-heading mb-4"><i class="bi bi-clipboard-check"></i>Quan ly hop dong</h2>

    <c:if test="${not empty success}">
        <div class="alert alert-custom-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <!-- Status filter -->
    <div class="mb-4">
        <div class="btn-action-group">
            <a href="${pageContext.request.contextPath}/staff/dashboard"
               class="btn ${empty statusFilter ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Tat ca</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=PENDING_PAYMENT"
               class="btn ${statusFilter == 'PENDING_PAYMENT' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Cho thu coc</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=RESERVED"
               class="btn ${statusFilter == 'RESERVED' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Da giu xe</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=CONFIRMED"
               class="btn ${statusFilter == 'CONFIRMED' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Da xac nhan</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=CAR_PICKED_UP"
               class="btn ${statusFilter == 'CAR_PICKED_UP' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Da nhan xe</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=CAR_RETURNED"
               class="btn ${statusFilter == 'CAR_RETURNED' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Da tra xe</a>
        </div>
    </div>

    <c:if test="${empty contracts}">
        <div class="text-center py-5">
            <i class="bi bi-inbox" style="font-size:4rem;color:var(--gray-300)"></i>
            <h4 class="text-muted mt-3">Khong co hop dong nao</h4>
        </div>
    </c:if>

    <c:if test="${not empty contracts}">
    <div class="table-responsive">
        <table class="table table-custom">
            <thead>
                <tr>
                    <th>Ma HD</th>
                    <th>Khach hang</th>
                    <th>Nhan xe</th>
                    <th>Tra xe</th>
                    <th>Dat coc</th>
                    <th>Tong tien</th>
                    <th class="table-status-col">Trang thai</th>
                    <th class="table-actions-col">Hanh dong</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="c" items="${contracts}">
                    <tr>
                        <td>
                            <a href="${pageContext.request.contextPath}/contract-detail?id=${c.contractId}"
                               class="text-accent fw-bold">${c.contractCode}</a>
                        </td>
                        <td>
                            <div>${c.customerName}</div>
                            <div class="text-muted small">${c.customerPhone}</div>
                        </td>
                        <td class="small">${c.pickupAt}</td>
                        <td class="small">${c.returnAt}</td>
                        <td><fmt:formatNumber value="${c.depositAmountDue}" pattern="#,###"/></td>
                        <td><fmt:formatNumber value="${c.finalAmountDue}" pattern="#,###"/></td>
                        <td class="table-status-cell">
                            <c:choose>
                                <c:when test="${c.status == 'PENDING_PAYMENT'}"><span class="badge-status badge-pending">Cho thu coc tien mat</span></c:when>
                                <c:when test="${c.status == 'RESERVED'}"><span class="badge-status badge-deposit">Da giu xe</span></c:when>
                                <c:when test="${c.status == 'CONFIRMED'}"><span class="badge-status badge-accepted">Da xac nhan</span></c:when>
                                <c:when test="${c.status == 'CAR_PICKED_UP'}"><span class="badge-status badge-picked">Da nhan xe</span></c:when>
                                <c:when test="${c.status == 'CAR_RETURNED'}"><span class="badge-status badge-returned">Da tra xe</span></c:when>
                                <c:when test="${c.status == 'COMPLETED'}"><span class="badge-status badge-completed">Hoan tat</span></c:when>
                                <c:when test="${c.status == 'CANCELLED'}"><span class="badge-status badge-cancelled">Da huy</span></c:when>
                                <c:otherwise><span class="badge bg-secondary">${c.status}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="table-actions-cell">
                            <div class="table-action-icons staff-action-icons">
                                <c:if test="${c.status == 'PENDING_PAYMENT'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline"
                                          onsubmit="return confirm('Xac nhan da nhan du tien coc bang tien mat?')">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="confirm_cash_deposit">
                                        <button class="btn btn-sm btn-outline-accent action-icon-btn"
                                                data-bs-toggle="tooltip" data-bs-title="Da thu coc tien mat"
                                                aria-label="Da thu coc tien mat"><i class="bi bi-cash-coin"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'RESERVED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="confirm">
                                        <button class="btn btn-sm btn-outline-accent action-icon-btn"
                                                data-bs-toggle="tooltip" data-bs-title="Xac nhan don"
                                                aria-label="Xac nhan don"><i class="bi bi-check-lg"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'CONFIRMED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="car_picked_up">
                                        <button class="btn btn-sm btn-outline-accent action-icon-btn"
                                                data-bs-toggle="tooltip" data-bs-title="Giao xe"
                                                aria-label="Giao xe"><i class="bi bi-truck"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'CAR_PICKED_UP'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="car_returned">
                                        <button class="btn btn-sm btn-outline-accent action-icon-btn"
                                                data-bs-toggle="tooltip" data-bs-title="Nhan xe tra"
                                                aria-label="Nhan xe tra"><i class="bi bi-box-arrow-in-down"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'CAR_RETURNED'}">
                                    <a href="${pageContext.request.contextPath}/staff/settlement?contractId=${c.contractId}"
                                       class="btn btn-sm btn-outline-accent action-icon-btn"
                                       data-bs-toggle="tooltip" data-bs-title="Quyet toan"
                                       aria-label="Quyet toan">
                                        <i class="bi bi-cash-stack"></i>
                                    </a>
                                </c:if>
                                <c:if test="${c.status == 'PENDING_PAYMENT' or c.status == 'RESERVED' or c.status == 'CONFIRMED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline"
                                          onsubmit="return confirm('Ban chac chan muon huy hop dong nay?')">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="cancel">
                                        <button class="btn btn-sm btn-outline-danger action-icon-btn"
                                                data-bs-toggle="tooltip" data-bs-title="Huy hop dong"
                                                aria-label="Huy hop dong"><i class="bi bi-trash"></i></button>
                                    </form>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
