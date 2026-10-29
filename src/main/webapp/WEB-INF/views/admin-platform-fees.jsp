<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Platform Fees &amp; Ledger — PhotoConnect Admin</title>
</head>

<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">

        <div class="pc-page-header pc-stack-mobile">
            <div>
                <span class="section-index">PLATFORM FINANCE</span>
                <h1 class="editorial-title">Photographer Platform Fees &amp; Ledger</h1>
            </div>
            <div class="pc-page-meta">
                Effective Commission Rate: <strong>${platformFeeRatePercent}%</strong> on completed bookings
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

        <!-- KPI Financial Cards -->
        <div class="row g-4 mb-4">
            <div class="col-12 col-md-3">
                <div class="admin-kpi-card accent-purple">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Completed GMV</span>
                        <span class="admin-kpi-icon">▣</span>
                    </div>
                    <div class="admin-kpi-value" style="font-size: 1.55rem; color: #8b5cf6;">
                        <fmt:formatNumber value="${completedRevenue}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Gross volume of fulfilled photo shoots</p>
                </div>
            </div>
            <div class="col-12 col-md-3">
                <div class="admin-kpi-card">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Platform Fees Accrued (${platformFeeRatePercent}%)</span>
                        <span class="admin-kpi-icon">％</span>
                    </div>
                    <div class="admin-kpi-value" style="font-size: 1.55rem; color: var(--admin-primary);">
                        <fmt:formatNumber value="${platformFeeIncurred}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Commission accrued on completed jobs</p>
                </div>
            </div>
            <div class="col-12 col-md-3">
                <div class="admin-kpi-card accent-success">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Platform Fees Collected</span>
                        <span class="admin-kpi-icon">✓</span>
                    </div>
                    <div class="admin-kpi-value" style="font-size: 1.55rem; color: #10b981;">
                        <fmt:formatNumber value="${platformFeeCollected}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Settled into platform treasury account</p>
                </div>
            </div>
            <div class="col-12 col-md-3">
                <div class="admin-kpi-card accent-danger">
                    <div class="admin-kpi-header">
                        <span class="admin-kpi-title">Outstanding Fee Balance</span>
                        <span class="admin-kpi-icon">!</span>
                    </div>
                    <div class="admin-kpi-value" style="font-size: 1.55rem; color: #ef4444;">
                        <fmt:formatNumber value="${platformFeeOutstanding}" pattern="#,##0"/> <small style="font-size: 0.85rem; font-weight: normal; color: var(--admin-text-muted);">VND</small>
                    </div>
                    <p class="admin-kpi-subtext">Receivables due from photographers</p>
                </div>
            </div>
        </div>

        <!-- Commission Rate Setting -->
        <div class="admin-card-container mb-4">
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
                <div>
                    <h4 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 4px;">Global Platform Fee Rate Configuration</h4>
                    <p style="font-size: 0.85rem; color: var(--admin-text-muted); margin: 0;">
                        Default standard is 10%. Modifications are audited and apply to bookings completed after this update.
                    </p>
                </div>
                <form action="${pageContext.request.contextPath}/admin/platform-fees/rate" method="post" class="d-flex align-items-center gap-2">
                    <c:if test="${not empty _csrf}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    </c:if>
                    <div class="input-group input-group-sm" style="width: 140px;">
                        <input type="number" step="0.5" min="0" max="30" name="ratePercent" class="form-control" value="${platformFeeRatePercent}" required>
                        <span class="input-group-text">%</span>
                    </div>
                    <button type="submit" class="btn btn-sm btn-primary">Save Rate</button>
                </form>
            </div>
        </div>

        <!-- Photographer Debt Ledger Table -->
        <div class="admin-card-container mb-4">
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-3">
                <div>
                    <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 4px;">Outstanding Balance by Photographer</h3>
                    <p style="font-size: 0.85rem; color: var(--admin-text-muted); margin: 0;">Aggregated directly from fulfilled photo shoots.</p>
                </div>
                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="exportFeeTableToCSV()">
                    Export CSV Summary
                </button>
            </div>

            <c:choose>
                <c:when test="${empty photographerFees}">
                    <div class="text-center py-5">
                        <div style="font-size: 2.5rem; color: var(--admin-text-muted); margin-bottom: 12px;">％</div>
                        <h4 style="font-weight: 700; margin-bottom: 8px;">No Platform Fees Accrued</h4>
                        <p style="color: var(--admin-text-muted); font-size: 0.9rem;">When bookings transition to COMPLETED, platform fees are automatically ledgered here.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-table-shell">
                        <table class="admin-modern-table" id="feeTable">
                            <thead>
                                <tr>
                                    <th>Profile ID</th>
                                    <th>Photographer</th>
                                    <th>Email</th>
                                    <th>Completed Shoots</th>
                                    <th>Gross GMV</th>
                                    <th>Platform Fee (${platformFeeRatePercent}%)</th>
                                    <th>Paid</th>
                                    <th>Outstanding</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${photographerFees}">
                                    <tr>
                                        <td><strong>#<c:out value="${p.profileId}"/></strong></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/photographers/${p.profileId}" style="color: var(--admin-primary); font-weight: 600; text-decoration: none;">
                                                <c:out value="${p.displayName}"/>
                                            </a>
                                        </td>
                                        <td><c:out value="${p.email}"/></td>
                                        <td style="font-weight: 600;"><c:out value="${p.completedBookingsCount}"/> shoots</td>
                                        <td style="font-weight: 700;">
                                            <fmt:formatNumber value="${p.totalRevenue}" pattern="#,##0"/> <small style="font-weight: normal; color: var(--admin-text-muted);">VND</small>
                                        </td>
                                        <td style="font-weight: 700; color: var(--admin-primary);">
                                            <fmt:formatNumber value="${p.feeDue}" pattern="#,##0"/> <small style="font-weight: normal; color: var(--admin-text-muted);">VND</small>
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${p.feePaid}" pattern="#,##0"/> VND
                                        </td>
                                        <td style="font-weight: 700; color: ${p.feeOutstanding > 0 ? '#ef4444' : '#10b981'};">
                                            <fmt:formatNumber value="${p.feeOutstanding}" pattern="#,##0"/> VND
                                        </td>
                                        <td>
                                            <span class="status-pill ${p.feeOutstanding > 0 ? 'warning' : 'success'}">
                                                ${p.feeOutstanding > 0 ? 'OUTSTANDING' : 'SETTLED'}
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Individual Booking Platform Fee Ledger -->
        <div class="admin-card-container">
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-3">
                <div>
                    <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 4px;">Individual Booking Platform Fee Ledger</h3>
                    <p style="font-size: 0.85rem; color: var(--admin-text-muted); margin: 0;">Auditable accounting record of each fee with rate locked at transaction time.</p>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <a href="${pageContext.request.contextPath}/admin/platform-fees/export" class="btn btn-sm btn-primary">
                        Download Full CSV
                    </a>
                </div>
            </div>

            <!-- Filter tabs -->
            <div class="d-flex gap-2 mb-3 flex-wrap">
                <a href="${pageContext.request.contextPath}/admin/platform-fees" class="btn btn-sm ${empty statusFilter ? 'btn-primary' : 'btn-outline-secondary'}">
                    All
                </a>
                <a href="${pageContext.request.contextPath}/admin/platform-fees?status=UNPAID" class="btn btn-sm ${statusFilter == 'UNPAID' ? 'btn-danger' : 'btn-outline-secondary'}">
                    Unpaid
                </a>
                <a href="${pageContext.request.contextPath}/admin/platform-fees?status=PARTIALLY_PAID" class="btn btn-sm ${statusFilter == 'PARTIALLY_PAID' ? 'btn-warning' : 'btn-outline-secondary'}">
                    Partially Paid
                </a>
                <a href="${pageContext.request.contextPath}/admin/platform-fees?status=PAID" class="btn btn-sm ${statusFilter == 'PAID' ? 'btn-success' : 'btn-outline-secondary'}">
                    Paid
                </a>
                <a href="${pageContext.request.contextPath}/admin/platform-fees?status=CANCELLED" class="btn btn-sm ${statusFilter == 'CANCELLED' ? 'btn-secondary' : 'btn-outline-secondary'}">
                    Cancelled
                </a>
            </div>

            <c:choose>
                <c:when test="${empty feeRecords}">
                    <div class="text-center py-4 text-muted">
                        <p class="mb-0">No platform fee records match the selected filter.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-table-shell">
                        <table class="admin-modern-table">
                            <thead>
                                <tr>
                                    <th>Fee ID</th>
                                    <th>Booking</th>
                                    <th>Photographer</th>
                                    <th>Booking Value</th>
                                    <th>Rate</th>
                                    <th>Fee Incurred</th>
                                    <th>Paid</th>
                                    <th>Remaining</th>
                                    <th>Status</th>
                                    <th>Recorded Date</th>
                                    <th>Tx Code</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="f" items="${feeRecords}">
                                    <tr>
                                        <td><strong>#<c:out value="${f.id}"/></strong></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/bookings" style="color: var(--admin-primary); font-weight: 600;">
                                                Booking #<c:out value="${f.bookingId}"/>
                                            </a>
                                        </td>
                                        <td><c:out value="${f.photographerName}"/></td>
                                        <td><c:out value="${f.formattedBookingAmount}"/></td>
                                        <td><span class="badge bg-light text-dark border"><c:out value="${f.feeRatePercent}"/></span></td>
                                        <td style="font-weight: 700; color: var(--admin-primary);"><c:out value="${f.formattedFeeAmount}"/></td>
                                        <td class="text-success"><c:out value="${f.formattedPaidAmount}"/></td>
                                        <td style="font-weight: 700; color: ${f.outstandingAmount > 0 ? '#ef4444' : '#10b981'};">
                                            <c:out value="${f.formattedOutstandingAmount}"/>
                                        </td>
                                        <td>
                                            <span class="status-pill ${f.status == 'PAID' ? 'success' : (f.status == 'CANCELLED' ? 'muted' : 'warning')}">
                                                <c:out value="${f.status}"/>
                                            </span>
                                        </td>
                                        <td class="small text-muted"><c:out value="${f.formattedCreatedAt}"/></td>
                                        <td class="small font-monospace"><c:out value="${f.transactionCode}" default="—"/></td>
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

<script>
function exportFeeTableToCSV() {
    const table = document.getElementById("feeTable");
    if (!table) return;
    let csv = [];
    for (let row of table.rows) {
        let cols = Array.from(row.cells).map(cell => '"' + cell.innerText.replace(/"/g, '""').trim() + '"');
        csv.push(cols.join(","));
    }
    const blob = new Blob(["\uFEFF" + csv.join("\n")], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "PhotoConnect_Platform_Fees_" + new Date().toISOString().slice(0, 10) + ".csv";
    link.click();
}
</script>
