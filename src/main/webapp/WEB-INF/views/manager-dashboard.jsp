<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <title>Operations Dashboard — PhotoConnect</title>
</head>
<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">
        <jsp:include page="fragments/manager-navbar.jsp"/>

        <div class="pc-page-header pc-stack-mobile mb-4">
            <div>
                <span class="section-index">STAFF OPERATIONS</span>
                <h1 class="editorial-title" style="margin-bottom: 4px;">Operations Console</h1>
                <p style="color: var(--admin-text-muted); font-size: 0.9rem; margin: 0;">Real-time operational backlog, approvals, and dispute management</p>
            </div>
            <div class="pc-page-meta d-flex align-items-center gap-2">
                <span class="status-pill success" style="padding: 4px 10px;">OPERATIONS ACTIVE</span>
                <a href="${pageContext.request.contextPath}/manager/dashboard" class="btn btn-sm btn-outline-secondary">Refresh</a>
            </div>
        </div>

        <jsp:include page="fragments/manager-messages.jsp"/>

        <div class="row g-3 mb-4">
            <div class="col-6 col-lg">
                <a class="pc-card d-block h-100 text-decoration-none" href="${pageContext.request.contextPath}/manager/photographers?status=PENDING" style="color: inherit;">
                    <div style="font-size: 1.85rem; font-weight: 700; color: var(--admin-text); line-height: 1.2;">${stats.pendingPhotographerApplications}</div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--admin-text-muted); margin-top: 4px;">Pending Applications</div>
                </a>
            </div>
            <div class="col-6 col-lg">
                <a class="pc-card d-block h-100 text-decoration-none" href="${pageContext.request.contextPath}/manager/reports" style="color: inherit;">
                    <div style="font-size: 1.85rem; font-weight: 700; color: var(--admin-text); line-height: 1.2;">${stats.openReports}</div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--admin-text-muted); margin-top: 4px;">Open Reports</div>
                </a>
            </div>
            <div class="col-6 col-lg">
                <a class="pc-card d-block h-100 text-decoration-none" href="${pageContext.request.contextPath}/manager/bookings?status=DISPUTED" style="color: inherit;">
                    <div style="font-size: 1.85rem; font-weight: 700; color: var(--admin-text); line-height: 1.2;">${stats.unresolvedDisputes}</div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--admin-text-muted); margin-top: 4px;">Disputes</div>
                </a>
            </div>
            <div class="col-6 col-lg">
                <a class="pc-card d-block h-100 text-decoration-none" href="${pageContext.request.contextPath}/manager/support" style="color: inherit;">
                    <div style="font-size: 1.85rem; font-weight: 700; color: var(--admin-text); line-height: 1.2;">${stats.openSupportTickets}</div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--admin-text-muted); margin-top: 4px;">Support Tickets</div>
                </a>
            </div>
            <div class="col-6 col-lg">
                <div class="pc-card h-100">
                    <div style="font-size: 1.85rem; font-weight: 700; color: var(--admin-text); line-height: 1.2;">${stats.totalBookings}</div>
                    <div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--admin-text-muted); margin-top: 4px;">Total Bookings</div>
                </div>
            </div>
        </div>

        <h2 class="editorial-heading h4 mb-3">My Recent Operational Actions</h2>
        <div class="admin-table-shell">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>Actor</th>
                        <th>Action</th>
                        <th>Target</th>
                        <th>Outcome</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="log" items="${activity}">
                        <tr>
                            <td><c:out value="${log.createdAt}"/></td>
                            <td><c:out value="${log.actor.fullName}"/></td>
                            <td><c:out value="${log.action}"/></td>
                            <td><c:out value="${log.targetType}"/> #<c:out value="${log.targetId}"/></td>
                            <td><c:out value="${log.outcome}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty activity}">
                        <tr><td colspan="5" class="text-muted">No recent operational actions recorded by your account.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</main>
