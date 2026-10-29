<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
    <title>Notification Center — PhotoConnect</title>
</head>

<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
            <h1 class="h3 fw-bold mb-1">Notification Center</h1>
            <p class="text-muted mb-0">
                <c:choose>
                    <c:when test="${sessionScope.userRole == 'MANAGER'}">
                        Operational escalations, safety reports, and dispute alerts
                    </c:when>
                    <c:otherwise>
                        Stay updated on your booking requests, deliverables, messages, and reviews
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
        <c:if test="${unreadCount > 0}">
            <form action="${pageContext.request.contextPath}/notifications/read-all" method="post" style="margin: 0;">
                <%@ include file="fragments/csrf-input.jsp" %>
                <button type="submit" class="btn btn-outline-primary btn-sm" id="btn-mark-all-read">
                    <i class="bi bi-check2-all me-1"></i> Mark All as Read
                </button>
            </form>
        </c:if>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>
            <c:out value="${successMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Notification Feed -->
    <div class="card border-0 shadow-sm">
        <div class="card-header bg-transparent py-3 d-flex justify-content-between align-items-center border-bottom">
            <h5 class="card-title fw-bold mb-0">Recent Notifications</h5>
            <span class="badge ${unreadCount > 0 ? 'bg-primary' : 'bg-secondary'} px-2 py-1">
                ${unreadCount} unread
            </span>
        </div>
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${empty notifications}">
                    <div class="text-center py-5">
                        <i class="bi bi-bell-slash text-muted" style="font-size: 3rem;"></i>
                        <h6 class="mt-3 fw-bold text-dark">No notifications yet</h6>
                        <p class="text-muted small mb-0">
                            <c:choose>
                                <c:when test="${sessionScope.userRole == 'MANAGER'}">
                                    No operational alerts or safety reports pending at this time.
                                </c:when>
                                <c:otherwise>
                                    When you receive booking updates, photos, or messages, they will appear here.
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="list-group list-group-flush">
                        <c:forEach var="n" items="${notifications}">
                            <div class="list-group-item p-3 d-flex justify-content-between align-items-start gap-3 ${n.read() ? '' : 'bg-light'}">
                                <div class="d-flex align-items-start gap-3">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center p-2" style="background: rgba(37, 99, 235, 0.1); width: 42px; height: 42px; min-width: 42px;">
                                        <i class="bi ${n.iconClass()} text-primary fs-5"></i>
                                    </div>
                                    <div>
                                        <div class="d-flex align-items-center gap-2 mb-1">
                                            <span class="fw-bold text-dark"><c:out value="${n.title()}"/></span>
                                            <c:if test="${!n.read()}">
                                                <span class="badge bg-primary" style="font-size: 0.65rem;">NEW</span>
                                            </c:if>
                                        </div>
                                        <p class="text-muted small mb-1" style="line-height: 1.4;"><c:out value="${n.message()}"/></p>
                                        <small class="text-muted" style="font-size: 0.75rem;"><c:out value="${n.createdAt()}"/></small>
                                    </div>
                                </div>
                                <div class="d-flex align-items-center gap-2">
                                    <c:if test="${not empty n.targetUrl()}">
                                        <a href="${pageContext.request.contextPath}${n.targetUrl()}" class="btn btn-sm btn-outline-primary">
                                            View
                                        </a>
                                    </c:if>
                                    <c:if test="${!n.read()}">
                                        <form action="${pageContext.request.contextPath}/notifications/${n.id()}/read" method="post" style="margin: 0;">
                                            <%@ include file="fragments/csrf-input.jsp" %>
                                            <button type="submit" class="btn btn-sm btn-ghost text-secondary" title="Mark as read">
                                                <i class="bi bi-check2"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
