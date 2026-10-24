<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<head>
    <title>Performance & Analytics — PhotoConnect</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>
    <style>
        .analytics-shell { padding: clamp(2rem, 5vw, 4rem) 0 5rem; }
        .analytics-header { display: flex; justify-content: space-between; align-items: flex-end; gap: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border); margin-bottom: 2.5rem; }
        .analytics-kicker { color: var(--accent); font-size: .8rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
        .analytics-title { margin: .45rem 0 .35rem; font-family: var(--font-primary); font-size: clamp(2rem, 4vw, 3rem); letter-spacing: -.03em; }
        .analytics-subtitle { margin: 0; color: var(--muted); max-width: 42rem; }
        .analytics-header-actions { display: flex; flex-wrap: wrap; gap: .75rem; }

        .analytics-kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; margin-bottom: 2.5rem; }
        .analytics-kpi-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 1.35rem 1.5rem; box-shadow: var(--shadow-subtle); display: flex; flex-direction: column; justify-content: space-between; }
        .analytics-kpi-card.highlight { border-color: var(--accent); border-left-width: 4px; }
        .analytics-kpi-label { color: var(--muted); font-size: .78rem; text-transform: uppercase; letter-spacing: .08em; font-weight: 600; margin-bottom: .5rem; }
        .analytics-kpi-value { font-size: 1.75rem; font-weight: 700; color: var(--text); line-height: 1.2; }
        .analytics-kpi-desc { color: var(--muted); font-size: .82rem; margin-top: .4rem; }

        .analytics-charts-grid { display: grid; grid-template-columns: minmax(0, 1.8fr) minmax(0, 1.2fr); gap: 1.75rem; margin-bottom: 2.5rem; }
        .analytics-chart-panel { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 1.5rem; box-shadow: var(--shadow-subtle); min-width: 0; }
        .analytics-panel-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 1.25rem; border-bottom: 1px solid var(--border); padding-bottom: .85rem; }
        .analytics-panel-head h2 { margin: 0; font-family: var(--font-primary); font-size: 1.35rem; letter-spacing: -.02em; }
        .chart-container { position: relative; width: 100%; min-height: 280px; max-height: 340px; }

        .analytics-tables-grid { display: grid; grid-template-columns: minmax(0, 1.6fr) minmax(0, 1.4fr); gap: 1.75rem; }
        .analytics-table-wrap { overflow-x: auto; }
        .analytics-table { width: 100%; border-collapse: collapse; font-size: .92rem; }
        .analytics-table th { text-align: left; padding: .75rem 1rem; color: var(--muted); font-size: .78rem; text-transform: uppercase; letter-spacing: .06em; border-bottom: 1px solid var(--border); }
        .analytics-table td { padding: .85rem 1rem; border-bottom: 1px solid var(--border); color: var(--text); }
        .analytics-table tr:last-child td { border-bottom: none; }

        .analytics-notif-list { display: grid; gap: .75rem; }
        .analytics-notif-item { display: flex; gap: 1rem; align-items: flex-start; padding: .85rem; border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--surface-subtle); }
        .analytics-notif-badge { padding: .25rem .5rem; font-size: .72rem; font-weight: 600; border-radius: 4px; background: var(--border); color: var(--text); }
        .analytics-notif-content h4 { margin: 0 0 .25rem; font-size: .92rem; font-weight: 600; }
        .analytics-notif-content p { margin: 0; font-size: .84rem; color: var(--muted); }

        @media (max-width: 1024px) {
            .analytics-charts-grid, .analytics-tables-grid { grid-template-columns: 1fr; }
        }
        @media (max-width: 768px) {
            .analytics-header { flex-direction: column; align-items: flex-start; }
            .analytics-kpi-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>

<main class="analytics-shell container" id="analyticsMain">
    <!-- Header -->
    <header class="analytics-header">
        <div>
            <span class="analytics-kicker">Photographer Studio</span>
            <h1 class="analytics-title">Performance & Analytics</h1>
            <p class="analytics-subtitle">
                Track your financial metrics, booking volumes, package popularity, and operational trends with precision.
            </p>
        </div>
        <div class="analytics-header-actions">
            <a href="${pageContext.request.contextPath}/photographer/dashboard" class="pc-btn pc-btn-secondary">Studio Dashboard</a>
            <a href="${pageContext.request.contextPath}/photographer/packages" class="pc-btn pc-btn-primary">Manage Packages</a>
        </div>
    </header>

    <!-- Financial Accounting KPI Cards -->
    <section class="analytics-kpi-grid" aria-label="Key Performance Indicators">
        <article class="analytics-kpi-card highlight">
            <span class="analytics-kpi-label">Completed Revenue</span>
            <span class="analytics-kpi-value text-success">
                <fmt:formatNumber value="${analytics.completedRevenue}" pattern="#,##0"/> <small style="font-size: .9rem;">VND</small>
            </span>
            <span class="analytics-kpi-desc">Realized revenue from successfully completed sessions.</span>
        </article>

        <article class="analytics-kpi-card">
            <span class="analytics-kpi-label">Confirmed Deposits</span>
            <span class="analytics-kpi-value text-primary">
                <fmt:formatNumber value="${analytics.confirmedDeposits}" pattern="#,##0"/> <small style="font-size: .9rem;">VND</small>
            </span>
            <span class="analytics-kpi-desc">Paid and confirmed customer booking deposits.</span>
        </article>

        <article class="analytics-kpi-card">
            <span class="analytics-kpi-label">Gross Booking Value</span>
            <span class="analytics-kpi-value">
                <fmt:formatNumber value="${analytics.grossBookingValue}" pattern="#,##0"/> <small style="font-size: .9rem;">VND</small>
            </span>
            <span class="analytics-kpi-desc">Total contracted value of all non-cancelled bookings.</span>
        </article>

        <article class="analytics-kpi-card">
            <span class="analytics-kpi-label">Total Bookings</span>
            <span class="analytics-kpi-value">${analytics.totalBookings}</span>
            <span class="analytics-kpi-desc">${analytics.pendingRequests} pending, ${analytics.upcomingSessions} upcoming shoots.</span>
        </article>

        <article class="analytics-kpi-card">
            <span class="analytics-kpi-label">Client Rating</span>
            <span class="analytics-kpi-value">
                <fmt:formatNumber value="${analytics.averageRating}" minFractionDigits="1" maxFractionDigits="1"/> ★
            </span>
            <span class="analytics-kpi-desc">Based on ${analytics.reviewCount} verified client reviews.</span>
        </article>
    </section>

    <!-- Charts Row -->
    <section class="analytics-charts-grid" aria-label="Visual Trends">
        <article class="analytics-chart-panel">
            <div class="analytics-panel-head">
                <h2>6-Month Revenue & Booking Volume</h2>
                <span class="text-muted" style="font-size: .82rem;">Monthly Breakdown</span>
            </div>
            <div class="chart-container">
                <canvas id="monthlyTrendChart" aria-label="Monthly trend chart" role="img"></canvas>
            </div>
        </article>

        <article class="analytics-chart-panel">
            <div class="analytics-panel-head">
                <h2>Booking Status Breakdown</h2>
                <span class="text-muted" style="font-size: .82rem;">Lifecycle Distribution</span>
            </div>
            <div class="chart-container">
                <canvas id="statusDistributionChart" aria-label="Status distribution chart" role="img"></canvas>
            </div>
        </article>
    </section>

    <!-- Tables & Details Grid -->
    <section class="analytics-tables-grid" aria-label="Service Packages and Activity">
        <!-- Popular Packages -->
        <article class="analytics-chart-panel">
            <div class="analytics-panel-head">
                <h2>Top Performing Packages</h2>
                <a href="${pageContext.request.contextPath}/photographer/packages" class="text-decoration-none" style="font-size: .85rem; color: var(--accent);">Edit Packages →</a>
            </div>
            <div class="analytics-table-wrap">
                <c:choose>
                    <c:when test="${not empty analytics.popularPackages}">
                        <table class="analytics-table">
                            <thead>
                                <tr>
                                    <th>Package Name</th>
                                    <th style="text-align: center;">Bookings</th>
                                    <th style="text-align: right;">Contracted Value</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="pkg" items="${analytics.popularPackages}">
                                    <tr>
                                        <td><strong><c:out value="${pkg.packageName}"/></strong></td>
                                        <td style="text-align: center;">${pkg.bookingCount}</td>
                                        <td style="text-align: right; font-weight: 600;">
                                            <fmt:formatNumber value="${pkg.totalRevenue}" pattern="#,##0"/> VND
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        <div class="p-4 text-center text-muted" style="background: var(--surface-subtle); border-radius: var(--radius-sm);">
                            No service package bookings recorded yet.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </article>

        <!-- Recent Activity / Notifications -->
        <article class="analytics-chart-panel">
            <div class="analytics-panel-head">
                <h2>Recent Notifications</h2>
                <a href="${pageContext.request.contextPath}/notifications" class="text-decoration-none" style="font-size: .85rem; color: var(--accent);">View All →</a>
            </div>
            <div class="analytics-notif-list">
                <c:choose>
                    <c:when test="${not empty analytics.recentNotifications}">
                        <c:forEach var="notif" items="${analytics.recentNotifications}">
                            <div class="analytics-notif-item">
                                <span class="analytics-notif-badge"><c:out value="${notif.typeName()}"/></span>
                                <div class="analytics-notif-content">
                                    <h4><c:out value="${notif.title()}"/></h4>
                                    <p><c:out value="${notif.message()}"/></p>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="p-4 text-center text-muted" style="background: var(--surface-subtle); border-radius: var(--radius-sm);">
                            No recent notifications.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </article>
    </section>
</main>

<!-- Chart.js Initialization -->
<script>
    document.addEventListener("DOMContentLoaded", function () {
        // Prepare Monthly Trend Data
        var trendLabels = [
            <c:forEach var="t" items="${analytics.monthlyTrends}" varStatus="status">
                "${t.monthLabel}"<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];
        var trendBookings = [
            <c:forEach var="t" items="${analytics.monthlyTrends}" varStatus="status">
                ${t.bookingCount}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];
        var trendGross = [
            <c:forEach var="t" items="${analytics.monthlyTrends}" varStatus="status">
                ${t.grossRevenue}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];
        var trendCompleted = [
            <c:forEach var="t" items="${analytics.monthlyTrends}" varStatus="status">
                ${t.completedRevenue}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        var trendCtx = document.getElementById('monthlyTrendChart');
        if (trendCtx && trendLabels.length > 0) {
            new Chart(trendCtx, {
                type: 'bar',
                data: {
                    labels: trendLabels,
                    datasets: [
                        {
                            type: 'line',
                            label: 'Completed Revenue (VND)',
                            data: trendCompleted,
                            borderColor: '#10B981',
                            backgroundColor: '#10B981',
                            borderWidth: 2,
                            tension: 0.25,
                            yAxisID: 'y1'
                        },
                        {
                            type: 'bar',
                            label: 'Gross Value (VND)',
                            data: trendGross,
                            backgroundColor: 'rgba(37, 99, 235, 0.65)',
                            borderColor: '#2563EB',
                            borderWidth: 1,
                            yAxisID: 'y1'
                        },
                        {
                            type: 'line',
                            label: 'Bookings Volume',
                            data: trendBookings,
                            borderColor: '#F59E0B',
                            backgroundColor: '#F59E0B',
                            borderWidth: 2,
                            borderDash: [4, 4],
                            yAxisID: 'y2'
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: { mode: 'index', intersect: false },
                    scales: {
                        y1: {
                            type: 'linear',
                            display: true,
                            position: 'left',
                            ticks: {
                                callback: function(value) {
                                    return (value >= 1000000 ? (value / 1000000).toFixed(1) + 'M' : (value / 1000).toFixed(0) + 'k');
                                }
                            }
                        },
                        y2: {
                            type: 'linear',
                            display: true,
                            position: 'right',
                            grid: { drawOnChartArea: false },
                            ticks: { precision: 0 }
                        }
                    }
                }
            });
        }

        // Prepare Status Distribution Data
        var statusMap = {
            <c:forEach var="entry" items="${analytics.statusDistribution}" varStatus="status">
                "${entry.key}": ${entry.value}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        var statusLabels = Object.keys(statusMap);
        var statusValues = Object.values(statusMap);
        var statusColors = [
            '#2563EB', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#06B6D4', '#64748B', '#EC4899'
        ];

        var distCtx = document.getElementById('statusDistributionChart');
        if (distCtx && statusLabels.length > 0) {
            new Chart(distCtx, {
                type: 'doughnut',
                data: {
                    labels: statusLabels,
                    datasets: [{
                        data: statusValues,
                        backgroundColor: statusColors.slice(0, statusLabels.length),
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom' }
                    }
                }
            });
        }
    });
</script>
