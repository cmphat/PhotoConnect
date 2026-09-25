<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Customer Dashboard — PhotoConnect</title>
    <style>
        .customer-dashboard { padding: clamp(2rem, 5vw, 4.5rem) 0 5rem; }
        .customer-dashboard__shell { max-width: 1180px; margin: 0 auto; padding: 0 1.25rem; }
        .customer-dashboard__hero { display: grid; grid-template-columns: minmax(0, 1.45fr) minmax(260px, .75fr); gap: 1.5rem; align-items: end; margin-bottom: 2.25rem; }
        .customer-dashboard__eyebrow { color: var(--accent); font-size: .76rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; margin-bottom: .65rem; }
        .customer-dashboard__title { font-family: var(--font-primary); font-size: clamp(2rem, 4vw, 3rem); font-weight: 650; line-height: 1.08; letter-spacing: -.035em; margin: 0 0 .85rem; }
        .customer-dashboard__lede { color: var(--muted); font-size: 1rem; line-height: 1.7; max-width: 650px; margin: 0; }
        .customer-dashboard__hero-actions { display: flex; flex-wrap: wrap; gap: .75rem; justify-content: flex-end; }
        .dashboard-summary { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1px; border: 1px solid var(--border); border-radius: var(--radius-sm); overflow: hidden; background: var(--border); margin-bottom: 2.75rem; }
        .dashboard-stat { display: block; background: var(--surface); padding: 1.25rem 1.35rem; color: var(--text); text-decoration: none; }
        .dashboard-stat:hover, .dashboard-stat:focus-visible { background: var(--surface-raised); color: var(--text); }
        .dashboard-stat__value { display: block; font-family: var(--font-primary); font-size: 2rem; font-weight: 650; line-height: 1; margin-bottom: .45rem; }
        .dashboard-stat__label { color: var(--muted); font-size: .76rem; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }
        .dashboard-section { margin-top: 3rem; }
        .dashboard-section__header { display: flex; justify-content: space-between; align-items: end; gap: 1rem; margin-bottom: 1.25rem; }
        .dashboard-section__title { font-family: var(--font-primary); font-size: clamp(1.5rem, 3vw, 2rem); font-weight: 650; margin: 0; }
        .dashboard-section__context { color: var(--muted); font-size: .9rem; margin: .35rem 0 0; }
        .attention-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; }
        .attention-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 1.4rem; display: flex; flex-direction: column; min-height: 220px; box-shadow: var(--shadow-subtle); }
        .attention-card__category { color: var(--accent); font-size: .72rem; font-weight: 700; letter-spacing: .1em; text-transform: uppercase; }
        .attention-card h3 { font-family: var(--font-primary); font-size: 1.2rem; font-weight: 650; margin: .65rem 0 .55rem; }
        .attention-card p { color: var(--muted); line-height: 1.6; margin: 0 0 1rem; }
        .attention-card__amount { font-weight: 700; margin-bottom: 1rem; }
        .attention-card__action { margin-top: auto; align-self: flex-start; }
        .dashboard-bookings { display: grid; gap: 1rem; }
        .dashboard-booking { display: grid; grid-template-columns: 168px minmax(0, 1fr) auto; gap: 1.25rem; align-items: stretch; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); overflow: hidden; }
        .dashboard-booking__image { min-height: 190px; background: var(--surface-raised); position: relative; overflow: hidden; }
        .dashboard-booking__image img { width: 100%; height: 100%; object-fit: cover; position: absolute; inset: 0; }
        .dashboard-booking__placeholder { height: 100%; min-height: 190px; display: grid; place-items: center; font-family: var(--font-primary); font-size: 2rem; font-weight: 650; color: var(--muted); }
        .dashboard-booking__body { padding: 1.25rem 0; min-width: 0; }
        .dashboard-booking__topline { display: flex; flex-wrap: wrap; gap: .65rem; align-items: center; margin-bottom: .65rem; }
        .dashboard-booking__body h3 { font-family: var(--font-primary); font-size: 1.35rem; font-weight: 650; margin: 0 0 .85rem; }
        .dashboard-booking__facts { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .7rem 1.4rem; color: var(--muted); font-size: .9rem; }
        .dashboard-booking__facts strong { display: block; color: var(--text); font-size: .72rem; letter-spacing: .07em; text-transform: uppercase; margin-bottom: .16rem; }
        .dashboard-booking__actions { padding: 1.25rem 1.25rem 1.25rem 0; display: flex; min-width: 155px; flex-direction: column; justify-content: center; gap: .55rem; }
        .dashboard-booking__actions .btn { width: 100%; }
        .saved-preview-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1rem; }
        .saved-preview-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); overflow: hidden; display: flex; flex-direction: column; }
        .saved-preview-card__image { aspect-ratio: 4 / 3; background: var(--surface-raised); position: relative; overflow: hidden; }
        .saved-preview-card__image img { width: 100%; height: 100%; object-fit: cover; }
        .saved-preview-card__placeholder { width: 100%; height: 100%; display: grid; place-items: center; font-family: var(--font-primary); font-weight: 650; color: var(--muted); font-size: 2rem; }
        .saved-preview-card__body { padding: 1.15rem; display: flex; flex: 1; flex-direction: column; }
        .saved-preview-card h3 { font-family: var(--font-primary); font-size: 1.15rem; font-weight: 650; margin: 0 0 .35rem; }
        .saved-preview-card__headline { color: var(--muted); font-size: .88rem; line-height: 1.5; margin-bottom: .8rem; }
        .saved-preview-card__meta { color: var(--muted); font-size: .82rem; display: flex; justify-content: space-between; gap: .5rem; margin-bottom: 1rem; }
        .saved-preview-card .btn { margin-top: auto; }
        .dashboard-empty { background: var(--surface); border: 1px dashed var(--border); border-radius: var(--radius-sm); padding: clamp(1.75rem, 4vw, 3rem); text-align: center; }
        .dashboard-empty h3 { font-family: var(--font-primary); font-size: 1.3rem; font-weight: 650; margin: 0 0 .55rem; }
        .dashboard-empty p { color: var(--muted); max-width: 540px; margin: 0 auto 1.2rem; line-height: 1.6; }
        @media (max-width: 900px) {
            .customer-dashboard__hero { grid-template-columns: 1fr; }
            .customer-dashboard__hero-actions { justify-content: flex-start; }
            .saved-preview-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .dashboard-booking { grid-template-columns: 130px minmax(0, 1fr); }
            .dashboard-booking__actions { grid-column: 1 / -1; padding: 0 1.25rem 1.25rem; flex-direction: row; flex-wrap: wrap; justify-content: flex-start; }
            .dashboard-booking__actions .btn { width: auto; }
        }
        @media (max-width: 640px) {
            .dashboard-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .attention-grid, .saved-preview-grid { grid-template-columns: 1fr; }
            .dashboard-booking { grid-template-columns: 1fr; }
            .dashboard-booking__image { min-height: 210px; }
            .dashboard-booking__body { padding: 1.1rem 1.1rem 0; }
            .dashboard-booking__facts { grid-template-columns: 1fr; }
            .dashboard-booking__actions { grid-column: auto; padding: 1.1rem; flex-direction: column; }
            .dashboard-booking__actions .btn { width: 100%; }
            .dashboard-section__header { align-items: flex-start; flex-direction: column; }
        }
    </style>
