<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Nhan vien - Ho tro"/></jsp:include>

<div class="container py-5">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <h2 class="page-heading mb-0"><i class="bi bi-headset"></i>Yeu cau ho tro</h2>
        <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline-accent btn-sm btn-action-nowrap">
            <i class="bi bi-clipboard-check me-1"></i>Hop dong
        </a>
    </div>

    <c:if test="${not empty success}"><div class="alert alert-custom-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-custom-error">${error}</div></c:if>

    <div class="mb-4">
        <div class="btn-action-group">
            <a href="${pageContext.request.contextPath}/staff/support"
               class="btn ${empty statusFilter ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Tat ca</a>
            <a href="${pageContext.request.contextPath}/staff/support?status=OPEN"
               class="btn ${statusFilter == 'OPEN' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Moi</a>
            <a href="${pageContext.request.contextPath}/staff/support?status=IN_PROGRESS"
               class="btn ${statusFilter == 'IN_PROGRESS' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Dang xu ly</a>
            <a href="${pageContext.request.contextPath}/staff/support?status=RESOLVED"
               class="btn ${statusFilter == 'RESOLVED' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Da xu ly</a>
            <a href="${pageContext.request.contextPath}/staff/support?status=REJECTED"
               class="btn ${statusFilter == 'REJECTED' ? 'btn-accent' : 'btn-outline-accent'} btn-action-nowrap btn-sm">Tu choi</a>
        </div>
    </div>

    <c:if test="${empty tickets}">
        <div class="text-center py-5">
            <i class="bi bi-inbox" style="font-size:4rem;color:var(--gray-300)"></i>
            <h4 class="text-muted mt-3">Khong co yeu cau ho tro</h4>
        </div>
    </c:if>

    <c:if test="${not empty tickets}">
        <div class="table-responsive">
            <table class="table table-custom">
                <thead>
                    <tr>
                        <th>Ma ticket</th>
                        <th>Khach hang</th>
                        <th>Van de</th>
                        <th>Hop dong</th>
                        <th class="table-status-col">Trang thai</th>
                        <th>Uu tien</th>
                        <th class="table-actions-col">Xu ly</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="t" items="${tickets}">
                        <tr>
                            <td>
                                <div class="fw-bold text-accent">${t.ticketCode}</div>
                                <div class="text-muted small">${t.createdAt}</div>
                            </td>
                            <td>
                                <div>${t.customerName}</div>
                                <div class="text-muted small">${t.customerPhone}</div>
                            </td>
                            <td>
                                <div class="fw-bold">${t.categoryLabel}</div>
                                <div>${t.subject}</div>
                                <div class="text-muted small">${t.message}</div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty t.contractCode}">
                                        <span class="text-accent fw-bold">${t.contractCode}</span>
                                    </c:when>
                                    <c:otherwise><span class="text-muted">Khong gan</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="table-status-cell">
                                <span class="badge-status
                                    ${t.status == 'OPEN' ? 'badge-pending' : ''}
                                    ${t.status == 'IN_PROGRESS' ? 'badge-deposit' : ''}
                                    ${t.status == 'RESOLVED' ? 'badge-completed' : ''}
                                    ${t.status == 'REJECTED' ? 'badge-rejected' : ''}">
                                    ${t.statusLabel}
                                </span>
                            </td>
                            <td>${t.priorityLabel}</td>
                            <td class="table-actions-cell">
                                <button class="btn btn-sm btn-outline-accent action-icon-btn"
                                        data-bs-toggle="modal"
                                        data-bs-target="#supportModal-${t.ticketId}"
                                        aria-label="Xu ly ticket">
                                    <i class="bi bi-pencil-square"></i>
                                </button>
                            </td>
                        </tr>

                        <div class="modal fade" id="supportModal-${t.ticketId}" tabindex="-1">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <form method="post" action="${pageContext.request.contextPath}/staff/support">
                                        <input type="hidden" name="ticketId" value="${t.ticketId}">
                                        <div class="modal-header">
                                            <h5 class="modal-title">Xu ly ${t.ticketCode}</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="row g-3">
                                                <div class="col-md-6">
                                                    <label class="form-label">Trang thai</label>
                                                    <select name="status" class="form-select">
                                                        <option value="OPEN" ${t.status == 'OPEN' ? 'selected' : ''}>Moi</option>
                                                        <option value="IN_PROGRESS" ${t.status == 'IN_PROGRESS' ? 'selected' : ''}>Dang xu ly</option>
                                                        <option value="RESOLVED" ${t.status == 'RESOLVED' ? 'selected' : ''}>Da xu ly</option>
                                                        <option value="REJECTED" ${t.status == 'REJECTED' ? 'selected' : ''}>Tu choi</option>
                                                    </select>
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label">Uu tien</label>
                                                    <select name="priority" class="form-select">
                                                        <option value="LOW" ${t.priority == 'LOW' ? 'selected' : ''}>Thap</option>
                                                        <option value="NORMAL" ${t.priority == 'NORMAL' ? 'selected' : ''}>Binh thuong</option>
                                                        <option value="HIGH" ${t.priority == 'HIGH' ? 'selected' : ''}>Cao</option>
                                                    </select>
                                                </div>
                                                <div class="col-12">
                                                    <label class="form-label">Phan hoi cho khach</label>
                                                    <textarea name="staffResponse" class="form-control" rows="5" maxlength="1000">${t.staffResponse}</textarea>
                                                </div>
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
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
