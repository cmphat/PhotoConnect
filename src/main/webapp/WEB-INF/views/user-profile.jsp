<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title><c:out value="${profile.displayName}"/> (@<c:out value="${profile.username}"/>) — PhotoConnect Profile</title>
    <meta name="description" content="View profile, creative portfolio, and interactive availability calendar for <c:out value="${profile.displayName}"/> on PhotoConnect.">

    <!-- FullCalendar 6 Bundle -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/main.min.css">
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js"></script>

    <style>
        .profile-page-region {
            padding: clamp(2rem, 4vw, 3.5rem) 0 5rem;
            background: var(--pc-background, #F8FAFC);
        }

        .profile-layout-grid {
            display: grid;
            grid-template-columns: minmax(280px, 320px) minmax(0, 1fr);
            gap: clamp(2rem, 4vw, 3.5rem);
            align-items: start;
        }

        /* ── Left Sidebar (GitHub-Inspired 28%) ─────────────────────── */
        .profile-sidebar {
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
        }

        .profile-avatar-frame {
            position: relative;
            width: clamp(160px, 20vw, 220px);
            height: clamp(160px, 20vw, 220px);
            border-radius: 50%;
            overflow: hidden;
            background: var(--pc-surface, #FFFFFF);
            border: 3px solid var(--pc-border, #CBD5E1);
            box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
            margin-bottom: 0.5rem;
        }

        .profile-avatar-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .profile-avatar-fallback {
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, #0F172A 0%, #1E40AF 100%);
            color: #FFFFFF;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: clamp(3rem, 6vw, 4.5rem);
            font-weight: 700;
            user-select: none;
        }

        .avatar-edit-overlay {
            position: absolute;
            inset: 0;
            background: rgba(15, 23, 42, 0.65);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #FFFFFF;
            opacity: 0;
            transition: opacity 0.2s ease;
            cursor: pointer;
            border: 0;
            padding: 0;
            font-size: 0.85rem;
            gap: 0.35rem;
        }

        .profile-avatar-frame:hover .avatar-edit-overlay,
        .avatar-edit-overlay:focus-visible {
            opacity: 1;
        }

        .profile-names-block {
            margin-bottom: 0.5rem;
        }

        .profile-fullname {
            font-size: 1.75rem;
            font-weight: 700;
            line-height: 1.2;
            color: var(--text-strong);
            margin: 0 0 0.25rem 0;
            word-break: break-word;
        }

        .profile-handle {
            font-size: 1.1rem;
            font-weight: 400;
            color: var(--muted);
            margin-bottom: 0.75rem;
        }

        .profile-role-badge {
            display: inline-flex;
            align-items: center;
            padding: 0.25rem 0.65rem;
            border-radius: var(--radius-sm);
            font-size: 0.75rem;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            background: var(--surface-subtle);
            color: var(--text-strong);
        }

        .profile-role-badge.role-photographer {
            background: var(--accent-subtle);
            color: var(--accent);
        }

        .profile-bio-box {
            font-size: 0.95rem;
            line-height: 1.6;
            color: var(--text);
            white-space: pre-line;
            word-break: break-word;
        }

        .profile-meta-list {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
            font-size: 0.9rem;
            color: var(--muted);
            border-top: 1px solid var(--border);
            padding-top: 1.25rem;
            margin-top: 0.5rem;
        }

        .profile-meta-item {
            display: flex;
            align-items: center;
            gap: 0.65rem;
            word-break: break-all;
        }

        .profile-meta-item svg {
            flex-shrink: 0;
            color: var(--muted);
        }

        .profile-meta-item a {
            color: var(--text);
            text-decoration: none;
            transition: color 0.15s ease;
        }

        .profile-meta-item a:hover {
            color: var(--accent);
            text-decoration: underline;
        }

        .social-links-grid {
            display: flex;
            flex-direction: column;
            gap: 0.65rem;
        }

        .social-link-item {
            display: inline-flex;
            align-items: center;
            gap: 0.65rem;
            color: var(--text);
            text-decoration: none;
            font-size: 0.88rem;
            font-weight: 500;
            padding: 0.4rem 0.65rem;
            border-radius: var(--radius-sm);
            background: var(--surface);
            border: 1px solid var(--border);
            transition: all 0.18s ease;
        }

        .social-link-item:hover {
            color: var(--accent);
            border-color: var(--accent);
            background: var(--surface-hover);
            transform: translateX(2px);
        }

        .social-link-item svg {
            flex-shrink: 0;
        }

        /* ── Right Content Area (GitHub-Inspired 72%) ───────────────── */
        .profile-main-content {
            display: flex;
            flex-direction: column;
            gap: 2.5rem;
            min-width: 0;
        }

        .profile-nav-tabs {
            display: flex;
            align-items: center;
            gap: 1.5rem;
            border-bottom: 1px solid var(--border);
            margin-bottom: 1.5rem;
        }

        .profile-nav-tab {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.75rem 0.25rem;
            font-size: 0.95rem;
            font-weight: 600;
            color: var(--muted);
            text-decoration: none;
            border-bottom: 2px solid transparent;
            margin-bottom: -1px;
            cursor: pointer;
            transition: all 0.15s ease;
        }

        .profile-nav-tab:hover {
            color: var(--text-strong);
        }

        .profile-nav-tab.active {
            color: var(--accent);
            border-bottom-color: var(--accent);
        }

        .profile-tab-count {
            background: var(--surface-subtle);
            color: var(--text-strong);
            font-size: 0.75rem;
            padding: 0.15rem 0.5rem;
            border-radius: 50px;
        }

        /* ── Calendar Card & Controls ──────────────────────────────── */
        .calendar-card-shell {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: clamp(1.25rem, 3vw, 2rem);
            box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
        }

        .calendar-header-strip {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 1rem;
            margin-bottom: 1.5rem;
        }

        .calendar-legend-bar {
            display: flex;
            flex-wrap: wrap;
            gap: 0.75rem 1.25rem;
            align-items: center;
            font-size: 0.8rem;
            color: var(--muted);
            padding: 0.5rem 0.85rem;
            background: var(--surface-subtle);
            border-radius: var(--radius-sm);
        }

        .legend-indicator {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
        }

        .legend-dot {
            width: 9px;
            height: 9px;
            border-radius: 50%;
        }

        /* FullCalendar Custom Theming */
        .fc {
            font-family: inherit;
        }
        .fc .fc-toolbar-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-strong);
        }
        .fc .fc-button-primary {
            background-color: var(--surface);
            border-color: var(--border);
            color: var(--text);
            font-weight: 600;
            font-size: 0.85rem;
            border-radius: var(--radius-sm);
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
            transition: all 0.15s ease;
        }
        .fc .fc-button-primary:hover {
            background-color: var(--surface-hover);
            border-color: var(--border-strong);
            color: var(--text-strong);
        }
        .fc .fc-button-primary:not(:disabled).fc-button-active,
        .fc .fc-button-primary:not(:disabled):active {
            background-color: var(--accent);
            border-color: var(--accent);
            color: #FFFFFF;
        }
        .fc-theme-standard td, .fc-theme-standard th {
            border-color: var(--border);
        }
        .fc .fc-col-header-cell-cushion,
        .fc .fc-daygrid-day-number {
            color: var(--text);
            text-decoration: none;
        }
        .fc-daygrid-event {
            border-radius: 4px;
            padding: 2px 5px;
            font-size: 0.8rem;
            cursor: pointer;
            transition: transform 0.1s ease;
        }
        .fc-daygrid-event:hover {
            transform: scale(1.02);
        }
        .fc-timegrid-event {
            border-radius: 4px;
            padding: 3px 5px;
            cursor: pointer;
        }

        /* ── Portfolio Grid Section ────────────────────────────────── */
        .portfolio-gallery-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
            gap: 1.25rem;
        }

        .portfolio-work-card {
            position: relative;
            aspect-ratio: 4/5;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            background: #E2E8F0;
            border: 1px solid var(--pc-border, #CBD5E1);
            cursor: pointer;
        }

        .portfolio-work-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
        }

        .portfolio-work-card:hover .portfolio-work-img {
            transform: scale(1.04);
        }

        .portfolio-work-caption {
            position: absolute;
            inset: auto 0 0 0;
            padding: 1rem;
            background: linear-gradient(180deg, transparent 0%, rgba(15, 23, 42, 0.85) 100%);
            color: #FFFFFF;
            font-size: 0.82rem;
            line-height: 1.4;
        }

        @media (max-width: 991px) {
            .profile-layout-grid {
                grid-template-columns: 1fr;
            }
            .profile-sidebar {
                align-items: center;
                text-align: center;
            }
            .profile-meta-list {
                align-items: center;
            }
            .social-links-grid {
                flex-direction: row;
                flex-wrap: wrap;
                justify-content: center;
            }
        }
    </style>
