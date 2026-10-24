<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Review Moderation – Admin – PhotoConnect</title>
</head>

    <main class="pc-page pc-page-compact">
        <div class="editorial-container pc-container-admin">

            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-page-header pc-stack-mobile">
                <div>
                    <span class="section-index">Administration</span>
                    <h1 class="editorial-title">Trust &amp; Safety Moderation</h1>
                </div>
                <div class="pc-page-meta">
                    <div class="btn-group" role="group">
                        <a href="${pageContext.request.contextPath}/admin/reviews?tab=reviews"
                           class="btn btn-sm ${activeTabSection != 'reports' ? 'btn-primary' : 'btn-outline-secondary'}">
                            Reviews (${allCount})
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/reviews?tab=reports"
                           class="btn btn-sm ${activeTabSection == 'reports' ? 'btn-primary' : 'btn-outline-secondary'}">
                            Violation Reports (${pendingReportsCount})
                        </a>
                    </div>
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

            <c:choose>
                <c:when test="${activeTabSection == 'reports'}">
                    <!-- Reports Status Filter Tabs -->
                    <div class="status-tabs mb-4">
                        <a href="${pageContext.request.contextPath}/admin/reviews?tab=reports&reportStatus=ALL"
                           class="status-tab ${activeReportStatus == 'ALL' ? 'active' : ''}">
                            All Reports
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/reviews?tab=reports&reportStatus=PENDING"
                           class="status-tab ${activeReportStatus == 'PENDING' ? 'active' : ''}">
                            Pending Review (${pendingReportsCount})
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/reviews?tab=reports&reportStatus=RESOLVED"
                           class="status-tab ${activeReportStatus == 'RESOLVED' ? 'active' : ''}">
                            Resolved
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/reviews?tab=reports&reportStatus=DISMISSED"
                           class="status-tab ${activeReportStatus == 'DISMISSED' ? 'active' : ''}">
                            Dismissed
                        </a>
                    </div>

                    <c:choose>
                        <c:when test="${empty reports}">
                            <div class="pc-empty-state">
                                <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No violation reports</h3>
                                <p style="color: var(--text-muted); margin-bottom: 0;">No dispute reports match the selected criteria.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-table-shell" tabindex="0" aria-label="Reports table">
                                <table class="admin-table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Category</th>
                                            <th>Reporter</th>
                                            <th>Reported User</th>
                                            <th>Booking</th>
                                            <th>Violation Details</th>
                                            <th>Status</th>
                                            <th>Submitted</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="rep" items="${reports}">
                                            <tr>
                                                <td style="color: var(--text-muted);">#<c:out value="${rep.id()}"/></td>
                                                <td>
                                                    <span class="badge bg-warning text-dark"><c:out value="${rep.categoryDisplayName()}"/></span>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 500;"><c:out value="${rep.reporterName()}"/></div>
                                                    <div style="color: var(--text-muted); font-size: 0.8rem;"><c:out value="${rep.reporterEmail()}"/></div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty rep.reportedUserName()}">
                                                            <div style="font-weight: 500;"><c:out value="${rep.reportedUserName()}"/></div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="color: var(--text-muted); font-style: italic;">Unspecified</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:if test="${not empty rep.bookingId()}">
                                                        <a href="${pageContext.request.contextPath}/bookings/${rep.bookingId()}" target="_blank" class="text-decoration-none">
                                                            #<c:out value="${rep.bookingId()}"/>
                                                        </a>
                                                    </c:if>
                                                </td>
                                                <td style="max-width: 250px;">
                                                    <div style="font-size: 0.88rem; line-height: 1.4;"><c:out value="${rep.details()}"/></div>
                                                    <c:if test="${not empty rep.adminNotes()}">
                                                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.35rem; font-style: italic;">
                                                            Admin: <c:out value="${rep.adminNotes()}"/>
                                                        </div>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${rep.status() == 'PENDING'}">
                                                            <span class="badge bg-danger">PENDING</span>
                                                        </c:when>
                                                        <c:when test="${rep.status() == 'RESOLVED'}">
                                                            <span class="badge bg-success">RESOLVED</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary">DISMISSED</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="font-size: 0.82rem; color: var(--text-muted);">
                                                    <c:out value="${rep.createdAt()}"/>
                                                </td>
                                                <td style="text-align: right;">
                                                    <c:if test="${rep.status() == 'PENDING'}">
                                                        <form method="post" action="${pageContext.request.contextPath}/admin/reviews/reports/${rep.id()}/resolve" style="display: inline;">
                                                            <%@ include file="fragments/csrf-input.jsp" %>
                                                            <button type="submit" class="btn btn-success btn-sm me-1" onclick="return confirm('Mark report #${rep.id()} as RESOLVED?');">
                                                                Resolve
                                                            </button>
                                                        </form>
                                                        <form method="post" action="${pageContext.request.contextPath}/admin/reviews/reports/${rep.id()}/dismiss" style="display: inline;">
                                                            <%@ include file="fragments/csrf-input.jsp" %>
                                                            <button type="submit" class="btn btn-secondary btn-sm" onclick="return confirm('Dismiss report #${rep.id()}?');">
                                                                Dismiss
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <!-- Reviews Status Tabs -->
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
                                                        <%@ include file="fragments/csrf-input.jsp" %>
                                                        <input type="hidden" name="currentStatus" value="${activeStatus}" />
                                                        <button type="submit" class="btn btn-secondary btn-sm" onclick="return confirm('Restore Review #${r.id} to public visibility?');">
                                                            Unhide
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/reviews/${r.id}/hide" style="display: inline;">
                                                        <%@ include file="fragments/csrf-input.jsp" %>
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
