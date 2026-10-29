<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Photographer Directory — PhotoConnect Admin</title>
</head>

<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">

        <div class="pc-page-header pc-stack-mobile">
            <div>
                <span class="section-index">PARTNER MANAGEMENT</span>
                <h1 class="editorial-title">Photographer Directory</h1>
            </div>
            <div class="pc-page-meta">
                Status: ${currentStatus == null || currentStatus == 'ALL' ? 'All' : currentStatus} (${fn:length(applications)})
            </div>
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

        <!-- Status Filter Tabs -->
        <div class="status-tabs">
            <a href="${pageContext.request.contextPath}/admin/photographers?status=ALL" class="status-tab ${currentStatus == 'ALL' ? 'active' : ''}">All Photographers</a>
            <a href="${pageContext.request.contextPath}/admin/photographers?status=APPROVED" class="status-tab ${currentStatus == 'APPROVED' ? 'active' : ''}">Approved</a>
            <a href="${pageContext.request.contextPath}/admin/photographers?status=PENDING" class="status-tab ${currentStatus == 'PENDING' ? 'active' : ''}">Pending Review</a>
            <a href="${pageContext.request.contextPath}/admin/photographers?status=REJECTED" class="status-tab ${currentStatus == 'REJECTED' ? 'active' : ''}">Rejected</a>
        </div>

        <c:choose>
            <c:when test="${empty applications}">
                <div class="pc-empty-state">
                    <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No Photographers Found</h3>
                    <p style="color: var(--text-muted); margin-bottom: 0;">There are currently no photographer applications matching filter "${currentStatus == null || currentStatus == 'ALL' ? 'All' : currentStatus}".</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="admin-table-shell" tabindex="0" aria-label="Photographer applications table">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Display Name</th>
                                <th>User / Email</th>
                                <th>City</th>
                                <th>Experience</th>
                                <th>Starting Price</th>
                                <th>Rating</th>
                                <th>Verification</th>
                                <th>Submitted</th>
                                <th style="text-align: right; min-width: 140px;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="app" items="${applications}">
                                <tr>
                                    <td style="color: var(--text-muted);">#<c:out value="${app.id}"/></td>
                                    <td style="font-weight: 600;"><c:out value="${app.displayName}"/></td>
                                    <td>
                                        <div style="font-weight: 500;"><c:out value="${app.user.fullName}"/></div>
                                        <div style="color: var(--text-muted); font-size: 0.82rem;"><c:out value="${app.user.email}"/></div>
                                    </td>
                                    <td><c:out value="${not empty app.city ? app.city : '—'}"/></td>
                                    <td><c:out value="${app.experienceYears}"/> yrs</td>
                                    <td><fmt:formatNumber value="${app.priceFrom}" pattern="#,##0" /> VND</td>
                                    <td>
                                        <span style="color: #f59e0b; font-weight: 600;">★ <fmt:formatNumber value="${app.averageRating}" pattern="0.0" /></span>
                                        <span style="color: var(--text-muted); font-size: 0.8rem;">(<c:out value="${app.reviewCount}"/>)</span>
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
                                    <td style="color: var(--text-muted); font-size: 0.82rem; white-space: nowrap;">
                                        <c:if test="${not empty app.createdAt}">
                                            <fmt:parseDate value="${fn:substring(app.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedAppDate" type="date" />
                                            <fmt:formatDate value="${parsedAppDate}" pattern="MMM d, yyyy" />
                                        </c:if>
                                    </td>
                                    <td style="white-space: nowrap !important; min-width: 150px; text-align: right;">
                                        <c:choose>
                                            <c:when test="${app.verificationStatus == 'PENDING'}">
                                                <a href="${pageContext.request.contextPath}/admin/photographers/${app.id}" class="btn btn-sm btn-warning text-white" style="font-size: 0.8rem; font-weight: 600; padding: 4px 10px; border-radius: 6px;">Review</a>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="d-inline-flex gap-1 align-items-center justify-content-end">
                                                    <a href="${pageContext.request.contextPath}/admin/photographers/${app.id}" class="btn btn-sm btn-outline-secondary" style="font-size: 0.8rem; padding: 3px 8px; border-radius: 6px;">Details</a>
                                                    <c:if test="${app.verificationStatus == 'APPROVED'}">
                                                        <a href="${pageContext.request.contextPath}/photographers/${app.id}" target="_blank" class="btn btn-sm btn-outline-primary" style="font-size: 0.8rem; padding: 3px 8px; border-radius: 6px;" title="View public profile">Public ↗</a>
                                                    </c:if>
                                                </div>
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
