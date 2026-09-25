<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Photographer Application Status — PhotoConnect</title>
    <style>
        .onboarding-shell {
            max-width: 840px;
            margin: 0 auto;
        }

        .onboarding-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: clamp(2rem, 5vw, 3.5rem);
            margin-bottom: 2.5rem;
            box-shadow: var(--shadow-subtle);
        }

        /* ── Vertical Step Tracker ─────────────────────────────────────────── */
        .timeline-tracker {
            display: flex;
            flex-direction: column;
            gap: 0;
            margin: 2.5rem 0 3rem;
            position: relative;
        }

        .timeline-step {
            display: grid;
            grid-template-columns: 36px 1fr;
            gap: 1.25rem;
            position: relative;
            padding-bottom: 2rem;
        }

        .timeline-step:last-child {
            padding-bottom: 0;
        }

        .timeline-line {
            position: absolute;
            left: 17px;
            top: 36px;
            bottom: 0;
            width: 2px;
            background: var(--border);
        }

        .timeline-step.completed .timeline-line {
            background: var(--success);
        }

        .timeline-node {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.85rem;
            font-weight: 600;
            background: var(--surface-raised);
            border: 2px solid var(--border-strong);
            color: var(--muted);
            z-index: 2;
        }

        .timeline-step.completed .timeline-node {
            background: var(--success);
            border-color: var(--success);
            color: #FFFFFF;
        }

        .timeline-step.active .timeline-node {
            background: var(--warning);
            border-color: var(--warning);
            color: #FFFFFF;
            box-shadow: 0 0 0 4px var(--warning-bg);
        }

        .timeline-step.rejected .timeline-node {
            background: var(--danger);
            border-color: var(--danger);
            color: #FFFFFF;
        }

        .timeline-content-title {
            font-family: var(--font-primary);
            font-size: 1.05rem;
            font-weight: 600;
            color: var(--text);
            margin: 0.35rem 0 0.25rem;
        }

        .timeline-content-desc {
            font-size: 0.88rem;
            color: var(--muted);
            line-height: 1.55;
            margin: 0;
        }

        .profile-summary-table {
            width: 100%;
            border-top: 1px solid var(--border);
            padding-top: 1.75rem;
            margin-top: 1.75rem;
            display: grid;
            gap: 0.85rem;
        }

        .summary-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.4rem 0;
            font-size: 0.92rem;
        }

        .summary-label {
            color: var(--muted);
        }

        .summary-value {
            font-weight: 500;
            color: var(--text);
        }
    </style>
