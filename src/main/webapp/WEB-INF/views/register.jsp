<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Dang ky"/></jsp:include>

<div class="auth-wrapper">
    <div class="auth-card" style="max-width:520px">
        <div class="text-center mb-4">
            <i class="bi bi-person-plus-fill" style="font-size:3rem;color:var(--primary)"></i>
            <h2 class="mt-2">Dang ky tai khoan</h2>
            <p class="text-muted">Tao tai khoan de bat dau thue xe</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-custom-error"><i class="bi bi-exclamation-circle me-1"></i>${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/register">
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">Ten dang nhap *</label>
                    <input type="text" name="username" class="form-control" required>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Email *</label>
                    <input type="email" name="email" class="form-control" required>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Mat khau *</label>
                <input type="password" name="password" class="form-control" required minlength="4">
            </div>
            <div class="mb-3">
                <label class="form-label">Ho va ten *</label>
                <input type="text" name="fullName" class="form-control" required>
            </div>
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">So dien thoai</label>
                    <input type="text" name="phone" class="form-control">
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Dia chi</label>
                    <input type="text" name="address" class="form-control">
                </div>
            </div>
            <button type="submit" class="btn btn-accent w-100 mt-2">
                <i class="bi bi-person-plus me-1"></i>Dang ky
            </button>
        </form>
        <div class="text-center mt-3">
            <span class="text-muted">Da co tai khoan?</span>
            <a href="${pageContext.request.contextPath}/login" class="text-accent">Dang nhap</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
