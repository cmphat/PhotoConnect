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
    <title>Review Moderation – Admin – PhotoConnect</title>
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
                    <span class="section-index">Administration</span>
                    <h1 class="editorial-title">Review Moderation</h1>
                </div>
                <div class="pc-page-meta">
                    Total Reviews: ${allCount}
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

            <!-- Status Tabs -->
            <div class="status-tabs">
                <a href="${pageContext.request.contextPath}/admin/reviews?status=ALL"
                   class="status-tab ${activeStatus == 'ALL' ? 'active' : ''}">
                    All Reviews (${allCount})
                </a>
                <a href="${pageContext.request.contextPath}/admin/reviews?status=VISIBLE"
                   class="status-tab ${activeStatus == 'VISIBLE' ? 'active' : ''}">
                    Visible (${visibleCount})
                </a>
                <a href="${pageContext.request.contextPath}/admin/reviews?status=HIDDEN"
                   class="status-tab ${activeStatus == 'HIDDEN' ? 'active' : ''}">
                    Hidden (${hiddenCount})
                </a>
            </div>

            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="pc-empty-state">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No reviews found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">No reviews match the selected filter criteria.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-table-shell" tabindex="0" aria-label="Reviews table">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Review #</th>
                                    <th>Status</th>
                                    <th>Booking</th>
                                    <th>Customer</th>
                                    <th>Photographer</th>
                                    <th>Rating</th>
                                    <th>Comment</th>
                                    <th>Submitted</th>
                                    <th style="text-align: right;">Moderation</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="r" items="${reviews}">
                                    <tr>
                                        <td style="color: var(--text-muted);">#<c:out value="${r.id}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${r.status == 'HIDDEN'}">
                                                    <span class="badge badge-hidden">HIDDEN</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-visible">VISIBLE</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span style="font-size: 0.85rem; color: var(--text-muted);">Booking #<c:out value="${r.booking.id}"/></span>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500;"><c:out value="${r.customer.fullName}"/></div>
                                            <div style="color: var(--text-muted); font-size: 0.85rem;"><c:out value="${r.customer.email}"/></div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500;"><c:out value="${r.photographerProfile.displayName}"/></div>
                                        </td>
                                        <td>
                                            <div class="rating-stars">
                                                <c:forEach begin="1" end="${r.rating}">★</c:forEach><c:forEach begin="${r.rating + 1}" end="5">☆</c:forEach>
                                                <span style="color: var(--text-muted); font-size: 0.8rem; margin-left: 0.25rem;">(${r.rating}/5)</span>
                                            </div>
                                        </td>
                                        <td style="max-width: 280px;">
                                            <c:choose>
                                                <c:when test="${not empty r.comment}">
                                                    <span style="font-size: 0.9rem; color: var(--text-on-dark);"><c:out value="${r.comment}"/></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: var(--text-muted); font-style: italic; font-size: 0.85rem;">No written comment</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="color: var(--text-muted); font-size: 0.85rem; white-space: nowrap;">
                                            <c:if test="${not empty r.createdAt}">
                                                <fmt:parseDate value="${fn:substring(r.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedReviewDate" type="date" />
                                                <fmt:formatDate value="${parsedReviewDate}" pattern="MMM d, yyyy" />
                                            </c:if>
                                        </td>
                                        <td style="text-align: right; white-space: nowrap;">
                                            <c:choose>
                                                <c:when test="${r.status == 'HIDDEN'}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews/${r.id}/unhide" style="display: inline;">
                                                        <input type="hidden" name="currentStatus" value="${activeStatus}" />
                                                        <button type="submit" class="btn btn-secondary btn-sm" onclick="return confirm('Restore Review #${r.id} to public visibility?');">
                                                            Unhide
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews/${r.id}/hide" style="display: inline;">
                                                        <input type="hidden" name="currentStatus" value="${activeStatus}" />
                                                        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Hide Review #${r.id} from public display?');">
                                                            Hide
                                                        </button>
                                                    </form>
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