</head>

<main class="profile-page-region">
    <div class="editorial-container">

        <!-- Flash Feedback Alerts -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success pc-alert pc-alert-success mb-4 rounded-3" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger pc-alert pc-alert-danger mb-4 rounded-3" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty securitySuccess}">
            <div class="alert alert-success pc-alert pc-alert-success mb-4 rounded-3" role="status">
                <c:out value="${securitySuccess}"/>
            </div>
        </c:if>
        <c:if test="${not empty securityError}">
            <div class="alert alert-danger pc-alert pc-alert-danger mb-4 rounded-3" role="alert">
                <c:out value="${securityError}"/>
            </div>
        </c:if>

        <div class="profile-layout-grid">

            <!-- ── Left Sidebar (GitHub Reference 28%) ────────────────── -->
            <aside class="profile-sidebar" aria-label="User identity sidebar">

                <!-- Avatar Frame with Deterministic Fallback & Owner Trigger -->
                <div class="profile-avatar-frame">
                    <c:choose>
                        <c:when test="${not empty profile.avatarUrl}">
                            <img src="<c:out value='${profile.avatarUrl}'/>"
                                 alt="<c:out value='${profile.displayName}'/> avatar"
                                 class="profile-avatar-img"
                                 onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=240&h=240&q=80';">
                        </c:when>
                        <c:otherwise>
                            <div class="profile-avatar-fallback">
                                <c:out value="${profile.initial}"/>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${isOwner}">
                        <button type="button"
                                class="avatar-edit-overlay"
                                data-bs-toggle="modal"
                                data-bs-target="#avatarModal"
                                title="Change avatar photo"
                                aria-label="Change avatar photo">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
                            <span>Edit Photo</span>
                        </button>
                    </c:if>
                </div>

                <!-- Names & Role -->
                <div class="profile-names-block w-100">
                    <h1 class="profile-fullname">
                        <c:out value="${profile.displayName}"/>
                    </h1>
                    <div class="profile-handle">
                        @<c:out value="${profile.username}"/>
                    </div>
                    <div>
                        <span class="profile-role-badge <c:if test="${profile.role == 'PHOTOGRAPHER'}">role-photographer</c:if>">
                            <c:out value="${profile.role}"/>
                        </span>
                    </div>
                </div>

                <!-- Biography -->
                <div class="profile-bio-box w-100">
                    <c:choose>
                        <c:when test="${not empty profile.bio}">
                            <c:out value="${profile.bio}"/>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted fst-italic">
                                <c:choose>
                                    <c:when test="${isOwner}">Add a short bio to introduce yourself to the PhotoConnect community.</c:when>
                                    <c:otherwise>No biography provided yet.</c:otherwise>
                                </c:choose>
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Action Button for Owner / Visitor -->
                <div class="w-100">
                    <c:choose>
                        <c:when test="${isOwner}">
                            <button type="button"
                                    class="btn btn-outline-secondary w-100 fw-semibold"
                                    data-bs-toggle="modal"
                                    data-bs-target="#editProfileModal"
                                    id="btn-open-edit-profile">
                                Edit Profile
                            </button>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${profile.role == 'PHOTOGRAPHER' && not empty profile.photographerProfileId}">
                                <a href="${pageContext.request.contextPath}/photographers/${profile.photographerProfileId}/book"
                                   class="btn btn-primary w-100 fw-semibold"
                                   id="btn-visitor-book">
                                    Request Booking
                                </a>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Metadata & Social Media Links -->
                <div class="profile-meta-list w-100">
                    <c:if test="${not empty profile.location}">
                        <div class="profile-meta-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                            <span><c:out value="${profile.location}"/></span>
                        </div>
                    </c:if>

                    <c:if test="${isOwner && not empty profile.email}">
                        <div class="profile-meta-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"></rect><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path></svg>
                            <span><c:out value="${profile.email}"/></span>
                        </div>
                    </c:if>

                    <c:if test="${isOwner && not empty profile.phone}">
                        <div class="profile-meta-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                            <span><c:out value="${profile.phone}"/></span>
                        </div>
                    </c:if>
                </div>

                <!-- Verified External Social Links -->
                <c:if test="${not empty profile.websiteUrl or not empty profile.instagramUrl or not empty profile.facebookUrl or not empty profile.tiktokUrl or not empty profile.youtubeUrl}">
                    <div class="social-links-grid w-100">
                        <c:if test="${not empty profile.websiteUrl}">
                            <a href="<c:out value='${profile.websiteUrl}'/>" target="_blank" rel="noopener noreferrer" class="social-link-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
                                <span>Website</span>
                            </a>
                        </c:if>

                        <c:if test="${not empty profile.instagramUrl}">
                            <a href="<c:out value='${profile.instagramUrl}'/>" target="_blank" rel="noopener noreferrer" class="social-link-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line></svg>
                                <span>Instagram</span>
                            </a>
                        </c:if>

                        <c:if test="${not empty profile.facebookUrl}">
                            <a href="<c:out value='${profile.facebookUrl}'/>" target="_blank" rel="noopener noreferrer" class="social-link-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path></svg>
                                <span>Facebook</span>
                            </a>
                        </c:if>

                        <c:if test="${not empty profile.tiktokUrl}">
                            <a href="<c:out value='${profile.tiktokUrl}'/>" target="_blank" rel="noopener noreferrer" class="social-link-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5"></path></svg>
                                <span>TikTok</span>
                            </a>
                        </c:if>

                        <c:if test="${not empty profile.youtubeUrl}">
                            <a href="<c:out value='${profile.youtubeUrl}'/>" target="_blank" rel="noopener noreferrer" class="social-link-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22.54 6.42a2.78 2.78 0 0 0-1.94-2C18.88 4 12 4 12 4s-6.88 0-8.6.46a2.78 2.78 0 0 0-1.94 2A29 29 0 0 0 1 11.75a29 29 0 0 0 .46 5.33A2.78 2.78 0 0 0 3.4 19c1.72.46 8.6.46 8.6.46s6.88 0 8.6-.46a2.78 2.78 0 0 0 1.94-2 29 29 0 0 0 .46-5.25 29 29 0 0 0-.46-5.33z"></path><polygon points="9.75 15.02 15.5 11.75 9.75 8.48 9.75 15.02"></polygon></svg>
                                <span>YouTube</span>
                            </a>
                        </c:if>
                    </div>
                </c:if>

            </aside>

            <!-- ── Right Content (GitHub Reference 72%) ───────────────── -->
            <section class="profile-main-content">

                <!-- Tab Headers -->
                <div class="profile-nav-tabs" role="tablist">
                    <button type="button" class="profile-nav-tab active btn" id="tab-calendar" onclick="switchProfileTab('calendar')">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                        Schedule &amp; Availability
                    </button>

                    <c:if test="${profile.role == 'PHOTOGRAPHER' && not empty profile.portfolioImages}">
                        <button type="button" class="profile-nav-tab btn" id="tab-portfolio" onclick="switchProfileTab('portfolio')">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                            Portfolio Works
                            <span class="profile-tab-count"><c:out value="${fn:length(profile.portfolioImages)}"/></span>
                        </button>
                    </c:if>

                    <c:if test="${isOwner}">
                        <button type="button" class="profile-nav-tab btn" id="tab-security" onclick="switchProfileTab('security')">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                            Bảo Mật Tài Khoản
                        </button>
                    </c:if>
                </div>

                <!-- Tab Pane 1: Interactive FullCalendar Component -->
                <div id="pane-calendar" class="calendar-card-shell">
                    <div class="calendar-header-strip">
                        <div>
                            <h2 style="font-size: 1.35rem; font-weight: 700; color: var(--text-strong); margin: 0 0 0.25rem 0;">
                                Interactive Availability Calendar
                            </h2>
                            <p style="font-size: 0.88rem; color: var(--muted); margin: 0;">
                                <c:choose>
                                    <c:when test="${isOwner}">Manage your shoot availability, blocked hours, and personal schedule.</c:when>
                                    <c:otherwise>View public open shoot slots and booked dates.</c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div class="d-flex align-items-center gap-2">
                            <c:if test="${isOwner}">
                                <button type="button"
                                        class="btn btn-primary btn-sm d-inline-flex align-items-center gap-1"
                                        onclick="openAddEventModal()"
                                        id="btn-add-event-manual">
                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                                    Add Event
                                </button>
                            </c:if>
                        </div>
                    </div>

                    <!-- Calendar Legend Bar -->
                    <div class="calendar-legend-bar mb-3">
                        <span class="fw-semibold">Legend:</span>
                        <div class="legend-indicator">
                            <span class="legend-dot" style="background:#10B981;"></span>
                            <span>Available Slot</span>
                        </div>
                        <div class="legend-indicator">
                            <span class="legend-dot" style="background:#2563EB;"></span>
                            <span>Session / Shoot</span>
                        </div>
                        <div class="legend-indicator">
                            <span class="legend-dot" style="background:#0F172A;"></span>
                            <span>Confirmed / Booked</span>
                        </div>
                        <div class="legend-indicator">
                            <span class="legend-dot" style="background:#94A3B8;"></span>
                            <span>Blocked / Day Off</span>
                        </div>
                        <c:if test="${isOwner}">
                            <div class="legend-indicator">
                                <span class="legend-dot" style="background:#6366F1;"></span>
                                <span>Personal Event</span>
                            </div>
                        </c:if>
                    </div>

                    <!-- FullCalendar Canvas -->
                    <div id="calendarMount" style="min-height: 580px;"></div>
                </div>

                <!-- Tab Pane 2: Portfolio Works Grid (if Photographer) -->
                <c:if test="${profile.role == 'PHOTOGRAPHER' && not empty profile.portfolioImages}">
                    <div id="pane-portfolio" style="display: none;">
                        <div class="portfolio-gallery-grid">
                            <c:forEach var="item" items="${profile.portfolioImages}">
                                <div class="portfolio-work-card">
                                    <img src="<c:out value='${not empty item.thumbnailUrl ? item.thumbnailUrl : item.imageUrl}'/>"
                                         alt="<c:out value='${item.caption != null ? item.caption : "Portfolio work"}'/>"
                                         class="portfolio-work-img"
                                         loading="lazy">
                                    <c:if test="${not empty item.caption}">
                                        <div class="portfolio-work-caption">
                                            <c:out value="${item.caption}"/>
                                        </div>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <!-- Tab Pane 3: Security & Account Management (if Owner) -->
                <c:if test="${isOwner}">
                    <div id="pane-security" class="calendar-card-shell" style="display: none;">
                        <h2 style="font-size: 1.35rem; font-weight: 700; color: var(--text-strong); margin: 0 0 1.5rem 0;">
                            Bảo Mật &amp; Cài Đặt Tài Khoản
                        </h2>

                        <div class="row g-4">
                            <!-- Section 1: Change Password -->
                            <div class="col-12 col-lg-6">
                                <div class="card h-100 border p-4 rounded-3 shadow-none bg-light">
                                    <h5 class="fw-bold mb-3 d-flex align-items-center gap-2">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                                        Đổi Mật Khẩu
                                    </h5>
                                    <form action="${pageContext.request.contextPath}/profile/security/password" method="post">
                                        <%@ include file="fragments/csrf-input.jsp" %>
                                        <div class="mb-3">
                                            <label class="form-label small fw-semibold">Mật khẩu hiện tại</label>
                                            <input type="password" name="currentPassword" class="form-control" required placeholder="••••••••" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label small fw-semibold">Mật khẩu mới (tối thiểu 8 ký tự)</label>
                                            <input type="password" name="newPassword" minlength="8" class="form-control" required placeholder="••••••••" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label small fw-semibold">Xác nhận mật khẩu mới</label>
                                            <input type="password" name="confirmPassword" minlength="8" class="form-control" required placeholder="••••••••" />
                                        </div>
                                        <button type="submit" class="btn btn-primary btn-sm">Lưu Mật Khẩu</button>
                                    </form>
                                </div>
                            </div>

                            <!-- Section 2: Change Email with OTP -->
                            <div class="col-12 col-lg-6">
                                <div class="card h-100 border p-4 rounded-3 shadow-none bg-light">
                                    <h5 class="fw-bold mb-3 d-flex align-items-center gap-2">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                                        Cập Nhật Email
                                    </h5>
                                    <p class="small text-muted mb-3">Email hiện tại: <strong><c:out value="${profile.email}"/></strong></p>

                                    <c:choose>
                                        <c:when test="${emailChangePending}">
                                            <form action="${pageContext.request.contextPath}/profile/security/email/confirm" method="post">
                                                <%@ include file="fragments/csrf-input.jsp" %>
                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Nhập mã OTP 6 chữ số gửi tới email</label>
                                                    <input type="text" name="otpCode" maxlength="6" class="form-control font-monospace text-center fw-bold fs-5" required placeholder="••••••" />
                                                    <div class="form-text small">Mã OTP đã được gửi tới email cũ của bạn để xác nhận cập nhật thành: <strong><c:out value="${targetNewEmail}"/></strong>.</div>
                                                </div>
                                                <button type="submit" class="btn btn-primary btn-sm">Xác Nhận Đổi Email</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="${pageContext.request.contextPath}/profile/security/email/request" method="post">
                                                <%@ include file="fragments/csrf-input.jsp" %>
                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Địa chỉ email mới</label>
                                                    <input type="email" name="newEmail" class="form-control" required placeholder="new.email@example.com" />
                                                    <div class="form-text small">Hệ thống sẽ gửi mã OTP xác nhận tới email của bạn.</div>
                                                </div>
                                                <button type="submit" class="btn btn-outline-primary btn-sm">Gửi Mã Xác Thực OTP</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- Section 3: Deactivate Account -->
                            <div class="col-12">
                                <div class="card border border-danger p-4 rounded-3 shadow-none bg-white">
                                    <h5 class="fw-bold text-danger mb-2">Vùng Nguy Hiểm: Vô Hiệu Hóa Tài Khoản</h5>
                                    <p class="small text-muted mb-3">
                                        Khi vô hiệu hóa tài khoản, bạn sẽ bị đăng xuất khỏi hệ thống ngay lập tức và hồ sơ sẽ bị tạm ẩn khỏi danh mục tìm kiếm. Bạn có thể liên hệ quản trị viên để mở lại.
                                    </p>
                                    <form action="${pageContext.request.contextPath}/profile/security/deactivate" method="post" onsubmit="return confirm('Bạn có chắc chắn muốn vô hiệu hóa tài khoản này? Bạn sẽ bị đăng xuất ngay lập tức.');">
                                        <%@ include file="fragments/csrf-input.jsp" %>
                                        <button type="submit" class="btn btn-outline-danger btn-sm">Vô Hiệu Hóa Tài Khoản</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

            </section>
        </div>

    </div>