</head>

<main class="customer-dashboard">
    <div class="customer-dashboard__shell">
        <section class="customer-dashboard__hero" aria-labelledby="dashboard-title">
            <div>
                <div class="customer-dashboard__eyebrow">Customer workspace</div>
                <h1 class="customer-dashboard__title" id="dashboard-title">
                    Welcome, <c:out value="${dashboard.customerName}"/>
                </h1>
                <p class="customer-dashboard__lede">
                    Keep bookings, deposits, session conversations, reviews, and saved photographers in one focused place.
                </p>
            </div>
            <div class="customer-dashboard__hero-actions">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/photographers">Explore Photographers</a>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/bookings">View All Bookings</a>
            </div>
        </section>

        <c:if test="${dashboard.summary.upcomingCount > 0 || dashboard.summary.pendingCount > 0 || dashboard.summary.completedCount > 0 || dashboard.summary.savedCount > 0}">
            <section class="dashboard-summary" aria-label="Booking summary">
                <a class="dashboard-stat" href="${pageContext.request.contextPath}/bookings"><span class="dashboard-stat__value"><c:out value="${dashboard.summary.upcomingCount}"/></span><span class="dashboard-stat__label">Upcoming</span></a>
                <a class="dashboard-stat" href="${pageContext.request.contextPath}/bookings"><span class="dashboard-stat__value"><c:out value="${dashboard.summary.pendingCount}"/></span><span class="dashboard-stat__label">Pending</span></a>
                <a class="dashboard-stat" href="${pageContext.request.contextPath}/bookings"><span class="dashboard-stat__value"><c:out value="${dashboard.summary.completedCount}"/></span><span class="dashboard-stat__label">Completed</span></a>
                <a class="dashboard-stat" href="${pageContext.request.contextPath}/customer/saved-photographers"><span class="dashboard-stat__value"><c:out value="${dashboard.summary.savedCount}"/></span><span class="dashboard-stat__label">Saved</span></a>
            </section>
        </c:if>

        <section class="dashboard-section" aria-labelledby="attention-heading">
            <div class="dashboard-section__header">
                <div><div class="customer-dashboard__eyebrow">Next steps</div><h2 class="dashboard-section__title" id="attention-heading">Needs Your Attention</h2></div>
                <p class="dashboard-section__context">Only actions currently available for your bookings appear here.</p>
            </div>
            <c:choose>
                <c:when test="${empty dashboard.attentionItems}">
                    <div class="dashboard-empty">
                        <h3>You’re all caught up.</h3>
                        <p>No deposit, upcoming-session, or review actions need your attention right now.</p>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/photographers">Find Your Next Photographer</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="attention-grid">
                        <c:forEach items="${dashboard.attentionItems}" var="item">
                            <article class="attention-card">
                                <div class="attention-card__category"><c:out value="${item.category}"/> · Booking #<c:out value="${item.bookingId}"/></div>
                                <h3><c:out value="${item.title}"/></h3>
                                <p><c:out value="${item.description}"/></p>
                                <c:if test="${not empty item.amount}"><div class="attention-card__amount"><fmt:formatNumber value="${item.amount}" pattern="#,#00"/> VND</div></c:if>
                                <c:url value="${item.action.path}" var="attentionUrl"/>
                                <a href="${attentionUrl}" class="attention-card__action btn ${item.action.style == 'primary' ? 'btn-primary' : 'btn-secondary'}"><c:out value="${item.action.label}"/></a>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="dashboard-section" aria-labelledby="bookings-heading">
            <div class="dashboard-section__header">
                <div><div class="customer-dashboard__eyebrow">Your sessions</div><h2 class="dashboard-section__title" id="bookings-heading">Upcoming &amp; Recent Bookings</h2></div>
                <a class="text-link" href="${pageContext.request.contextPath}/bookings">View all bookings &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty dashboard.bookings}">
                    <div class="dashboard-empty">
                        <h3>No bookings yet.</h3>
                        <p>Explore approved photographers and send your first photography session request.</p>
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/photographers">Explore Photographers</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="dashboard-bookings">
                        <c:forEach items="${dashboard.bookings}" var="booking">
                            <article class="dashboard-booking">
                                <div class="dashboard-booking__image">
                                    <c:choose>
                                        <c:when test="${not empty booking.coverImageUrl}"><img src="${fn:escapeXml(booking.coverImageUrl)}" alt="Portfolio cover for ${fn:escapeXml(booking.photographerName)}" loading="lazy"/></c:when>
                                        <c:otherwise><div class="dashboard-booking__placeholder" aria-hidden="true"><c:out value="${fn:substring(booking.photographerName, 0, 1)}"/></div></c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="dashboard-booking__body">
                                    <div class="dashboard-booking__topline"><span class="status-badge ${booking.status}"><c:out value="${booking.status}"/></span><span class="status-badge"><c:out value="${booking.depositLabel}"/></span></div>
                                    <h3><c:out value="${booking.photographerName}"/></h3>
                                    <div class="dashboard-booking__facts">
                                        <div><strong>Date &amp; time</strong><c:out value="${booking.bookingDate}"/> at <c:out value="${booking.bookingTime}"/></div>
                                        <div><strong>Location</strong><c:out value="${booking.location}"/></div>
                                        <div><strong>Session price</strong><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,#00"/> VND</div>
                                        <div><strong>Deposit</strong><c:choose><c:when test="${not empty booking.depositAmount}"><fmt:formatNumber value="${booking.depositAmount}" pattern="#,#00"/> VND</c:when><c:otherwise><c:out value="${booking.depositLabel}"/></c:otherwise></c:choose></div>
                                    </div>
                                </div>
                                <div class="dashboard-booking__actions">
                                    <c:forEach items="${booking.actions}" var="action">
                                        <c:url value="${action.path}" var="bookingActionUrl"/>
                                        <c:choose>
                                            <c:when test="${action.style == 'primary'}"><a class="btn btn-primary" href="${bookingActionUrl}"><c:out value="${action.label}"/></a></c:when>
                                            <c:when test="${action.style == 'secondary'}"><a class="btn btn-secondary" href="${bookingActionUrl}"><c:out value="${action.label}"/></a></c:when>
                                            <c:otherwise><a class="text-link" href="${bookingActionUrl}"><c:out value="${action.label}"/> &rarr;</a></c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="dashboard-section" aria-labelledby="saved-heading">
            <div class="dashboard-section__header">
                <div><div class="customer-dashboard__eyebrow">Shortlist</div><h2 class="dashboard-section__title" id="saved-heading">Recently Saved Photographers</h2></div>
                <a class="text-link" href="${pageContext.request.contextPath}/customer/saved-photographers">View all saved &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty dashboard.savedPhotographers}">
                    <div class="dashboard-empty">
                        <h3>Your shortlist is ready when you are.</h3>
                        <p>Save approved photographers while browsing and your newest favorites will appear here.</p>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/photographers">Explore Photographers</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="saved-preview-grid">
                        <c:forEach items="${dashboard.savedPhotographers}" var="photographer">
                            <article class="saved-preview-card">
                                <div class="saved-preview-card__image">
                                    <c:choose>
                                        <c:when test="${not empty photographer.coverImageUrl}"><img src="${fn:escapeXml(photographer.coverImageUrl)}" alt="Portfolio cover for ${fn:escapeXml(photographer.displayName)}" loading="lazy"/></c:when>
                                        <c:otherwise><div class="saved-preview-card__placeholder" aria-hidden="true"><c:out value="${fn:substring(photographer.displayName, 0, 1)}"/></div></c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="saved-preview-card__body">
                                    <h3><c:out value="${photographer.displayName}"/></h3>
                                    <div class="saved-preview-card__headline"><c:out value="${photographer.headline}" default="Approved PhotoConnect photographer"/></div>
                                    <div class="saved-preview-card__meta"><span><c:out value="${photographer.location}" default="Location flexible"/></span><span>★ <fmt:formatNumber value="${photographer.averageRating}" pattern="0.0"/></span></div>
                                    <c:if test="${not empty photographer.priceFrom}"><div class="saved-preview-card__meta"><span>From</span><strong><fmt:formatNumber value="${photographer.priceFrom}" pattern="#,#00"/> VND</strong></div></c:if>
                                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/photographers/${photographer.photographerId}">View Profile</a>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
