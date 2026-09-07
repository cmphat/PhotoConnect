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
    <title>Pending Applications – Admin – PhotoConnect</title>
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
            padding: 1.5rem 1rem;
            border-bottom: 1px solid var(--border);
            vertical-align: middle;
        }
        .admin-table tbody tr:hover {
            background-color: var(--bg-dark-secondary);
        }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1200px;">
            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <h1 class="editorial-title" style="font-size: 2rem;">Pending Photographer Applications</h1>
                <span style="color: var(--text-muted); font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.05em;">Admin Panel</span>
            </div>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(52, 211, 153, 0.2);">
                    ${successMessage}
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(239, 68, 68, 0.2);">
                    ${errorMessage}
                </div>
            </c:if>

            <c:choose>
                <c:when test="${empty applications}">
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem;">No pending applications</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">There are no new photographer applications to review at this time.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="overflow-x: auto;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Display Name</th>
                                    <th>Email</th>
                                    <th>City</th>
                                    <th>Experience</th>
                                    <th>Starting Rate</th>
                                    <th>Status</th>
                                    <th>Submitted</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="app" items="${applications}">
                                    <tr>
                                        <td style="color: var(--text-muted);">#<c:out value="${app.id}"/></td>
                                        <td style="font-weight: 500;"><c:out value="${app.displayName}"/></td>
                                        <td><c:out value="${app.user.email}"/></td>
                                        <td><c:out value="${app.city}"/></td>
                                        <td><c:out value="${app.experienceYears}"/> yrs</td>
                                        <td><fmt:formatNumber value="${app.priceFrom}" pattern="#,##0" /> VND</td>
                                        <td><span style="color: #f59e0b; font-weight: 500;"><c:out value="${app.verificationStatus}"/></span></td>
                                        <td style="color: var(--text-muted); font-size: 0.9rem;">
                                            <c:if test="${not empty app.createdAt}">
                                                <fmt:parseDate value="${fn:substring(app.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedAppDate" type="date" />
                                                <fmt:formatDate value="${parsedAppDate}" pattern="MMM d, yyyy" />
                                            </c:if>
                                        </td>
                                        <td>
                                            <a href="/admin/photographers/${app.id}" class="text-link" style="font-size: 0.9rem;">Review</a>
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
