<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Application Detail — Admin — PhotoConnect</title>
</head>

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

                    <c:if test="${not empty evaluation}">
                        <div class="p-3 my-4 border rounded" style="background: rgba(59, 130, 246, 0.03); border-color: rgba(59, 130, 246, 0.3) !important;">
                            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-2">
                                <div>
                                    <span class="badge bg-primary mb-1">VERIFICATION CRITERIA</span>
                                    <h3 class="h6 fw-bold mb-0">Application Evaluation Breakdown (6 Criteria)</h3>
                                </div>
                                <div class="text-end">
                                    <span class="fs-5 fw-bold text-${evaluation.badgeClass}"><c:out value="${evaluation.totalScore}"/> / 100 pts</span>
                                    <div class="small text-muted"><c:out value="${evaluation.completenessLevel}"/></div>
                                </div>
                            </div>
                            <div class="progress mb-2" style="height: 6px;">
                                <div class="progress-bar bg-${evaluation.badgeClass}" role="progressbar" style="width: ${evaluation.totalScore}%;"></div>
                            </div>
                            <div class="alert alert-${evaluation.badgeClass == 'success' ? 'success' : (evaluation.badgeClass == 'warning' ? 'warning' : 'danger')} p-2 mb-2 small" role="alert">
                                <strong>Recommendation:</strong> <c:out value="${evaluation.recommendation}"/> &mdash; <c:out value="${evaluation.explanation}"/>
                            </div>
                            <div class="row g-2">
                                <c:forEach var="c" items="${evaluation.criteria}">
                                    <div class="col-12 col-md-4">
                                        <div class="p-2 border rounded bg-white small h-100">
                                            <div class="d-flex justify-content-between">
                                                <span class="fw-semibold"><c:out value="${c.name}"/></span>
                                                <span class="badge ${c.met ? 'bg-success' : 'bg-secondary'}">${c.earnedScore}/${c.weight} pts</span>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>

                    <h2 class="editorial-heading" style="font-size: 1.25rem; margin-top: 2rem; margin-bottom: 1.5rem;">Bio</h2>
                    <div style="color: var(--text-muted); line-height: 1.7; margin-bottom: 3rem;">
                        <c:out value="${profile.bio}"/>
                    </div>

                    <c:if test="${profile.verificationStatus == 'PENDING'}">
                        <div class="row g-4" style="border-top: 1px solid var(--border); padding-top: 2rem;">
                            <div class="col-md-6">
                                <div class="p-3 border rounded-3 bg-light">
                                    <h3 class="h6 fw-bold mb-2 text-success">Approve Application</h3>
                                    <p class="small text-muted mb-3">The profile will be published on the marketplace and eligible to receive bookings.</p>
                                    <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/approve" method="post" style="margin: 0;">
                                        <%@ include file="fragments/csrf-input.jsp" %>
                                        <div class="mb-2">
                                            <input type="text" name="adminNotes" class="form-control form-control-sm" placeholder="Internal admin notes (optional)">
                                        </div>
                                        <button type="submit" class="btn btn-primary btn-sm" onclick="return confirm('Confirm approval for this photographer application?')">
                                            Approve Application
                                        </button>
                                    </form>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="p-3 border rounded-3 bg-light">
                                    <h3 class="h6 fw-bold mb-2 text-danger">Reject Application</h3>
                                    <p class="small text-muted mb-3">The photographer will receive the rejection rationale and can update their profile to reapply.</p>
                                    <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/reject" method="post" style="margin: 0;">
                                        <%@ include file="fragments/csrf-input.jsp" %>
                                        <div class="mb-2">
                                            <textarea name="rejectionReason" class="form-control form-control-sm" rows="2" placeholder="Rejection reason (sent to applicant)" required></textarea>
                                        </div>
                                        <div class="mb-2">
                                            <input type="text" name="adminNotes" class="form-control form-control-sm" placeholder="Internal admin notes (optional)">
                                        </div>
                                        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Confirm rejection for this application?')">
                                            Reject Application
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${profile.verificationStatus != 'PENDING'}">
                        <div class="border-top pt-4 mt-3" style="font-size: 0.9rem;">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <span class="text-muted">Verification Status:</span>
                                <span class="status-badge ${profile.verificationStatus}"><c:out value="${profile.verificationStatus}"/></span>
                            </div>
                            <c:if test="${not empty profile.reviewedAt}">
                                <div class="text-muted small mb-1">
                                    Reviewed at: <fmt:parseDate value="${fn:substring(profile.reviewedAt, 0, 19)}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedReviewDate" type="both" />
                                    <fmt:formatDate value="${parsedReviewDate}" pattern="MMM d, yyyy HH:mm" />
                                    <c:if test="${not empty profile.reviewedByAdminId}">
                                        &middot; Reviewed by Admin ID #${profile.reviewedByAdminId}
                                    </c:if>
                                </div>
                            </c:if>
                            <c:if test="${not empty profile.rejectionReason}">
                                <div class="alert alert-warning pc-alert pc-alert-warning mt-2 mb-2">
                                    <strong>Rejection Reason:</strong> <c:out value="${profile.rejectionReason}"/>
                                </div>
                            </c:if>
                            <c:if test="${not empty profile.adminNotes}">
                                <div class="text-muted small">
                                    <em>Internal Notes:</em> <c:out value="${profile.adminNotes}"/>
                                </div>
                            </c:if>
                            <c:if test="${profile.verificationStatus == 'APPROVED'}">
                                <div class="p-3 border rounded-3 bg-light mt-4">
                                    <h3 class="h6 fw-bold mb-2 text-warning">Suspend Photographer</h3>
                                    <p class="small text-muted mb-3">Temporarily delist profile and block new bookings pending investigation or due to policy violations.</p>
                                    <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/suspend" method="post" style="margin: 0;">
                                        <%@ include file="fragments/csrf-input.jsp" %>
                                        <div class="mb-2">
                                            <textarea name="suspensionReason" class="form-control form-control-sm" rows="2" placeholder="Suspension rationale (required)" required></textarea>
                                        </div>
                                        <div class="mb-2">
                                            <input type="text" name="adminNotes" class="form-control form-control-sm" placeholder="Internal admin notes (optional)">
                                        </div>
                                        <button type="submit" class="btn btn-warning btn-sm" onclick="return confirm('Confirm suspension for this photographer profile?')">
                                            Suspend Account
                                        </button>
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </c:if>
                </div>
            </c:when>
            <c:otherwise>
                <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border);">
                    <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem; color: #ef4444;">Application Not Found</h3>
                    <p style="color: var(--text-muted); margin-bottom: 0;">The requested photographer profile does not exist.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>
