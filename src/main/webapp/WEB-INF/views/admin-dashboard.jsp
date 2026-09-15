<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard – PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2.5rem;
        }
        .kpi-card {
            background-color: var(--bg-dark-secondary);
            border: 1px solid var(--border-dark);
            padding: 1.75rem;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            transition: border-color var(--transition-fast);
        }
        .kpi-card:hover {
            border-color: rgba(255, 255, 255, 0.3);
        }
        .kpi-card.pending-alert {
            border-color: #f59e0b;
        }
        .kpi-card.pending-alert .kpi-value {
            color: #f59e0b;
        }
        .kpi-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }
        .kpi-title {
            color: var(--text-muted);
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin: 0;
        }
        .kpi-value {
            font-size: 2.5rem;
            font-weight: 300;
            line-height: 1;
            margin: 0 0 0.5rem 0;
            letter-spacing: -0.02em;
        }
        .kpi-subtext {
            color: var(--text-muted);
            font-size: 0.85rem;
            margin: 0;
        }
        .disclaimer-banner {
            background-color: rgba(245, 158, 11, 0.08);
            border: 1px solid rgba(245, 158, 11, 0.25);
            padding: 1.25rem 1.5rem;
            margin-bottom: 2.5rem;
            font-size: 0.9rem;
            line-height: 1.5;
            color: #fbbf24;
        }
        .quick-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 1rem;
            margin-top: 1.5rem;
        }
        .quick-action-btn {
            display: inline-block;
            padding: 0.75rem 1.5rem;
            border: 1px solid var(--border-dark);
            color: var(--text-on-dark);
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            transition: all var(--transition-fast);
        }
        .quick-action-btn:hover {
            background-color: var(--text-on-dark);
            color: var(--bg-dark);
        }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 0 6rem; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1200px; margin: 0 auto; padding: 0 1.5rem;">
            
            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-stack-mobile" style="display: flex; justify-content: space-between; align-items: baseline; gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <div>
                    <span class="section-index">Administration Panel</span>
                    <h1 class="editorial-title" style="font-size: 2.2rem; margin: 0.25rem 0 0;">System Overview & Analytics</h1>
                </div>
                <div style="color: var(--text-muted); font-size: 0.85rem;">
                    Platform Engine Active
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding: 1rem; border: 1px solid rgba(52, 211, 153, 0.3); background-color: rgba(52, 211, 153, 0.05);">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding: 1rem; border: 1px solid rgba(239, 68, 68, 0.3); background-color: rgba(239, 68, 68, 0.05);">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- KPI Group 1: Users & Membership -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Platform Membership</h3>
                <div class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Total Users</span>
                        </div>
                        <div class="kpi-value"><c:out value="${stats.totalUsers}"/></div>
                        <p class="kpi-subtext">All registered accounts</p>
                    </div>

                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Customers</span>
                        </div>
                        <div class="kpi-value"><c:out value="${stats.totalCustomers}"/></div>
                        <p class="kpi-subtext">Registered client profiles</p>
                    </div>

                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Photographers</span>
                        </div>
                        <div class="kpi-value"><c:out value="${stats.totalPhotographers}"/></div>
                        <p class="kpi-subtext">Registered creators</p>
                    </div>
                </div>
            </div>

            <!-- KPI Group 2: Photographer Verification Queue -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Verification & Quality</h3>
                <div class="kpi-grid">
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

                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Approved Photographers</span>
                        </div>
                        <div class="kpi-value" style="color: #34d399;"><c:out value="${stats.approvedPhotographers}"/></div>
                        <p class="kpi-subtext">Active on public marketplace</p>
                    </div>

                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Client Reviews</span>
                        </div>
                        <div class="kpi-value"><c:out value="${stats.totalReviews}"/></div>
                        <p class="kpi-subtext">Verified post-shoot feedback</p>
                    </div>
                </div>
            </div>

            <!-- KPI Group 3: Bookings -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Bookings & Engagement</h3>
                <div class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Total Bookings</span>
                        </div>
                        <div class="kpi-value"><c:out value="${stats.totalBookings}"/></div>
                        <p class="kpi-subtext">Lifetime booking requests</p>
                    </div>

                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Pending Bookings</span>
                        </div>
                        <div class="kpi-value" style="color: #60a5fa;"><c:out value="${stats.pendingBookings}"/></div>
                        <p class="kpi-subtext">Awaiting photographer response</p>
                    </div>

                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Completed Shoots</span>
                        </div>
                        <div class="kpi-value" style="color: #34d399;"><c:out value="${stats.completedBookings}"/></div>
                        <p class="kpi-subtext">Successfully fulfilled sessions</p>
                    </div>
                </div>
            </div>

            <!-- KPI Group 4: Financial Simulation (Explicitly Disclaimed) -->
            <div style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">Deposit Simulation Metrics</h3>
                
                <div class="disclaimer-banner">
                    <strong>Development Simulation Notice:</strong> Deposit transactions and amounts displayed below represent simulated test data (TASK-015 sandbox). They do not represent real-world funds or actual platform revenue.
                </div>

                <div class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-header">
                            <span class="kpi-title">Paid Deposits Count</span>
                        </div>
                        <div class="kpi-value"><c:out value="${stats.paidDepositsCount}"/></div>
                        <p class="kpi-subtext">Simulated deposits processed</p>
                    </div>

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

            <!-- Quick Management Shortcuts -->
            <div style="background-color: var(--bg-dark-secondary); border: 1px solid var(--border-dark); padding: 2rem;">
                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem;">Administrative Shortcuts</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">Jump directly into specific operational sections:</p>
                <div class="quick-actions">
                    <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="quick-action-btn">Review Pending Applications (${stats.pendingPhotographerApprovals})</a>
                    <a href="${pageContext.request.contextPath}/admin/users" class="quick-action-btn">Manage Users & Statuses</a>
                    <a href="${pageContext.request.contextPath}/admin/photographers" class="quick-action-btn">All Photographers</a>
                    <a href="${pageContext.request.contextPath}/admin/bookings" class="quick-action-btn">Monitor Bookings</a>
                    <a href="${pageContext.request.contextPath}/admin/reviews" class="quick-action-btn">Inspect Client Reviews</a>
                </div>
            </div>

        </div>
    </main>
</body>
</html>
