<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Hop dong cua toi"/></jsp:include>

<div class="container py-5">
    <h2 class="page-heading mb-4"><i class="bi bi-file-text"></i>Hop dong cua toi</h2>

    <c:if test="${not empty success}">
        <div class="alert alert-custom-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <c:if test="${empty contracts}">
        <div class="text-center py-5">
            <i class="bi bi-inbox" style="font-size:4rem;color:var(--gray-300)"></i>
            <h4 class="text-muted mt-3">Chua co hop dong nao</h4>
            <a href="${pageContext.request.contextPath}/search" class="btn btn-accent mt-2">
                <i class="bi bi-search me-1"></i>Tim xe ngay
            </a>
        </div>
    </c:if>

    <c:if test="${not empty contracts}">
        <div class="table-responsive">
            <table class="table table-custom">
                <thead>
                    <tr>
                        <th>Ma HD</th>
                        <th>Ngay nhan</th>
                        <th>Ngay tra</th>
                        <th>Dat coc</th>
                        <th>Tong tien</th>
                        <th class="table-status-col">Trang thai</th>
                        <th>Ngay tao</th>
                        <th class="table-actions-col">Hanh dong</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="c" items="${contracts}">
                        <tr>
                            <td><strong class="text-accent">${c.contractCode}</strong></td>
                            <td>${c.pickupAt}</td>
                            <td>${c.returnAt}</td>
                            <td><fmt:formatNumber value="${c.depositAmountDue}" pattern="#,###"/></td>
                            <td><fmt:formatNumber value="${c.finalAmountDue}" pattern="#,###"/></td>
                            <td class="table-status-cell">
                                <c:choose>
                                    <c:when test="${c.status == 'PENDING_PAYMENT'}"><span class="badge-status badge-pending">Cho thanh toan</span></c:when>
                                    <c:when test="${c.status == 'PAYMENT_EXPIRED'}"><span class="badge-status badge-rejected">Het han</span></c:when>
                                    <c:when test="${c.status == 'RESERVED'}"><span class="badge-status badge-deposit">Da giu xe</span></c:when>
                                    <c:when test="${c.status == 'CONFIRMED'}"><span class="badge-status badge-accepted">Da xac nhan</span></c:when>
                                    <c:when test="${c.status == 'CAR_PICKED_UP'}"><span class="badge-status badge-picked">Da nhan xe</span></c:when>
                                    <c:when test="${c.status == 'CAR_RETURNED'}"><span class="badge-status badge-returned">Da tra xe</span></c:when>
                                    <c:when test="${c.status == 'SETTLEMENT_PENDING'}"><span class="badge-status badge-pending">Dang quyet toan</span></c:when>
                                    <c:when test="${c.status == 'COMPLETED'}"><span class="badge-status badge-completed">Hoan tat</span></c:when>
                                    <c:when test="${c.status == 'CANCELLED'}"><span class="badge-status badge-cancelled">Da huy</span></c:when>
                                    <c:otherwise><span class="badge bg-secondary">${c.status}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-muted small">${c.createdAt}</td>
                            <td class="table-actions-cell">
                                <div class="table-action-icons">
                                    <a href="${pageContext.request.contextPath}/contract-detail?id=${c.contractId}"
                                       class="btn btn-sm btn-outline-accent action-icon-btn"
                                       data-bs-toggle="tooltip" data-bs-title="Chi tiet hop dong"
                                       aria-label="Chi tiet hop dong">
                                        <i class="bi bi-eye"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/support?contractId=${c.contractId}"
                                       class="btn btn-sm btn-outline-accent action-icon-btn"
                                       data-bs-toggle="tooltip" data-bs-title="Yeu cau ho tro"
                                       aria-label="Yeu cau ho tro">
                                        <i class="bi bi-life-preserver"></i>
                                    </a>
                                    <c:if test="${not empty pendingPaymentRefs[c.contractId]}">
                                        <a href="${pageContext.request.contextPath}/payment/pending?ref=${pendingPaymentRefs[c.contractId]}"
                                           class="btn btn-sm btn-accent action-icon-btn"
                                           data-bs-toggle="tooltip" data-bs-title="Thanh toan"
                                           aria-label="Thanh toan">
                                            <i class="bi bi-credit-card"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${pendingRefundContractIds.contains(c.contractId)}">
                                        <span class="status-chip status-chip-waiting"
                                              data-bs-toggle="tooltip" data-bs-title="Dang cho nhan vien hoan tien">
                                            <i class="bi bi-arrow-counterclockwise"></i>
                                        </span>
                                    </c:if>
                                    <!-- Cancel button: only show for cancellable statuses -->
                                    <c:if test="${cancellableContractIds.contains(c.contractId)}">
                                        <form method="post" action="${pageContext.request.contextPath}/my-contracts" class="d-inline"
                                              onsubmit="return confirm('Ban co chac chan muon huy hop dong ${c.contractCode}?\n\nHanh dong nay khong the hoan tac!')">
                                            <input type="hidden" name="action" value="cancel">
                                            <input type="hidden" name="contractId" value="${c.contractId}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger action-icon-btn"
                                                    data-bs-toggle="tooltip" data-bs-title="Huy hop dong"
                                                    aria-label="Huy hop dong">
                                                <i class="bi bi-x-circle"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${c.status == 'CAR_PICKED_UP'}">
                                        <span class="status-chip status-chip-muted"
                                              data-bs-toggle="tooltip" data-bs-title="Khach dang thue xe">
                                            <i class="bi bi-lock"></i>
                                        </span>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="text-muted small mt-2">
            <i class="bi bi-info-circle me-1"></i>Ban co the huy don khi chua thanh toan coc. Sau khi da giu xe, vui long lien he nhan vien de xu ly huy/hoan coc.
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