</main>

<!-- ── Edit Profile Modal ─────────────────────────────────────────── -->
<c:if test="${isOwner}">
    <div class="modal fade" id="editProfileModal" tabindex="-1" aria-labelledby="editProfileModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow">
                <form action="${pageContext.request.contextPath}/profile/edit" method="post" id="profileEditForm">
                    <%@ include file="fragments/csrf-input.jsp" %>
                    <div class="modal-header">
                        <h5 class="modal-title fw-bold" id="editProfileModalLabel">Edit Profile Information</h5>
                        <button type="button" class="btn-close btn" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body p-4">
                        <div class="row g-3">
                            <div class="col-12 col-md-6">
                                <label for="editDisplayName" class="form-label fw-semibold">Display Name *</label>
                                <input type="text"
                                       class="form-control"
                                       id="editDisplayName"
                                       name="displayName"
                                       value="<c:out value='${profile.displayName}'/>"
                                       maxlength="150"
                                       required>
                            </div>

                            <div class="col-12 col-md-6">
                                <label for="editUsername" class="form-label fw-semibold">Username *</label>
                                <div class="input-group">
                                    <span class="input-group-text">@</span>
                                    <input type="text"
                                           class="form-control"
                                           id="editUsername"
                                           name="username"
                                           value="<c:out value='${profile.username}'/>"
                                           maxlength="100"
                                           required>
                                </div>
                            </div>

                            <div class="col-12">
                                <label for="editLocation" class="form-label fw-semibold">Location / Region</label>
                                <input type="text"
                                       class="form-control"
                                       id="editLocation"
                                       name="location"
                                       placeholder="e.g. Ho Chi Minh City, Vietnam"
                                       value="<c:out value='${profile.location}'/>"
                                       maxlength="150">
                            </div>

                            <div class="col-12">
                                <label for="editBio" class="form-label fw-semibold">Biography</label>
                                <textarea class="form-control"
                                          id="editBio"
                                          name="bio"
                                          rows="4"
                                          maxlength="1000"
                                          placeholder="Tell clients and collaborators about your photography journey, style, and creative vision..."><c:out value='${profile.bio}'/></textarea>
                                <div class="form-text">Supports multiple lines (max 1000 characters).</div>
                            </div>

                            <!-- Social Links Section -->
                            <div class="col-12 mt-4">
                                <h6 class="fw-bold border-bottom pb-2">Social &amp; Web Links</h6>
                            </div>

                            <div class="col-12 col-md-6">
                                <label for="editWebsiteUrl" class="form-label small fw-semibold">Personal Website</label>
                                <input type="url" class="form-control" id="editWebsiteUrl" name="websiteUrl" placeholder="https://example.com" value="<c:out value='${profile.websiteUrl}'/>">
                            </div>

                            <div class="col-12 col-md-6">
                                <label for="editInstagramUrl" class="form-label small fw-semibold">Instagram URL</label>
                                <input type="url" class="form-control" id="editInstagramUrl" name="instagramUrl" placeholder="https://instagram.com/username" value="<c:out value='${profile.instagramUrl}'/>">
                            </div>

                            <div class="col-12 col-md-6">
                                <label for="editFacebookUrl" class="form-label small fw-semibold">Facebook URL</label>
                                <input type="url" class="form-control" id="editFacebookUrl" name="facebookUrl" placeholder="https://facebook.com/username" value="<c:out value='${profile.facebookUrl}'/>">
                            </div>

                            <div class="col-12 col-md-6">
                                <label for="editTiktokUrl" class="form-label small fw-semibold">TikTok URL</label>
                                <input type="url" class="form-control" id="editTiktokUrl" name="tiktokUrl" placeholder="https://tiktok.com/@username" value="<c:out value='${profile.tiktokUrl}'/>">
                            </div>

                            <div class="col-12">
                                <label for="editYoutubeUrl" class="form-label small fw-semibold">YouTube URL</label>
                                <input type="url" class="form-control" id="editYoutubeUrl" name="youtubeUrl" placeholder="https://youtube.com/@channel" value="<c:out value='${profile.youtubeUrl}'/>">
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">Save Profile Changes</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ── Avatar Upload Modal ────────────────────────────────────────── -->
    <div class="modal fade" id="avatarModal" tabindex="-1" aria-labelledby="avatarModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold" id="avatarModalLabel">Update Avatar Photo</h5>
                    <button type="button" class="btn-close btn" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4 text-center">
                    <form action="${pageContext.request.contextPath}/profile/avatar" method="post" enctype="multipart/form-data" id="avatarUploadForm">
                        <%@ include file="fragments/csrf-input.jsp" %>
                        <div class="mb-3">
                            <label for="avatarFileInput" class="form-label fw-semibold">Select Image (JPG, PNG, WebP &bull; max 5MB)</label>
                            <input class="form-control" type="file" id="avatarFileInput" name="avatarFile" accept="image/jpeg,image/png,image/webp" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100 mb-2">Upload to Cloudinary</button>
                    </form>

                    <c:if test="${not empty profile.avatarUrl}">
                        <hr class="my-3">
                        <form action="${pageContext.request.contextPath}/profile/avatar/remove" method="post">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <button type="submit" class="btn btn-outline-danger btn-sm w-100" onclick="return confirm('Remove your current avatar?');">
                                Remove Current Avatar
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- ── Event Editor / Create Modal ───────────────────────────────── -->
    <div class="modal fade" id="eventEditorModal" tabindex="-1" aria-labelledby="eventEditorModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold" id="eventEditorModalLabel">Calendar Event</h5>
                    <button type="button" class="btn-close btn" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <input type="hidden" id="eventEditId" value="">
                    <div class="mb-3">
                        <label for="eventTitle" class="form-label fw-semibold">Event Title *</label>
                        <input type="text" class="form-control" id="eventTitle" placeholder="e.g. Portrait Shoot Prep, Available for Shoots" maxlength="200" required>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label for="eventStart" class="form-label fw-semibold small">Start Time *</label>
                            <input type="datetime-local" class="form-control form-control-sm" id="eventStart" required>
                        </div>
                        <div class="col-6">
                            <label for="eventEnd" class="form-label fw-semibold small">End Time *</label>
                            <input type="datetime-local" class="form-control form-control-sm" id="eventEnd" required>
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label for="eventType" class="form-label fw-semibold small">Event Type</label>
                            <select class="form-select form-select-sm" id="eventType">
                                <option value="PERSONAL">Personal Note</option>
                                <option value="AVAILABLE">Available Slot</option>
                                <option value="BLOCKED">Blocked Time</option>
                                <option value="SESSION">Session Work</option>
                            </select>
                        </div>
                        <div class="col-6">
                            <label for="eventVisibility" class="form-label fw-semibold small">Visibility</label>
                            <select class="form-select form-select-sm" id="eventVisibility">
                                <option value="PRIVATE">Private (Only Me)</option>
                                <option value="PUBLIC">Public</option>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="eventDescription" class="form-label fw-semibold small">Description / Notes</label>
                        <textarea class="form-control" id="eventDescription" rows="3" maxlength="1000" placeholder="Optional notes..."></textarea>
                    </div>

                    <div id="eventErrorMessage" class="text-danger small mb-2" style="display:none;"></div>
                </div>
                <div class="modal-footer bg-light justify-content-between">
                    <button type="button" class="btn btn-outline-danger btn-sm" id="btnDeleteEvent" style="display:none;" onclick="handleDeleteEvent()">
                        Delete
                    </button>
                    <div class="d-flex gap-2 ms-auto">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-primary btn-sm" id="btnSaveEvent" onclick="handleSaveEvent()">Save Event</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</c:if>

