<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Edit Professional Profile — PhotoConnect Creator Studio</title>
    <style>
        .profile-edit-shell {
            max-width: 1040px;
            margin: 0 auto;
            padding: clamp(2rem, 4vw, 3.5rem) 1.5rem 5rem;
        }

        .profile-edit-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            margin-bottom: 2.5rem;
            padding-bottom: 1.75rem;
            border-bottom: 1px solid var(--border);
            flex-wrap: wrap;
            gap: 1.5rem;
        }

        /* ── Completeness Meter ───────────────────────────────────────────── */
        .completeness-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: 1.75rem 2rem;
            margin-bottom: 3rem;
            box-shadow: var(--shadow-subtle);
        }

        .completeness-header-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
            flex-wrap: wrap;
            gap: 0.75rem;
        }

        .completeness-progress-track {
            width: 100%;
            height: 10px;
            background: var(--surface-subtle);
            border-radius: 999px;
            overflow: hidden;
            border: 1px solid var(--border-subtle);
        }

        .completeness-progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #b45309, #d97706);
            border-radius: 999px;
            transition: width 400ms ease;
        }

        .completeness-checklist {
            margin-top: 1.25rem;
            padding-top: 1rem;
            border-top: 1px solid var(--border-subtle);
            font-size: 0.88rem;
            color: var(--muted);
        }

        .completeness-checklist ul {
            margin: 0.5rem 0 0 1.25rem;
            padding: 0;
        }

        .completeness-checklist li {
            margin-bottom: 0.35rem;
        }

        /* ── Form Layout ─────────────────────────────────────────────────── */
        .edit-section {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: clamp(1.75rem, 3vw, 2.5rem);
            margin-bottom: 2.5rem;
            box-shadow: var(--shadow-subtle);
        }

        .section-lead-title {
            font-family: var(--font-editorial);
            font-size: 1.45rem;
            font-weight: 500;
            color: var(--text);
            margin: 0 0 0.35rem 0;
        }

        .section-lead-desc {
            font-size: 0.88rem;
            color: var(--muted);
            margin-bottom: 2rem;
        }

        .specialty-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
            gap: 0.75rem;
            margin-top: 0.5rem;
        }

        .specialty-item-label {
            display: flex;
            align-items: center;
            gap: 0.65rem;
            padding: 0.75rem 1rem;
            background: var(--surface-subtle);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            cursor: pointer;
            font-size: 0.92rem;
            color: var(--text);
            transition: all var(--transition-fast);
        }

        .specialty-item-label:hover {
            border-color: var(--accent);
            background: var(--surface);
        }

        .specialty-item-label input[type="checkbox"] {
            accent-color: #d97706;
            width: 1.1rem;
            height: 1.1rem;
            cursor: pointer;
        }

        .actions-bar {
            display: flex;
            justify-content: flex-end;
            gap: 1rem;
            align-items: center;
            padding-top: 1.5rem;
        }

        @media (max-width: 640px) {
            .specialty-grid {
                grid-template-columns: 1fr;
            }
            .actions-bar {
                flex-direction: column-reverse;
                width: 100%;
            }
            .actions-bar button,
            .actions-bar a {
                width: 100%;
                text-align: center;
            }
        }
    </style>
</head>

