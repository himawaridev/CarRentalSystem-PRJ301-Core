<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="requestedUri" value="${requestScope['jakarta.servlet.forward.request_uri']}" />
<c:if test="${empty requestedUri}">
    <c:set var="requestedUri" value="${pageContext.request.requestURI}" />
</c:if>
<c:set var="currentPath" value="${fn:substringAfter(requestedUri, pageContext.request.contextPath)}" />
<c:set var="hasCustomerRole" value="${not empty sessionScope.userRoles and sessionScope.userRoles.contains('CUSTOMER')}" />
<c:set var="hasStaffRole" value="${not empty sessionScope.userRoles and (sessionScope.userRoles.contains('STAFF') or sessionScope.userRoles.contains('MANAGER') or sessionScope.userRoles.contains('ADMIN'))}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.title} - CarRental</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${ctx}/css/style.css?v=13" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-light bg-dark-custom sticky-top">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${ctx}/search">
            <i class="bi bi-car-front-fill me-2"></i>CarRental
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link ${currentPath == '/search' ? 'active' : ''}"
                       href="${ctx}/search" ${currentPath == '/search' ? 'aria-current="page"' : ''}>
                        <i class="bi bi-search me-1"></i>Tim xe
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPath == '/cars' or currentPath == '/select-car' or currentPath == '/book' ? 'active' : ''}"
                       href="${ctx}/cars" ${currentPath == '/cars' or currentPath == '/select-car' or currentPath == '/book' ? 'aria-current="page"' : ''}>
                        <i class="bi bi-grid me-1"></i>Xem xe
                    </a>
                </li>
                <c:if test="${not empty sessionScope.loggedInUser}">
                    <c:if test="${hasCustomerRole}">
                        <li class="nav-item">
                            <a class="nav-link ${currentPath == '/my-contracts' or (currentPath == '/contract-detail' and not hasStaffRole) ? 'active' : ''}"
                               href="${ctx}/my-contracts" ${currentPath == '/my-contracts' or (currentPath == '/contract-detail' and not hasStaffRole) ? 'aria-current="page"' : ''}>
                                <i class="bi bi-file-text me-1"></i>Hop dong cua toi
                            </a>
                        </li>
                    </c:if>
                    <c:if test="${hasStaffRole}">
                        <li class="nav-item">
                            <a class="nav-link ${fn:startsWith(currentPath, '/staff') or currentPath == '/contract-detail' ? 'active' : ''}"
                               href="${ctx}/staff/dashboard" ${fn:startsWith(currentPath, '/staff') or currentPath == '/contract-detail' ? 'aria-current="page"' : ''}>
                                <i class="bi bi-clipboard-check me-1"></i>Nhan vien
                            </a>
                        </li>
                    </c:if>
                    <c:if test="${sessionScope.userRoles.contains('MANAGER') or sessionScope.userRoles.contains('ADMIN')}">
                        <li class="nav-item">
                            <a class="nav-link ${fn:startsWith(currentPath, '/manager') ? 'active' : ''}"
                               href="${ctx}/manager/dashboard" ${fn:startsWith(currentPath, '/manager') ? 'aria-current="page"' : ''}>
                                <i class="bi bi-gear me-1"></i>Quan ly
                            </a>
                        </li>
                    </c:if>
                    <c:if test="${sessionScope.userRoles.contains('DRIVER')}">
                        <li class="nav-item">
                            <a class="nav-link ${fn:startsWith(currentPath, '/driver') ? 'active' : ''}"
                               href="${ctx}/driver/schedule" ${fn:startsWith(currentPath, '/driver') ? 'aria-current="page"' : ''}>
                                <i class="bi bi-calendar-check me-1"></i>Lich lai xe
                            </a>
                        </li>
                    </c:if>
                    <c:if test="${sessionScope.userRoles.contains('ADMIN')}">
                        <li class="nav-item">
                            <a class="nav-link ${fn:startsWith(currentPath, '/admin') ? 'active' : ''}"
                               href="${ctx}/admin/dashboard" ${fn:startsWith(currentPath, '/admin') ? 'aria-current="page"' : ''}>
                                <i class="bi bi-shield-lock me-1"></i>Admin
                            </a>
                        </li>
                    </c:if>
                </c:if>
            </ul>
            <ul class="navbar-nav">
                <c:choose>
                    <c:when test="${not empty sessionScope.loggedInUser}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-person-circle me-1"></i>${sessionScope.loggedInUser.fullName}
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><span class="dropdown-item-text text-muted small">${sessionScope.userRoles}</span></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="${ctx}/logout"><i class="bi bi-box-arrow-right me-1"></i>Dang xuat</a></li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link ${currentPath == '/login' ? 'active' : ''}"
                               href="${ctx}/login" ${currentPath == '/login' ? 'aria-current="page"' : ''}>
                                <i class="bi bi-box-arrow-in-right me-1"></i>Dang nhap
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link btn-register-nav ${currentPath == '/register' ? 'active' : ''}"
                               href="${ctx}/register" ${currentPath == '/register' ? 'aria-current="page"' : ''}>Dang ky</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
