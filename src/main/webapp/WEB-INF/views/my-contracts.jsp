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
                        <th>Trang thai</th>
                        <th>Ngay tao</th>
                        <th></th>
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
                            <td class="text-muted small">${c.createdAt}</td>
                            <td>
                                <!-- Cancel button: only show for cancellable statuses -->
                                <c:if test="${c.status == 'PENDING_REVIEW' || c.status == 'ACCEPTED' || c.status == 'DEPOSIT_PAID'}">
                                    <form method="post" action="${pageContext.request.contextPath}/my-contracts"
                                          onsubmit="return confirm('Ban co chac chan muon huy hop dong ${c.contractCode}?\n\nHanh dong nay khong the hoan tac!')">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="contractId" value="${c.contractId}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                            <i class="bi bi-x-circle me-1"></i>Huy
                                        </button>
                                    </form>
                                </c:if>
                                <c:if test="${c.status == 'CAR_PICKED_UP'}">
                                    <span class="text-muted small"><i class="bi bi-lock me-1"></i>Dang thue</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="text-muted small mt-2">
            <i class="bi bi-info-circle me-1"></i>Ban co the huy hop dong khi chua nhan xe (Cho duyet / Da duyet / Da dat coc).
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
