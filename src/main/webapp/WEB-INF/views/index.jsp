<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="PhotoConnect is a curated marketplace for editorial and professional photography.">
    <title>PhotoConnect | Premium Photography Marketplace</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .home-hero {
            position: relative;
            height: 95vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: var(--bg-dark-secondary);
            overflow: hidden;
        }
        
        /* Placeholder background if no three.js yet */
        .hero-bg-placeholder {
            position: absolute;
            inset: 0;
            background: linear-gradient(to bottom, rgba(10,10,10,0.3), rgba(10,10,10,0.8)), url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect width="100" height="100" fill="%23111"/></svg>');
            background-size: cover;
            background-position: center;
            z-index: 0;
        }

        .hero-content {
            position: relative;
            z-index: 10;
            text-align: center;
            padding: 0 2rem;
        }
        
        .hero-subtitle {
            font-size: clamp(1rem, 2vw, 1.25rem);
            font-weight: 300;
            color: var(--text-on-dark);
            opacity: 0.8;
            margin-top: 1.5rem;
            margin-bottom: 3rem;
            max-width: 500px;
            margin-left: auto;
            margin-right: auto;
        }

        .editorial-section {
            padding: 8rem 0;
            border-bottom: 1px solid var(--border-dark);
        }

        .home-footer {
            padding: 4rem 0;
            text-align: center;
            color: var(--text-muted);
            font-size: 0.85rem;
        }
    </style>
</head>
<body>

    <!-- Transparent Navbar for Hero -->
    <jsp:include page="fragments/navbar.jsp" />

    <main>
        <!-- ── Cinematic Hero ────────────────────────────────────────── -->
        <section class="home-hero">
            <!-- 3D Hook -->
            <div id="three-hero-container"></div>
            
            <div class="hero-bg-placeholder"></div>

            <div class="hero-content">
                <h1 class="hero-display">
                    Photo<br>Connect
                </h1>
                <p class="hero-subtitle">
                    Find photographers for moments worth remembering.
                </p>
                <a href="/photographers" class="primary-link" style="font-size: 1.1rem; border-bottom-width: 2px;">Explore photographers →</a>
            </div>
        </section>

        <!-- ── 01 / Discover ─────────────────────────────────────────── -->
        <section class="editorial-section">
            <div class="editorial-container">
                <span class="section-index">01 / Discover</span>
                <h2 class="editorial-heading" style="max-width: 800px;">
                    Curated visual artists for editorial, commercial, and portrait storytelling.
                </h2>
                <div style="margin-top: 4rem;">
                    <a href="/photographers" class="text-link" style="font-size: 1.25rem;">View all artists</a>
                </div>
            </div>
        </section>

        <!-- ── 02 / Selected Work ────────────────────────────────────── -->
        <section class="editorial-section">
            <div class="editorial-container">
                <span class="section-index">02 / Selected Artists</span>
                
                <c:choose>
                    <c:when test="${empty featuredPhotographers}">
                        <div style="padding: 4rem 0; color: var(--text-muted);">
                            <p>Our roster is currently being curated.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="photographer-grid" style="margin-top: 4rem;">
                            <c:forEach var="p" items="${featuredPhotographers}">
                                <a href="/photographers/${p.id}" class="photographer-card">
                                    <div class="photo-frame">
                                        <c:choose>
                                            <c:when test="${not empty p.coverImageUrl}">
                                                <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio" loading="lazy">
                                            </c:when>
                                            <c:otherwise>
                                                <!-- Visual placeholder -->
                                                <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: var(--bg-dark); color: var(--border-dark); font-size: 4rem; font-weight: 300;">
                                                    <c:out value="${fn:toUpperCase(fn:substring(p.displayName, 0, 1))}"/>
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
                                                    <c:otherwise>Global</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                        <div class="card-arrow" style="font-size: 1.25rem;">→</div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <!-- ── 03 / Become a Photographer ────────────────────────────── -->
        <section class="editorial-section" style="border-bottom: none; text-align: center; padding: 12rem 0;">
            <div class="editorial-container">
                <span class="section-index">03 / Join the Roster</span>
                <h2 class="editorial-heading" style="margin: 0 auto 3rem auto; max-width: 700px;">
                    Are you a professional visual artist?
                </h2>
                <a href="/become-photographer" class="primary-link" style="font-size: 1.25rem; border-bottom-width: 2px;">Apply to join PhotoConnect</a>
            </div>
        </section>
    </main>

    <footer class="home-footer">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace.
        </div>
    </footer>

    <!-- For future Three.js injection -->
    <script>
        // document.addEventListener('DOMContentLoaded', () => { initThreeJS(); });
    </script>
</body>
</html>
