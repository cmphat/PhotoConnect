<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Admin Dashboard – PhotoConnect</title>
</head>

    <main class="pc-page pc-page-compact">
        <div class="editorial-container pc-container-admin">

            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-page-header pc-stack-mobile">
                <div>
                    <span class="section-index">Administration Panel</span>
                    <h1 class="editorial-title">System Overview &amp; Analytics</h1>
                </div>
                <div class="pc-page-meta">
                    Platform Engine Active
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
            <c:if test="${stats.pendingPhotographerApprovals > 0 or stats.disputedBookings > 0}">
                <div class="alert alert-warning pc-alert pc-alert-warning" role="alert">
                    <strong>Operational Notice:</strong> Pending photographer verifications or booking disputes require administrative review.
                </div>
            </c:if>

            <!-- KPI Group 1: Users & Membership -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Platform Membership</h3>
                <div class="kpi-grid row g-4">
                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Total Users</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.totalUsers}"/></div>
                            <p class="kpi-subtext">All registered accounts</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Customers</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.totalCustomers}"/></div>
                            <p class="kpi-subtext">Registered client profiles</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Photographers</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.totalPhotographers}"/></div>
                            <p class="kpi-subtext">Registered creators</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- KPI Group 2: Photographer Verification Queue -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Verification & Quality</h3>
                <div class="kpi-grid row g-4">
                    <div class="col-12 col-md-4">
                        <div class="kpi-card <c:if test="${stats.pendingPhotographerApprovals > 0}">pending-alert</c:if>">
                            <div class="kpi-header">
                                <span class="kpi-title">Pending Approvals</span>
                                <c:if test="${stats.pendingPhotographerApprovals > 0}">
                                    <span style="background-color: #f59e0b; color: #111; font-size: 0.75rem; font-weight: 600; padding: 0.2rem 0.6rem; border-radius: 2px;">Action Required</span>
                                </c:if>
                            </div>
                            <div class="kpi-value">
                                <c:out value="${stats.pendingPhotographerApprovals}"/>
                            </div>
                            <p class="kpi-subtext">Awaiting admin review & decision</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Approved Photographers</span>
                            </div>
                            <div class="kpi-value" style="color: #34d399;"><c:out value="${stats.approvedPhotographers}"/></div>
                            <p class="kpi-subtext">Active on public marketplace</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Client Reviews</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.totalReviews}"/></div>
                            <p class="kpi-subtext">Verified post-shoot feedback</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- KPI Group 3: Bookings & Disputes -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Bookings & Lifecycle</h3>
                <div class="kpi-grid row g-4">
                    <div class="col-12 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Total Bookings</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.totalBookings}"/></div>
                            <p class="kpi-subtext">Lifetime booking requests</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Pending Bookings</span>
                            </div>
                            <div class="kpi-value" style="color: #60a5fa;"><c:out value="${stats.pendingBookings}"/></div>
                            <p class="kpi-subtext">Awaiting creator acceptance</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-3">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Completed Shoots</span>
                            </div>
                            <div class="kpi-value" style="color: #34d399;"><c:out value="${stats.completedBookings}"/></div>
                            <p class="kpi-subtext">Successfully fulfilled</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-3">
                        <div class="kpi-card <c:if test="${stats.disputedBookings > 0}">pending-alert</c:if>">
                            <div class="kpi-header">
                                <span class="kpi-title">Disputed</span>
                                <c:if test="${stats.disputedBookings > 0}">
                                    <span style="background-color: #ef4444; color: #fff; font-size: 0.72rem; font-weight: 600; padding: 0.2rem 0.5rem; border-radius: 2px;">Review Needed</span>
                                </c:if>
                            </div>
                            <div class="kpi-value" style="color: <c:choose><c:when test="${stats.disputedBookings > 0}">#ef4444</c:when><c:otherwise>var(--text)</c:otherwise></c:choose>;">
                                <c:out value="${stats.disputedBookings}"/>
                            </div>
                            <p class="kpi-subtext">Cancellations awaiting dispute audit</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- KPI Group 4: Financial & Escrow Overview -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Financial & Escrow Monitoring</h3>

                <div class="kpi-grid row g-4">
                    <div class="col-12 col-md-6">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Gross Platform Volume</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.6rem;">
                                <fmt:formatNumber value="${stats.totalPlatformBookingVolume}" pattern="#,##0" /> <span style="font-size: 0.9rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">Contracted value of active/fulfilled bookings</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-6">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Completed Revenue</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.6rem; color: #34d399;">
                                <fmt:formatNumber value="${stats.completedRevenue}" pattern="#,##0" /> <span style="font-size: 0.9rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">Delivered & confirmed session value</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Platform Fee (10% Comm.)</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.6rem; color: var(--accent);">
                                <fmt:formatNumber value="${stats.platformFeeRevenue}" pattern="#,##0" /> <span style="font-size: 0.9rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">Estimated platform commission</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Confirmed Escrow Deposits</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.6rem; color: #60a5fa;">
                                <fmt:formatNumber value="${stats.simulatedDepositAmount}" pattern="#,##0" /> <span style="font-size: 0.9rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">${stats.paidDepositsCount} confirmed deposit payments</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Refunded Deposits</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.6rem;">
                                <fmt:formatNumber value="${stats.refundedDepositsAmount}" pattern="#,##0" /> <span style="font-size: 0.9rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">${stats.refundedDepositsCount} refunded via 48h policy / dispute</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Forfeited Deposits</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.6rem; color: #f59e0b;">
                                <fmt:formatNumber value="${stats.forfeitedDepositsAmount}" pattern="#,##0" /> <span style="font-size: 0.9rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">${stats.forfeitedDepositsCount} retained due to late cancellation</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Quick Management Shortcuts -->
            <div class="pc-panel">
                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem;">Administrative Shortcuts</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">Jump directly into specific operational sections:</p>
                <div class="quick-actions d-flex flex-wrap gap-2">
                    <c:if test="${stats.disputedBookings > 0}">
                        <a href="${pageContext.request.contextPath}/admin/bookings?status=DISPUTED" class="quick-action-btn" style="background-color: rgba(239, 68, 68, 0.15); color: #ef4444; border-color: rgba(239, 68, 68, 0.3);">
                            Review Disputed Bookings (${stats.disputedBookings})
                        </a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="quick-action-btn">Review Pending Applications (${stats.pendingPhotographerApprovals})</a>
                    <a href="${pageContext.request.contextPath}/admin/bookings" class="quick-action-btn">Monitor All Bookings</a>
                    <a href="${pageContext.request.contextPath}/admin/users" class="quick-action-btn">Manage Users & Statuses</a>
                    <a href="${pageContext.request.contextPath}/admin/photographers" class="quick-action-btn">All Photographers</a>
                    <a href="${pageContext.request.contextPath}/admin/reviews" class="quick-action-btn">Inspect Client Reviews</a>
                </div>
            </div>

        </div>
    </main>
