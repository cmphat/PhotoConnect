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
    <title>Application Detail – Admin – PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .detail-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 1rem;
            margin-bottom: 2rem;
        }
        .detail-table th {
            text-align: left;
            padding: 1.5rem 0;
            color: var(--text-muted);
            font-weight: 400;
            width: 200px;
            border-bottom: 1px solid var(--border-dark);
        }
        .detail-table td {
            padding: 1.5rem 0;
            font-weight: 500;
            border-bottom: 1px solid var(--border-dark);
        }
        .status-badge {
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.4rem 0.8rem;
            border: 1px solid currentColor;
            font-weight: 600;
        }
        .status-badge.PENDING { color: #f59e0b; }
        .status-badge.APPROVED { color: #10b981; }
        .status-badge.REJECTED { color: #ef4444; }
        .status-badge.SUSPENDED { color: #6b7280; }

        .btn-approve {
            background-color: #10b981;
            color: #fff;
            border: none;
            padding: 1rem 2rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        .btn-approve:hover { background-color: #059669; }

        .btn-reject {
            background-color: transparent;
            color: #ef4444;
            border: 1px solid #ef4444;
            padding: 1rem 2rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            cursor: pointer;
            transition: all 0.2s;
        }
        .btn-reject:hover { background-color: #ef4444; color: #fff; }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 800px;">
            <div style="margin-bottom: 2rem;">
                <a href="/admin/photographers" class="text-link" style="color: var(--text-muted); font-size: 0.9rem;">← Back to Applications</a>
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
                <c:when test="${not empty profile}">
                    <div style="border: 1px solid var(--border); padding: 3rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 3rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 2rem;">
                            <h1 class="editorial-title" style="margin-bottom: 0; font-size: 2.5rem;"><c:out value="${profile.displayName}"/></h1>
                            <span class="status-badge ${profile.verificationStatus}">
                                <c:out value="${profile.verificationStatus}"/>
                            </span>
                        </div>
                        
                        <table class="detail-table">
                            <tbody>
                                <tr>
                                    <th>Account Email</th>
                                    <td><c:out value="${profile.user.email}"/></td>
                                </tr>
                                <tr>
                                    <th>Full Name</th>
                                    <td><c:out value="${profile.user.fullName}"/></td>
                                </tr>
                                <tr>
                                    <th>City</th>
                                    <td><c:out value="${profile.city}"/></td>
                                </tr>
                                <tr>
                                    <th>Experience</th>
                                    <td><c:out value="${profile.experienceYears}"/> years</td>
                                </tr>
                                <tr>
                                    <th>Starting Rate</th>
                                    <td><fmt:formatNumber value="${profile.priceFrom}" pattern="#,##0" /> VND</td>
                                </tr>
                                <tr>
                                    <th>Submitted</th>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty profile.createdAt}">
                                                <fmt:parseDate value="${fn:substring(profile.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedProfileDate" type="date" />
                                                <fmt:formatDate value="${parsedProfileDate}" pattern="MMM d, yyyy" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </tbody>
                        </table>

                        <h2 class="editorial-heading" style="font-size: 1.25rem; margin-top: 3rem; margin-bottom: 1.5rem;">Bio</h2>
                        <div style="color: var(--text-muted); line-height: 1.7; margin-bottom: 4rem;">
                            <c:out value="${profile.bio}" escapeXml="false"/>
                        </div>

                        <c:if test="${profile.verificationStatus == 'PENDING'}">
                            <div style="display: flex; gap: 1rem; border-top: 1px solid var(--border-dark); padding-top: 3rem;">
                                <form action="/admin/photographers/${profile.id}/approve" method="post" style="margin: 0;">
                                    <button type="submit" class="btn-approve" onclick="return confirm('Approve this application?')">
                                        Approve Application
                                    </button>
                                </form>
                                <form action="/admin/photographers/${profile.id}/reject" method="post" style="margin: 0;">
                                    <button type="submit" class="btn-reject" onclick="return confirm('Reject this application?')">
                                        Reject
                                    </button>
                                </form>
                            </div>
                        </c:if>

                        <c:if test="${profile.verificationStatus != 'PENDING'}">
                            <div style="color: var(--text-muted); font-size: 0.9rem; border-top: 1px solid var(--border-dark); padding-top: 2rem;">
                                This application has already been processed (status: <strong><c:out value="${profile.verificationStatus}"/></strong>).
                            </div>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem; color: #ef4444;">Application not found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">The requested photographer profile does not exist.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</body>
</html>
