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

    <c:if test="${requiredBank}">
        <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle me-1"></i>
            Vui long cap nhat thong tin ngan hang truoc khi dat xe. He thong se dung thong tin nay de hoan coc neu can.
        </div>
    </c:if>
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

        <div class="row g-4">
            <div class="col-lg-5">
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

            <div class="col-lg-7">
                <div class="card-custom h-100">
                    <div class="card-header"><i class="bi bi-bank me-2"></i>Tai khoan nhan hoan coc</div>
                    <div class="card-body">
                        <div class="alert alert-warning small">
                            <i class="bi bi-shield-exclamation me-1"></i>
                            Hay kiem tra that ky ngan hang, so tai khoan va ten chu tai khoan.
                            Sau khi luu, thong tin tai khoan se bi khoa. Neu can doi, vui long lien he admin de xac minh.
                        </div>

                        <c:set var="bankLocked" value="${profileUser.bankInfoLocked}" />
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Ngan hang</label>
                                <select name="bankCode" class="form-select" ${bankLocked ? 'disabled' : 'required'}>
                                    <option value="">Chon ngan hang</option>
                                    <c:forEach var="bank" items="${bankOptions}">
                                        <option value="${bank.key}" ${profileUser.bankCode == bank.key ? 'selected' : ''}>
                                            ${bank.value} (${bank.key})
                                        </option>
                                    </c:forEach>
                                </select>
                                <c:if test="${bankLocked}">
                                    <div class="form-text">${profileUser.bankName} (${profileUser.bankCode})</div>
                                </c:if>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">So tai khoan</label>
                                <input type="text" name="bankAccountNumber" class="form-control"
                                       value="${profileUser.bankAccountNumber}" maxlength="20"
                                       ${bankLocked ? 'readonly' : 'required'}
                                       placeholder="Chi nhap chu so">
                            </div>
                            <div class="col-md-12">
                                <label class="form-label">Ten chu tai khoan</label>
                                <input type="text" name="bankAccountHolder" class="form-control text-uppercase"
                                       value="${profileUser.bankAccountHolder}" maxlength="120"
                                       ${bankLocked ? 'readonly' : 'required'}
                                       placeholder="VD: NGUYEN VAN A">
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${profileUser.refundBankInfo}">
                                <div class="profile-bank-locked mt-3">
                                    <i class="bi bi-lock-fill"></i>
                                    Tai khoan hoan coc da duoc khoa de tranh hoan nham nguoi nhan.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="profile-bank-open mt-3">
                                    <i class="bi bi-info-circle"></i>
                                    Ban chi co mot lan tu cap nhat thong tin ngan hang. Vui long doi chieu voi app ngan hang truoc khi luu.
                                </div>
                            </c:otherwise>
                        </c:choose>
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