<main class="profile-edit-shell">
    <!-- Breadcrumb & Top Bar -->
    <div style="margin-bottom: 1.25rem;">
        <a href="${pageContext.request.contextPath}/photographers/${profile.id}" class="text-link" style="font-size: 0.88rem;">
            &larr; View Public Profile
        </a>
    </div>

    <div class="profile-edit-header">
        <div>
            <span class="section-index">Creator Studio</span>
            <h1 class="editorial-title" style="font-size: clamp(2rem, 4vw, 3rem); margin: 0 0 0.5rem 0;">
                Edit Professional Profile
            </h1>
            <p class="pc-lead" style="margin: 0;">
                Manage the professional information, specialties, and rates clients see on your marketplace profile.
            </p>
        </div>
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/photographers/${profile.id}" class="btn btn-secondary btn-sm" target="_blank" rel="noopener noreferrer">
                Preview Profile ↗
            </a>
            <a href="${pageContext.request.contextPath}/photographer/portfolio" class="btn btn-secondary btn-sm">
                Manage Portfolio
            </a>
        </div>
    </div>

    <!-- Alert Notifications -->
    <c:if test="${not empty successMessage}">
        <div class="pc-alert pc-alert-success" role="alert" style="margin-bottom: 2rem;">
            <strong>Success:</strong> <c:out value="${successMessage}"/>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="pc-alert pc-alert-error" role="alert" style="margin-bottom: 2rem;">
            <strong>Error:</strong> <c:out value="${errorMessage}"/>
        </div>
    </c:if>

    <!-- ── Profile Completeness Tracker ──────────────────────────────── -->
    <div class="completeness-card">
        <div class="completeness-header-row">
            <div>
                <h2 style="font-size: 1.15rem; font-weight: 600; margin: 0; color: var(--text);">
                    Profile Completeness: <c:out value="${completenessScore}"/>%
                </h2>
                <span style="font-size: 0.85rem; color: var(--muted);">
                    <c:choose>
                        <c:when test="${completenessScore >= 90}">
                            Excellent! Your profile is complete and optimized for client discoverability.
                        </c:when>
                        <c:when test="${completenessScore >= 60}">
                            Good foundation. Complete the items below to reach higher ranking in Explore.
                        </c:when>
                        <c:otherwise>
                            Complete your professional details to establish credibility with clients.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div>
                <span class="status-badge ${completenessScore >= 80 ? 'status-approved' : 'status-pending'}">
                    <c:out value="${completenessScore >= 80 ? 'Studio Ready' : 'In Progress'}"/>
                </span>
            </div>
        </div>

        <div class="completeness-progress-track">
            <div class="completeness-progress-fill" style="width: ${completenessScore}%;"></div>
        </div>

        <c:if test="${not empty completenessRecommendations}">
            <div class="completeness-checklist">
                <span style="font-weight: 600; color: var(--text);">Suggestions to improve your creator profile:</span>
                <ul>
                    <c:forEach var="rec" items="${completenessRecommendations}">
                        <li><c:out value="${rec}"/></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>
    </div>

    <!-- ── Profile Edit Form ─────────────────────────────────────────── -->
    <form action="${pageContext.request.contextPath}/photographer/profile/edit" method="post" id="profile-edit-form">
        <%@ include file="fragments/csrf-input.jsp" %>

        <!-- Section 1: Basic Identity -->
        <section class="edit-section">
            <h2 class="section-lead-title">Basic Information</h2>
            <p class="section-lead-desc">Your public artist name, headline, and primary market location.</p>

            <div class="row g-3">
                <div class="col-12 col-md-6">
                    <label for="displayName" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Professional Name <span style="color: var(--error);">*</span>
                    </label>
                    <input type="text"
                           class="form-control"
                           id="displayName"
                           name="displayName"
                           value="<c:out value='${editRequest.displayName}'/>"
                           required
                           minlength="2"
                           maxlength="150">
                    <div class="form-text">Studio name or stage name presented to clients.</div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="headline" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Professional Headline
                    </label>
                    <input type="text"
                           class="form-control"
                           id="headline"
                           name="headline"
                           placeholder="e.g. Editorial Portrait & Documentary Wedding Photographer"
                           value="<c:out value='${editRequest.headline}'/>"
                           maxlength="255">
                    <div class="form-text">A concise, one-sentence creative introduction.</div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="city" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Primary City <span style="color: var(--error);">*</span>
                    </label>
                    <input type="text"
                           class="form-control"
                           id="city"
                           name="city"
                           value="<c:out value='${editRequest.city}'/>"
                           required
                           maxlength="100">
                    <div class="form-text">Base city where you accept local photography assignments.</div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="country" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Country / Region
                    </label>
                    <input type="text"
                           class="form-control"
                           id="country"
                           name="country"
                           placeholder="Vietnam"
                           value="<c:out value='${editRequest.country}'/>"
                           maxlength="100">
                    <div class="form-text">Primary country of residence or legal operation.</div>
                </div>
            </div>
        </section>

        <!-- Section 2: About & Experience -->
        <section class="edit-section">
            <h2 class="section-lead-title">About the Creator</h2>
            <p class="section-lead-desc">Detailed artistic biography and commercial experience.</p>

            <div class="row g-3">
                <div class="col-12">
                    <label for="bio" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Biography & Artist Statement <span style="color: var(--error);">*</span>
                    </label>
                    <textarea class="form-control"
                              id="bio"
                              name="bio"
                              rows="6"
                              required
                              minlength="20"
                              maxlength="2000"><c:out value="${editRequest.bio}"/></textarea>
                    <div class="form-text">Describe your background, visual aesthetic, and creative approach (20–2,000 characters).</div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="experienceYears" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Years of Experience <span style="color: var(--error);">*</span>
                    </label>
                    <input type="number"
                           class="form-control"
                           id="experienceYears"
                           name="experienceYears"
                           value="<c:out value='${editRequest.experienceYears}'/>"
                           required
                           min="0"
                           max="80">
                    <div class="form-text">Number of years actively producing photographic work.</div>
                </div>
            </div>
        </section>

        <!-- Section 3: Services, Specialties & Starting Rate -->
        <section class="edit-section">
            <h2 class="section-lead-title">Services & Pricing</h2>
            <p class="section-lead-desc">Select photography specialties and set an indicative session rate.</p>

            <div style="margin-bottom: 2rem;">
                <label class="form-label" style="font-weight: 500; font-size: 0.9rem; display: block;">
                    Photography Specialties
                </label>
                <div class="form-text" style="margin-bottom: 0.75rem;">
                    Select all photography genres you actively shoot. These power marketplace discovery filters.
                </div>

                <div class="specialty-grid">
                    <c:forEach var="spec" items="${allSpecialties}">
                        <c:set var="isSelected" value="false" />
                        <c:if test="${not empty editRequest.specialties}">
                            <c:forEach var="sel" items="${editRequest.specialties}">
                                <c:if test="${sel == spec.name()}">
                                    <c:set var="isSelected" value="true" />
                                </c:if>
                            </c:forEach>
                        </c:if>

                        <label class="specialty-item-label" for="spec_${spec.name()}">
                            <input type="checkbox"
                                   name="specialties"
                                   value="${spec.name()}"
                                   id="spec_${spec.name()}"
                                   ${isSelected ? 'checked="checked"' : ''}>
                            <span><c:out value="${spec.displayName}"/></span>
                        </label>
                    </c:forEach>
                </div>
            </div>

            <div class="row g-3">
                <div class="col-12 col-md-6">
                    <label for="priceFrom" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Starting Rate (VND) <span style="color: var(--error);">*</span>
                    </label>
                    <div class="input-group">
                        <input type="number"
                               class="form-control"
                               id="priceFrom"
                               name="priceFrom"
                               value="<c:out value='${editRequest.priceFrom}'/>"
                               required
                               min="0"
                               step="50000">
                        <span class="input-group-text">VND</span>
                    </div>
                    <div class="form-text">
                        Indicative starting rate displayed in directory search. Agreed rates are set per booking request.
                    </div>
                </div>
            </div>
        </section>

        <!-- Section 4: Online Presence & Channels -->
        <section class="edit-section">
            <h2 class="section-lead-title">Online Presence</h2>
            <p class="section-lead-desc">Safe external links so potential clients can explore your wider portfolio.</p>

            <div class="row g-3">
                <div class="col-12 col-md-4">
                    <label for="websiteUrl" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Portfolio Website
                    </label>
                    <input type="url"
                           class="form-control"
                           id="websiteUrl"
                           name="websiteUrl"
                           placeholder="https://yourstudio.com"
                           value="<c:out value='${editRequest.websiteUrl}'/>"
                           maxlength="255">
                    <div class="form-text">Must start with http:// or https://.</div>
                </div>

                <div class="col-12 col-md-4">
                    <label for="instagramUrl" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Instagram Profile
                    </label>
                    <input type="url"
                           class="form-control"
                           id="instagramUrl"
                           name="instagramUrl"
                           placeholder="https://instagram.com/yourhandle"
                           value="<c:out value='${editRequest.instagramUrl}'/>"
                           maxlength="255">
                    <div class="form-text">Direct URL to your Instagram profile.</div>
                </div>

                <div class="col-12 col-md-4">
                    <label for="facebookUrl" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Facebook Page
                    </label>
                    <input type="url"
                           class="form-control"
                           id="facebookUrl"
                           name="facebookUrl"
                           placeholder="https://facebook.com/yourpage"
                           value="<c:out value='${editRequest.facebookUrl}'/>"
                           maxlength="255">
                    <div class="form-text">Direct URL to your Facebook artist page.</div>
                </div>
            </div>
        </section>

        <!-- Section 5: Logistics & Equipment -->
        <section class="edit-section">
            <h2 class="section-lead-title">Studio & Logistics (Optional)</h2>
            <p class="section-lead-desc">Equipment summary, working languages, and travel availability.</p>

            <div class="row g-3">
                <div class="col-12 col-md-6">
                    <label for="languages" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Languages Spoken
                    </label>
                    <input type="text"
                           class="form-control"
                           id="languages"
                           name="languages"
                           placeholder="e.g. English, Vietnamese, French"
                           value="<c:out value='${editRequest.languages}'/>"
                           maxlength="150">
                    <div class="form-text">Helps international and destination clients communicate smoothly.</div>
                </div>

                <div class="col-12 col-md-6 d-flex align-items-center">
                    <div class="form-check form-switch pt-md-3">
                        <input class="form-check-input"
                               type="checkbox"
                               role="switch"
                               id="travelAvailable"
                               name="travelAvailable"
                               value="true"
                               ${editRequest.travelAvailable ? 'checked="checked"' : ''}>
                        <label class="form-check-label" for="travelAvailable" style="font-weight: 500; font-size: 0.92rem;">
                            Available for Destination & Travel Bookings
                        </label>
                        <div class="form-text">Indicate willingness to travel outside your home city for shoots.</div>
                    </div>
                </div>

                <div class="col-12">
                    <label for="equipmentSummary" class="form-label" style="font-weight: 500; font-size: 0.9rem;">
                        Camera Gear & Studio Lighting
                    </label>
                    <textarea class="form-control"
                              id="equipmentSummary"
                              name="equipmentSummary"
                              rows="3"
                              placeholder="e.g. Sony A7R V, 35mm f/1.4 GM, 85mm f/1.4 GM, Profoto B10X lights"
                              maxlength="500"><c:out value="${editRequest.equipmentSummary}"/></textarea>
                    <div class="form-text">Summary of primary camera bodies, lenses, and lighting equipment.</div>
                </div>
            </div>
        </section>

        <!-- Save Actions Bar -->
        <div class="actions-bar">
            <a href="${pageContext.request.contextPath}/photographers/${profile.id}" class="btn btn-secondary btn-lg">
                Discard Changes
            </a>
            <button type="submit" class="btn btn-primary btn-lg" id="btn-save-profile">
                Save Professional Profile
            </button>
        </div>
    </form>
</main>
