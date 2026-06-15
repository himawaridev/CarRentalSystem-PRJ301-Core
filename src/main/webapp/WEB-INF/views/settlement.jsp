<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Hoa don quyet toan"/></jsp:include>

<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="page-heading mb-1"><i class="bi bi-receipt-cutoff"></i>Hoa don quyet toan</h2>
            <div class="text-muted small">Kiem tra thu/hoan tien truoc khi dong hop dong</div>
        </div>
        <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline-accent btn-sm btn-action-nowrap">
            <i class="bi bi-arrow-left me-1"></i>Quan ly
        </a>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-custom-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-custom-error">${error}</div>
    </c:if>

    <div class="settlement-invoice">
        <div class="settlement-invoice-header">
            <div>
                <div class="text-muted small text-uppercase fw-bold">Ma hop dong</div>
                <h3>${contract.contractCode}</h3>
                <div class="text-muted small">${contract.pickupAt} - ${contract.returnAt}</div>
            </div>
            <div class="text-end">
                <span class="badge-status
                    ${contract.status == 'CANCELLED' ? 'badge-cancelled' : contract.status == 'COMPLETED' ? 'badge-completed' : 'badge-pending'}">
                    ${contract.status}
                </span>
                <div class="text-muted small mt-2">Khach hang: <strong>${contract.customerName}</strong></div>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-7">
                <div class="settlement-panel">
                    <div class="settlement-panel-title">
                        <i class="bi bi-calculator"></i> Bang doi soat
                    </div>
                    <div class="settlement-lines">
                        <div class="settlement-line-row">
                            <span>Coc da thu</span>
                            <strong><fmt:formatNumber value="${settlement.depositPaid}" pattern="#,###"/> VND</strong>
                        </div>
                        <div class="settlement-line-row">
                            <span>Tien thue/tai xe da thu</span>
                            <strong><fmt:formatNumber value="${settlement.rentalPaid}" pattern="#,###"/> VND</strong>
                        </div>
                        <div class="settlement-line-row">
                            <span>Tien thue/tai xe phai thu</span>
                            <strong><fmt:formatNumber value="${settlement.expectedRental}" pattern="#,###"/> VND</strong>
                        </div>
                        <div class="settlement-line-row">
                            <span>Phi phat sinh chua thu</span>
                            <strong><fmt:formatNumber value="${settlement.extraCharge}" pattern="#,###"/> VND</strong>
                        </div>
                        <div class="settlement-line-row">
                            <span>Khau tru vao coc</span>
                            <strong><fmt:formatNumber value="${settlement.deductionAmount}" pattern="#,###"/> VND</strong>
                        </div>
                    </div>

                    <div class="settlement-total-grid">
                        <div class="settlement-total-box">
                            <span>Can thu them</span>
                            <strong><fmt:formatNumber value="${settlement.amountToCollect}" pattern="#,###"/> VND</strong>
                        </div>
                        <div class="settlement-total-box refund">
                            <span>Can hoan cho khach</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${pendingRefund != null}">
                                        <fmt:formatNumber value="${pendingRefund.refundAmount}" pattern="#,###"/>
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${settlement.refundAmount}" pattern="#,###"/>
                                    </c:otherwise>
                                </c:choose>
                                VND
                            </strong>
                        </div>
                    </div>

                    <c:if test="${contract.status == 'CANCELLED'}">
                        <div class="alert alert-warning small mb-0">
                            <i class="bi bi-info-circle me-1"></i>
                            Hop dong da huy. Neu khach huy truoc 2 ngay, so tien da thanh toan duoc dua vao yeu cau hoan tien.
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="settlement-panel h-100">
                    <div class="settlement-panel-title">
                        <i class="bi bi-bank"></i> Tai khoan nhan tien
                    </div>
                    <c:choose>
                        <c:when test="${customerUser != null && customerUser.refundBankInfo}">
                            <div class="refund-bank-card">
                                <div>
                                    <span>Ngan hang</span>
                                    <strong>${customerUser.bankName} (${customerUser.bankCode})</strong>
                                </div>
                                <div>
                                    <span>So tai khoan</span>
                                    <strong>${customerUser.bankAccountNumber}</strong>
                                </div>
                                <div>
                                    <span>Chu tai khoan</span>
                                    <strong>${customerUser.bankAccountHolder}</strong>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-custom-error mb-0">
                                Khach hang chua cap nhat tai khoan ngan hang. Vui long yeu cau khach vao Profile de bo sung.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="settlement-panel mt-4">
            <div class="settlement-panel-title">
                <i class="bi bi-credit-card-2-front"></i> Phuong thuc thanh toan da ghi nhan
            </div>
            <c:choose>
                <c:when test="${not empty paymentRecords}">
                    <div class="table-responsive">
                        <table class="table table-custom mb-0">
                            <thead>
                                <tr>
                                    <th>Khoan tien</th>
                                    <th>So tien</th>
                                    <th>Phuong thuc</th>
                                    <th>Trang thai</th>
                                    <th>Ma GD</th>
                                    <th>Thoi gian</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${paymentRecords}">
                                    <tr>
                                        <td>${p.displayType}</td>
                                        <td><fmt:formatNumber value="${p.amount}" pattern="#,###"/> VND</td>
                                        <td>${p.displayMethod}</td>
                                        <td>
                                            <span class="badge-status ${p.paymentStatus == 'PAID' || p.paymentStatus == 'REFUNDED' ? 'badge-completed' : p.paymentStatus == 'PENDING' || p.paymentStatus == 'REFUND_PENDING' ? 'badge-pending' : 'badge-cancelled'}">
                                                ${p.paymentStatus}
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty p.transactionRef}">${p.transactionRef}</c:when>
                                                <c:otherwise>${p.providerTransactionRef}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.paidAt != null}">${p.paidAt}</c:when>
                                                <c:otherwise>${p.createdAt}</c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-warning small mb-0">Chua co dong thanh toan nao duoc ghi nhan.</div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="divider"></div>
        <fmt:formatNumber var="amountToCollectText" value="${settlement.amountToCollect}" pattern="#,###"/>

        <c:choose>
            <c:when test="${pendingRefund != null}">
                <div class="settlement-refund-workspace">
                    <div class="settlement-qr-panel">
                        <div class="settlement-panel-title">
                            <i class="bi bi-qr-code"></i> QR hoan tien
                        </div>
                        <c:choose>
                            <c:when test="${not empty refundQrUrl}">
                                <img src="${refundQrUrl}" alt="QR hoan tien ${contract.contractCode}" class="refund-qr-img">
                                <div class="refund-qr-note">
                                    Noi dung CK: <strong>HOAN ${contract.contractCode} RF${pendingRefund.refundId}</strong>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-custom-error mb-0">
                                    Khong the tao QR vi thieu thong tin ngan hang cua khach.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="settlement-confirm-panel">
                        <div class="settlement-panel-title">
                            <i class="bi bi-check2-circle"></i> Xac nhan hoan tien
                        </div>
                        <div class="settlement-refund-amount">
                            <span>So tien can chuyen</span>
                            <strong><fmt:formatNumber value="${pendingRefund.refundAmount}" pattern="#,###"/> VND</strong>
                        </div>

                        <c:if test="${not empty pendingRefund.proofOfRefund}">
                            <div class="alert alert-warning small mt-3 mb-0">
                                ${pendingRefund.proofOfRefund}
                            </div>
                        </c:if>
                        <div class="alert alert-info small mt-3 mb-0">
                            Hoan coc dang xu ly thu cong: nhan vien quet QR ben trai, chuyen khoan cho khach,
                            sau do nhap ma giao dich ngan hang de hoan tat.
                        </div>

                        <form method="post" action="${pageContext.request.contextPath}/staff/settlement" class="mt-3">
                            <input type="hidden" name="contractId" value="${contract.contractId}">
                            <input type="hidden" name="refundId" value="${pendingRefund.refundId}">
                            <input type="hidden" name="refundMethod" value="MANUAL_BANK_TRANSFER">
                            <label class="form-label">Ma giao dich sau khi chuyen khoan</label>
                            <input type="text" class="form-control" name="proofOfRefund"
                                   placeholder="VD: FT25123456789" maxlength="100" required>
                            <button type="submit" name="action" value="completeRefund"
                                    class="btn btn-accent btn-action-nowrap w-100 mt-3">
                                <i class="bi bi-check-circle me-1"></i>Xac nhan da hoan tien
                            </button>
                        </form>
                    </div>
                </div>
            </c:when>
            <c:when test="${settlement.amountToCollect > 0}">
                <div class="settlement-refund-workspace">
                    <div class="settlement-qr-panel">
                        <div class="settlement-panel-title">
                            <i class="bi bi-qr-code"></i> QR thu tien con lai
                        </div>
                        <c:choose>
                            <c:when test="${balancePaymentTransaction != null}">
                                <img src="${pageContext.request.contextPath}/payment/qr?ref=${balancePaymentTransaction.providerTransactionRef}"
                                     alt="QR thu tien con lai ${contract.contractCode}" class="refund-qr-img">
                                <div class="refund-qr-note">
                                    Ma GD: <strong>${balancePaymentTransaction.providerTransactionRef}</strong>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-warning small mb-0">
                                    Chua co QR thu tien con lai. Hay tao QR va gui cho khach quet thanh toan.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="settlement-confirm-panel">
                        <div class="settlement-panel-title">
                            <i class="bi bi-cash-coin"></i> Thu tien thue/tai xe con lai
                        </div>
                        <div class="settlement-refund-amount">
                            <span>So tien can thu</span>
                            <strong><fmt:formatNumber value="${settlement.amountToCollect}" pattern="#,###"/> VND</strong>
                        </div>
                        <div class="alert alert-info small mt-3 mb-0">
                            Khach can thanh toan het tien thue/tai xe con lai bang QR truoc, sau do moi tao QR hoan coc.
                        </div>

                        <c:choose>
                            <c:when test="${balancePaymentTransaction != null}">
                                <div class="payment-line mt-3">
                                    <span>Trang thai QR</span>
                                    <strong>${balancePaymentTransaction.status}</strong>
                                </div>
                                <div class="payment-line">
                                    <span>Het han luc</span>
                                    <strong>${balancePaymentTransaction.expiredAt}</strong>
                                </div>
                                <div class="d-flex flex-wrap gap-2 mt-3">
                                    <a href="${pageContext.request.contextPath}/payment/pending?ref=${balancePaymentTransaction.providerTransactionRef}"
                                       target="_blank" rel="noopener noreferrer"
                                       class="btn btn-outline-accent btn-action-nowrap">
                                        <i class="bi bi-box-arrow-up-right me-1"></i>Mo trang thanh toan
                                    </a>
                                    <form method="post" action="${pageContext.request.contextPath}/staff/settlement">
                                        <input type="hidden" name="contractId" value="${contract.contractId}">
                                        <input type="hidden" name="balancePaymentRef" value="${balancePaymentTransaction.providerTransactionRef}">
                                        <button type="submit" name="action" value="checkBalancePayment"
                                                class="btn btn-accent btn-action-nowrap">
                                            <i class="bi bi-arrow-repeat me-1"></i>Kiem tra PayOS
                                        </button>
                                    </form>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${pageContext.request.contextPath}/staff/settlement" class="mt-3">
                                    <input type="hidden" name="contractId" value="${contract.contractId}">
                                    <button type="submit" name="action" value="createBalancePayment"
                                            class="btn btn-accent btn-action-nowrap w-100">
                                        <i class="bi bi-qr-code me-1"></i>Tao QR thu tien con lai
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:when>
            <c:when test="${settlement.refundAmount > 0}">
                <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                      class="settlement-action-panel">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <input type="hidden" name="refundMethod" value="MANUAL_BANK_TRANSFER">
                    <div>
                        <label class="form-label">Ly do hoan/khau tru</label>
                        <input type="text" class="form-control" name="reason"
                               value="Quyet toan sau khi tra xe" maxlength="500">
                    </div>
                    <div class="d-flex flex-wrap gap-2 justify-content-end">
                        <button type="submit" name="action" value="createRefund" class="btn btn-accent btn-action-nowrap">
                            <i class="bi bi-qr-code me-1"></i>Tao QR hoan coc thu cong
                        </button>
                    </div>
                    <div class="alert alert-info small mt-3 mb-0">
                        He thong se tao QR chuyen tien toi tai khoan ngan hang cua khach. Sau khi nhan vien chuyen khoan,
                        nhap ma giao dich de xac nhan hoan coc.
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/staff/settlement"
                      class="text-end">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <button type="submit" name="action" value="complete" class="btn btn-accent btn-action-nowrap">
                        <i class="bi bi-check-all me-1"></i>Hoan tat tat toan
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
