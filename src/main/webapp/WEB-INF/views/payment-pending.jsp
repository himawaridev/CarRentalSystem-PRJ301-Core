<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Thanh toan hop dong"/></jsp:include>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="card-custom">
                <div class="card-header"><i class="bi bi-credit-card me-2"></i>Thanh toan hop dong</div>
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                        <div>
                            <div class="text-muted small">Ma hop dong</div>
                            <h4 class="text-accent fw-bold mb-0">${contract.contractCode}</h4>
                        </div>
                        <span class="badge-status ${paymentTransaction.status == 'PAID' ? 'badge-accepted' : 'badge-pending'}">
                            ${paymentTransaction.status}
                        </span>
                    </div>

                    <c:choose>
                        <c:when test="${paymentTransaction.status == 'PAID'}">
                            <div class="alert alert-custom-success mb-4">
                                <i class="bi bi-check-circle-fill me-1"></i>
                                Thanh toan thanh cong. Hop dong da duoc giu xe.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-info mb-4">
                                <i class="bi bi-info-circle me-1"></i>
                                Day la luong thanh toan noi bo dung cho demo PRJ301. Bam thanh toan de xac nhan dat coc/tra truoc va giu xe.
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="payment-line">
                        <span>So tien can thanh toan</span>
                        <strong><fmt:formatNumber value="${paymentTransaction.amount}" pattern="#,###"/> VND</strong>
                    </div>
                    <div class="payment-line">
                        <span>Ma giao dich</span>
                        <strong>${paymentTransaction.providerTransactionRef}</strong>
                    </div>
                    <div class="payment-line">
                        <span>Het han luc</span>
                        <strong>${paymentTransaction.expiredAt}</strong>
                    </div>

                    <div class="d-flex justify-content-end flex-wrap gap-2 mt-4">
                        <a href="${paymentReturnUrl}" class="btn btn-outline-accent text-nowrap">
                            <i class="bi bi-arrow-left me-1"></i>Ve hop dong
                        </a>
                        <c:if test="${paymentTransaction.status != 'PAID'}">
                            <form method="post" action="${pageContext.request.contextPath}/payment/pending" class="m-0">
                                <input type="hidden" name="ref" value="${paymentTransaction.providerTransactionRef}">
                                <button type="submit" class="btn btn-accent text-nowrap">
                                    <i class="bi bi-check-circle me-1"></i>Thanh toan
                                </button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
