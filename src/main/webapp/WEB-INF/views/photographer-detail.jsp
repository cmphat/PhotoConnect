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
    <meta name="description" content="View the photographer profile of ${photographer.displayName} on PhotoConnect.">
    <title><c:out value="${photographer.displayName}"/> – PhotoConnect</title>
    
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    
    <style>
        .cover-region {
            height: 60vh;
            background-color: var(--bg-dark-secondary);
            position: relative;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .cover-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            opacity: 0.6;
        }
        .cover-text {
            position: absolute;
            bottom: 3rem;
            left: 3rem;
            z-index: 10;
        }
        .profile-name {
            font-size: clamp(3rem, 6vw, 6rem);
            font-weight: 300;
            line-height: 1;
            margin-bottom: 1rem;
            letter-spacing: -0.04em;
        }
        .profile-meta {
            display: flex;
            gap: 2rem;
            font-size: 1rem;
            color: var(--text-on-dark);
            opacity: 0.9;
        }

        .layout-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 4rem;
            padding: 4rem 0;
        }
        
        @media (max-width: 991px) {
            .layout-grid {
                grid-template-columns: 1fr;
            }
            .cover-text {
                left: 1.5rem;
                bottom: 1.5rem;
            }
        }

        .bio-section {
            font-size: 1.25rem;
            font-weight: 300;
            line-height: 1.7;
            color: var(--text-muted);
            margin-bottom: 4rem;
        }

        .masonry-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 1rem;
        }
        
        .masonry-item {
            position: relative;
            background: var(--bg-dark-secondary);
            overflow: hidden;
            aspect-ratio: 4/5;
        }

        .masonry-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            cursor: pointer;
            transition: transform var(--transition-slow);
        }

        .masonry-item:hover .masonry-img {
            transform: scale(1.03);
        }

        .booking-sidebar {
            position: sticky;
            top: 100px;
            background: var(--bg-dark-secondary);
            padding: 3rem;
            border: 1px solid var(--border-dark);
        }

        .stat-row {
            display: flex;
            justify-content: space-between;
            padding: 1rem 0;
            border-bottom: 1px solid var(--border-dark);
        }

        .stat-row:last-child {
            border-bottom: none;
        }

        /* Lightbox */
        .lightbox-overlay {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(10,10,10,0.95);
            z-index: 9999;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            padding: 2rem;
        }
        .lightbox-overlay.active { display: flex; }
        .lightbox-img {
            max-width: 90vw;
            max-height: 85vh;
        }
        .lightbox-caption {
            color: var(--text-on-dark);
            margin-top: 1.5rem;
            font-size: 1rem;
            text-align: center;
            font-weight: 300;
        }
        .lightbox-close {
            position: absolute;
            top: 2rem;
            right: 3rem;
            color: var(--text-muted);
            font-size: 2.5rem;
            cursor: pointer;
            line-height: 1;
            transition: color var(--transition-fast);
        }
        .lightbox-close:hover { color: var(--text-on-dark); }
    </style>
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main>
        <!-- ── Cover Region ────────────────────────────────────────────── -->
        <section class="cover-region">
            <!-- 3D Hook -->
            <div id="three-hero-container"></div>

            <c:choose>
                <c:when test="${not empty photographer.coverImageUrl}">
                    <img src="<c:out value='${photographer.coverImageUrl}'/>" alt="Cover" class="cover-img">
                </c:when>
                <c:otherwise>
                    <div style="font-size: 8rem; opacity: 0.1; font-weight: 300;"><c:out value="${fn:toUpperCase(fn:substring(photographer.displayName, 0, 1))}"/></div>
                </c:otherwise>
            </c:choose>

            <div class="cover-text">
                <div style="margin-bottom: 1rem;"><span style="border: 1px solid rgba(255,255,255,0.2); padding: 4px 12px; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em;">Verified Artist</span></div>
                <h1 class="profile-name"><c:out value="${photographer.displayName}"/></h1>
                <div class="profile-meta">
                    <c:if test="${not empty photographer.city}">
                        <span><c:out value="${photographer.city}"/></span>
                    </c:if>
                    <c:if test="${not empty photographer.experienceYears}">
                        <span><c:out value="${photographer.experienceYears}"/> Yrs Exp</span>
                    </c:if>
                </div>
            </div>
        </section>

        <!-- ── Main content ───────────────────────────────────────────── -->
        <section class="editorial-container">
            <div class="layout-grid">
                
                <!-- Left: Bio & Portfolio -->
                <div>
                    <div class="bio-section">
                        <c:choose>
                            <c:when test="${not empty photographer.bio}">
                                <c:out value="${photographer.bio}"/>
                            </c:when>
                            <c:otherwise>
                                <em>This artist hasn't added a bio yet.</em>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <h2 class="editorial-heading" style="font-size: 2.5rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1.5rem; margin-bottom: 3rem;">Selected Work</h2>
                    
                    <c:choose>
                        <c:when test="${empty portfolioImages}">
                            <p style="color: var(--text-muted);">This photographer hasn't added portfolio images yet.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="masonry-grid">
                                <c:forEach var="img" items="${portfolioImages}">
                                    <div class="masonry-item">
                                        <img src="<c:out value='${img.imageUrl}'/>" 
                                             alt="<c:out value='${not empty img.caption ? img.caption : "Portfolio image"}'/>" 
                                             class="masonry-img"
                                             loading="lazy"
                                             onclick="openLightbox(this.src, this.alt)">
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Right: Booking Sidebar -->
                <div>
                    <div class="booking-sidebar">
                        <h3 style="font-size: 1.5rem; font-weight: 400; margin-bottom: 1rem;">Book a Session</h3>
                        <p style="color: var(--text-muted); margin-bottom: 2rem;">Reserve a photoshoot directly with this verified artist.</p>
                        
                        <a href="/photographers/${photographer.id}/book" class="submit-btn" style="text-align: center; display: block; margin-bottom: 3rem;" id="btn-book-photographer">
                            Request Booking
                        </a>

                        <div class="stat-row">
                            <span style="color: var(--text-muted);">Starting Rate</span>
                            <span>
                                <c:choose>
                                    <c:when test="${not empty photographer.priceFrom}">
                                        <fmt:formatNumber value="${photographer.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                    </c:when>
                                    <c:otherwise>Contact for rates</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="stat-row">
                            <span style="color: var(--text-muted);">Location</span>
                            <span>
                                <c:choose>
                                    <c:when test="${not empty photographer.city}"><c:out value="${photographer.city}"/></c:when>
                                    <c:otherwise>Global</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="stat-row">
                            <span style="color: var(--text-muted);">Member Since</span>
                            <span>
                                <c:choose>
                                    <c:when test="${not empty photographer.createdAt}">${photographer.createdAt.year}</c:when>
                                    <c:otherwise>2026</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                </div>

            </div>
        </section>
    </main>

    <!-- ── Lightbox overlay ────────────────────────────────────────────── -->
    <div class="lightbox-overlay" id="lightbox" onclick="closeLightbox()">
        <span class="lightbox-close" onclick="closeLightbox()">✕</span>
        <img src="" alt="" class="lightbox-img" id="lightbox-img" onclick="event.stopPropagation()">
        <div class="lightbox-caption" id="lightbox-caption"></div>
    </div>

    <footer style="border-top: 1px solid var(--border-dark); padding: 3rem 0; text-align: center; font-size: 0.85rem; color: var(--text-muted);">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>

    <script>
        function openLightbox(src, alt) {
            document.getElementById('lightbox-img').src = src;
            document.getElementById('lightbox-caption').textContent = alt !== 'Portfolio image' ? alt : '';
            document.getElementById('lightbox').classList.add('active');
            document.body.style.overflow = 'hidden';
        }
        function closeLightbox() {
            document.getElementById('lightbox').classList.remove('active');
            document.getElementById('lightbox-img').src = '';
            document.body.style.overflow = '';
        }
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') closeLightbox();
        });
    </script>
</body>
</html>
