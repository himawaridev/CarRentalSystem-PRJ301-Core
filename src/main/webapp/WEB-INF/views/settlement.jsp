<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Quyet toan hop dong"/></jsp:include>

<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="page-heading mb-0"><i class="bi bi-cash-stack"></i>Quyet toan hop dong</h2>
        <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline-accent btn-sm">
            <i class="bi bi-arrow-left me-1"></i>Quay lai
        </a>
    </div>

    <div class="card-custom">
        <div class="card-header">${contract.contractCode}</div>
        <div class="card-body">
            <c:if test="${not empty success}">
                <div class="alert alert-success" role="alert">${success}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">${error}</div>
            </c:if>

            <div class="row g-3">
                <div class="col-md-4">
                    <div class="settlement-item">
                        <span>Coc da thu</span>
                        <strong><fmt:formatNumber value="${settlement.depositPaid}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="settlement-item">
                        <span>Tien thue da thu</span>
                        <strong><fmt:formatNumber value="${settlement.rentalPaid}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="settlement-item">
                        <span>Tien thue can thu</span>
                        <strong><fmt:formatNumber value="${settlement.expectedRental}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="settlement-item">
                        <span>Phi phat sinh chua thu</span>
                        <strong><fmt:formatNumber value="${settlement.extraCharge}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="settlement-item">
                        <span>Khau tru vao coc</span>
                        <strong><fmt:formatNumber value="${settlement.deductionAmount}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="settlement-item settlement-strong">
                        <span>Can thu them</span>
                        <strong><fmt:formatNumber value="${settlement.amountToCollect}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
                <div class="col-md-12">
                    <div class="settlement-item settlement-refund">
                        <span>So tien coc can hoan</span>
                        <strong><fmt:formatNumber value="${settlement.refundAmount}" pattern="#,###"/> VND</strong>
                    </div>
                </div>
            </div>

            <div class="divider"></div>
            <fmt:formatNumber var="amountToCollectText" value="${settlement.amountToCollect}" pattern="#,###"/>

            <c:choose>
                <c:when test="${pendingRefund != null}">
                    <div class="settlement-item settlement-refund mb-3">
                        <span>Yeu cau hoan coc dang cho xac nhan</span>
                        <strong><fmt:formatNumber value="${pendingRefund.refundAmount}" pattern="#,###"/> VND</strong>
                    </div>
                    <c:if test="${not empty pendingRefund.proofOfRefund}">
                        <div class="alert alert-warning" role="alert">${pendingRefund.proofOfRefund}</div>
                    </c:if>
                    <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                          class="row g-3 align-items-end">
                        <input type="hidden" name="contractId" value="${contract.contractId}">
                        <input type="hidden" name="refundId" value="${pendingRefund.refundId}">
                        <div class="col-md-4">
                            <label class="form-label">Phuong thuc hoan</label>
                            <select class="form-select" name="refundMethod">
                                <option value="GATEWAY_REFUND">Gateway refund</option>
                                <option value="CASH_AT_COUNTER">Tien mat tai quay</option>
                                <option value="MANUAL_BANK_TRANSFER">Chuyen khoan thu cong</option>
                                <option value="WALLET_CREDIT">Vi he thong</option>
                            </select>
                        </div>
                        <div class="col-md-5">
                            <label class="form-label">Bang chung/ma giao dich</label>
                            <input type="text" class="form-control" name="proofOfRefund"
                                   placeholder="Ma CK, bien nhan tien mat, hoac ghi chu vi"
                                   value="${pendingRefund.providerRefundRef}" maxlength="100">
                        </div>
                        <div class="col-md-3 text-end">
                            <button type="submit" name="action" value="completeRefund" class="btn btn-accent">
                                <i class="bi bi-check2-circle me-1"></i>Xac nhan da hoan coc
                            </button>
                        </div>
                    </form>
                </c:when>
                <c:when test="${settlement.amountToCollect > 0}">
                    <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                          class="row g-3 align-items-end">
                        <input type="hidden" name="contractId" value="${contract.contractId}">
                        <div class="col-md-8">
                            <label class="form-label">So tien thu them</label>
                            <input type="text" class="form-control"
                                   value="${amountToCollectText} VND"
                                   readonly>
                        </div>
                        <div class="col-md-4 text-end">
                            <button type="submit" name="action" value="collectBalance" class="btn btn-accent">
                                <i class="bi bi-cash-coin me-1"></i>Ghi nhan da thu them
                            </button>
                        </div>
                    </form>
                </c:when>
                <c:when test="${settlement.refundAmount > 0}">
                    <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                          class="row g-3 align-items-end">
                        <input type="hidden" name="contractId" value="${contract.contractId}">
                        <div class="col-md-4">
                            <label class="form-label">Phuong thuc hoan</label>
                            <select class="form-select" name="refundMethod">
                                <option value="GATEWAY_REFUND">Gateway refund</option>
                                <option value="CASH_AT_COUNTER">Tien mat tai quay</option>
                                <option value="MANUAL_BANK_TRANSFER">Chuyen khoan thu cong</option>
                                <option value="WALLET_CREDIT">Vi he thong</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Ly do hoan/khau tru</label>
                            <input type="text" class="form-control" name="reason"
                                   value="Quyet toan sau khi tra xe" maxlength="500">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Bang chung neu hoan thu cong</label>
                            <input type="text" class="form-control" name="proofOfRefund"
                                   placeholder="Ma CK, bien nhan tien mat, hoac ghi chu vi" maxlength="1000">
                        </div>
                        <div class="col-md-12 text-end">
                            <button type="submit" name="action" value="processCheckout" class="btn btn-accent">
                                <i class="bi bi-arrow-counterclockwise me-1"></i>Xu ly hoan coc
                            </button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                          class="text-end">
                        <input type="hidden" name="contractId" value="${contract.contractId}">
                        <button type="submit" name="action" value="complete" class="btn btn-accent">
                            <i class="bi bi-check-all me-1"></i>Hoan tat tat toan
                        </button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
