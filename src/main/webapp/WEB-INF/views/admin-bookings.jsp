<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Booking Monitoring – Admin – PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .admin-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.95rem;
        }
        .admin-table th {
            text-align: left;
            padding: 1rem;
            border-bottom: 2px solid var(--border-dark);
            color: var(--text-muted);
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            font-size: 0.8rem;
        }
        .admin-table td {
            padding: 1.25rem 1rem;
            border-bottom: 1px solid var(--border-dark);
            vertical-align: middle;
        }
        .admin-table tbody tr:hover {
            background-color: var(--bg-dark-secondary);
        }
        .status-tabs {
            display: flex;
            gap: 0.75rem;
            margin-bottom: 2rem;
            flex-wrap: wrap;
        }
        .status-tab {
            padding: 0.4rem 0.9rem;
            border: 1px solid var(--border-dark);
            color: var(--text-muted);
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            text-decoration: none;
            transition: all var(--transition-fast);
        }
        .status-tab.active {
            background-color: var(--text-on-dark);
            color: var(--bg-dark);
            border-color: var(--text-on-dark);
            font-weight: 600;
        }
        .badge {
            display: inline-block;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.2rem 0.5rem;
            border-radius: 2px;
        }
        .badge-pending { background-color: rgba(245, 158, 11, 0.15); color: #f59e0b; }
        .badge-accepted { background-color: rgba(96, 165, 250, 0.15); color: #60a5fa; }
        .badge-completed { background-color: rgba(52, 211, 153, 0.15); color: #34d399; }
        .badge-cancelled { background-color: rgba(156, 163, 175, 0.15); color: #9ca3af; }
        .badge-rejected { background-color: rgba(239, 68, 68, 0.15); color: #ef4444; }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 0 6rem; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1200px; margin: 0 auto; padding: 0 1.5rem;">
            
            <jsp:include page="fragments/admin-navbar.jsp" />

            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <div>
                    <span class="section-index">Administration (Read-Only)</span>
                    <h1 class="editorial-title" style="font-size: 2.2rem; margin: 0.25rem 0 0;">Booking Monitoring</h1>
                </div>
                <div style="color: var(--text-muted); font-size: 0.85rem;">
                    Total In View: ${fn:length(bookings)}
                </div>
            </div>

            <!-- Status Filter Tabs -->
            <div class="status-tabs">
                <a href="/admin/bookings?status=ALL" class="status-tab ${selectedStatus == 'ALL' ? 'active' : ''}">All Bookings</a>
                <c:forEach var="st" items="${statuses}">
                    <a href="/admin/bookings?status=${st.name()}" class="status-tab ${selectedStatus == st.name() ? 'active' : ''}">${st.name()}</a>
                </c:forEach>
            </div>

            <c:choose>
                <c:when test="${empty bookings}">
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border-dark); background-color: var(--bg-dark-secondary);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No bookings found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">There are no shoot bookings matching the "${selectedStatus}" status filter.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="overflow-x: auto; border: 1px solid var(--border-dark);">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Customer</th>
                                    <th>Photographer</th>
                                    <th>Shoot Date & Time</th>
                                    <th>Location</th>
                                    <th>Agreed Price</th>
                                    <th>Status</th>
                                    <th>Created</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="b" items="${bookings}">
                                    <tr>
                                        <td style="color: var(--text-muted);">#<c:out value="${b.id}"/></td>
                                        <td>
                                            <div style="font-weight: 500;"><c:out value="${b.customer.fullName}"/></div>
                                            <div style="color: var(--text-muted); font-size: 0.85rem;"><c:out value="${b.customer.email}"/></div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500;"><c:out value="${b.photographerProfile.displayName}"/></div>
                                            <div style="color: var(--text-muted); font-size: 0.85rem;"><c:out value="${b.photographerProfile.user.email}"/></div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500;">
                                                <fmt:parseDate value="${b.bookingDate}" pattern="yyyy-MM-dd" var="parsedShootDate" type="date" />
                                                <fmt:formatDate value="${parsedShootDate}" pattern="MMM d, yyyy" />
                                            </div>
                                            <div style="color: var(--text-muted); font-size: 0.85rem;"><c:out value="${b.bookingTime}"/></div>
                                        </td>
                                        <td><c:out value="${b.location}"/></td>
                                        <td style="font-weight: 500;">
                                            <fmt:formatNumber value="${b.agreedPrice}" pattern="#,##0" /> VND
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${b.status == 'PENDING'}">
                                                    <span class="badge badge-pending">PENDING</span>
                                                </c:when>
                                                <c:when test="${b.status == 'ACCEPTED'}">
                                                    <span class="badge badge-accepted">ACCEPTED</span>
                                                </c:when>
                                                <c:when test="${b.status == 'COMPLETED'}">
                                                    <span class="badge badge-completed">COMPLETED</span>
                                                </c:when>
                                                <c:when test="${b.status == 'CANCELLED'}">
                                                    <span class="badge badge-cancelled">CANCELLED</span>
                                                </c:when>
                                                <c:when test="${b.status == 'REJECTED'}">
                                                    <span class="badge badge-rejected">REJECTED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge"><c:out value="${b.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="color: var(--text-muted); font-size: 0.85rem;">
                                            <c:if test="${not empty b.createdAt}">
                                                <fmt:parseDate value="${fn:substring(b.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedCreatedDate" type="date" />
                                                <fmt:formatDate value="${parsedCreatedDate}" pattern="MMM d, yyyy" />
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>
</body>
</html>
