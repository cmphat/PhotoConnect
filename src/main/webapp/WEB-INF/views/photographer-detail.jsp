<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title><c:out value="${photographer.displayName}"/> — PhotoConnect Creator Profile</title>
    <meta name="description" content="View verified portfolio, reviews, and booking availability for <c:out value="${photographer.displayName}"/> on PhotoConnect.">
    <style>
        .profile-hero-region {
            padding: clamp(2rem, 4vw, 3.5rem) 0 clamp(2.5rem, 5vw, 4rem);
            border-bottom: 1px solid var(--border);
            background: var(--surface-subtle);
        }

        .profile-banner-frame {
            width: 100%;
            height: clamp(340px, 48vw, 540px);
            position: relative;
            overflow: hidden;
            border-radius: var(--radius-sm);
            background-color: var(--surface-subtle);
            border: 1px solid var(--border);
            margin-top: 2rem;
            box-shadow: var(--shadow-subtle);
        }

        .profile-banner-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .profile-header-meta {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            flex-wrap: wrap;
            gap: 1.5rem;
        }

        .profile-pills-row {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.75rem 1.25rem;
            margin-top: 0.75rem;
            color: var(--muted);
            font-size: 0.95rem;
        }

        .layout-grid {
            display: grid;
            grid-template-columns: minmax(0, 1.85fr) minmax(320px, 380px);
            gap: clamp(2.5rem, 5vw, 4.5rem);
            padding: 4rem 1.5rem;
            max-width: 1480px;
            margin: 0 auto;
            align-items: start;
        }

        .layout-grid > *,
        .booking-sidebar-shell,
        .booking-sidebar-column {
            min-width: 0;
        }

        .bio-section {
            font-family: var(--font-primary);
            font-size: 1.15rem;
            font-weight: 300;
            line-height: 1.75;
            color: var(--text);
            margin-bottom: 3.5rem;
            padding: 2rem 2.25rem;
            background: var(--surface);
            border-left: 3px solid var(--accent);
            border-radius: var(--radius-sm);
        }

        .masonry-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 1.5rem;
        }

        .masonry-item {
            position: relative;
            background: var(--surface-subtle);
            overflow: hidden;
            aspect-ratio: 4/5;
            border-radius: var(--radius-sm);
            border: 1px solid var(--border-subtle);
        }

        .masonry-trigger {
            display: block;
            width: 100%;
            height: 100%;
            padding: 0;
            border: 0;
            background: transparent;
            cursor: zoom-in;
        }

        .masonry-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform var(--transition-smooth);
        }

        .masonry-item:hover .masonry-img,
        .masonry-trigger:focus-visible .masonry-img {
            transform: scale(1.035);
        }

        .booking-sidebar-column {
            display: flex;
            justify-content: flex-start;
            width: 100%;
        }

        .booking-sidebar {
            position: sticky;
            top: 100px;
            background: var(--surface);
            padding: clamp(2rem, 3.5vw, 2.5rem);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            width: 100%;
            max-width: 390px;
            margin: 0;
            box-shadow: var(--shadow-subtle);
        }

        .stat-row {
            display: flex;
            justify-content: space-between;
            padding: 0.95rem 0;
            border-bottom: 1px solid var(--border);
            gap: 1rem;
            font-size: 0.9rem;
        }

        .stat-row > :last-child {
            min-width: 0;
            overflow-wrap: anywhere;
            text-align: right;
            font-weight: 500;
        }

        .stat-row:last-child {
            border-bottom: none;
        }

        /* Lightbox */
        .lightbox-overlay {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(15, 14, 12, 0.94);
            backdrop-filter: blur(12px);
            z-index: 9999;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            padding: 2.5rem;
        }
        .lightbox-overlay.active { display: flex; }
        .lightbox-img {
            max-width: 88vw;
            max-height: 82vh;
            object-fit: contain;
            border-radius: var(--radius-sm);
            box-shadow: 0 20px 48px rgba(0,0,0,0.5);
        }
        .lightbox-caption {
            color: #F8FAFC;
            margin-top: 1.5rem;
            font-size: 1rem;
            text-align: center;
            font-weight: 300;
            max-width: 60ch;
        }
        .lightbox-close {
            position: absolute;
            top: 2rem;
            right: 2.5rem;
            color: #F8FAFC;
            font-size: 2.5rem;
            cursor: pointer;
            line-height: 1;
            background: transparent;
            border: 0;
            padding: 0.5rem;
            opacity: 0.75;
            transition: opacity var(--transition-fast);
        }
        .lightbox-close:hover { opacity: 1; }

        @media (max-width: 991px) {
            .layout-grid {
                grid-template-columns: 1fr;
                gap: 3.5rem;
            }
            .booking-sidebar {
                position: static;
                max-width: none;
                margin: 0;
            }
        }

        @media (max-width: 640px) {
            .layout-grid {
                padding: 2.5rem 0;
            }
            .booking-sidebar {
                padding: 1.5rem;
            }
            .profile-header-meta {
                flex-direction: column;
                align-items: flex-start;
            }
        }
    </style>
