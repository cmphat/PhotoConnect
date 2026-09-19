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
    <meta name="description" content="PhotoConnect is a curated editorial marketplace connecting discerning clients with verified professional photographers across Vietnam.">
    <title>PhotoConnect — Contemporary Editorial Photography Marketplace</title>

    <!-- Google Fonts: Plus Jakarta Sans & Playfair Display -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">

    <style>
        /* ── Homepage Editorial Hero ────────────────────────────────────────── */
        .home-hero-section {
            padding: clamp(3.5rem, 8vw, 7rem) 0 clamp(4rem, 8vw, 7.5rem);
            border-bottom: 1px solid var(--border);
            position: relative;
            overflow: hidden;
            background: var(--background);
        }

        .hero-split-grid {
            display: grid;
            grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
            gap: clamp(3rem, 6vw, 5.5rem);
            align-items: center;
        }

        .hero-lead-text {
            font-size: clamp(1.1rem, 2vw, 1.3rem);
            font-weight: 300;
            line-height: 1.65;
            color: var(--muted);
            margin-bottom: 2.5rem;
            max-width: 54ch;
        }

        .hero-actions-row {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 1.25rem;
        }

        /* ── Asymmetric Hero Photo Composition ──────────────────────────────── */
        .hero-photo-stage {
            position: relative;
            width: 100%;
            height: clamp(400px, 52vw, 560px);
            perspective: 1200px;
        }

        .hero-frame-main {
            position: absolute;
            top: 0;
            right: 0;
            width: 78%;
            height: 86%;
            border-radius: var(--radius-sm);
            overflow: hidden;
            box-shadow: var(--shadow-raised);
            border: 1px solid var(--border);
            background: var(--surface-subtle);
            transition: transform var(--transition-smooth);
            z-index: 2;
        }

        .hero-frame-main img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .hero-frame-secondary {
            position: absolute;
            bottom: 0;
            left: 0;
            width: 52%;
            height: 58%;
            border-radius: var(--radius-sm);
            overflow: hidden;
            box-shadow: var(--shadow-raised);
            border: 4px solid var(--surface);
            background: var(--surface-subtle);
            transition: transform var(--transition-smooth);
            z-index: 3;
        }

        .hero-frame-secondary img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .hero-caption-card {
            position: absolute;
            bottom: 2rem;
            right: -1rem;
            background: var(--surface);
            backdrop-filter: blur(12px);
            border: 1px solid var(--border);
            padding: 0.85rem 1.25rem;
            border-radius: var(--radius-sm);
            z-index: 4;
            max-width: 220px;
            box-shadow: var(--shadow-subtle);
        }

        .hero-caption-label {
            font-size: 0.72rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--accent);
            margin-bottom: 0.2rem;
        }

        .hero-caption-title {
            font-size: 0.88rem;
            font-weight: 600;
            color: var(--text);
            line-height: 1.3;
        }

        /* ── Selected Work Collage ──────────────────────────────────────────── */
        .editorial-gallery-grid {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 1.5rem;
            margin-top: 3.5rem;
        }

        .gallery-item-wide {
            grid-column: span 7;
            height: 440px;
            position: relative;
            overflow: hidden;
            border-radius: var(--radius-sm);
            background: var(--surface-subtle);
        }

        .gallery-item-tall {
            grid-column: span 5;
            height: 440px;
            position: relative;
            overflow: hidden;
            border-radius: var(--radius-sm);
            background: var(--surface-subtle);
        }

        .gallery-item-small {
            grid-column: span 4;
            height: 320px;
            position: relative;
            overflow: hidden;
            border-radius: var(--radius-sm);
            background: var(--surface-subtle);
        }

        .gallery-item-wide img,
        .gallery-item-tall img,
        .gallery-item-small img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform var(--transition-slow);
        }

        .gallery-item-wide:hover img,
        .gallery-item-tall:hover img,
        .gallery-item-small:hover img {
            transform: scale(1.035);
        }

        .gallery-overlay-badge {
            position: absolute;
            bottom: 1.25rem;
            left: 1.25rem;
            background: var(--surface);
            border: 1px solid var(--border);
            color: var(--text);
            padding: 0.4rem 0.85rem;
            font-size: 0.76rem;
            font-weight: 600;
            letter-spacing: 0.04em;
            border-radius: var(--radius-sm);
            pointer-events: none;
            box-shadow: var(--shadow-subtle);
        }

        /* ── How PhotoConnect Works (Typography-Driven Process) ──────────────── */
        .editorial-process-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 3.5rem;
            margin-top: 4rem;
        }

        .process-column {
            display: flex;
            flex-direction: column;
            border-top: 1px solid var(--border);
            padding-top: 2.25rem;
            position: relative;
        }

        .process-num {
            font-family: var(--font-primary);
            font-size: 0.85rem;
            font-weight: 700;
            letter-spacing: 0.12em;
            color: var(--accent);
            margin-bottom: 1rem;
            text-transform: uppercase;
        }

        .process-title {
            font-family: var(--font-editorial);
            font-size: 1.5rem;
            font-weight: 500;
            margin-bottom: 0.85rem;
            color: var(--text);
        }

        .process-desc {
            font-size: 0.95rem;
            color: var(--muted);
            line-height: 1.7;
            margin: 0;
        }

        /* ── Final Call to Action ───────────────────────────────────────────── */
        .home-cta-section {
            padding: clamp(5rem, 9vw, 8rem) 0;
            background: var(--deep-navy);
            color: var(--navy-text);
            text-align: center;
            position: relative;
        }

        .home-cta-section h2 {
            color: #FFFFFF;
            margin-bottom: 1.5rem;
        }

        .home-cta-section p {
            color: rgba(248, 250, 252, 0.75);
            max-width: 58ch;
            margin: 0 auto 3rem;
            font-size: 1.15rem;
            line-height: 1.65;
        }

        .home-cta-actions {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            align-items: center;
            gap: 1.5rem;
        }

        .btn-editorial-light {
            background: #FFFFFF;
            color: #0B1220;
            border: 1px solid #FFFFFF;
        }

        .btn-editorial-light:hover {
            background: var(--accent);
            border-color: var(--accent);
            color: #FFFFFF;
            transform: translateY(-1px);
        }

        .btn-editorial-outline {
            background: transparent;
            color: #FFFFFF;
            border: 1px solid rgba(255, 255, 255, 0.35);
        }

        .btn-editorial-outline:hover {
            border-color: #FFFFFF;
            background: rgba(255, 255, 255, 0.1);
            color: #FFFFFF;
            transform: translateY(-1px);
        }

        @media (max-width: 991px) {
            .hero-split-grid {
                grid-template-columns: 1fr;
                gap: 3.5rem;
            }
            .hero-photo-stage {
                height: 420px;
                max-width: 520px;
                margin: 0 auto;
            }
            .editorial-process-grid {
                grid-template-columns: 1fr;
                gap: 2rem;
            }
            .editorial-gallery-grid {
                grid-template-columns: 1fr;
            }
            .gallery-item-wide,
            .gallery-item-tall,
            .gallery-item-small {
                grid-column: span 1;
                height: 320px;
            }
        }
    </style>
