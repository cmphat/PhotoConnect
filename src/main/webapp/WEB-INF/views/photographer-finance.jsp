<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Platform Fees &amp; Revenue — PhotoConnect Studio</title>
    <meta name="description" content="Track booking income, 10% platform commission, and fee settlement history for Photographers." />
    <style>
        .finance-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: 1.5rem;
            box-shadow: var(--shadow-subtle);
            height: 100%;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .finance-card:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-medium);
        }
        .finance-stat-label {
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: var(--muted);
            font-weight: 600;
            margin-bottom: 0.4rem;
        }
        .finance-stat-value {
            font-size: 1.75rem;
            font-weight: 700;
            font-family: var(--font-primary);
            line-height: 1.1;
        }
        .fee-badge {
            display: inline-block;
            padding: 0.25rem 0.65rem;
            border-radius: 999px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
        }
        .fee-badge-paid { background: rgba(46, 125, 50, 0.15); color: #2e7d32; }
        .fee-badge-unpaid { background: rgba(198, 40, 40, 0.15); color: #c62828; }
        .fee-badge-partial { background: rgba(239, 108, 0, 0.15); color: #ef6c00; }
        .fee-badge-cancelled { background: rgba(117, 117, 117, 0.15); color: #757575; }
    </style>
</head>
<main class="container py-4 my-3">
    <!-- Header -->
    <div class="row align-items-center mb-4 g-3">
        <div class="col-12 col-md-8">
            <div class="text-uppercase text-primary fw-bold small mb-1" style="letter-spacing: 0.1em;">STUDIO FINANCE</div>
            <h1 class="h2 fw-bold mb-1">Platform Fees &amp; Revenue</h1>
            <p class="text-muted mb-0">Track booking income, 10% platform commission, and fee settlement history.</p>
        </div>
        <div class="col-12 col-md-4 text-md-end">
            <a href="${pageContext.request.contextPath}/photographer/dashboard" class="btn btn-outline-secondary btn-sm">
                &larr; Back to Studio
            </a>
        </div>
    </div>

    <!-- Flash Alerts -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <strong>Success!</strong> <c:out value="${successMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <strong>Error!</strong> <c:out value="${errorMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- KPI Metric Cards -->
    <div class="row g-3 mb-4">
        <div class="col-12 col-sm-6 col-lg-3">
            <div class="finance-card">
                <div class="finance-stat-label">Total Booking Revenue</div>
                <div class="finance-stat-value text-primary"><c:out value="${summary.formattedGrossEarnings}"/></div>
                <div class="small text-muted mt-2">Gross value of all completed photo shoots</div>
            </div>
        </div>
        <div class="col-12 col-sm-6 col-lg-3">
            <div class="finance-card">
                <div class="finance-stat-label">Platform Fees (<c:out value="${summary.currentFeeRatePercent}"/>)</div>
                <div class="finance-stat-value text-dark"><c:out value="${summary.formattedFeesIncurred}"/></div>
                <div class="small text-muted mt-2">10% commission accrued on completed shoots</div>
            </div>
        </div>
        <div class="col-12 col-sm-6 col-lg-3">
            <div class="finance-card">
                <div class="finance-stat-label">Commissions Paid</div>
                <div class="finance-stat-value text-success"><c:out value="${summary.formattedFeesPaid}"/></div>
                <div class="small text-muted mt-2">Settled to PhotoConnect platform</div>
            </div>
        </div>
        <div class="col-12 col-sm-6 col-lg-3">
            <div class="finance-card border-warning">
                <div class="finance-stat-label">Outstanding Balance</div>
                <div class="finance-stat-value text-danger"><c:out value="${summary.formattedFeesOutstanding}"/></div>
                <div class="small text-muted mt-2">Pending platform commission to be settled</div>
            </div>
        </div>
    </div>

    <!-- Notice / Policy Banner -->
    <div class="alert alert-info border-0 shadow-sm d-flex align-items-center mb-4">
        <div class="me-3 fs-3 text-info">ℹ</div>
        <div>
            <strong>PhotoConnect Commission Policy:</strong>
            Default platform fee is <strong>10%</strong> on fulfilled booking value.
            The 30% initial deposit from the client is escrow protection held securely until project delivery, not platform fee.
            Your projected net earnings after platform commission: <strong><c:out value="${summary.formattedNetEarningsExpected}"/></strong>.
        </div>
    </div>

    <!-- Fee Ledger Table Card -->
    <div class="card border shadow-sm">
        <div class="card-header bg-transparent border-bottom py-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
            <h2 class="h5 fw-bold mb-0">Booking Platform Fee Settlement History</h2>
            <span class="badge bg-secondary"><c:out value="${summary.fees != null ? summary.fees.size() : 0}"/> transactions</span>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th scope="col" style="width: 80px;">Fee ID</th>
                    <th scope="col">Booking</th>
                    <th scope="col">Booking Value</th>
                    <th scope="col">Rate</th>
                    <th scope="col">Fee Incurred</th>
                    <th scope="col">Paid</th>
                    <th scope="col">Balance</th>
                    <th scope="col">Status</th>
                    <th scope="col">Recorded Date</th>
                    <th scope="col" class="text-end" style="min-width: 140px;">Action</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty summary.fees}">
                        <tr>
                            <td colspan="10" class="text-center py-5 text-muted">
                                <div class="fs-4 mb-2">✦</div>
                                <p class="mb-0">No platform fees accrued yet.</p>
                                <small>Fees will be automatically ledgered once a shoot is completed.</small>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${summary.fees}" var="fee">
                            <tr>
                                <td class="fw-bold">#<c:out value="${fee.id}"/></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/photographer/bookings/${fee.bookingId}" class="text-decoration-none fw-semibold">
                                        Booking #<c:out value="${fee.bookingId}"/>
                                    </a>
                                </td>
                                <td><c:out value="${fee.formattedBookingAmount}"/></td>
                                <td><span class="badge bg-light text-dark border"><c:out value="${fee.feeRatePercent}"/></span></td>
                                <td class="fw-bold"><c:out value="${fee.formattedFeeAmount}"/></td>
                                <td class="text-success"><c:out value="${fee.formattedPaidAmount}"/></td>
                                <td class="text-danger fw-bold"><c:out value="${fee.formattedOutstandingAmount}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${fee.status == 'PAID'}">
                                            <span class="fee-badge fee-badge-paid">Paid</span>
                                        </c:when>
                                        <c:when test="${fee.status == 'PARTIALLY_PAID'}">
                                            <span class="fee-badge fee-badge-partial">Partially Paid</span>
                                        </c:when>
                                        <c:when test="${fee.status == 'UNPAID'}">
                                            <span class="fee-badge fee-badge-unpaid">Unpaid</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="fee-badge fee-badge-cancelled"><c:out value="${fee.status}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="small text-muted"><c:out value="${fee.formattedCreatedAt}"/></td>
                                <td class="text-end" style="white-space: nowrap;">
                                    <c:if test="${fee.status == 'UNPAID' || fee.status == 'PARTIALLY_PAID'}">
                                        <form action="${pageContext.request.contextPath}/photographer/platform-fees/${fee.id}/pay" method="post" class="d-inline">
                                            <%@ include file="fragments/csrf-input.jsp" %>
                                            <button type="submit" class="btn btn-sm btn-primary" onclick="return confirm('Confirm settlement of this platform fee via Demo Payment Gateway?');">
                                                Pay Fee &rarr;
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${fee.status == 'PAID'}">
                                        <span class="text-success small fw-semibold">✓ Settled</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</main>