</head>

    <main>
        <!-- ── Artist Header & Hero Cover ────────────────────────────── -->
        <section class="profile-hero-region">
            <div class="editorial-container">
                <div style="margin-bottom: 1.25rem;">
                    <a href="${pageContext.request.contextPath}/photographers" class="text-link" style="font-size: 0.88rem;">
                        &larr; Back to Directory
                    </a>
                </div>

                <c:if test="${not empty successMessage}">
                    <div class="pc-alert pc-alert-success alert alert-success mb-3" role="alert">
                        <c:out value="${successMessage}"/>
                    </div>
                </c:if>
                <c:if test="${not empty errorMessage}">
                    <div class="pc-alert pc-alert-danger alert alert-danger mb-3" role="alert">
                        <c:out value="${errorMessage}"/>
                    </div>
                </c:if>

                <div class="profile-header-meta d-flex justify-content-between align-items-end flex-wrap gap-4">
                    <div>
                        <div style="margin-bottom: 0.5rem;">
                            <span class="status-badge status-approved badge">Verified Creator</span>
                        </div>
                        <h1 class="editorial-title" style="font-size: clamp(2.5rem, 5vw, 4rem); margin: 0 0 0.5rem 0;">
                            <c:out value="${photographer.displayName}"/>
                        </h1>
                        <c:if test="${not empty photographer.headline}">
                            <div style="font-size: 1.15rem; color: var(--muted); font-weight: 400; margin-bottom: 0.75rem;">
                                <c:out value="${photographer.headline}"/>
                            </div>
                        </c:if>
                        <div class="profile-pills-row d-flex align-items-center flex-wrap gap-2">
                            <c:if test="${not empty photographer.city}">
                                <span><c:out value="${photographer.city}"/><c:if test="${not empty photographer.country}">, <c:out value="${photographer.country}"/></c:if></span>
                            </c:if>
                            <c:if test="${not empty photographer.experienceYears}">
                                <span>&bull; <c:out value="${photographer.experienceYears}"/> Years Experience</span>
                            </c:if>
                            <c:choose>
                                <c:when test="${photographer.reviewCount > 0}">
                                    <span style="color: var(--warning); font-weight: 600;">
                                        ★ <fmt:formatNumber value="${photographer.averageRating}" pattern="0.0" /> (${photographer.reviewCount} <c:out value="${photographer.reviewCount == 1 ? 'review' : 'reviews'}"/>)
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span style="color: var(--muted);">★ Emerging Artist</span>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${photographer.travelAvailable}">
                                <span class="badge bg-secondary" style="font-size: 0.78rem;">Available for Travel</span>
                            </c:if>
                        </div>
                        <c:if test="${not empty photographer.specialtyDisplayNames}">
                            <div class="d-flex flex-wrap gap-1 mt-2">
                                <c:forEach var="specName" items="${photographer.specialtyDisplayNames}">
                                    <span class="badge bg-light text-dark border" style="font-size: 0.78rem; font-weight: 500;"><c:out value="${specName}"/></span>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>

                    <div class="d-flex align-items-center gap-2">
                        <c:choose>
                            <c:when test="${isSaved}">
                                <form action="${pageContext.request.contextPath}/photographers/${photographer.id}/unsave" method="post" style="display:inline; margin:0;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                    <button type="submit" class="btn btn-secondary btn-lg" id="btn-hero-unsave-photographer" title="Remove from your saved photographers">
                                        ✓ Saved
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/photographers/${photographer.id}/save" method="post" style="display:inline; margin:0;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                    <button type="submit" class="btn btn-secondary btn-lg" id="btn-hero-save-photographer" title="Save this photographer to your favorites">
                                        + Save Photographer
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                        <a href="${pageContext.request.contextPath}/photographers/${photographer.id}/book" class="btn btn-primary btn-lg">
                            Request Booking
                        </a>
                    </div>
                </div>

                <!-- Hero Cover Photograph Banner -->
                <div class="profile-banner-frame">
                    <c:choose>
                        <c:when test="${not empty photographer.coverImageUrl}">
                            <img src="<c:out value='${photographer.coverImageUrl}'/>" alt="<c:out value='${photographer.displayName}'/> featured photography" class="profile-banner-img">
                        </c:when>
                        <c:otherwise>
                            <div class="pc-card-placeholder">
                                <div class="placeholder-initial"><c:out value="${fn:toUpperCase(fn:substring(photographer.displayName, 0, 1))}"/></div>
                                <div class="placeholder-badge">Editorial Artist Profile</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>

        <!-- ── Main Content Grid: Bio, Gallery, Reviews & Booking Panel ── -->
        <section class="editorial-container">
            <div class="layout-grid">

                <!-- Left: Bio, Selected Work & Client Reviews -->
                <div class="booking-sidebar-shell">

                    <!-- About the Artist -->
                    <div class="bio-section">
                        <c:choose>
                            <c:when test="${not empty photographer.bio}">
                                <c:out value="${photographer.bio}"/>
                            </c:when>
                            <c:otherwise>
                                <em>This creator has not added a written statement yet. Reach out via booking to discuss artistic directions.</em>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Selected Work Gallery -->
                    <div style="margin-bottom: 5rem;">
                        <div style="display: flex; justify-content: space-between; align-items: baseline; border-bottom: 1px solid var(--border); padding-bottom: 1.25rem; margin-bottom: 2.5rem;">
                            <div>
                                <span class="section-index">Portfolio Gallery</span>
                                <h2 class="editorial-heading" style="margin: 0; font-size: 2.2rem;">Selected Work</h2>
                            </div>
                            <div style="font-size: 0.88rem; color: var(--muted);">
                                <c:out value="${fn:length(portfolioImages)}"/> Photographs
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${empty portfolioImages}">
                                <div class="pc-empty-state">
                                    <p>This photographer has not published public portfolio photographs yet.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="masonry-grid">
                                    <c:forEach var="img" items="${portfolioImages}">
                                        <div class="masonry-item">
                                            <button type="button" class="masonry-trigger" onclick="openLightbox(this.firstElementChild.src, this.firstElementChild.alt)" aria-label="Open portfolio image: <c:out value='${not empty img.caption ? img.caption : "Portfolio image"}'/>">
                                                <img src="<c:out value='${not empty img.thumbnailUrl ? img.thumbnailUrl : img.imageUrl}'/>"
                                                     alt="<c:out value='${not empty img.caption ? img.caption : "Portfolio image"}'/>"
                                                     class="masonry-img"
                                                     loading="lazy">
                                            </button>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Client Reviews Section -->
                    <div>
                        <div style="display: flex; justify-content: space-between; align-items: baseline; border-bottom: 1px solid var(--border); padding-bottom: 1.25rem; margin-bottom: 2.5rem;">
                            <div>
                                <span class="section-index">Client Feedback</span>
                                <h2 class="editorial-heading" style="margin: 0; font-size: 2rem;">
                                    Client Reviews
                                </h2>
                            </div>
                            <c:if test="${photographer.reviewCount > 0}">
                                <div style="font-size: 0.95rem; color: var(--warning); font-weight: 600;">
                                    ★ <fmt:formatNumber value="${photographer.averageRating}" pattern="0.0" /> (${photographer.reviewCount} <c:out value="${photographer.reviewCount == 1 ? 'review' : 'reviews'}"/>)
                                </div>
                            </c:if>
                        </div>

                        <c:choose>
                            <c:when test="${empty reviews}">
                                <div class="pc-empty-state">
                                    <p>No client reviews have been recorded yet for this creator.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="pc-review-list">
                                    <c:forEach var="rev" items="${reviews}">
                                        <article class="pc-review-card" style="border-radius: var(--radius-sm); border: 1px solid var(--border); background: var(--surface);">
                                            <div class="pc-review-meta">
                                                <div>
                                                    <div style="font-weight: 600; font-size: 1rem; color: var(--text);"><c:out value="${rev.customerName}"/></div>
                                                    <div style="color: var(--warning); font-size: 0.95rem; margin-top: 0.25rem;">
                                                        <c:forEach begin="1" end="${rev.rating}">★</c:forEach><c:forEach begin="${rev.rating + 1}" end="5">☆</c:forEach>
                                                    </div>
                                                </div>
                                                <c:if test="${not empty rev.createdAt}">
                                                    <fmt:parseDate value="${fn:substring(rev.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedRevDate" type="date" />
                                                    <span style="color: var(--muted); font-size: 0.82rem;"><fmt:formatDate value="${parsedRevDate}" pattern="MMM d, yyyy" /></span>
                                                </c:if>
                                            </div>
                                            <c:if test="${not empty rev.comment}">
                                                <div style="color: var(--muted); line-height: 1.65; font-size: 0.95rem; margin-top: 0.75rem;">
                                                    &ldquo;<c:out value="${rev.comment}"/>&rdquo;
                                                </div>
                                            </c:if>
                                        </article>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>

                <!-- Right: Booking Sidebar Panel -->
                <div class="booking-sidebar-column">
                    <aside class="booking-sidebar" aria-label="Booking reservation card">
                        <span class="section-index" style="margin-bottom: 0.35rem;">Direct Booking</span>
                        <h3 style="font-family: var(--font-editorial); font-size: 1.6rem; font-weight: 500; margin-bottom: 0.5rem;">Reserve Session</h3>
                        <p style="color: var(--muted); font-size: 0.9rem; line-height: 1.5; margin-bottom: 1.75rem;">
                            Request a personalized photoshoot directly with this verified artist.
                        </p>

                        <a href="${pageContext.request.contextPath}/photographers/${photographer.id}/book" class="submit-btn" style="text-align: center; display: block; margin-bottom: 0.75rem;" id="btn-book-photographer">
                            Request Booking
                        </a>

                        <c:choose>
                            <c:when test="${isSaved}">
                                <form action="${pageContext.request.contextPath}/photographers/${photographer.id}/unsave" method="post" style="margin-bottom: 1.75rem;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                    <button type="submit" class="btn btn-secondary w-100" id="btn-sidebar-unsave-photographer">
                                        ✓ Saved &bull; Remove
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/photographers/${photographer.id}/save" method="post" style="margin-bottom: 1.75rem;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                    <button type="submit" class="btn btn-secondary w-100" id="btn-sidebar-save-photographer">
                                        + Save Photographer
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>

                        <div class="stat-row">
                            <span style="color: var(--muted);">Starting Rate</span>
                            <span>
                                <c:choose>
                                    <c:when test="${not empty photographer.priceFrom}">
                                        <fmt:formatNumber value="${photographer.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                    </c:when>
                                    <c:otherwise>Rates on agreement</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="stat-row">
                            <span style="color: var(--muted);">Location</span>
                            <span>
                                <c:choose>
                                    <c:when test="${not empty photographer.city}"><c:out value="${photographer.city}"/></c:when>
                                    <c:otherwise>Vietnam</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="stat-row">
                            <span style="color: var(--muted);">Experience</span>
                            <span>
                                <c:choose>
                                    <c:when test="${not empty photographer.experienceYears}"><c:out value="${photographer.experienceYears}"/> years</c:when>
                                    <c:otherwise>Professional</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="stat-row">
                            <span style="color: var(--muted);">Deposit Required</span>
                            <span>30% Upon Acceptance</span>
                        </div>
                        <div class="stat-row">
                            <span style="color: var(--muted);">Client Rating</span>
                            <span>
                                <c:choose>
                                    <c:when test="${photographer.reviewCount > 0}">
                                        ★ <fmt:formatNumber value="${photographer.averageRating}" pattern="0.0" />
                                    </c:when>
                                    <c:otherwise>New Roster Member</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <c:if test="${not empty photographer.languages}">
                            <div class="stat-row">
                                <span style="color: var(--muted);">Languages</span>
                                <span><c:out value="${photographer.languages}"/></span>
                            </div>
                        </c:if>
                        <c:if test="${photographer.travelAvailable}">
                            <div class="stat-row">
                                <span style="color: var(--muted);">Travel Bookings</span>
                                <span style="color: var(--success); font-weight: 500;">Available Worldwide</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty photographer.equipmentSummary}">
                            <div class="stat-row">
                                <span style="color: var(--muted);">Studio Equipment</span>
                                <span style="font-size: 0.85rem;"><c:out value="${photographer.equipmentSummary}"/></span>
                            </div>
                        </c:if>
                        <c:if test="${not empty photographer.websiteUrl or not empty photographer.instagramUrl or not empty photographer.facebookUrl}">
                            <div class="stat-row" style="border-bottom: none; padding-top: 1.25rem;">
                                <span style="color: var(--muted);">Channels</span>
                                <div class="d-flex gap-2 justify-content-end flex-wrap">
                                    <c:if test="${not empty photographer.websiteUrl}">
                                        <a href="<c:out value='${photographer.websiteUrl}'/>" target="_blank" rel="noopener noreferrer" class="text-link" style="font-size: 0.85rem;">Website ↗</a>
                                    </c:if>
                                    <c:if test="${not empty photographer.instagramUrl}">
                                        <a href="<c:out value='${photographer.instagramUrl}'/>" target="_blank" rel="noopener noreferrer" class="text-link" style="font-size: 0.85rem;">Instagram ↗</a>
                                    </c:if>
                                    <c:if test="${not empty photographer.facebookUrl}">
                                        <a href="<c:out value='${photographer.facebookUrl}'/>" target="_blank" rel="noopener noreferrer" class="text-link" style="font-size: 0.85rem;">Facebook ↗</a>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                    </aside>
                </div>

            </div>
        </section>
    </main>

    <!-- ── Accessible Lightbox Modal ────────────────────────────────── -->
    <div class="lightbox-overlay" id="lightbox" role="dialog" aria-modal="true" aria-label="Portfolio image preview" aria-hidden="true" onclick="closeLightbox()">
        <button type="button" class="lightbox-close nav-action" onclick="closeLightbox()" aria-label="Close image preview">&times;</button>
        <img src="" alt="" class="lightbox-img" id="lightbox-img" onclick="event.stopPropagation()">
        <div class="lightbox-caption" id="lightbox-caption"></div>
    </div>

    <!-- Editorial Footer -->
    <footer class="pc-footer">
        <div class="editorial-container">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1.5rem;">
                <div style="font-family: var(--font-editorial); font-size: 1.25rem; font-weight: 500; color: var(--text);">
                    PhotoConnect
                </div>
                <div style="font-size: 0.85rem; color: var(--muted);">
                    &copy; 2026 PhotoConnect. Contemporary Editorial Photography Marketplace.
                </div>
                <div style="display: flex; gap: 1.5rem;">
                    <a href="${pageContext.request.contextPath}/photographers" class="text-link">Explore Directory</a>
                    <a href="${pageContext.request.contextPath}/become-photographer" class="text-link">Join Roster</a>
                </div>
            </div>
        </div>
    </footer>

    <script>
        function openLightbox(src, alt) {
            document.getElementById('lightbox-img').src = src;
            document.getElementById('lightbox-caption').textContent = (alt && alt !== 'Portfolio image') ? alt : '';
            document.getElementById('lightbox').classList.add('active');
            document.getElementById('lightbox').setAttribute('aria-hidden', 'false');
            document.body.style.overflow = 'hidden';
        }
        function closeLightbox() {
            document.getElementById('lightbox').classList.remove('active');
            document.getElementById('lightbox').setAttribute('aria-hidden', 'true');
            document.getElementById('lightbox-img').src = '';
            document.body.style.overflow = '';
        }
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') closeLightbox();
        });
    </script>
