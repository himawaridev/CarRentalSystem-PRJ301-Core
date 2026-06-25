<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Dang nhap"/></jsp:include>

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="text-center mb-4">
            <i class="bi bi-car-front-fill" style="font-size:3rem;color:var(--primary)"></i>
            <h2 class="mt-2">Dang nhap</h2>
            <p class="text-muted">Chao mung tro lai CarRental</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-custom-error"><i class="bi bi-exclamation-circle me-1"></i>${error}</div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-custom-success"><i class="bi bi-check-circle me-1"></i>${success}</div>
        </c:if>
        <c:if test="${param.error == 'auth'}">
            <div class="alert alert-custom-error"><i class="bi bi-lock me-1"></i>Vui long dang nhap de tiep tuc!</div>
        </c:if>
        <c:if test="${param.success == 'registered'}">
            <div class="alert alert-custom-success"><i class="bi bi-check-circle me-1"></i>Dang ky thanh cong! Hay dang nhap.</div>
        </c:if>
        <c:if test="${param.success == 'verified'}">
            <div class="alert alert-custom-success"><i class="bi bi-check-circle me-1"></i>Email da duoc xac minh. Hay dang nhap.</div>
        </c:if>
        <c:if test="${param.success == 'reset'}">
            <div class="alert alert-custom-success"><i class="bi bi-check-circle me-1"></i>Mat khau da duoc cap nhat. Hay dang nhap lai.</div>
        </c:if>

        <div class="oauth-grid mb-3">
            <a class="btn btn-oauth btn-oauth-google" href="${pageContext.request.contextPath}/oauth/google?entry=login">
                <span class="oauth-icon"><i class="bi bi-google"></i></span>
                <span class="oauth-label">Tiep tuc voi Google</span>
            </a>
            <a class="btn btn-oauth btn-oauth-facebook" href="${pageContext.request.contextPath}/oauth/facebook?entry=login">
                <span class="oauth-icon"><i class="bi bi-facebook"></i></span>
                <span class="oauth-label">Tiep tuc voi Facebook</span>
            </a>
        </div>
        <div class="auth-divider"><span>hoac</span></div>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="mb-3">
                <label class="form-label">Ten dang nhap</label>
                <input type="text" name="username" class="form-control" placeholder="Nhap username..." required autofocus>
            </div>
            <div class="mb-3">
                <label class="form-label">Mat khau</label>
                <input type="password" name="password" class="form-control" placeholder="Nhap password..." required>
                <div class="text-end mt-1">
                    <a href="${pageContext.request.contextPath}/forgot-password" class="small text-accent">Quen mat khau?</a>
                </div>
            </div>
            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="${param.redirect}">
            </c:if>
            <button type="submit" class="btn btn-accent w-100 mt-2">
                <i class="bi bi-box-arrow-in-right me-1"></i>Dang nhap
            </button>
        </form>
        <div class="text-center mt-3">
            <span class="text-muted">Chua co tai khoan?</span>
            <a href="${pageContext.request.contextPath}/register" class="text-accent">Dang ky ngay</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
