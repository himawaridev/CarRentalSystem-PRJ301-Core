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
        <div class="d-flex flex-wrap gap-2">
            <a href="${pageContext.request.contextPath}/staff/dashboard"
               class="btn ${empty statusFilter ? 'btn-accent' : 'btn-outline-accent'} btn-sm">Tat ca</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=PENDING_REVIEW"
               class="btn ${statusFilter == 'PENDING_REVIEW' ? 'btn-accent' : 'btn-outline-accent'} btn-sm">Cho duyet</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=ACCEPTED"
               class="btn ${statusFilter == 'ACCEPTED' ? 'btn-accent' : 'btn-outline-accent'} btn-sm">Da duyet</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=DEPOSIT_PAID"
               class="btn ${statusFilter == 'DEPOSIT_PAID' ? 'btn-accent' : 'btn-outline-accent'} btn-sm">Da dat coc</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=CAR_PICKED_UP"
               class="btn ${statusFilter == 'CAR_PICKED_UP' ? 'btn-accent' : 'btn-outline-accent'} btn-sm">Da nhan xe</a>
            <a href="${pageContext.request.contextPath}/staff/dashboard?status=CAR_RETURNED"
               class="btn ${statusFilter == 'CAR_RETURNED' ? 'btn-accent' : 'btn-outline-accent'} btn-sm">Da tra xe</a>
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
                    <th>Trang thai</th>
                    <th>Hanh dong</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="c" items="${contracts}">
                    <tr>
                        <td>
                            <a href="${pageContext.request.contextPath}/staff/contract-detail?id=${c.contractId}"
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
                        <td>
                            <c:choose>
                                <c:when test="${c.status == 'PENDING_REVIEW'}"><span class="badge-status badge-pending">Cho duyet</span></c:when>
                                <c:when test="${c.status == 'ACCEPTED'}"><span class="badge-status badge-accepted">Da duyet</span></c:when>
                                <c:when test="${c.status == 'REJECTED'}"><span class="badge-status badge-rejected">Tu choi</span></c:when>
                                <c:when test="${c.status == 'DEPOSIT_PAID'}"><span class="badge-status badge-deposit">Da dat coc</span></c:when>
                                <c:when test="${c.status == 'CAR_PICKED_UP'}"><span class="badge-status badge-picked">Da nhan xe</span></c:when>
                                <c:when test="${c.status == 'CAR_RETURNED'}"><span class="badge-status badge-returned">Da tra xe</span></c:when>
                                <c:when test="${c.status == 'FINAL_PAYMENT_COMPLETED'}"><span class="badge-status badge-completed">Hoan tat</span></c:when>
                                <c:when test="${c.status == 'CANCELLED'}"><span class="badge-status badge-cancelled">Da huy</span></c:when>
                                <c:otherwise><span class="badge bg-secondary">${c.status}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <div class="d-flex gap-1 flex-wrap">
                                <c:if test="${c.status == 'PENDING_REVIEW'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="accept">
                                        <button class="btn btn-sm btn-outline-accent" title="Duyet"><i class="bi bi-check-lg"></i></button>
                                    </form>
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="reject">
                                        <button class="btn btn-sm btn-outline-danger" title="Tu choi"><i class="bi bi-x-lg"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'ACCEPTED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="deposit_paid">
                                        <button class="btn btn-sm btn-outline-accent" title="Xac nhan dat coc"><i class="bi bi-cash-coin"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'DEPOSIT_PAID'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="car_picked_up">
                                        <button class="btn btn-sm btn-outline-accent" title="Giao xe"><i class="bi bi-truck"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'CAR_PICKED_UP'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="car_returned">
                                        <button class="btn btn-sm btn-outline-accent" title="Nhan xe tra"><i class="bi bi-box-arrow-in-down"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'CAR_RETURNED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="final_payment">
                                        <button class="btn btn-sm btn-outline-accent" title="Thanh toan"><i class="bi bi-credit-card"></i></button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status != 'REJECTED' and c.status != 'CANCELLED' and c.status != 'FINAL_PAYMENT_COMPLETED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/process" style="display:inline"
                                          onsubmit="return confirm('Ban chac chan muon huy hop dong nay?')">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <input type="hidden" name="action" value="cancel">
                                        <button class="btn btn-sm btn-outline-danger" title="Huy"><i class="bi bi-trash"></i></button>
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
