<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Dat lai mat khau"/></jsp:include>

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="text-center mb-4">
            <i class="bi bi-shield-lock-fill" style="font-size:3rem;color:var(--primary)"></i>
            <h2 class="mt-2">Dat lai mat khau</h2>
            <p class="text-muted">Nhap ma trong email va mat khau moi</p>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-custom-success"><i class="bi bi-check-circle me-1"></i>${success}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-custom-error"><i class="bi bi-exclamation-circle me-1"></i>${error}</div>
        </c:if>
        <c:if test="${not empty devCode}">
            <div class="alert alert-custom-info">
                <i class="bi bi-tools me-1"></i>Dev mode: ma reset la <strong>${devCode}</strong>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/reset-password">
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control"
                       value="${not empty email ? email : param.email}" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Ma dat lai mat khau</label>
                <input type="text" name="code" class="form-control verification-code-input"
                       inputmode="numeric" maxlength="6" placeholder="123456" required autofocus>
            </div>
            <div class="mb-3">
                <label class="form-label">Mat khau moi</label>
                <input type="password" name="password" class="form-control" required minlength="8">
            </div>
            <div class="mb-3">
                <label class="form-label">Nhap lai mat khau moi</label>
                <input type="password" name="confirmPassword" class="form-control" required minlength="8">
            </div>
            <button type="submit" class="btn btn-accent w-100">
                <i class="bi bi-check2-circle me-1"></i>Cap nhat mat khau
            </button>
        </form>

        <div class="text-center mt-3">
            <a href="${pageContext.request.contextPath}/forgot-password" class="text-accent">Gui lai ma</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