</head>
<body>

    <!-- Global Professional Navigation -->
    <jsp:include page="fragments/navbar.jsp" />

    <main>
        <!-- ── 00 / Editorial Hero Section ───────────────────────────── -->
        <section class="home-hero-section">
            <div class="editorial-container">
                <div class="hero-split-grid">

                    <!-- Left: Narrative & Typography -->
                    <div>
                        <span class="section-index">PHOTOGRAPHY / PEOPLE / STORIES</span>
                        <h1 class="hero-display">
                            FIND THE<br>PHOTOGRAPHER<br>FOR YOUR STORY.
                        </h1>
                        <p class="hero-lead-text">
                            PhotoConnect brings together Vietnam&rsquo;s most distinctive visual artists for editorial portraiture, intimate weddings, and commercial storytelling.
                        </p>
                        <div class="hero-actions-row">
                            <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary btn-lg">
                                Explore Photographers &rarr;
                            </a>
                            <a href="${pageContext.request.contextPath}/become-photographer" class="btn btn-secondary btn-lg">
                                Join as Artist
                            </a>
                        </div>
                    </div>

                    <!-- Right: Large Asymmetric Photography Composition -->
                    <div class="hero-photo-stage" id="heroPhotoStage">
                        <div class="hero-frame-main" id="heroFrameMain">
                            <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80"
                                 alt="Editorial Fashion Portraiture in Studio"
                                 loading="eager">
                        </div>
                        <div class="hero-frame-secondary" id="heroFrameSecondary">
                            <img src="https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80"
                                 alt="Warm coastal wedding golden hour"
                                 loading="eager">
                        </div>
                        <div class="hero-caption-card">
                            <div class="hero-caption-label">Curated Selection</div>
                            <div class="hero-caption-title">Editorial &amp; Documentary Artists</div>
                        </div>
                    </div>

                </div>
            </div>
        </section>

        <!-- ── 01 / Featured Photographers ───────────────────────────── -->
        <section class="pc-section" style="padding: clamp(4rem, 7vw, 6rem) 0; border-bottom: 1px solid var(--border);">
            <div class="editorial-container">
                <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 3.5rem; flex-wrap: wrap; gap: 1.5rem;">
                    <div>
                        <span class="section-index">01 / Verified Artists</span>
                        <h2 class="editorial-heading" style="margin-bottom: 0;">Featured Photographers</h2>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/photographers" class="primary-link">
                            View full directory (${fn:length(featuredPhotographers)}+ artists) &rarr;
                        </a>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty featuredPhotographers}">
                        <div class="pc-empty-state">
                            <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Our roster is currently being curated</h3>
                            <p>Newly approved visual artists will appear here shortly.</p>
                            <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary" style="margin-top: 1rem;">Browse Directory</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="photographer-grid">
                            <c:forEach var="p" items="${featuredPhotographers}">
                                <a href="${pageContext.request.contextPath}/photographers/${p.id}" class="photographer-card" id="featured-photographer-${p.id}">
                                    <div class="photo-frame">
                                        <c:choose>
                                            <c:when test="${not empty p.coverImageUrl}">
                                                <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio cover" loading="lazy">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="pc-card-placeholder">
                                                    <div class="placeholder-initial"><c:out value="${fn:toUpperCase(fn:substring(p.displayName, 0, 1))}"/></div>
                                                    <div class="placeholder-badge">Editorial Artist</div>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="card-meta">
                                        <div>
                                            <h3 class="card-name"><c:out value="${p.displayName}"/></h3>
                                            <div class="card-subtitle">
                                                <c:choose>
                                                    <c:when test="${not empty p.city}"><c:out value="${p.city}"/></c:when>
                                                    <c:otherwise>Vietnam</c:otherwise>
                                                </c:choose>
                                                &bull;
                                                <c:choose>
                                                    <c:when test="${not empty p.priceFrom}">
                                                        From <fmt:formatNumber value="${p.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                                    </c:when>
                                                    <c:otherwise>Rates on request</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                        <div class="card-arrow">&rarr;</div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <!-- ── 02 / Selected Work & Editorial Photo Composition ──────── -->
        <section class="pc-section" style="padding: clamp(4rem, 7vw, 6rem) 0; border-bottom: 1px solid var(--border);">
            <div class="editorial-container">
                <div style="max-width: 720px;">
                    <span class="section-index">02 / Selected Work</span>
                    <h2 class="editorial-heading">Every Frame Tells a Human Story</h2>
                    <p class="pc-lead">
                        From raw Old Quarter portraits to intimate seaside celebrations, explore the depth of visual storytelling created across the PhotoConnect network.
                    </p>
                </div>

                <div class="editorial-gallery-grid">
                    <div class="gallery-item-wide">
                        <img src="https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1200&q=80"
                             alt="Natural daylight editorial in Hanoi Old Quarter"
                             loading="lazy">
                        <div class="gallery-overlay-badge">Editorial Portraiture &middot; Hanoi</div>
                    </div>
                    <div class="gallery-item-tall">
                        <img src="https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=1000&q=80"
                             alt="Intimate wedding reception details"
                             loading="lazy">
                        <div class="gallery-overlay-badge">Documentary Wedding &middot; Da Nang</div>
                    </div>
                    <div class="gallery-item-small">
                        <img src="https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80"
                             alt="Monochrome lookbook photography"
                             loading="lazy">
                        <div class="gallery-overlay-badge">Lookbook &middot; Saigon</div>
                    </div>
                    <div class="gallery-item-small">
                        <img src="https://images.unsplash.com/photo-1554046920-90dc5f3ac6ed?auto=format&fit=crop&w=800&q=80"
                             alt="Studio creator session"
                             loading="lazy">
                        <div class="gallery-overlay-badge">Personal Brand &middot; Hue</div>
                    </div>
                    <div class="gallery-item-small">
                        <img src="https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80"
                             alt="Sunset ceremony along the coast"
                             loading="lazy">
                        <div class="gallery-overlay-badge">Destination &middot; Nha Trang</div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ── 03 / How PhotoConnect Works ───────────────────────────── -->
        <section class="pc-section" style="padding: clamp(4rem, 7vw, 6.5rem) 0; border-bottom: 1px solid var(--border);">
            <div class="editorial-container">
                <div style="text-align: center; max-width: 660px; margin: 0 auto;">
                    <span class="section-index">03 / The Process</span>
                    <h2 class="editorial-heading" style="margin-bottom: 1rem;">A Clear Path from Vision to Session</h2>
                    <p class="pc-lead" style="margin-inline: auto;">
                        Connecting with exceptional talent should feel effortless, transparent, and refined.
                    </p>
                </div>

                <div class="editorial-process-grid">
                    <div class="process-column">
                        <div class="process-num">01</div>
                        <h3 class="process-heading">Discover</h3>
                        <p class="process-text">
                            Explore verified portfolios filtered by style, city, and starting rate. Examine full resolution galleries before reaching out.
                        </p>
                    </div>

                    <div class="process-column">
                        <div class="process-num">02</div>
                        <h3 class="process-heading">Book &amp; Secure</h3>
                        <p class="process-text">
                            Select your date and preferred time. Once the photographer accepts, lock in your booking with a safe 30% demonstration deposit.
                        </p>
                    </div>

                    <div class="process-column">
                        <div class="process-num">03</div>
                        <h3 class="process-heading">Create</h3>
                        <p class="process-text">
                            Coordinate shot lists directly via dedicated real-time messaging, meet on set, and receive your final delivered photograph collection.
                        </p>
                    </div>
                </div>
            </div>
        </section>

        <!-- ── 04 / Final Call to Action ─────────────────────────────── -->
        <section class="home-cta-section">
            <div class="editorial-container">
                <h2 class="editorial-heading" style="font-size: clamp(2.25rem, 5vw, 4rem);">
                    Ready to capture your next story?
                </h2>
                <p>
                    Discover exceptional photographers across Vietnam, or apply to join our verified roster of independent visual artists.
                </p>
                <div class="home-cta-actions">
                    <a href="${pageContext.request.contextPath}/photographers" class="btn btn-editorial-light btn-lg">
                        Explore Photographers &rarr;
                    </a>
                    <a href="${pageContext.request.contextPath}/become-photographer" class="btn btn-editorial-outline btn-lg">
                        Apply to Join the Roster
                    </a>
                </div>
            </div>
        </section>
    </main>

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
                    <a href="${pageContext.request.contextPath}/photographers" class="text-link">Explore</a>
                    <a href="${pageContext.request.contextPath}/become-photographer" class="text-link">Join Roster</a>
                </div>
            </div>
        </div>
    </footer>

    <!-- Restrained Hero Pointer Movement (Respects prefers-reduced-motion) -->
    <script>
        (function() {
            if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
                return;
            }
            const stage = document.getElementById('heroPhotoStage');
            const mainFrame = document.getElementById('heroFrameMain');
            const secondaryFrame = document.getElementById('heroFrameSecondary');
            if (!stage || !mainFrame || !secondaryFrame) return;

            stage.addEventListener('mousemove', function(e) {
                const rect = stage.getBoundingClientRect();
                const x = (e.clientX - rect.left) / rect.width - 0.5;
                const y = (e.clientY - rect.top) / rect.height - 0.5;

                mainFrame.style.transform = 'translate3d(' + (x * -12) + 'px, ' + (y * -12) + 'px, 0)';
                secondaryFrame.style.transform = 'translate3d(' + (x * 16) + 'px, ' + (y * 16) + 'px, 0)';
            });

            stage.addEventListener('mouseleave', function() {
                mainFrame.style.transform = 'translate3d(0, 0, 0)';
                secondaryFrame.style.transform = 'translate3d(0, 0, 0)';
            });
        })();
    </script>
</body>
</html>