<!-- ── Read-only Event Details Modal (Visitors / Bookings) ────────── -->
<div class="modal fade" id="eventDetailsModal" tabindex="-1" aria-labelledby="eventDetailsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="eventDetailsModalLabel">Schedule Item</h5>
                <button type="button" class="btn-close btn" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-4">
                <h5 id="viewEventTitle" class="fw-bold mb-2"></h5>
                <div id="viewEventTime" class="text-muted small mb-3"></div>
                <div id="viewEventDescription" class="mb-3" style="line-height:1.6;"></div>
                <div id="viewEventStatus" class="mb-3"></div>
            </div>
            <div class="modal-footer bg-light">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<script>
    const TARGET_USER_ID = ${profile.userId};
    const IS_OWNER = ${isOwner};
    const CONTEXT_PATH = '${pageContext.request.contextPath}';

    let calendarInstance = null;

    document.addEventListener('DOMContentLoaded', function() {
        initFullCalendar();
        if (window.location.hash === '#security') {
            switchProfileTab('security');
        }
    });

    function initFullCalendar() {
        const calendarEl = document.getElementById('calendarMount');
        if (!calendarEl) return;

        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="csrf-header"]')?.getAttribute('content');

        calendarInstance = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek'
            },
            themeSystem: 'standard',
            height: 'auto',
            navLinks: true,
            selectable: IS_OWNER,
            selectMirror: true,
            dayMaxEvents: true,
            events: function(fetchInfo, successCallback, failureCallback) {
                const endpoint = IS_OWNER
                    ? CONTEXT_PATH + '/api/me/calendar'
                    : CONTEXT_PATH + '/api/users/' + TARGET_USER_ID + '/calendar';

                fetch(endpoint, {
                    headers: { 'Accept': 'application/json' }
                })
                .then(res => res.json())
                .then(data => {
                    successCallback(data);
                })
                .catch(err => {
                    console.error('Failed to fetch calendar events', err);
                    failureCallback(err);
                });
            },
            select: function(arg) {
                if (!IS_OWNER) return;
                openAddEventModal(arg.startStr, arg.endStr);
            },
            eventClick: function(info) {
                const props = info.event.extendedProps || {};
                const isBooking = props.isBooking;
                const editable = props.editable;

                if (IS_OWNER && editable && props.manualEventId) {
                    openEditEventModal(info.event);
                } else {
                    openViewEventModal(info.event);
                }
            }
        });

        calendarInstance.render();
    }

    function switchProfileTab(tab) {
        document.getElementById('tab-calendar')?.classList.toggle('active', tab === 'calendar');
        document.getElementById('tab-portfolio')?.classList.toggle('active', tab === 'portfolio');
        document.getElementById('tab-security')?.classList.toggle('active', tab === 'security');

        const paneCal = document.getElementById('pane-calendar');
        const panePort = document.getElementById('pane-portfolio');
        const paneSec = document.getElementById('pane-security');

        if (paneCal) paneCal.style.display = tab === 'calendar' ? 'block' : 'none';
        if (panePort) panePort.style.display = tab === 'portfolio' ? 'block' : 'none';
        if (paneSec) paneSec.style.display = tab === 'security' ? 'block' : 'none';

        if (tab === 'calendar' && calendarInstance) {
            setTimeout(() => calendarInstance.updateSize(), 50);
        }
    }

    function openAddEventModal(startDateStr, endDateStr) {
        if (!IS_OWNER) return;
        document.getElementById('eventEditId').value = '';
        document.getElementById('eventTitle').value = '';
        document.getElementById('eventDescription').value = '';
        document.getElementById('eventType').value = 'PERSONAL';
        document.getElementById('eventVisibility').value = 'PRIVATE';
        document.getElementById('eventErrorMessage').style.display = 'none';
        document.getElementById('btnDeleteEvent').style.display = 'none';
        document.getElementById('eventEditorModalLabel').textContent = 'Create Calendar Event';

        const now = new Date();
        const start = startDateStr ? formatToLocalDatetimeInput(new Date(startDateStr)) : formatToLocalDatetimeInput(now);
        const end = endDateStr ? formatToLocalDatetimeInput(new Date(endDateStr)) : formatToLocalDatetimeInput(new Date(now.getTime() + 60*60*1000));

        document.getElementById('eventStart').value = start;
        document.getElementById('eventEnd').value = end;

        const modal = new bootstrap.Modal(document.getElementById('eventEditorModal'));
        modal.show();
    }

    function openEditEventModal(calEvent) {
        if (!IS_OWNER) return;
        const props = calEvent.extendedProps || {};
        document.getElementById('eventEditId').value = props.manualEventId || '';
        document.getElementById('eventTitle').value = props.rawTitle || calEvent.title || '';
        document.getElementById('eventDescription').value = props.rawDescription || calEvent.description || '';
        document.getElementById('eventType').value = props.eventType || 'PERSONAL';
        document.getElementById('eventVisibility').value = props.visibility || 'PRIVATE';
        document.getElementById('eventErrorMessage').style.display = 'none';
        document.getElementById('btnDeleteEvent').style.display = 'inline-block';
        document.getElementById('eventEditorModalLabel').textContent = 'Edit Calendar Event';

        if (calEvent.start) {
            document.getElementById('eventStart').value = formatToLocalDatetimeInput(calEvent.start);
        }
        if (calEvent.end) {
            document.getElementById('eventEnd').value = formatToLocalDatetimeInput(calEvent.end);
        } else if (calEvent.start) {
            document.getElementById('eventEnd').value = formatToLocalDatetimeInput(new Date(calEvent.start.getTime() + 60*60*1000));
        }

        const modal = new bootstrap.Modal(document.getElementById('eventEditorModal'));
        modal.show();
    }

    function openViewEventModal(calEvent) {
        const props = calEvent.extendedProps || {};
        document.getElementById('viewEventTitle').textContent = calEvent.title;

        let timeStr = '';
        if (calEvent.start) {
            timeStr = calEvent.start.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
            if (calEvent.end) {
                timeStr += ' – ' + calEvent.end.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
            }
        }
        document.getElementById('viewEventTime').textContent = timeStr;

        const desc = calEvent.extendedProps?.description || '';
        document.getElementById('viewEventDescription').textContent = desc;

        const statusEl = document.getElementById('viewEventStatus');
        statusEl.textContent = '';
        if (props.isBooking && props.bookingStatus) {
            const badge = document.createElement('span');
            badge.className = 'badge bg-primary';
            badge.textContent = 'Status: ' + props.bookingStatus;
            statusEl.appendChild(badge);
        }

        const modal = new bootstrap.Modal(document.getElementById('eventDetailsModal'));
        modal.show();
    }

    function handleSaveEvent() {
        const eventId = document.getElementById('eventEditId').value;
        const title = document.getElementById('eventTitle').value.trim();
        const startAt = document.getElementById('eventStart').value;
        const endAt = document.getElementById('eventEnd').value;
        const eventType = document.getElementById('eventType').value;
        const visibility = document.getElementById('eventVisibility').value;
        const description = document.getElementById('eventDescription').value.trim();

        const errEl = document.getElementById('eventErrorMessage');
        if (!title) {
            errEl.textContent = 'Event title is required.';
            errEl.style.display = 'block';
            return;
        }
        if (!startAt || !endAt || new Date(endAt) <= new Date(startAt)) {
            errEl.textContent = 'End time must be after start time.';
            errEl.style.display = 'block';
            return;
        }

        const payload = { title, startAt, endAt, eventType, visibility, description };
        const url = eventId
            ? CONTEXT_PATH + '/api/me/calendar/events/' + eventId
            : CONTEXT_PATH + '/api/me/calendar/events';
        const method = eventId ? 'PUT' : 'POST';

        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="csrf-header"]')?.getAttribute('content');
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch(url, {
            method: method,
            headers: headers,
            body: JSON.stringify(payload)
        })
        .then(async res => {
            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                throw new Error(data.error || 'Failed to save event');
            }
            return res.json();
        })
        .then(saved => {
            bootstrap.Modal.getInstance(document.getElementById('eventEditorModal')).hide();
            if (calendarInstance) calendarInstance.refetchEvents();
        })
        .catch(err => {
            errEl.textContent = err.message;
            errEl.style.display = 'block';
        });
    }

    function handleDeleteEvent() {
        const eventId = document.getElementById('eventEditId').value;
        if (!eventId || !confirm('Are you sure you want to delete this event?')) return;

        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="csrf-header"]')?.getAttribute('content');
        const headers = {};
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch(CONTEXT_PATH + '/api/me/calendar/events/' + eventId, {
            method: 'DELETE',
            headers: headers
        })
        .then(res => {
            if (!res.ok) throw new Error('Failed to delete event');
            bootstrap.Modal.getInstance(document.getElementById('eventEditorModal')).hide();
            if (calendarInstance) calendarInstance.refetchEvents();
        })
        .catch(err => {
            alert(err.message);
        });
    }

    function formatToLocalDatetimeInput(date) {
        const pad = num => String(num).padStart(2, '0');
        const year = date.getFullYear();
        const month = pad(date.getMonth() + 1);
        const day = pad(date.getDate());
        const hours = pad(date.getHours());
        const minutes = pad(date.getMinutes());
        return year + '-' + month + '-' + day + 'T' + hours + ':' + minutes;
    }
</script>
