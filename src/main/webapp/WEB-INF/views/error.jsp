<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Loi"/></jsp:include>

<div class="container py-5 text-center">
    <div style="font-size:6rem;color:var(--primary);opacity:0.4">
        <i class="bi bi-exclamation-triangle"></i>
    </div>
    <h2 class="page-heading mt-3">Oops! Co loi xay ra</h2>
    <p class="text-muted">Trang ban tim khong ton tai hoac da xay ra loi he thong.</p>
    <a href="${pageContext.request.contextPath}/search" class="btn btn-accent mt-3">
        <i class="bi bi-house me-1"></i>Ve trang chu
    </a>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
