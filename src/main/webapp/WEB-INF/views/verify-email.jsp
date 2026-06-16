<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Xac minh email"/></jsp:include>

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="text-center mb-4">
            <i class="bi bi-envelope-check-fill" style="font-size:3rem;color:var(--primary)"></i>
            <h2 class="mt-2">Xac minh email</h2>
            <p class="text-muted">Nhap ma 6 chu so da gui toi email cua ban</p>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-custom-success"><i class="bi bi-check-circle me-1"></i>${success}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-custom-error"><i class="bi bi-exclamation-circle me-1"></i>${error}</div>
        </c:if>
        <c:if test="${not empty devCode}">
            <div class="alert alert-custom-info">
                <i class="bi bi-tools me-1"></i>Dev mode: ma xac minh la <strong>${devCode}</strong>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/verify-email">
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control"
                       value="${not empty email ? email : param.email}" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Ma xac minh</label>
                <input type="text" name="code" class="form-control verification-code-input"
                       inputmode="numeric" maxlength="6" placeholder="123456" required autofocus>
            </div>
            <button type="submit" class="btn btn-accent w-100">
                <i class="bi bi-check2-circle me-1"></i>Xac minh va tao tai khoan
            </button>
        </form>

        <form method="post" action="${pageContext.request.contextPath}/verify-email" class="mt-3">
            <input type="hidden" name="action" value="resend">
            <input type="hidden" name="email" value="${not empty email ? email : param.email}">
            <button type="submit" class="btn btn-outline-accent w-100">
                <i class="bi bi-arrow-clockwise me-1"></i>Gui lai ma
            </button>
        </form>

        <div class="text-center mt-3">
            <a href="${pageContext.request.contextPath}/register" class="text-accent">Doi email dang ky</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
