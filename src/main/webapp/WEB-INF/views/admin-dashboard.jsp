<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Admin - Quan tri"/></jsp:include>

<div class="container py-5">
    <h2 class="page-heading mb-4"><i class="bi bi-shield-lock"></i>Quan tri he thong</h2>

    <c:if test="${not empty success}"><div class="alert alert-custom-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-custom-error">${error}</div></c:if>

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0">Danh sach tai khoan (${users.size()})</h5>
        <button class="btn btn-accent btn-sm" data-bs-toggle="modal" data-bs-target="#createUserModal">
            <i class="bi bi-person-plus me-1"></i>Tao tai khoan
        </button>
    </div>

    <div class="table-responsive">
        <table class="table table-custom">
            <thead>
                <tr>
                    <th>ID</th><th>Username</th><th>Ho ten</th><th>Email</th>
                    <th>Phone</th><th>Ngan hang</th><th>Roles</th><th>Trang thai</th><th>Hanh dong</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="u" items="${users}">
                <tr>
                    <td>${u.userId}</td>
                    <td class="fw-bold">${u.username}</td>
                    <td>${u.fullName}</td>
                    <td class="text-muted small">${u.email}</td>
                    <td>${u.phone}</td>
                    <td class="small">
                        <c:choose>
                            <c:when test="${u.refundBankInfo}">
                                <strong>${u.bankName}</strong><br>
                                <span class="text-muted">${u.bankAccountNumber}</span>
                            </c:when>
                            <c:otherwise><span class="text-muted">Chua co</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:forEach var="r" items="${userRolesMap[u.userId]}">
                            <span class="car-tag">${r}</span>
                        </c:forEach>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${u.status == 'ACTIVE'}"><span class="badge-status badge-active">ACTIVE</span></c:when>
                            <c:when test="${u.status == 'LOCKED'}"><span class="badge-status badge-locked">LOCKED</span></c:when>
                            <c:otherwise><span class="badge-status badge-cancelled">${u.status}</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <div class="d-flex gap-1">
                            <button class="btn btn-sm btn-outline-accent" data-bs-toggle="modal"
                                    data-bs-target="#editUserModal-${u.userId}" title="Sua"><i class="bi bi-pencil"></i></button>
                            <button class="btn btn-sm btn-outline-accent" data-bs-toggle="modal"
                                    data-bs-target="#roleModal-${u.userId}" title="Quyen"><i class="bi bi-key"></i></button>
                        </div>
                    </td>
                </tr>

                <!-- Edit User Modal -->
                <div class="modal fade" id="editUserModal-${u.userId}" tabindex="-1">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <form method="post" action="${pageContext.request.contextPath}/admin/dashboard">
                                <input type="hidden" name="action" value="updateUserDetails">
                                <input type="hidden" name="userId" value="${u.userId}">
                                <div class="modal-header"><h5 class="modal-title">Sua tai khoan: ${u.username}</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                <div class="modal-body">
                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <label class="form-label">Username</label>
                                            <input type="text" class="form-control" value="${u.username}" readonly>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label">Trang thai</label>
                                            <select name="status" class="form-select">
                                                <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                                <option value="LOCKED" ${u.status == 'LOCKED' ? 'selected' : ''}>LOCKED</option>
                                                <option value="DISABLED" ${u.status == 'DISABLED' ? 'selected' : ''}>DISABLED</option>
                                            </select>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label">Ho ten</label>
                                            <input type="text" name="fullName" class="form-control" value="${u.fullName}" required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label">Email</label>
                                            <input type="email" name="email" class="form-control" value="${u.email}" required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label">Phone</label>
                                            <input type="text" name="phone" class="form-control" value="${u.phone}">
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label">CMND/CCCD</label>
                                            <input type="text" name="identityNumber" class="form-control" value="${u.identityNumber}">
                                        </div>
                                        <div class="col-12">
                                            <label class="form-label">Dia chi</label>
                                            <input type="text" name="address" class="form-control" value="${u.address}">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">Ngan hang nhan hoan tien</label>
                                            <select name="bankCode" class="form-select">
                                                <option value="">Chua cau hinh</option>
                                                <c:forEach var="bank" items="${bankOptions}">
                                                    <option value="${bank.key}" ${u.bankCode == bank.key ? 'selected' : ''}>
                                                        ${bank.value}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">So tai khoan</label>
                                            <input type="text" name="bankAccountNumber" class="form-control"
                                                   value="${u.bankAccountNumber}" maxlength="20">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">Chu tai khoan</label>
                                            <input type="text" name="bankAccountHolder" class="form-control"
                                                   value="${u.bankAccountHolder}">
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

                <!-- Edit Roles Modal -->
                <div class="modal fade" id="roleModal-${u.userId}" tabindex="-1">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <form method="post" action="${pageContext.request.contextPath}/admin/dashboard">
                                <input type="hidden" name="action" value="updateRoles">
                                <input type="hidden" name="userId" value="${u.userId}">
                                <div class="modal-header"><h5 class="modal-title">Quyen: ${u.username}</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                <div class="modal-body">
                                    <c:forEach var="role" items="${allRoles}">
                                        <div class="form-check mb-2">
                                            <input class="form-check-input" type="checkbox" name="roles" value="${role}"
                                                   id="role-${u.userId}-${role}"
                                                   ${userRolesMap[u.userId].contains(role) ? 'checked' : ''}>
                                            <label class="form-check-label" for="role-${u.userId}-${role}">${role}</label>
                                        </div>
                                    </c:forEach>
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

    <!-- Create User Modal -->
    <div class="modal fade" id="createUserModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <form method="post" action="${pageContext.request.contextPath}/admin/dashboard">
                    <input type="hidden" name="action" value="createUser">
                    <div class="modal-header"><h5 class="modal-title">Tao tai khoan moi</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-md-6"><label class="form-label">Username</label><input type="text" name="username" class="form-control" required></div>
                            <div class="col-md-6"><label class="form-label">Email</label><input type="email" name="email" class="form-control" required></div>
                            <div class="col-md-6"><label class="form-label">Mat khau</label><input type="password" name="password" class="form-control" required></div>
                            <div class="col-md-6"><label class="form-label">Ho ten</label><input type="text" name="fullName" class="form-control" required></div>
                            <div class="col-md-6"><label class="form-label">Phone</label><input type="text" name="phone" class="form-control"></div>
                            <div class="col-12">
                                <label class="form-label">Quyen</label>
                                <div class="d-flex flex-wrap gap-3">
                                    <c:forEach var="role" items="${allRoles}">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" name="roles" value="${role}"
                                                   id="newRole-${role}" ${role == 'CUSTOMER' ? 'checked' : ''}>
                                            <label class="form-check-label" for="newRole-${role}">${role}</label>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-accent" data-bs-dismiss="modal">Huy</button>
                        <button type="submit" class="btn btn-accent">Tao</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
