<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Ho tro"/></jsp:include>

<div class="container py-5">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <h2 class="page-heading mb-0"><i class="bi bi-life-preserver"></i>Ho tro khach hang</h2>
        <a href="${pageContext.request.contextPath}/my-contracts" class="btn btn-outline-accent btn-sm btn-action-nowrap">
            <i class="bi bi-file-text me-1"></i>Hop dong cua toi
        </a>
    </div>

    <c:if test="${not empty success}"><div class="alert alert-custom-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-custom-error">${error}</div></c:if>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="card-custom">
                <div class="card-header"><i class="bi bi-send me-2"></i>Tao yeu cau ho tro</div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/support">
                        <div class="mb-3">
                            <label class="form-label">Loai van de</label>
                            <select name="category" class="form-select" required>
                                <option value="BANK_INFO">Sai tai khoan ngan hang</option>
                                <option value="PAYMENT">Thanh toan</option>
                                <option value="REFUND">Hoan coc</option>
                                <option value="CONTRACT">Hop dong</option>
                                <option value="ACCOUNT">Tai khoan</option>
                                <option value="OTHER">Khac</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Hop dong lien quan</label>
                            <select name="contractId" class="form-select">
                                <option value="">Khong gan hop dong</option>
                                <c:forEach var="c" items="${contracts}">
                                    <option value="${c.contractId}" ${selectedContractId == c.contractId ? 'selected' : ''}>
                                        ${c.contractCode} - ${c.status}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Tieu de</label>
                            <input type="text" name="subject" class="form-control" maxlength="150" required
                                   placeholder="VD: Can doi tai khoan nhan hoan coc">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Noi dung</label>
                            <textarea name="message" class="form-control" rows="6" maxlength="1000" required
                                      placeholder="Mo ta ngan gon van de can ho tro"></textarea>
                        </div>
                        <button type="submit" class="btn btn-accent btn-action-nowrap w-100">
                            <i class="bi bi-send-check me-1"></i>Gui yeu cau
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="card-custom">
                <div class="card-header"><i class="bi bi-list-check me-2"></i>Yeu cau da gui</div>
                <div class="card-body">
                    <c:if test="${empty tickets}">
                        <div class="text-center py-4">
                            <i class="bi bi-inbox" style="font-size:3rem;color:var(--gray-300)"></i>
                            <h5 class="text-muted mt-3">Chua co yeu cau ho tro</h5>
                        </div>
                    </c:if>

                    <c:forEach var="t" items="${tickets}">
                        <div class="support-ticket-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="fw-bold text-accent">${t.ticketCode}</div>
                                    <h6 class="mb-1">${t.subject}</h6>
                                    <div class="text-muted small">
                                        ${t.categoryLabel}
                                        <c:if test="${not empty t.contractCode}"> · ${t.contractCode}</c:if>
                                    </div>
                                </div>
                                <span class="badge-status
                                    ${t.status == 'OPEN' ? 'badge-pending' : ''}
                                    ${t.status == 'IN_PROGRESS' ? 'badge-deposit' : ''}
                                    ${t.status == 'RESOLVED' ? 'badge-completed' : ''}
                                    ${t.status == 'REJECTED' ? 'badge-rejected' : ''}">
                                    ${t.statusLabel}
                                </span>
                            </div>
                            <p class="small mb-2 mt-3">${t.message}</p>
                            <c:if test="${not empty t.staffResponse}">
                                <div class="support-response">
                                    <div class="fw-bold small mb-1">Phan hoi cua nhan vien</div>
                                    <div class="small">${t.staffResponse}</div>
                                </div>
                            </c:if>
                            <div class="text-muted small mt-2">Tao luc: ${t.createdAt}</div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
