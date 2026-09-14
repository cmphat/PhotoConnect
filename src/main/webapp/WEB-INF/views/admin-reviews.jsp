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
        .badge-visible { background-color: rgba(52, 211, 153, 0.15); color: #34d399; }
        .badge-hidden { background-color: rgba(239, 68, 68, 0.15); color: #ef4444; }
        .rating-stars {
            color: #f59e0b;
            font-size: 0.9rem;
            letter-spacing: 1px;
        }
        .action-btn {
            background-color: transparent;
            border: 1px solid var(--border-dark);
            color: var(--text-on-dark);
            padding: 0.35rem 0.75rem;
            font-size: 0.8rem;
            cursor: pointer;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            transition: all var(--transition-fast);
            border-radius: 2px;
        }
        .btn-hide {
            border-color: rgba(239, 68, 68, 0.4);
            color: #ef4444;
        }
        .btn-hide:hover {
            background-color: #ef4444;
            color: #ffffff;
            border-color: #ef4444;
        }
        .btn-unhide {
            border-color: rgba(52, 211, 153, 0.4);
            color: #34d399;
        }
        .btn-unhide:hover {
            background-color: #34d399;
            color: #0b0f17;
            border-color: #34d399;
        }
        .alert-banner {
            padding: 1rem 1.25rem;
            margin-bottom: 2rem;
            border-radius: 2px;
            font-size: 0.9rem;
            line-height: 1.5;
        }
        .alert-success {
            background-color: rgba(52, 211, 153, 0.1);
            border: 1px solid rgba(52, 211, 153, 0.3);
            color: #34d399;
        }
        .alert-error {
            background-color: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #ef4444;
        }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 0 6rem; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1200px; margin: 0 auto; padding: 0 1.5rem;">
            
            <jsp:include page="fragments/admin-navbar.jsp" />

            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <div>
                    <span class="section-index">Administration</span>
                    <h1 class="editorial-title" style="font-size: 2.2rem; margin: 0.25rem 0 0;">Review Moderation</h1>
                </div>
                <div style="color: var(--text-muted); font-size: 0.85rem;">
                    Total Reviews: ${allCount}
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="alert-banner alert-success">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="alert-banner alert-error">
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
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border-dark); background-color: var(--bg-dark-secondary);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No reviews found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">No reviews match the selected filter criteria.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="overflow-x: auto; border: 1px solid var(--border-dark);">
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
                                                        <button type="submit" class="action-btn btn-unhide" onclick="return confirm('Restore Review #${r.id} to public visibility?');">
                                                            Unhide
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews/${r.id}/hide" style="display: inline;">
                                                        <input type="hidden" name="currentStatus" value="${activeStatus}" />
                                                        <button type="submit" class="action-btn btn-hide" onclick="return confirm('Hide Review #${r.id} from public display?');">
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
