<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Escrow Deposit Transactions — PhotoConnect Admin</title>
</head>

<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">

        <div class="pc-page-header pc-stack-mobile">
            <div>
                <span class="section-index">FINANCIAL GOVERNANCE</span>
                <h1 class="editorial-title">Escrow Deposit Transactions</h1>
            </div>
            <div class="pc-page-meta">
                Total Transactions: <strong>${totalCount}</strong>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success pc-alert pc-alert-success" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger pc-alert pc-alert-danger" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <!-- KPI Cards -->
        <div class="row g-4 mb-4">
            <div class="col-12 col-md-4">
                <div class="admin-kpi-card accent-success">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Verified Deposits (PAID)</span>
                        <span class="admin-kpi-icon">₫</span>
                    </div>
                    <div class="admin-kpi-value" style="color: #10b981;">
                        <fmt:formatNumber value="${paidAmount}" pattern="#,##0"/> <small style="font-size: 0.9rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Secured via VietQR / Payment Gateway</p>
                </div>
            </div>
            <div class="col-12 col-md-4">
                <div class="admin-kpi-card accent-warning">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Refunded Deposits (REFUNDED)</span>
                        <span class="admin-kpi-icon">↺</span>
                    </div>
                    <div class="admin-kpi-value" style="color: #f59e0b;">
                        <fmt:formatNumber value="${refundedAmount}" pattern="#,##0"/> <small style="font-size: 0.9rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Cancelled &gt;48h or refunded via dispute resolution</p>
                </div>
            </div>
            <div class="col-12 col-md-4">
                <div class="admin-kpi-card accent-danger">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Forfeited Deposits (FORFEITED)</span>
                        <span class="admin-kpi-icon">✕</span>
                    </div>
                    <div class="admin-kpi-value" style="color: #ef4444;">
                        <fmt:formatNumber value="${forfeitedAmount}" pattern="#,##0"/> <small style="font-size: 0.9rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Retained due to cancellation within 48 hours</p>
                </div>
            </div>
        </div>

        <!-- Filter Controls -->
        <div class="admin-card-container mb-4">
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
                <div class="d-flex align-items-center gap-2 flex-wrap">
                    <span style="font-size: 0.82rem; font-weight: 700; color: var(--admin-text-muted); text-transform: uppercase;">Filter Status:</span>
                    <a href="${pageContext.request.contextPath}/admin/payments" class="btn btn-sm ${selectedStatus == 'ALL' ? 'btn-primary' : 'btn-outline-secondary'}">All (${totalCount})</a>
                    <c:forEach var="st" items="${statuses}">
                        <a href="${pageContext.request.contextPath}/admin/payments?status=${st.name()}" class="btn btn-sm ${selectedStatus == st.name() ? 'btn-primary' : 'btn-outline-secondary'}">${st.name()}</a>
                    </c:forEach>
                </div>
            </div>
        </div>

        <!-- Payments Table -->
        <div class="admin-card-container">
            <c:choose>
                <c:when test="${empty deposits}">
                    <div class="text-center py-5">
                        <div style="font-size: 2.5rem; color: var(--admin-text-muted); margin-bottom: 12px;">₫</div>
                        <h4 style="font-weight: 700; margin-bottom: 8px;">No Escrow Deposit Transactions</h4>
                        <p style="color: var(--admin-text-muted); font-size: 0.9rem;">No deposit transactions found matching the selected filter criteria.</p>
                        <a href="${pageContext.request.contextPath}/admin/payments" class="btn btn-sm btn-outline-primary">Clear Filters</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-table-shell">
                        <table class="admin-modern-table">
                            <thead>
                                <tr>
                                    <th>Deposit ID</th>
                                    <th>Booking</th>
                                    <th>Customer</th>
                                    <th>Photographer</th>
                                    <th>Deposit Amount</th>
                                    <th>Status</th>
                                    <th>Method</th>
                                    <th>Reference</th>
                                    <th>Timestamp</th>
                                    <th style="text-align: right; min-width: 90px;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="d" items="${deposits}">
                                    <tr>
                                        <td><strong>#<c:out value="${d.id}"/></strong></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/bookings/${d.booking.id}" style="color: var(--admin-primary); font-weight: 600; text-decoration: none;">
                                                Booking #<c:out value="${d.booking.id}"/>
                                            </a>
                                        </td>
                                        <td><c:out value="${d.booking.customer.fullName}"/></td>
                                        <td><c:out value="${d.booking.photographerProfile.displayName}"/></td>
                                        <td style="font-weight: 700;">
                                            <fmt:formatNumber value="${d.amount}" pattern="#,##0"/> <small style="font-weight: normal; color: var(--admin-text-muted);">VND</small>
                                        </td>
                                        <td>
                                            <span class="status-pill <c:choose><c:when test="${d.status == 'PAID'}">success</c:when><c:when test="${d.status == 'PENDING'}">warning</c:when><c:when test="${d.status == 'REFUNDED'}">primary</c:when><c:otherwise>danger</c:otherwise></c:choose>">
                                                <c:out value="${d.status}"/>
                                            </span>
                                        </td>
                                        <td><c:out value="${d.paymentMethod != null ? d.paymentMethod : 'VIETQR'}"/></td>
                                        <td><small style="color: var(--admin-text-muted);"><c:out value="${d.paymentReference != null ? d.paymentReference : '—'}"/></small></td>
                                        <td><small style="color: var(--admin-text-muted);"><c:choose><c:when test="${not empty d.createdAt}"><c:out value="${fn:substring(d.createdAt, 0, 10)}"/> <c:out value="${fn:substring(d.createdAt, 11, 16)}"/></c:when><c:otherwise>—</c:otherwise></c:choose></small></td>
                                        <td style="text-align: right; white-space: nowrap;">
                                            <a href="${pageContext.request.contextPath}/admin/bookings/${d.booking.id}" class="btn btn-sm btn-outline-secondary" style="font-size: 0.75rem; padding: 3px 8px;">
                                                Details &rarr;
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

    </div>
</main>
