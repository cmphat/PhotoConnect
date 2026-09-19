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
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main class="pc-page">
        <div class="editorial-container pc-container-narrow">
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/admin/photographers" class="text-link" style="color: var(--text-muted); font-size: 0.9rem;">← Back to Applications</a>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="pc-alert pc-alert-success" role="status">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty profile}">
                    <div class="pc-panel">
                        <div class="pc-page-header pc-stack-mobile">
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
                            <c:out value="${profile.bio}"/>
                        </div>

                        <c:if test="${profile.verificationStatus == 'PENDING'}">
                            <div class="pc-actions" style="border-top: 1px solid var(--border); padding-top: 3rem;">
                                <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/approve" method="post" style="margin: 0;">
                                    <button type="submit" class="btn btn-primary" onclick="return confirm('Approve this application?')">
                                        Approve Application
                                    </button>
                                </form>
                                <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/reject" method="post" style="margin: 0;">
                                    <button type="submit" class="btn btn-danger" onclick="return confirm('Reject this application?')">
                                        Reject
                                    </button>
                                </form>
                            </div>
                        </c:if>

                        <c:if test="${profile.verificationStatus != 'PENDING'}">
                            <div style="color: var(--text-muted); font-size: 0.9rem; border-top: 1px solid var(--border); padding-top: 2rem;">
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
