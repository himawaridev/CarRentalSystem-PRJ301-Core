<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Quen mat khau"/></jsp:include>

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="text-center mb-4">
            <i class="bi bi-key-fill" style="font-size:3rem;color:var(--primary)"></i>
            <h2 class="mt-2">Quen mat khau</h2>
            <p class="text-muted">Nhap email tai khoan de nhan ma dat lai mat khau</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-custom-error"><i class="bi bi-exclamation-circle me-1"></i>${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/forgot-password">
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control" required autofocus>
            </div>
            <button type="submit" class="btn btn-accent w-100">
                <i class="bi bi-send me-1"></i>Gui ma dat lai mat khau
            </button>
        </form>
        <div class="text-center mt-3">
            <a href="${pageContext.request.contextPath}/login" class="text-accent">Quay lai dang nhap</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
