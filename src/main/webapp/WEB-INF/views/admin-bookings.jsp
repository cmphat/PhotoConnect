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
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main class="pc-page pc-page-compact">
        <div class="editorial-container pc-container-admin">

            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-page-header pc-stack-mobile">
                <div>
                    <span class="section-index">Administration (Read-Only)</span>
                    <h1 class="editorial-title">Booking Monitoring</h1>
                </div>
                <div class="pc-page-meta">
                    Total In View: ${fn:length(bookings)}
                </div>
            </div>

            <!-- Status Filter Tabs -->
            <div class="status-tabs">
                <a href="${pageContext.request.contextPath}/admin/bookings?status=ALL" class="status-tab ${selectedStatus == 'ALL' ? 'active' : ''}">All Bookings</a>
                <c:forEach var="st" items="${statuses}">
                    <a href="${pageContext.request.contextPath}/admin/bookings?status=${st.name()}" class="status-tab ${selectedStatus == st.name() ? 'active' : ''}">${st.name()}</a>
                </c:forEach>
            </div>

            <c:choose>
                <c:when test="${empty bookings}">
                    <div class="pc-empty-state">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No bookings found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">There are no shoot bookings matching the "${selectedStatus}" status filter.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-table-shell" tabindex="0" aria-label="Bookings table">
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
