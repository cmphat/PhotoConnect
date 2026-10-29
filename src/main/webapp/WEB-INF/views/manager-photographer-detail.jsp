<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <fmt:setLocale value="en_US" />

            <head>
                <title>Review Profile:
                    <c:out value="${profile.displayName}" /> — PhotoConnect Manager
                </title>
            </head>
            <main class="pc-page">
                <div class="editorial-container pc-container-admin">
                    <jsp:include page="fragments/manager-navbar.jsp" />
                    <jsp:include page="fragments/manager-messages.jsp" />

                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
                        <a href="${pageContext.request.contextPath}/manager/photographers"
                            class="btn btn-sm btn-outline-secondary">
                            &larr; Back to Applicant Directory
                        </a>
                        <span
                            class="badge ${profile.verificationStatus == 'APPROVED' ? 'bg-success' : (profile.verificationStatus == 'PENDING' ? 'bg-warning text-dark' : 'bg-danger')}">
                            STATUS:
                            <c:out value="${profile.verificationStatus}" />
                        </span>
                    </div>

                    <!-- 1. Header Profile Summary Card -->
                    <div class="pc-card mb-4"
                        style="background: var(--surface); border-radius: 8px; border: 1px solid var(--border); padding: 1.5rem;">
                        <div class="d-flex align-items-start gap-3 flex-wrap">
                            <div
                                style="width: 72px; height: 72px; border-radius: 50%; overflow: hidden; background: #e2e8f0; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: bold; color: #475569; flex-shrink: 0;">
                                <c:choose>
                                    <c:when test="${not empty profile.user.avatarUrl}">
                                        <img src="${profile.user.avatarUrl}" alt="Avatar"
                                            style="width: 100%; height: 100%; object-fit: cover;">
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${profile.displayName.substring(0, 1)}" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="flex-grow-1">
                                <h1 class="editorial-title mb-1" style="font-size: 1.6rem;">
                                    <c:out value="${profile.displayName}" />
                                </h1>
                                <p class="text-muted mb-2">
                                    <c:out
                                        value="${profile.headline != null ? profile.headline : 'Freelance Photographer'}" />
                                </p>
                                <div class="d-flex flex-wrap gap-3" style="font-size: 0.9rem;">
                                    <span><strong>Email:</strong>
                                        <c:out value="${profile.user.email}" />
                                    </span>
                                    <span><strong>Phone:</strong>
                                        <c:out
                                            value="${profile.user.phone != null ? profile.user.phone : 'Not specified'}" />
                                    </span>
                                    <span><strong>Location:</strong>
                                        <c:out value="${profile.city != null ? profile.city : 'Nationwide'}" />
                                        <c:if test="${not empty profile.country}">,
                                            <c:out value="${profile.country}" />
                                        </c:if>
                                    </span>
                                    <span><strong>Experience:</strong>
                                        <c:out
                                            value="${profile.experienceYears != null ? profile.experienceYears : 0}" />
                                        yrs
                                    </span>
                                    <span><strong>Starting Price:</strong>
                                        <c:choose>
                                            <c:when test="${profile.priceFrom != null}">
                                                <fmt:formatNumber value="${profile.priceFrom}" pattern="#,##0" /> VND
                                            </c:when>
                                            <c:otherwise>Not set</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <c:if test="${not empty profile.rejectionReason}">
                            <div class="alert alert-warning mt-3 mb-0" role="alert">
                                <strong>Previous Rejection Reason:</strong>
                                <c:out value="${profile.rejectionReason}" />
                            </div>
                        </c:if>
                    </div>

                    <!-- 2. AI & Rule-based Screening Evaluation -->
                    <div class="pc-card mb-4"
                        style="border: 2px solid #3b82f6; border-radius: 8px; padding: 1.5rem; background: rgba(59, 130, 246, 0.02);">
                        <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
                            <div>
                                <span class="badge bg-primary mb-1">SCREENING RECOMMENDATION</span>
                                <h2 class="h5 fw-bold mb-0">Applicant Evaluation &amp; Verification (6-Criteria
                                    Framework)</h2>
                            </div>
                            <div class="text-end">
                                <span class="fs-4 fw-bold text-${evaluation.badgeClass}">
                                    <c:out value="${evaluation.totalScore}" /> / 100 pts
                                </span>
                                <div style="font-size: 0.85rem; color: var(--text-muted);">
                                    <c:out value="${evaluation.completenessLevel}" />
                                </div>
                            </div>
                        </div>

                        <!-- Progress Bar (Đã xử lý triệt để lỗi CSS của IDE) -->
                        <div class="progress mb-3" style="height: 10px;">
                            <div class="progress-bar bg-${evaluation.badgeClass}" role="progressbar"
                                id="evalProgressBar" aria-valuenow="${evaluation.totalScore}" aria-valuemin="0"
                                aria-valuemax="100"></div>
                        </div>
                        <script>
                            document.getElementById('evalProgressBar').style.width = '<c:out value="${evaluation.totalScore}"/>%';
                        </script>

                        <!-- Recommendation Alert -->
                        <div class="alert alert-${evaluation.badgeClass == 'success' ? 'success' : (evaluation.badgeClass == 'warning' ? 'warning' : 'danger')} mb-3"
                            role="alert">
                            <h3 class="h6 fw-bold mb-1">System Recommendation:
                                <c:out value="${evaluation.recommendation}" />
                            </h3>
                            <p class="mb-0" style="font-size: 0.9rem;">
                                <c:out value="${evaluation.explanation}" />
                            </p>
                        </div>

                        <!-- 6 Criteria Breakdown -->
                        <h3 class="h6 fw-bold text-uppercase text-muted mb-2">Evaluation Breakdown (6 Standards)</h3>
                        <div class="row g-3 mb-3">
                            <c:forEach var="c" items="${evaluation.criteria}">
                                <div class="col-12 col-md-6">
                                    <div class="p-3 border rounded h-100" style="background: var(--surface);">
                                        <div class="d-flex justify-content-between align-items-center mb-1">
                                            <span class="fw-semibold" style="font-size: 0.95rem;">
                                                <c:out value="${c.name}" />
                                            </span>
                                            <span
                                                class="badge ${c.met ? 'bg-success' : 'bg-secondary'}">${c.earnedScore}
                                                / ${c.weight} pts</span>
                                        </div>
                                        <c:if test="${not empty c.metDetails}">
                                            <ul class="list-unstyled mb-1 text-success" style="font-size: 0.82rem;">
                                                <c:forEach var="m" items="${c.metDetails}">
                                                    <li>✓
                                                        <c:out value="${m}" />
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                        <c:if test="${not empty c.missingDetails}">
                                            <ul class="list-unstyled mb-0 text-muted" style="font-size: 0.82rem;">
                                                <c:forEach var="miss" items="${c.missingDetails}">
                                                    <li>–
                                                        <c:out value="${miss}" />
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <!-- Verification Points & Ethical Disclaimer -->
                        <div class="row g-3">
                            <div class="col-12 col-md-7">
                                <div class="p-3 border rounded h-100"
                                    style="background: rgba(245, 158, 11, 0.05); border-color: rgba(245, 158, 11, 0.2) !important;">
                                    <h4 class="h6 fw-bold text-warning text-dark mb-2">🔍 Items Requiring Manual Manager
                                        Verification:</h4>
                                    <ul class="mb-0 ps-3" style="font-size: 0.85rem;">
                                        <c:forEach var="p" items="${evaluation.manualVerificationPoints}">
                                            <li class="mb-1">
                                                <c:out value="${p}" />
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </div>
                            <div class="col-12 col-md-5">
                                <div class="p-3 border rounded h-100"
                                    style="background: var(--surface-muted); font-size: 0.82rem; color: var(--text-muted);">
                                    <p class="fw-bold mb-1">📌 Transparency &amp; Review Principles:</p>
                                    <ul class="ps-3 mb-0">
                                        <li>Scores indicate <strong>information completeness</strong>, not subjective
                                            artistic merit.</li>
                                        <li>Portfolio uploads unlock upon onboarding approval. Applicants are
                                            <strong>not penalized</strong> for pending portfolio items.</li>
                                        <li>Evaluations strictly exclude demographic factors such as age, gender, or
                                            origin.</li>
                                        <li>Manager staff retains full independent authority to approve, reject, or
                                            request information.</li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 3. Professional Profile Declarations (Detailed View) -->
                    <div class="pc-card mb-4" style="padding: 1.5rem;">
                        <h2 class="h5 fw-bold mb-3">Professional Credentials &amp; Equipment</h2>
                        <div class="row g-3 mb-3">
                            <div class="col-12 col-md-6">
                                <div class="p-2 border-bottom">
                                    <span class="text-muted d-block small">Specializations:</span>
                                    <strong>
                                        <c:out
                                            value="${profile.specialties != null ? profile.specialties : 'Not specified'}" />
                                    </strong>
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="p-2 border-bottom">
                                    <span class="text-muted d-block small">Spoken Languages:</span>
                                    <strong>
                                        <c:out
                                            value="${profile.languages != null ? profile.languages : 'Vietnamese'}" />
                                    </strong>
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="p-2 border-bottom">
                                    <span class="text-muted d-block small">Willing to Travel:</span>
                                    <strong>
                                        <c:out
                                            value="${profile.travelAvailable ? 'Yes (Available for travel shoots)' : 'No (Local region only)'}" />
                                    </strong>
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="p-2 border-bottom">
                                    <span class="text-muted d-block small">Submission Date:</span>
                                    <strong>
                                        <c:out value="${profile.createdAt}" />
                                    </strong>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="p-2 border-bottom">
                                    <span class="text-muted d-block small">Gear &amp; Equipment (Cameras, Lenses,
                                        Lighting, Drone):</span>
                                    <p class="mb-0 mt-1">
                                        <c:out
                                            value="${profile.equipmentSummary != null ? profile.equipmentSummary : 'No equipment details provided'}" />
                                    </p>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="p-2 border-bottom">
                                    <span class="text-muted d-block small">Biography &amp; Creative Style:</span>
                                    <p class="mb-0 mt-1" style="white-space: pre-line;">
                                        <c:out
                                            value="${profile.bio != null ? profile.bio : 'No biography provided yet'}" />
                                    </p>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="p-2">
                                    <span class="text-muted d-block small mb-1">External Portfolios &amp; Social
                                        Channels:</span>
                                    <div class="d-flex flex-wrap gap-2">
                                        <c:if test="${not empty profile.websiteUrl}">
                                            <a href="${profile.websiteUrl}" target="_blank" rel="noopener noreferrer"
                                                class="btn btn-sm btn-outline-secondary">🌐 Personal Website &rarr;</a>
                                        </c:if>
                                        <c:if test="${not empty profile.instagramUrl}">
                                            <a href="${profile.instagramUrl}" target="_blank" rel="noopener noreferrer"
                                                class="btn btn-sm btn-outline-secondary">📸 Instagram &rarr;</a>
                                        </c:if>
                                        <c:if test="${not empty profile.facebookUrl}">
                                            <a href="${profile.facebookUrl}" target="_blank" rel="noopener noreferrer"
                                                class="btn btn-sm btn-outline-secondary">📘 Facebook &rarr;</a>
                                        </c:if>
                                        <c:if test="${not empty profile.tiktokUrl}">
                                            <a href="${profile.tiktokUrl}" target="_blank" rel="noopener noreferrer"
                                                class="btn btn-sm btn-outline-secondary">🎵 TikTok &rarr;</a>
                                        </c:if>
                                        <c:if test="${not empty profile.youtubeUrl}">
                                            <a href="${profile.youtubeUrl}" target="_blank" rel="noopener noreferrer"
                                                class="btn btn-sm btn-outline-secondary">▶ YouTube &rarr;</a>
                                        </c:if>
                                        <c:if
                                            test="${empty profile.websiteUrl and empty profile.instagramUrl and empty profile.facebookUrl and empty profile.tiktokUrl and empty profile.youtubeUrl}">
                                            <span class="text-muted fst-italic">No external social or portfolio channels
                                                provided.</span>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 4. Manager Actions (Phê duyệt / Từ chối / Đình chỉ) -->
                    <c:if test="${profile.verificationStatus == 'PENDING'}">
                        <h2 class="h5 fw-bold mb-3">Verification Decision &amp; Actions</h2>
                        <div class="row g-3 mb-4">
                            <div class="col-12 col-md-6">
                                <form method="post"
                                    action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/approve"
                                    class="pc-card h-100 p-3" style="border-top: 3px solid #10b981;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                        <h3 class="h6 fw-bold text-success mb-2">Approve Application</h3>
                                        <p class="small text-muted mb-2">Photographer will receive marketplace
                                            credentials, package creation access, and gallery upload permissions.</p>
                                        <div class="mb-3">
                                            <label for="approveNotes" class="form-label small fw-semibold">Internal
                                                Audit / Verification Notes:</label>
                                            <textarea id="approveNotes" name="notes" class="form-control" rows="3"
                                                maxlength="1000"
                                                placeholder="Record verification findings or approval justification..."></textarea>
                                        </div>
                                        <button type="submit" class="btn btn-primary w-100"
                                            onclick="return confirm('Confirm approval of this photographer application?')">
                                            Approve Application
                                        </button>
                                </form>
                            </div>
                            <div class="col-12 col-md-6">
                                <form method="post"
                                    action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/reject"
                                    class="pc-card h-100 p-3" style="border-top: 3px solid #ef4444;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                        <h3 class="h6 fw-bold text-danger mb-2">Reject Application</h3>
                                        <p class="small text-muted mb-2">Application will be declined; reason will be
                                            shared with the applicant for reapplication.</p>
                                        <div class="mb-2">
                                            <label for="reason" class="form-label small fw-semibold">Rejection Reason
                                                (Required, visible to applicant):</label>
                                            <textarea id="reason" name="reason" required maxlength="1000"
                                                class="form-control" rows="2"
                                                placeholder="e.g. Missing equipment list, broken external portfolio link, unverified identity..."></textarea>
                                        </div>
                                        <div class="mb-3">
                                            <label for="rejectNotes" class="form-label small fw-semibold">Internal
                                                Manager Notes:</label>
                                            <textarea id="rejectNotes" name="notes" maxlength="1000"
                                                class="form-control" rows="2"
                                                placeholder="Internal notes for audit history..."></textarea>
                                        </div>
                                        <button type="submit" class="btn btn-danger w-100"
                                            onclick="return confirm('Confirm rejection of this application?')">
                                            Reject Application
                                        </button>
                                </form>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${profile.verificationStatus == 'APPROVED'}">
                        <div class="row g-3 mb-4">
                            <div class="col-12">
                                <form method="post"
                                    action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/suspend"
                                    class="pc-card p-3" style="border-left: 4px solid #f59e0b;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                        <h3 class="h6 fw-bold text-warning mb-2">Suspend Photographer Account</h3>
                                        <p class="small text-muted mb-2">For verified policy breaches, code-of-conduct
                                            violations, or safety concerns.</p>
                                        <div class="row g-2 mb-3">
                                            <div class="col-12 col-md-6">
                                                <label for="suspendReason"
                                                    class="form-label small fw-semibold">Suspension Reason
                                                    (Required):</label>
                                                <textarea id="suspendReason" name="reason" required maxlength="1000"
                                                    class="form-control" rows="2"
                                                    placeholder="Specify investigation findings or violation justification..."></textarea>
                                            </div>
                                            <div class="col-12 col-md-6">
                                                <label for="suspendNotes" class="form-label small fw-semibold">Internal
                                                    Audit Notes:</label>
                                                <textarea id="suspendNotes" name="notes" maxlength="1000"
                                                    class="form-control" rows="2"
                                                    placeholder="Audit record for compliance..."></textarea>
                                            </div>
                                        </div>
                                        <button type="submit" class="btn btn-warning"
                                            onclick="return confirm('Confirm suspension of this photographer account?')">
                                            Suspend Photographer
                                        </button>
                                </form>
                            </div>
                        </div>
                    </c:if>

                </div>
            </main>