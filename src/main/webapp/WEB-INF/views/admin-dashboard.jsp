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

            <!-- KPI Group 3: Bookings -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Bookings & Engagement</h3>
                <div class="kpi-grid row g-4">
                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Total Bookings</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.totalBookings}"/></div>
                            <p class="kpi-subtext">Lifetime booking requests</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Pending Bookings</span>
                            </div>
                            <div class="kpi-value" style="color: #60a5fa;"><c:out value="${stats.pendingBookings}"/></div>
                            <p class="kpi-subtext">Awaiting photographer response</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-4">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Completed Shoots</span>
                            </div>
                            <div class="kpi-value" style="color: #34d399;"><c:out value="${stats.completedBookings}"/></div>
                            <p class="kpi-subtext">Successfully fulfilled sessions</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- KPI Group 4: Financial Simulation (Explicitly Disclaimed) -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Deposit Simulation Metrics</h3>

                <div class="alert alert-warning pc-alert pc-alert-warning" role="note">
                    <strong>Development Simulation Notice:</strong> Deposit transactions and amounts displayed below represent simulated test data (TASK-015 sandbox). They do not represent real-world funds or actual platform revenue.
                </div>

                <div class="kpi-grid row g-4">
                    <div class="col-12 col-md-6">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Paid Deposits Count</span>
                            </div>
                            <div class="kpi-value"><c:out value="${stats.paidDepositsCount}"/></div>
                            <p class="kpi-subtext">Simulated deposits processed</p>
                        </div>
                    </div>

                    <div class="col-12 col-md-6">
                        <div class="kpi-card">
                            <div class="kpi-header">
                                <span class="kpi-title">Simulated Deposit Volume</span>
                            </div>
                            <div class="kpi-value" style="font-size: 1.8rem;">
                                <fmt:formatNumber value="${stats.simulatedDepositAmount}" pattern="#,##0" /> <span style="font-size: 1rem; color: var(--text-muted);">VND</span>
                            </div>
                            <p class="kpi-subtext">Simulated test funds (not actual revenue)</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Quick Management Shortcuts -->
            <div class="pc-panel">
                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem;">Administrative Shortcuts</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">Jump directly into specific operational sections:</p>
                <div class="quick-actions d-flex flex-wrap gap-2">
                    <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="quick-action-btn">Review Pending Applications (${stats.pendingPhotographerApprovals})</a>
                    <a href="${pageContext.request.contextPath}/admin/users" class="quick-action-btn">Manage Users & Statuses</a>
                    <a href="${pageContext.request.contextPath}/admin/photographers" class="quick-action-btn">All Photographers</a>
                    <a href="${pageContext.request.contextPath}/admin/bookings" class="quick-action-btn">Monitor Bookings</a>
                    <a href="${pageContext.request.contextPath}/admin/reviews" class="quick-action-btn">Inspect Client Reviews</a>
                </div>
            </div>

        </div>
    </main>
