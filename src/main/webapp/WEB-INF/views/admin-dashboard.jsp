<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Admin Dashboard — PhotoConnect</title>
</head>

<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">

        <!-- Top Header Banner -->
        <div class="pc-page-header pc-stack-mobile mb-4">
            <div>
                <span class="section-index">PLATFORM OPERATIONS</span>
                <h1 class="editorial-title" style="margin-bottom: 4px;">Dashboard Overview &amp; KPI Analytics</h1>
                <p style="color: var(--admin-text-muted); font-size: 0.9rem; margin: 0;">Real-time platform metrics and operations from PhotoConnect database</p>
            </div>
            <div class="pc-page-meta d-flex align-items-center gap-2">
                <span class="status-pill success" style="padding: 4px 10px;">ENGINE ACTIVE</span>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-sm btn-outline-secondary">Refresh Metrics</a>
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

        <c:if test="${stats.pendingPhotographerApprovals > 0 or stats.disputedBookings > 0 or stats.pendingReportsCount > 0}">
            <div class="alert alert-warning d-flex align-items-center justify-content-between flex-wrap gap-2 mb-4" role="alert">
                <div class="d-flex align-items-center gap-2">
                    <span style="font-size: 1.25rem;">⚠️</span>
                    <div>
                        <strong>Emergency Operations Alert:</strong> 
                        <c:if test="${stats.pendingPhotographerApprovals > 0}">There are <strong>${stats.pendingPhotographerApprovals}</strong> photographer applications pending review. </c:if>
                        <c:if test="${stats.disputedBookings > 0}">There are <strong>${stats.disputedBookings}</strong> bookings in dispute. </c:if>
                        <c:if test="${stats.pendingReportsCount > 0}">There are <strong>${stats.pendingReportsCount}</strong> safety reports requiring moderation.</c:if>
                    </div>
                </div>
                <div class="d-flex gap-2">
                    <c:if test="${stats.pendingPhotographerApprovals > 0}">
                        <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="btn btn-sm btn-warning text-white">Review Applications</a>
                    </c:if>
                    <c:if test="${stats.disputedBookings > 0}">
                        <a href="${pageContext.request.contextPath}/admin/bookings?status=DISPUTED" class="btn btn-sm btn-danger">Handle Disputes</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <!-- KPI Group 1: User Accounts & Roles -->
        <div class="mb-4">
            <div class="kpi-section-title">
                <span>User Accounts &amp; Role Distribution</span>
            </div>
            <div class="row g-3">
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Total Accounts</span>
                            <span class="admin-kpi-icon">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle></svg>
                            </span>
                        </div>
                        <div class="admin-kpi-value"><c:out value="${stats.totalUsers}"/></div>
                        <p class="admin-kpi-subtext">All registered accounts in system</p>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Customers</span>
                            <span class="admin-kpi-icon">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                            </span>
                        </div>
                        <div class="admin-kpi-value" style="color: #3b82f6;"><c:out value="${stats.totalCustomers}"/></div>
                        <p class="admin-kpi-subtext">Client accounts booking photo shoots</p>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card accent-success">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Approved Photographers</span>
                            <span class="admin-kpi-icon">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
                            </span>
                        </div>
                        <div class="admin-kpi-value" style="color: #10b981;"><c:out value="${stats.approvedPhotographers}"/></div>
                        <p class="admin-kpi-subtext">Active &amp; published on marketplace</p>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card accent-purple">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Staff Managers</span>
                            <span class="admin-kpi-icon">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                            </span>
                        </div>
                        <div class="admin-kpi-value" style="color: #8b5cf6;"><c:out value="${stats.totalManagers}"/></div>
                        <p class="admin-kpi-subtext">Staff assigned operational access</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- KPI Group 2: Booking Operations -->
        <div class="mb-4">
            <div class="kpi-section-title">
                <span>Booking Operations &amp; Quality Assurance</span>
            </div>
            <div class="row g-3">
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Total Bookings</span>
                            <span class="admin-kpi-icon">▣</span>
                        </div>
                        <div class="admin-kpi-value"><c:out value="${stats.totalBookings}"/></div>
                        <p class="admin-kpi-subtext">All shoot requests initiated</p>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card accent-success">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Completed Shoots</span>
                            <span class="admin-kpi-icon">✓</span>
                        </div>
                        <div class="admin-kpi-value" style="color: #10b981;"><c:out value="${stats.completedBookings}"/></div>
                        <p class="admin-kpi-subtext">Albums delivered &amp; accepted</p>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card accent-warning">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Pending Approvals</span>
                            <c:if test="${stats.pendingPhotographerApprovals > 0}">
                                <span class="badge bg-warning text-dark">PENDING</span>
                            </c:if>
                        </div>
                        <div class="admin-kpi-value" style="color: #f59e0b;"><c:out value="${stats.pendingPhotographerApprovals}"/></div>
                        <p class="admin-kpi-subtext"><a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" style="color: inherit; text-decoration: underline;">Review Applications &rarr;</a></p>
                    </div>
                </div>
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="admin-kpi-card accent-danger">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Disputed / Cancelled</span>
                            <c:if test="${stats.disputedBookings > 0}">
                                <span class="badge bg-danger">DISPUTE</span>
                            </c:if>
                        </div>
                        <div class="admin-kpi-value" style="color: ${stats.disputedBookings > 0 ? '#ef4444' : 'var(--admin-text)'};">
                            <c:out value="${stats.disputedBookings}"/> <span style="font-size: 0.95rem; color: var(--admin-text-muted); font-weight: normal;">/ <c:out value="${stats.cancelledBookings}"/> cancelled</span>
                        </div>
                        <p class="admin-kpi-subtext"><a href="${pageContext.request.contextPath}/admin/bookings?status=DISPUTED" style="color: inherit; text-decoration: underline;">Resolve Disputes &rarr;</a></p>
                    </div>
                </div>
            </div>
        </div>

        <!-- KPI Group 3: Platform Financials -->
        <div class="mb-4">
            <div class="kpi-section-title">
                <span>Platform Financials &amp; Revenue Ledger</span>
            </div>
            <div class="row g-4 mb-3">
                <div class="col-12 col-md-6">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Gross Completed GMV</span>
                            <span class="admin-kpi-icon">₫</span>
                        </div>
                        <div class="admin-kpi-value" style="font-size: 1.6rem;">
                            <fmt:formatNumber value="${stats.totalPlatformBookingVolume}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                        </div>
                        <p class="admin-kpi-subtext">Total cumulative value of completed bookings</p>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="admin-kpi-card accent-success">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Settled Shoot Volume</span>
                            <span class="admin-kpi-icon">✓</span>
                        </div>
                        <div class="admin-kpi-value" style="font-size: 1.6rem; color: #10b981;">
                            <fmt:formatNumber value="${stats.completedRevenue}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                        </div>
                        <p class="admin-kpi-subtext">Gross volume from fulfilled photo shoots</p>
                    </div>
                </div>
                <div class="col-12 col-md-4">
                    <div class="admin-kpi-card accent-purple">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Platform Fees Accrued (10%)</span>
                            <span class="admin-kpi-icon">％</span>
                        </div>
                        <div class="admin-kpi-value" style="font-size: 1.55rem; color: #8b5cf6;">
                            <fmt:formatNumber value="${stats.platformFeeRevenue}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                        </div>
                        <p class="admin-kpi-subtext">Commission accrued on completed jobs</p>
                    </div>
                </div>
                <div class="col-12 col-md-4">
                    <div class="admin-kpi-card accent-success">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Platform Fees Collected</span>
                            <span class="admin-kpi-icon">✓</span>
                        </div>
                        <div class="admin-kpi-value" style="font-size: 1.55rem; color: #10b981;">
                            <fmt:formatNumber value="${stats.platformFeeCollected}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                        </div>
                        <p class="admin-kpi-subtext">Successfully settled into platform account</p>
                    </div>
                </div>
                <div class="col-12 col-md-4">
                    <div class="admin-kpi-card accent-danger">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-title">Outstanding Fee Balance</span>
                            <span class="admin-kpi-icon">!</span>
                        </div>
                        <div class="admin-kpi-value" style="font-size: 1.55rem; color: #ef4444;">
                            <fmt:formatNumber value="${stats.platformFeeOutstanding}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                        </div>
                        <p class="admin-kpi-subtext"><a href="${pageContext.request.contextPath}/admin/platform-fees" style="color: inherit; text-decoration: underline;">View Fee Ledger &rarr;</a></p>
                    </div>
                </div>
            </div>
            <div class="quick-actions d-flex flex-wrap gap-2 mt-2">
                <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="btn btn-sm btn-outline-secondary">Pending Applications (${stats.pendingPhotographerApprovals})</a>
                <a href="${pageContext.request.contextPath}/admin/bookings" class="btn btn-sm btn-outline-secondary">All Bookings</a>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm btn-outline-secondary">User Directory</a>
                <a href="${pageContext.request.contextPath}/admin/platform-fees" class="btn btn-sm btn-outline-secondary">Platform Fee Ledger</a>
                <a href="${pageContext.request.contextPath}/admin/payments" class="btn btn-sm btn-outline-secondary">Escrow Deposits</a>
                <a href="${pageContext.request.contextPath}/admin/settings" class="btn btn-sm btn-outline-secondary">System Settings</a>
            </div>
        </div>

        <!-- Analytics & Chart Section -->
        <section class="admin-analytics-section" id="analytics" aria-labelledby="analyticsTitle">
            <div class="admin-section-heading">
                <div>
                    <span class="section-index">REAL-TIME PLATFORM METRICS</span>
                    <h2 id="analyticsTitle" style="font-size: 1.35rem; font-weight: 800; margin: 0;">Operational &amp; Financial Analytics</h2>
                </div>
                <form class="admin-range-filter" id="analyticsFilter">
                    <button type="button" class="btn btn-sm btn-outline-secondary range-btn" data-range="7">7 Days</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary range-btn" data-range="30">30 Days</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary range-btn active" data-range="365">1 Year</button>
                    <label>From <input type="date" name="startDate"></label>
                    <label>To <input type="date" name="endDate"></label>
                    <button type="submit" class="btn btn-sm btn-primary" style="padding: 5px 12px;">Apply</button>
                </form>
            </div>

            <div class="admin-chart-feedback" id="chartLoading" role="status" style="padding: 20px; text-align: center; color: var(--admin-text-muted);">
                <div class="spinner-border spinner-border-sm text-primary" role="status"></div> Loading chart data from database…
            </div>
            <div class="admin-chart-feedback error d-none alert alert-danger" id="chartError" role="alert">
                Unable to load analytics data. Please check server connection.
            </div>

            <!-- 6 Comprehensive Charts (Real Database Metrics) -->
            <div class="admin-chart-grid" id="chartGrid">
                <!-- Chart 1: Line Chart - User Registrations -->
                <article class="admin-chart-card">
                    <h3>1. Monthly User Registrations</h3>
                    <p>Track account growth across Customers and Photographers</p>
                    <div class="admin-chart-canvas"><canvas id="usersChart"></canvas></div>
                </article>

                <!-- Chart 2: Bar Chart - Monthly Bookings -->
                <article class="admin-chart-card">
                    <h3>2. Monthly Booking Volume</h3>
                    <p>Total photo shoot bookings initiated on platform</p>
                    <div class="admin-chart-canvas"><canvas id="bookingsChart"></canvas></div>
                </article>

                <!-- Chart 3: Doughnut Chart - Booking Status Distribution -->
                <article class="admin-chart-card">
                    <h3>3. Booking Status Distribution</h3>
                    <p>Distribution across Completed, Pending, Confirmed, Cancelled, Disputed</p>
                    <div class="admin-chart-canvas"><canvas id="statusChart"></canvas></div>
                </article>

                <!-- Chart 4: Bar/Line Chart - Completed Booking Revenue -->
                <article class="admin-chart-card">
                    <h3>4. Completed Booking GMV (VND)</h3>
                    <p>Gross booking value for fulfilled photo sessions</p>
                    <div class="admin-chart-canvas"><canvas id="revenueChart"></canvas></div>
                </article>

                <!-- Chart 5: Bar Chart - Customer vs Photographer Growth -->
                <article class="admin-chart-card">
                    <h3>5. Customer vs Photographer Growth</h3>
                    <p>New customer acquisitions compared with photographer onboarding</p>
                    <div class="admin-chart-canvas"><canvas id="comparisonChart"></canvas></div>
                </article>

                <!-- Chart 6: Platform Fee Trends -->
                <article class="admin-chart-card">
                    <h3>6. Platform Fees: Incurred vs Outstanding</h3>
                    <p>Platform commission (10%) compared to pending receivables</p>
                    <div class="admin-chart-canvas"><canvas id="feeChart"></canvas></div>
                </article>
            </div>
        </section>

        <!-- Quick Navigation Shortcuts -->
        <div class="admin-card-container">
            <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 6px;">Administrative Quick Actions</h3>
            <p style="color: var(--admin-text-muted); font-size: 0.88rem; margin-bottom: 16px;">Fast access to primary administrative subsystems across PhotoConnect:</p>
            <div class="d-flex flex-wrap gap-2">
                <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="btn btn-sm btn-outline-warning">
                    Review Photographer Applications (${stats.pendingPhotographerApprovals})
                </a>
                <c:if test="${stats.disputedBookings > 0}">
                    <a href="${pageContext.request.contextPath}/admin/bookings?status=DISPUTED" class="btn btn-sm btn-outline-danger">
                        Resolve Disputed Bookings (${stats.disputedBookings})
                    </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/bookings" class="btn btn-sm btn-outline-primary">Monitor All Bookings</a>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm btn-outline-secondary">User Directory</a>
                <a href="${pageContext.request.contextPath}/admin/users?role=MANAGER" class="btn btn-sm btn-outline-info">Staff Manager Access</a>
                <a href="${pageContext.request.contextPath}/admin/payments" class="btn btn-sm btn-outline-success">Escrow Deposits</a>
                <a href="${pageContext.request.contextPath}/admin/platform-fees" class="btn btn-sm btn-outline-primary">Platform Fee Ledger</a>
                <a href="${pageContext.request.contextPath}/admin/reviews" class="btn btn-sm btn-outline-secondary">Review &amp; Safety Moderation</a>
                <a href="${pageContext.request.contextPath}/admin/audit" class="btn btn-sm btn-outline-dark">Audit Trail Logs</a>
                <a href="${pageContext.request.contextPath}/admin/settings" class="btn btn-sm btn-outline-secondary">System Settings</a>
            </div>
        </div>

    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js"></script>
