<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<head>
    <title>Studio — PhotoConnect</title>
    <style>
        .studio-shell { padding: clamp(2rem, 5vw, 4.5rem) 0 5rem; }
        .studio-header { display: flex; justify-content: space-between; align-items: flex-end; gap: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border); }
        .studio-kicker { color: var(--accent); font-size: .78rem; font-weight: 700; letter-spacing: .13em; text-transform: uppercase; }
        .studio-title { margin: .45rem 0 .35rem; font-family: var(--font-primary); font-size: clamp(2rem, 4vw, 3.5rem); letter-spacing: -.04em; }
        .studio-subtitle { margin: 0; color: var(--muted); max-width: 42rem; }
        .studio-header-actions, .studio-actions { display: flex; flex-wrap: wrap; gap: .65rem; }
        .studio-layout { display: grid; grid-template-columns: minmax(0, 1.65fr) minmax(280px, .75fr); gap: clamp(1.5rem, 4vw, 3rem); margin-top: 2rem; align-items: start; }
        .studio-main, .studio-rail { min-width: 0; display: grid; gap: 2rem; }
        .studio-panel { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: clamp(1.25rem, 3vw, 2rem); box-shadow: var(--shadow-subtle); }
        .studio-panel-head { display: flex; justify-content: space-between; gap: 1rem; align-items: baseline; margin-bottom: 1.25rem; }
        .studio-panel-head h2 { margin: 0; font-family: var(--font-primary); font-size: clamp(1.25rem, 2vw, 1.65rem); letter-spacing: -.02em; }
        .studio-panel-head a { white-space: nowrap; }
        .studio-attention-list { display: grid; gap: .75rem; }
        .studio-attention { display: grid; grid-template-columns: 2.25rem minmax(0, 1fr) auto; gap: 1rem; align-items: center; padding: 1rem 0; border-top: 1px solid var(--border); }
        .studio-attention:first-child { border-top: 0; padding-top: 0; }
        .studio-priority { display: grid; place-items: center; width: 2.25rem; height: 2.25rem; border: 1px solid var(--border-strong); border-radius: 50%; color: var(--accent); font-weight: 700; }
        .studio-attention h3, .studio-booking h3 { margin: 0 0 .3rem; font-size: 1rem; }
        .studio-attention p, .studio-booking p { margin: 0; color: var(--muted); font-size: .9rem; }
        .studio-bookings { display: grid; gap: 1rem; }
        .studio-booking { padding: 1.15rem; border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--surface-subtle); }
        .studio-booking-top { display: flex; justify-content: space-between; gap: 1rem; align-items: flex-start; }
        .studio-meta { display: flex; flex-wrap: wrap; gap: .4rem 1rem; margin: .75rem 0; color: var(--muted); font-size: .88rem; }
        .studio-context { padding-top: .75rem; border-top: 1px solid var(--border); }
        .studio-booking-actions { display: flex; flex-wrap: wrap; gap: .55rem; margin-top: 1rem; align-items: center; }
        .studio-booking-actions form { margin: 0; }
        .studio-health-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1px; background: var(--border); border: 1px solid var(--border); }
        .studio-health { background: var(--surface); padding: 1.25rem; }
        .studio-health-label { color: var(--muted); font-size: .78rem; text-transform: uppercase; letter-spacing: .08em; }
        .studio-health-value { display: block; margin-top: .4rem; font-size: 1.4rem; font-weight: 700; }
        .studio-progress { height: .4rem; margin: 1rem 0 .75rem; background: var(--surface-subtle); border-radius: 0; }
        .studio-progress .progress-bar { background: var(--accent); }
        .studio-preview { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: .6rem; }
        .studio-preview figure { margin: 0; aspect-ratio: 4 / 5; overflow: hidden; background: var(--surface-subtle); border: 1px solid var(--border); }
        .studio-preview img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform var(--transition-smooth); }
        .studio-preview a:hover img, .studio-preview a:focus-visible img { transform: scale(1.035); }
        .studio-empty { padding: 1.5rem; border: 1px dashed var(--border-strong); background: var(--surface-subtle); }
        .studio-empty h3 { margin: 0 0 .5rem; font-size: 1.05rem; }
        .studio-empty p { color: var(--muted); margin: 0 0 1rem; }
        .studio-status { display: inline-flex; align-items: center; gap: .35rem; color: var(--muted); font-size: .82rem; }
        .studio-status::before { content: ''; width: .5rem; height: .5rem; border-radius: 50%; background: var(--accent); }
        .studio-status.PAID::before, .studio-status.APPROVED::before { background: var(--success); }
        .studio-status.FAILED::before, .studio-status.CANCELLED::before { background: var(--danger); }
        .studio-quick-links { display: grid; }
        .studio-quick-links a { display: flex; justify-content: space-between; gap: 1rem; padding: .85rem 0; border-top: 1px solid var(--border); color: var(--text); text-decoration: none; }
        .studio-quick-links a:first-child { border-top: 0; padding-top: 0; }
        .studio-quick-links a:hover, .studio-quick-links a:focus-visible { color: var(--accent); }
        @media (max-width: 1024px) { .studio-layout { grid-template-columns: minmax(0, 1.4fr) minmax(260px, .8fr); gap: 1.5rem; } }
        @media (max-width: 768px) {
            .studio-header { align-items: flex-start; flex-direction: column; }
            .studio-layout { grid-template-columns: 1fr; }
            .studio-rail { grid-row: auto; }
            .studio-attention { grid-template-columns: 2.25rem minmax(0, 1fr); }
            .studio-attention > :last-child { grid-column: 2; justify-self: start; }
        }
        @media (max-width: 480px) {
            .studio-shell { padding-top: 1.5rem; }
            .studio-header-actions, .studio-header-actions .btn { width: 100%; }
            .studio-panel { padding: 1rem; }
            .studio-panel-head { align-items: flex-start; flex-direction: column; }
            .studio-booking-top { flex-direction: column; }
            .studio-health-grid { grid-template-columns: 1fr; }
            .studio-preview { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .studio-booking-actions .btn, .studio-booking-actions form { flex: 1 1 auto; }
            .studio-booking-actions form .btn { width: 100%; }
        }
    </style>
</head>

<main class="studio-shell">
    <div class="editorial-container pc-container-wide">
        <header class="studio-header">
            <div>
                <span class="studio-kicker">Photographer Studio</span>
                <h1 class="studio-title"><c:out value="${dashboard.professionalName}"/></h1>
                <p class="studio-subtitle">Manage your bookings, portfolio and availability.</p>
                <span class="studio-status ${dashboard.verificationStatus}" style="margin-top:.8rem;">
                    Profile status: <c:out value="${dashboard.verificationStatus}"/>
                </span>
            </div>
            <div class="studio-header-actions">
                <c:if test="${dashboard.publicProfileAvailable}">
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/photographers/${dashboard.profileId}">View Public Profile</a>
                </c:if>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/photographer/profile/edit">Edit Profile</a>
            </div>
        </header>

        <div class="studio-layout">
            <div class="studio-main">
                <section class="studio-panel" aria-labelledby="attention-title">
                    <div class="studio-panel-head">
                        <h2 id="attention-title">Needs Your Attention</h2>
                        <span class="text-muted small">Ordered by operational priority</span>
                    </div>
                    <c:choose>
                        <c:when test="${empty dashboard.attentionItems}">
                            <div class="studio-empty">
                                <h3>You are caught up</h3>
                                <p>No booking response or Studio setup item needs immediate attention.</p>
                                <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/photographer/schedule">Review Availability</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="studio-attention-list">
                                <c:forEach var="item" items="${dashboard.attentionItems}" varStatus="loop">
                                    <article class="studio-attention">
                                        <span class="studio-priority" aria-label="Priority ${loop.count}">${loop.count}</span>
                                        <div>
                                            <h3><c:out value="${item.title}"/></h3>
                                            <p><c:out value="${item.description}"/></p>
                                        </div>
                                        <a class="btn ${item.action.style == 'primary' ? 'btn-primary' : 'btn-secondary'} btn-sm"
                                           href="${pageContext.request.contextPath}${item.action.path}">
                                            <c:out value="${item.action.label}"/>
                                        </a>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="studio-panel" aria-labelledby="requests-title">
                    <div class="studio-panel-head">
                        <h2 id="requests-title">Booking Requests</h2>
                        <a href="${pageContext.request.contextPath}/photographer/bookings">View all requests</a>
                    </div>
                    <c:choose>
                        <c:when test="${empty dashboard.bookingRequests}">
                            <div class="studio-empty">
                                <h3>No requests awaiting a response</h3>
                                <p>New customer inquiries will appear here when they need your decision.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="studio-bookings">
                                <c:forEach var="booking" items="${dashboard.bookingRequests}">
                                    <article class="studio-booking">
                                        <div class="studio-booking-top">
                                            <div>
                                                <h3>Request from <c:out value="${booking.customerName}"/></h3>
                                                <div class="studio-meta">
                                                    <span><c:out value="${booking.bookingDate}"/> at <c:out value="${booking.bookingTime}"/></span>
                                                    <span><c:out value="${booking.location}"/></span>
                                                </div>
                                            </div>
                                            <div>
                                                <span class="status-badge ${booking.status}"><c:out value="${booking.status}"/></span>
                                                <div style="margin-top:.5rem; font-weight:700; white-space:nowrap;">
                                                    <fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0"/> VND
                                                </div>
                                            </div>
                                        </div>
                                        <c:if test="${not empty booking.context}">
                                            <p class="studio-context"><c:out value="${booking.context}"/></p>
                                        </c:if>
                                        <div class="studio-booking-actions" aria-label="Actions for booking request">
                                            <c:forEach var="action" items="${booking.actions}">
                                                <c:choose>
                                                    <c:when test="${action.method == 'POST'}">
                                                        <form method="post" action="${pageContext.request.contextPath}${action.path}">
                                                            <%@ include file="fragments/csrf-input.jsp" %>
                                                            <button type="submit" class="btn btn-sm ${action.style == 'primary' ? 'btn-primary' : action.style == 'danger' ? 'btn-outline-danger' : 'btn-secondary'}">
                                                                <c:out value="${action.label}"/>
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}${action.path}"><c:out value="${action.label}"/></a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="studio-panel" aria-labelledby="shoots-title">
                    <div class="studio-panel-head">
                        <h2 id="shoots-title">Upcoming Shoots</h2>
                        <a href="${pageContext.request.contextPath}/photographer/bookings">View bookings</a>
                    </div>
                    <c:choose>
                        <c:when test="${empty dashboard.upcomingShoots}">
                            <div class="studio-empty">
                                <h3>No accepted shoots scheduled</h3>
                                <p>Accepted future bookings will appear here with customer deposit status and chat access.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="studio-bookings">
                                <c:forEach var="shoot" items="${dashboard.upcomingShoots}">
                                    <article class="studio-booking">
                                        <div class="studio-booking-top">
                                            <div>
                                                <h3><c:out value="${shoot.customerName}"/></h3>
                                                <div class="studio-meta">
                                                    <span><c:out value="${shoot.bookingDate}"/> at <c:out value="${shoot.bookingTime}"/></span>
                                                    <span><c:out value="${shoot.location}"/></span>
                                                </div>
                                            </div>
                                            <div style="text-align:right;">
                                                <span class="status-badge ${shoot.status}"><c:out value="${shoot.status}"/></span>
                                                <div class="studio-status ${shoot.depositStatus}" style="margin-top:.65rem;">
                                                    <c:out value="${shoot.depositLabel}"/>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="studio-booking-actions">
                                            <c:forEach var="action" items="${shoot.actions}">
                                                <a class="btn btn-sm ${action.style == 'text' ? 'btn-ghost' : 'btn-secondary'}"
                                                   href="${pageContext.request.contextPath}${action.path}"><c:out value="${action.label}"/></a>
                                            </c:forEach>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="studio-panel" aria-labelledby="portfolio-title">
                    <div class="studio-panel-head">
                        <h2 id="portfolio-title">Portfolio Preview</h2>
                        <a href="${pageContext.request.contextPath}/photographer/portfolio">Manage portfolio</a>
                    </div>
                    <c:choose>
                        <c:when test="${empty dashboard.portfolioPreview}">
                            <div class="studio-empty">
                                <h3>Show clients what makes your work distinctive</h3>
                                <p>Add work to your portfolio so clients can evaluate your style.</p>
                                <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/photographer/portfolio">Add Portfolio Work</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="studio-preview">
                                <c:forEach var="image" items="${dashboard.portfolioPreview}">
                                    <a href="${pageContext.request.contextPath}/photographer/portfolio" aria-label="Manage portfolio">
                                        <figure>
                                            <img src="<c:out value='${image.thumbnailUrl}'/>" alt="<c:out value='${image.altText}'/>" loading="lazy"/>
                                        </figure>
                                    </a>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

            <aside class="studio-rail" aria-label="Studio overview">
                <section class="studio-panel" aria-labelledby="profile-health-title">
                    <div class="studio-panel-head"><h2 id="profile-health-title">Profile Health</h2></div>
                    <span class="studio-health-value">${dashboard.profileCompleteness}% complete</span>
                    <div class="progress studio-progress" role="progressbar" aria-label="Profile completeness" aria-valuenow="${dashboard.profileCompleteness}" aria-valuemin="0" aria-valuemax="100">
                        <div class="progress-bar" style="width: ${dashboard.profileCompleteness}%"></div>
                    </div>
                    <a href="${pageContext.request.contextPath}/photographer/profile/edit">${dashboard.profileCompleteness == 100 ? 'Edit Profile' : 'Complete Profile'}</a>
                </section>

                <section class="studio-panel" aria-labelledby="portfolio-health-title">
                    <div class="studio-panel-head"><h2 id="portfolio-health-title">Portfolio Health</h2></div>
                    <div class="studio-health-grid">
                        <div class="studio-health"><span class="studio-health-label">Works</span><span class="studio-health-value">${dashboard.portfolioSummary.imageCount}</span></div>
                        <div class="studio-health"><span class="studio-health-label">Categories</span><span class="studio-health-value">${dashboard.portfolioSummary.categoryCount}</span></div>
                    </div>
                    <p class="text-muted small" style="margin:1rem 0 0;">Cover: ${dashboard.portfolioSummary.hasCover ? 'Configured' : 'Not configured'}</p>
                </section>

                <section class="studio-panel" aria-labelledby="availability-title">
                    <div class="studio-panel-head"><h2 id="availability-title">Availability</h2></div>
                    <span class="studio-health-value">${dashboard.availabilitySummary.upcomingBlockedCount} blocked upcoming</span>
                    <p class="text-muted small" style="margin:.6rem 0 1rem;">
                        <c:choose>
                            <c:when test="${not empty dashboard.availabilitySummary.nextUnavailableDate}">Next unavailable: <c:out value="${dashboard.availabilitySummary.nextUnavailableDate}"/></c:when>
                            <c:otherwise>No future blocked dates.</c:otherwise>
                        </c:choose>
                    </p>
                    <a href="${pageContext.request.contextPath}/photographer/schedule">Manage Availability</a>
                </section>

                <section class="studio-panel" aria-labelledby="reputation-title">
                    <div class="studio-panel-head"><h2 id="reputation-title">Reputation</h2></div>
                    <span class="studio-health-value"><fmt:formatNumber value="${dashboard.ratingSummary.averageRating}" minFractionDigits="1" maxFractionDigits="2"/> / 5</span>
                    <p class="text-muted small" style="margin:.5rem 0 1rem;">Based on ${dashboard.ratingSummary.reviewCount} visible reviews</p>
                    <c:if test="${dashboard.publicProfileAvailable}">
                        <a href="${pageContext.request.contextPath}/photographers/${dashboard.profileId}">View Public Profile</a>
                    </c:if>
                </section>

                <section class="studio-panel" aria-labelledby="quick-actions-title">
                    <div class="studio-panel-head"><h2 id="quick-actions-title">Quick Actions</h2></div>
                    <nav class="studio-quick-links" aria-label="Studio actions">
                        <a href="${pageContext.request.contextPath}/photographer/bookings"><span>Booking Requests</span><span aria-hidden="true">→</span></a>
                        <a href="${pageContext.request.contextPath}/photographer/portfolio"><span>Manage Portfolio</span><span aria-hidden="true">→</span></a>
                        <a href="${pageContext.request.contextPath}/photographer/schedule"><span>Manage Availability</span><span aria-hidden="true">→</span></a>
                        <a href="${pageContext.request.contextPath}/photographer/profile/edit"><span>Edit Profile</span><span aria-hidden="true">→</span></a>
                        <c:if test="${dashboard.publicProfileAvailable}">
                            <a href="${pageContext.request.contextPath}/photographers/${dashboard.profileId}"><span>View Public Profile</span><span aria-hidden="true">→</span></a>
                        </c:if>
                    </nav>
                </section>
            </aside>
        </div>
    </div>
</main>
