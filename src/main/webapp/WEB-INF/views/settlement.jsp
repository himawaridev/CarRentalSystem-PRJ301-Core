<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Hoa don quyet toan"/></jsp:include>

<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="page-heading mb-1"><i class="bi bi-receipt-cutoff"></i>Hoa don quyet toan</h2>
            <div class="text-muted small">Kiem tra tien thue, phi tai xe va hoan tat hop dong</div>
        </div>
        <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline-accent btn-sm btn-action-nowrap">
            <i class="bi bi-arrow-left me-1"></i>Quan ly
        </a>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-custom-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <div class="settlement-invoice">
        <div class="settlement-invoice-header">
            <div>
                <div class="text-muted small text-uppercase fw-bold">Ma hop dong</div>
                <h3>${contract.contractCode}</h3>
                <div class="text-muted small">${contract.pickupAt} - ${contract.returnAt}</div>
            </div>
            <div class="text-end">
                <span class="badge-status ${contract.status == 'COMPLETED' ? 'badge-completed' : 'badge-pending'}">
                    ${contract.status}
                </span>
                <div class="text-muted small mt-2">Khach hang: <strong>${contract.customerName}</strong></div>
            </div>
        </div>

        <div class="settlement-panel">
            <div class="settlement-panel-title">
                <i class="bi bi-calculator"></i> Bang doi soat
            </div>
            <div class="settlement-lines">
                <div class="settlement-line-row">
                    <span>Coc da thu</span>
                    <strong><fmt:formatNumber value="${settlement.depositPaid}" pattern="#,###"/> VND</strong>
                </div>
                <div class="settlement-line-row">
                    <span>Tien thue/tai xe phai thu</span>
                    <strong><fmt:formatNumber value="${settlement.expectedRental}" pattern="#,###"/> VND</strong>
                </div>
                <div class="settlement-line-row">
                    <span>Tien thue/tai xe da thu</span>
                    <strong><fmt:formatNumber value="${settlement.rentalPaid}" pattern="#,###"/> VND</strong>
                </div>
            </div>

            <div class="settlement-total-grid">
                <div class="settlement-total-box">
                    <span>Can thu them</span>
                    <strong><fmt:formatNumber value="${settlement.amountToCollect}" pattern="#,###"/> VND</strong>
                </div>
                <div class="settlement-total-box">
                    <span>Trang thai hoa don</span>
                    <strong>${settlement.amountToCollect == 0 ? 'Da du tien' : 'Chua du tien'}</strong>
                </div>
            </div>
        </div>

        <div class="settlement-panel mt-4">
            <div class="settlement-panel-title">
                <i class="bi bi-credit-card-2-front"></i> Cac khoan thanh toan da ghi nhan
            </div>
            <c:choose>
                <c:when test="${not empty paymentRecords}">
                    <div class="table-responsive">
                        <table class="table table-custom mb-0">
                            <thead>
                                <tr>
                                    <th>Khoan tien</th>
                                    <th>So tien</th>
                                    <th>Phuong thuc</th>
                                    <th>Trang thai</th>
                                    <th>Ma GD</th>
                                    <th>Thoi gian</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${paymentRecords}">
                                    <tr>
                                        <td>${p.displayType}</td>
                                        <td><fmt:formatNumber value="${p.amount}" pattern="#,###"/> VND</td>
                                        <td>${p.displayMethod}</td>
                                        <td>
                                            <span class="badge-status ${p.paymentStatus == 'PAID' ? 'badge-completed' : p.paymentStatus == 'PENDING' ? 'badge-pending' : 'badge-cancelled'}">
                                                ${p.paymentStatus}
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty p.transactionRef}">${p.transactionRef}</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.paidAt != null}">${p.paidAt}</c:when>
                                                <c:otherwise>${p.createdAt}</c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-warning small mb-0">Chua co dong thanh toan nao duoc ghi nhan.</div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="divider"></div>
        <c:choose>
            <c:when test="${settlement.amountToCollect > 0}">
                <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                      class="settlement-action-panel">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <div class="alert alert-info mb-0">
                        Nhan vien thu them <strong><fmt:formatNumber value="${settlement.amountToCollect}" pattern="#,###"/> VND</strong>
                        bang tien mat, sau do bam nut de ghi nhan.
                    </div>
                    <div class="text-end">
                        <button type="submit" name="action" value="collectBalance" class="btn btn-accent btn-action-nowrap">
                            <i class="bi bi-cash-coin me-1"></i>Ghi nhan da thu tien
                        </button>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                      class="text-end">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <button type="submit" name="action" value="complete" class="btn btn-accent btn-action-nowrap">
                        <i class="bi bi-check-all me-1"></i>Hoan tat hoa don
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
