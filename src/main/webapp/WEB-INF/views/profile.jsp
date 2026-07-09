<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Ho so ca nhan"/></jsp:include>

<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="page-heading mb-0"><i class="bi bi-person-vcard"></i>Ho so ca nhan</h2>
        <a href="${pageContext.request.contextPath}/search" class="btn btn-outline-accent btn-sm btn-action-nowrap">
            <i class="bi bi-arrow-left me-1"></i>Tim xe
        </a>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-custom-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/profile">
        <c:if test="${not empty redirect}">
            <input type="hidden" name="redirect" value="${redirect}">
        </c:if>

        <div class="row g-4 justify-content-center">
            <div class="col-lg-7">
                <div class="card-custom h-100">
                    <div class="card-header"><i class="bi bi-person-lines-fill me-2"></i>Thong tin lien he</div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label class="form-label">Ho ten</label>
                            <input type="text" name="fullName" class="form-control"
                                   value="${profileUser.fullName}" maxlength="100" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">So dien thoai</label>
                            <input type="text" name="phone" class="form-control"
                                   value="${profileUser.phone}" maxlength="20">
                        </div>
                        <div class="mb-0">
                            <label class="form-label">Dia chi</label>
                            <input type="text" name="address" class="form-control"
                                   value="${profileUser.address}" maxlength="255">
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="btn-action-group justify-content-end mt-4">
            <button type="submit" class="btn btn-accent btn-action-nowrap px-4">
                <i class="bi bi-save me-1"></i>Luu ho so
            </button>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
