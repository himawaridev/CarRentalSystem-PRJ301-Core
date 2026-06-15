<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="title" value="Thanh toan hop dong"/></jsp:include>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="card-custom">
                <div class="card-header"><i class="bi bi-qr-code me-2"></i>Thanh toan hop dong</div>
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                        <div>
                            <div class="text-muted small">Ma hop dong</div>
                            <h4 class="text-accent fw-bold mb-0">${contract.contractCode}</h4>
                        </div>
                        <span id="paymentStatusBadge" class="badge-status ${paymentTransaction.status == 'PAID' ? 'badge-accepted' : 'badge-pending'}">${paymentTransaction.status}</span>
                    </div>

                    <div id="paymentSuccessPanel" class="alert alert-custom-success mb-4"
                         style="${paymentTransaction.status == 'PAID' ? '' : 'display:none'}">
                        <i class="bi bi-check-circle-fill me-1"></i>
                        Thanh toan thanh cong. He thong se tu dong chuyen ve danh sach hop dong.
                    </div>

                    <div id="paymentExpiredPanel" class="alert alert-warning mb-4" style="display:none">
                        <i class="bi bi-clock-history me-1"></i>
                        Giao dich da het han. Vui long quay ve hop dong va tao thanh toan moi.
                    </div>

                    <div id="paymentWaitingPanel" class="row g-3 align-items-stretch"
                         style="${paymentTransaction.status == 'PAID' ? 'display:none' : ''}">
                        <div class="col-md-5">
                            <div class="qr-box h-100">
                                <c:choose>
                                    <c:when test="${not empty paymentTransaction.qrPayload}">
                                        <img src="${pageContext.request.contextPath}/payment/qr?ref=${paymentTransaction.providerTransactionRef}"
                                             alt="QR thanh toan ${paymentTransaction.providerTransactionRef}"
                                             class="payment-qr-img">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-warning mb-0">
                                            Chua co QR thanh toan. Vui long lien he nhan vien de kiem tra cau hinh cong thanh toan.
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="small text-muted text-center mt-2">QR thanh toan rieng cho giao dich nay</div>
                            </div>
                        </div>
                        <div class="col-md-7">
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
                            <div class="mt-3 p-2 bg-accent-soft rounded small">
                                ${paymentTransaction.qrPayload}
                            </div>
                        </div>
                    </div>

                    <div class="text-end mt-4">
                        <a href="${paymentReturnUrl}" class="btn btn-outline-accent me-2">
                            <i class="bi bi-arrow-left me-1"></i>Ve hop dong
                        </a>
                        <c:if test="${not empty paymentTransaction.providerCheckoutUrl}">
                            <a id="paymentGatewayLink" href="${paymentTransaction.providerCheckoutUrl}" target="_blank"
                               rel="noopener noreferrer" class="btn btn-accent">
                                <i class="bi bi-box-arrow-up-right me-1"></i>Mo cong thanh toan
                            </a>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
(function () {
    const paymentRef = '${paymentTransaction.providerTransactionRef}';
    const statusUrl = '${pageContext.request.contextPath}/payment/status?ref=' + encodeURIComponent(paymentRef);
    const redirectUrl = '${paymentReturnUrl}';
    const badge = document.getElementById('paymentStatusBadge');
    const waitingPanel = document.getElementById('paymentWaitingPanel');
    const successPanel = document.getElementById('paymentSuccessPanel');
    const expiredPanel = document.getElementById('paymentExpiredPanel');
    const gatewayLink = document.getElementById('paymentGatewayLink');
    let paymentWindow = null;
    let redirected = false;

    function setStatusBadge(status, paid) {
        if (!badge) return;
        badge.textContent = status;
        badge.classList.remove('badge-pending', 'badge-accepted', 'badge-cancelled');
        badge.classList.add(paid ? 'badge-accepted' : status === 'EXPIRED' ? 'badge-cancelled' : 'badge-pending');
    }

    function showPaid(data) {
        if (redirected) return;
        redirected = true;
        setStatusBadge(data.paymentStatus || 'PAID', true);
        if (waitingPanel) waitingPanel.style.display = 'none';
        if (gatewayLink) gatewayLink.style.display = 'none';
        if (expiredPanel) expiredPanel.style.display = 'none';
        if (successPanel) successPanel.style.display = '';
        try {
            if (paymentWindow && !paymentWindow.closed) {
                paymentWindow.close();
            }
        } catch (e) {
            // Some browsers do not allow closing a tab after the gateway navigates cross-origin.
        }
        window.setTimeout(function () {
            window.location.href = data.redirectUrl || redirectUrl;
        }, 1800);
    }

    function showExpired(data) {
        setStatusBadge('EXPIRED', false);
        if (expiredPanel) expiredPanel.style.display = '';
        if (gatewayLink) gatewayLink.style.display = 'none';
        if (waitingPanel) waitingPanel.style.opacity = '0.55';
        window.clearInterval(timer);
    }

    async function pollPaymentStatus() {
        try {
            const response = await fetch(statusUrl, {
                headers: { 'Accept': 'application/json' },
                cache: 'no-store'
            });
            if (!response.ok) return;
            const data = await response.json();
            if (!data.ok) return;
            if (data.paid || data.paymentStatus === 'PAID') {
                showPaid(data);
                window.clearInterval(timer);
                return;
            }
            if (data.expired) {
                showExpired(data);
                return;
            }
            setStatusBadge(data.paymentStatus || 'PENDING', false);
        } catch (e) {
            // Keep polling; network hiccups are common while webhook and UI race each other.
        }
    }

    if (badge && badge.textContent.trim() === 'PAID') {
        window.setTimeout(function () { window.location.href = redirectUrl; }, 1800);
        return;
    }

    if (gatewayLink) {
        gatewayLink.addEventListener('click', function (event) {
            event.preventDefault();
            paymentWindow = window.open(gatewayLink.href, 'payosCheckout');
            if (paymentWindow) {
                try {
                    paymentWindow.opener = null;
                } catch (e) {
                    // Keep the checkout usable even if the browser blocks changing opener.
                }
            } else {
                window.location.href = gatewayLink.href;
            }
        });
    }

    const timer = window.setInterval(pollPaymentStatus, 2500);
    pollPaymentStatus();
})();
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp"/>