<script>
(() => {
    const baseUrl = '${pageContext.request.contextPath}/admin/dashboard/analytics';
    const loading = document.getElementById('chartLoading');
    const error = document.getElementById('chartError');
    const charts = {};
    const colors = {
        navy: '#0f172a',
        blue: '#2563eb',
        teal: '#10b981',
        amber: '#f59e0b',
        red: '#ef4444',
        purple: '#8b5cf6',
        cyan: '#06b6d4',
        slate: '#64748b'
    };

    const destroy = key => { if (charts[key]) charts[key].destroy(); };
    const moneyFormat = value => new Intl.NumberFormat('en-US').format(value);

    async function loadAnalytics(params = '') {
        loading.classList.remove('d-none');
        error.classList.add('d-none');
        try {
            const response = await fetch(baseUrl + params, { headers: { Accept: 'application/json' } });
            if (!response.ok) throw new Error('HTTP ' + response.status);
            const data = await response.json();

            // 1. Line Chart: User Registrations
            destroy('users');
            charts.users = new Chart(document.getElementById('usersChart'), {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [
                        { label: 'Total Accounts', data: data.userRegistrations, borderColor: colors.blue, backgroundColor: 'rgba(37,99,235,0.12)', fill: true, tension: 0.3 },
                        { label: 'Customers', data: data.customerRegistrations, borderColor: colors.teal, tension: 0.3 },
                        { label: 'Photographers', data: data.photographerRegistrations, borderColor: colors.amber, tension: 0.3 }
                    ]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });

            // 2. Bar Chart: Monthly Bookings
            destroy('bookings');
            charts.bookings = new Chart(document.getElementById('bookingsChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{ label: 'Bookings', data: data.bookings, backgroundColor: colors.blue, borderRadius: 4 }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });

            // 3. Doughnut Chart: Booking Statuses
            destroy('status');
            charts.status = new Chart(document.getElementById('statusChart'), {
                type: 'doughnut',
                data: {
                    labels: data.bookingStatusLabels,
                    datasets: [{
                        data: data.bookingStatusCounts,
                        backgroundColor: [colors.teal, colors.blue, colors.amber, colors.red, colors.purple, colors.cyan, colors.slate]
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });

            // 4. Bar Chart: Completed Revenue
            destroy('revenue');
            charts.revenue = new Chart(document.getElementById('revenueChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{ label: 'Completed GMV (VND)', data: data.completedBookingValue, backgroundColor: colors.teal, borderRadius: 4 }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { ticks: { callback: moneyFormat } } }
                }
            });

            // 5. Comparison Bar Chart: Customer vs Photographer
            destroy('comparison');
            charts.comparison = new Chart(document.getElementById('comparisonChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [
                        { label: 'New Customers', data: data.customerRegistrations, backgroundColor: colors.cyan, borderRadius: 4 },
                        { label: 'New Photographers', data: data.photographerRegistrations, backgroundColor: colors.purple, borderRadius: 4 }
                    ]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });

            // 6. Platform Fee Chart: Incurred vs Outstanding
            destroy('fee');
            charts.fee = new Chart(document.getElementById('feeChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [
                        { label: 'Fees Accrued (10%)', data: data.platformFeeValues, backgroundColor: colors.blue, borderRadius: 4 },
                        { label: 'Outstanding Balance', data: data.platformFeeUnpaidValues, backgroundColor: colors.red, borderRadius: 4 }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { ticks: { callback: moneyFormat } } }
                }
            });

        } catch (e) {
            console.error('Analytics load error:', e);
            error.classList.remove('d-none');
        } finally {
            loading.classList.add('d-none');
        }
    }

    // Range Filter Handlers
    document.querySelectorAll('[data-range]').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelectorAll('[data-range]').forEach(item => item.classList.remove('active'));
            button.classList.add('active');
            const end = new Date();
            const start = new Date();
            start.setDate(end.getDate() - Number(button.dataset.range) + 1);
            loadAnalytics('?startDate=' + start.toISOString().slice(0,10) + '&endDate=' + end.toISOString().slice(0,10));
        });
    });

    document.getElementById('analyticsFilter')?.addEventListener('submit', event => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const start = form.get('startDate');
        const end = form.get('endDate');
        if (start && end) {
            document.querySelectorAll('[data-range]').forEach(item => item.classList.remove('active'));
            loadAnalytics('?startDate=' + start + '&endDate=' + end);
        }
    });

    loadAnalytics();
})();
</script>
