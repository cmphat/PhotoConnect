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
    <title>Photographer Management – Admin – PhotoConnect</title>
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
            gap: 1rem;
            margin-bottom: 2rem;
            flex-wrap: wrap;
        }
        .status-tab {
            padding: 0.5rem 1rem;
            border: 1px solid var(--border-dark);
            color: var(--text-muted);
            font-size: 0.85rem;
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
        .badge-approved { background-color: rgba(52, 211, 153, 0.15); color: #34d399; }
        .badge-rejected { background-color: rgba(239, 68, 68, 0.15); color: #ef4444; }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 0 6rem; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1200px; margin: 0 auto; padding: 0 1.5rem;">
            
            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-stack-mobile" style="display: flex; justify-content: space-between; align-items: baseline; gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <div>
                    <span class="section-index">Administration</span>
                    <h1 class="editorial-title" style="font-size: 2.2rem; margin: 0.25rem 0 0;">Photographer Management</h1>
                </div>
                <div style="color: var(--text-muted); font-size: 0.85rem;">
                    Showing: ${currentStatus == null ? 'PENDING' : currentStatus} (${fn:length(applications)})
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

            <!-- Status Filter Tabs -->
            <div class="status-tabs">
                <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="status-tab ${currentStatus == 'PENDING' || empty currentStatus ? 'active' : ''}">Pending Queue</a>
                <a href="${pageContext.request.contextPath}/admin/photographers?status=APPROVED" class="status-tab ${currentStatus == 'APPROVED' ? 'active' : ''}">Approved</a>
                <a href="${pageContext.request.contextPath}/admin/photographers?status=REJECTED" class="status-tab ${currentStatus == 'REJECTED' ? 'active' : ''}">Rejected</a>
                <a href="${pageContext.request.contextPath}/admin/photographers?status=ALL" class="status-tab ${currentStatus == 'ALL' ? 'active' : ''}">All Photographers</a>
            </div>

            <c:choose>
                <c:when test="${empty applications}">
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border-dark); background-color: var(--bg-dark-secondary);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No photographers found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">There are no photographers matching the "${currentStatus == null ? 'PENDING' : currentStatus}" verification status.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="overflow-x: auto; border: 1px solid var(--border-dark);">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Display Name</th>
                                    <th>User / Email</th>
                                    <th>City</th>
                                    <th>Experience</th>
                                    <th>Starting Rate</th>
                                    <th>Rating & Reviews</th>
                                    <th>Verification Status</th>
                                    <th>Submitted</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="app" items="${applications}">
                                    <tr>
                                        <td style="color: var(--text-muted);">#<c:out value="${app.id}"/></td>
                                        <td style="font-weight: 500;"><c:out value="${app.displayName}"/></td>
                                        <td>
                                            <div><c:out value="${app.user.fullName}"/></div>
                                            <div style="color: var(--text-muted); font-size: 0.85rem;"><c:out value="${app.user.email}"/></div>
                                        </td>
                                        <td><c:out value="${not empty app.city ? app.city : '—'}"/></td>
                                        <td><c:out value="${app.experienceYears}"/> yrs</td>
                                        <td><fmt:formatNumber value="${app.priceFrom}" pattern="#,##0" /> VND</td>
                                        <td>
                                            <span style="color: #f59e0b;">★ <fmt:formatNumber value="${app.averageRating}" pattern="0.0" /></span>
                                            <span style="color: var(--text-muted); font-size: 0.85rem;">(<c:out value="${app.reviewCount}"/>)</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${app.verificationStatus == 'PENDING'}">
                                                    <span class="badge badge-pending">PENDING</span>
                                                </c:when>
                                                <c:when test="${app.verificationStatus == 'APPROVED'}">
                                                    <span class="badge badge-approved">APPROVED</span>
                                                </c:when>
                                                <c:when test="${app.verificationStatus == 'REJECTED'}">
                                                    <span class="badge badge-rejected">REJECTED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge"><c:out value="${app.verificationStatus}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="color: var(--text-muted); font-size: 0.85rem;">
                                            <c:if test="${not empty app.createdAt}">
                                                <fmt:parseDate value="${fn:substring(app.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedAppDate" type="date" />
                                                <fmt:formatDate value="${parsedAppDate}" pattern="MMM d, yyyy" />
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${app.verificationStatus == 'PENDING'}">
                                                    <a href="${pageContext.request.contextPath}/admin/photographers/${app.id}" class="text-link" style="font-size: 0.85rem; font-weight: 500; color: #f59e0b;">Review & Decide</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/admin/photographers/${app.id}" class="text-link" style="font-size: 0.85rem; margin-right: 0.75rem;">Application</a>
                                                    <c:if test="${app.verificationStatus == 'APPROVED'}">
                                                        <a href="${pageContext.request.contextPath}/photographers/${app.id}" target="_blank" class="text-link" style="font-size: 0.85rem; color: var(--text-muted);">Marketplace ↗</a>
                                                    </c:if>
                                                </c:otherwise>
                                            </c:choose>
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