</head>

    <main class="pc-page">
        <div class="editorial-container onboarding-shell">

            <div style="margin-bottom: 2rem;">
                <span class="section-index">Creator Onboarding</span>
                <h1 class="editorial-title" style="font-size: clamp(2.25rem, 4.5vw, 3.5rem); margin: 0 0 0.5rem 0;">
                    Application Status
                </h1>
                <p class="pc-lead" style="font-size: 1.05rem;">
                    Track your verification progress and manage your presence on the PhotoConnect marketplace.
                </p>
            </div>

            <c:choose>
                <c:when test="${not empty profile}">

                    <div class="onboarding-card">

                        <!-- Status Header Banner -->
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; border-bottom: 1px solid var(--border); padding-bottom: 1.75rem;">
                            <div>
                                <h2 class="editorial-heading" style="font-size: 1.65rem; margin: 0 0 0.25rem 0;">
                                    <c:out value="${profile.displayName}"/>
                                </h2>
                                <div style="color: var(--muted); font-size: 0.88rem;">
                                    Application Reference &middot; ID #${profile.id}
                                </div>
                            </div>
                            <div>
                                <span class="status-badge ${verificationStatus}">
                                    <c:out value="${verificationStatus}"/>
                                </span>
                            </div>
                        </div>

                        <!-- Honest Visual Onboarding Stepper -->
                        <div class="timeline-tracker">

                            <!-- Step 1: Profile Submitted -->
                            <div class="timeline-step completed">
                                <div class="timeline-line"></div>
                                <div class="timeline-node">✓</div>
                                <div>
                                    <div class="timeline-content-title">Profile Submitted</div>
                                    <p class="timeline-content-desc">Your artist profile, bio, experience, and pricing details were recorded.</p>
                                </div>
                            </div>

                            <!-- Step 2: Editorial Review -->
                            <c:choose>
                                <c:when test="${verificationStatus == 'APPROVED'}">
                                    <div class="timeline-step completed">
                                        <div class="timeline-line"></div>
                                        <div class="timeline-node">✓</div>
                                        <div>
                                            <div class="timeline-content-title">Review Completed</div>
                                            <p class="timeline-content-desc">Our curatorial team reviewed and verified your credentials.</p>
                                        </div>
                                    </div>
                                </c:when>
                                <c:when test="${verificationStatus == 'PENDING'}">
                                    <div class="timeline-step active">
                                        <div class="timeline-line"></div>
                                        <div class="timeline-node">⏳</div>
                                        <div>
                                            <div class="timeline-content-title">Awaiting Editorial Review</div>
                                            <p class="timeline-content-desc">Your profile is currently under review by our curation team. This typically takes 1–3 business days.</p>
                                        </div>
                                    </div>
                                </c:when>
                                <c:when test="${verificationStatus == 'REJECTED'}">
                                    <div class="timeline-step rejected">
                                        <div class="timeline-line"></div>
                                        <div class="timeline-node">✕</div>
                                        <div>
                                            <div class="timeline-content-title">Application Not Approved</div>
                                            <p class="timeline-content-desc">Unfortunately, your application does not meet our current curation guidelines.</p>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="timeline-step">
                                        <div class="timeline-line"></div>
                                        <div class="timeline-node">!</div>
                                        <div>
                                            <div class="timeline-content-title">Account Suspended</div>
                                            <p class="timeline-content-desc">Profile has been placed on hold. Please reach out to support for resolution.</p>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <!-- Step 3: Public Marketplace Listing -->
                            <c:choose>
                                <c:when test="${verificationStatus == 'APPROVED'}">
                                    <div class="timeline-step completed">
                                        <div class="timeline-line"></div>
                                        <div class="timeline-node">✓</div>
                                        <div>
                                            <div class="timeline-content-title">Approved &amp; Publicly Listed</div>
                                            <p class="timeline-content-desc">Your profile is active and discoverable on the public marketplace.</p>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="timeline-step">
                                        <div class="timeline-line"></div>
                                        <div class="timeline-node">○</div>
                                        <div>
                                            <div class="timeline-content-title">Public Marketplace Listing</div>
                                            <p class="timeline-content-desc">Unlocks upon successful curation approval.</p>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <!-- Step 4: Build Your Portfolio -->
                            <c:choose>
                                <c:when test="${verificationStatus == 'APPROVED'}">
                                    <div class="timeline-step active">
                                        <div class="timeline-node">★</div>
                                        <div>
                                            <div class="timeline-content-title">Build Your Portfolio</div>
                                            <p class="timeline-content-desc">Upload your signature photographs so prospective clients can book your sessions.</p>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="timeline-step">
                                        <div class="timeline-node">○</div>
                                        <div>
                                            <div class="timeline-content-title">Build Your Portfolio</div>
                                            <p class="timeline-content-desc">Upload signature photographs once approved.</p>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                        </div>

                        <!-- Profile Summary Data -->
                        <div class="profile-summary-table">
                            <div class="summary-row">
                                <span class="summary-label">Primary City</span>
                                <span class="summary-value"><c:out value="${profile.city}"/></span>
                            </div>
                            <div class="summary-row">
                                <span class="summary-label">Experience</span>
                                <span class="summary-value"><c:out value="${profile.experienceYears}"/> years</span>
                            </div>
                            <div class="summary-row">
                                <span class="summary-label">Starting Session Rate</span>
                                <span class="summary-value">
                                    <fmt:formatNumber value="${profile.priceFrom}" pattern="#,##0" /> VND
                                </span>
                            </div>
                        </div>

                        <!-- Primary & Secondary CTAs using ONLY existing routes -->
                        <div style="margin-top: 2.5rem; pt: 1rem; display: flex; flex-wrap: wrap; gap: 1.25rem;">
                            <c:choose>
                                <c:when test="${verificationStatus == 'APPROVED'}">
                                    <a href="${pageContext.request.contextPath}/photographer/portfolio" class="btn btn-primary btn-lg" id="btn-manage-portfolio">
                                        Manage Portfolio &rarr;
                                    </a>
                                    <a href="${pageContext.request.contextPath}/photographers/${profile.id}" class="btn btn-secondary btn-lg" id="btn-view-public-profile">
                                        View Public Profile
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                                        &larr; Return to Home
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </div>

                    </div>

                </c:when>
                <c:otherwise>
                    <!-- Empty State: No Application Yet -->
                    <div class="pc-empty-state">
                        <div style="font-size: 2.5rem; margin-bottom: 1rem; color: var(--accent);">✦</div>
                        <h2 class="editorial-heading" style="font-size: 1.75rem; margin-bottom: 0.75rem;">No Application Found</h2>
                        <p style="margin-bottom: 2rem;">You have not yet submitted a photographer application to join our verified roster.</p>
                        <a href="${pageContext.request.contextPath}/become-photographer" class="btn btn-primary btn-lg">
                            Apply to Join PhotoConnect
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>

            <div style="text-align: center; margin-top: 2rem;">
                <a href="${pageContext.request.contextPath}/" class="text-link">Back to Marketplace Home</a>
            </div>

        </div>
    </main>

    <!-- Editorial Footer -->
    <footer class="pc-footer">
        <div class="editorial-container">
            <div style="color: var(--muted); font-size: 0.85rem;">
                &copy; 2026 PhotoConnect. Creator Onboarding &amp; Verification.
            </div>
        </div>
    </footer>
