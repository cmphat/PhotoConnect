<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>PhotoConnect — Editorial Photography Marketplace</title>
    <meta name="description" content="Discover inspiring portfolios, connect with talented photographers, and book your next shoot on PhotoConnect.">

    <style>
        /* ── Homepage Editorial Layout ──────────────────────────────────────── */
        .home-hero-wrap {
            padding: clamp(3rem, 6vw, 5.5rem) 0 clamp(2.5rem, 5vw, 4rem);
            background: var(--pc-background, #f8fafc);
            border-bottom: 1px solid var(--pc-border, #cbd5e1);
        }

        .hero-layout-grid {
            display: grid;
            grid-template-columns: minmax(0, 5fr) minmax(0, 7fr);
            gap: clamp(2.5rem, 5vw, 5rem);
            align-items: center;
        }

        .hero-content-col {
            max-width: 520px;
        }

        .hero-editorial-headline {
            font-family: var(--pc-font-editorial, 'Playfair Display', Georgia, serif);
            font-size: clamp(2.5rem, 5.2vw, 4.25rem);
            font-weight: 700;
            line-height: 1.1;
            letter-spacing: -0.025em;
            color: var(--pc-navy, #0f172a);
            margin: 0 0 1.5rem 0;
        }

        .hero-supporting-text {
            font-family: var(--pc-font, 'Inter', sans-serif);
            font-size: clamp(1.05rem, 1.4vw, 1.2rem);
            line-height: 1.65;
            color: var(--pc-text-secondary, #64748b);
            margin: 0 0 2.25rem 0;
            font-weight: 400;
        }

        .hero-actions {
            display: flex;
            align-items: center;
            gap: 1rem;
            flex-wrap: wrap;
        }

        /* ── Hero Editorial Photo Collage (Photography First) ─────────────── */
        .hero-photo-collage {
            display: grid;
            grid-template-columns: 1.1fr 0.9fr;
            grid-template-rows: 240px 240px;
            gap: 1.25rem;
            width: 100%;
        }

        .collage-photo-main {
            grid-column: 1 / 2;
            grid-row: 1 / 3;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            background: var(--pc-surface-secondary, #e9eef5);
            border: 1px solid var(--pc-border, #cbd5e1);
            position: relative;
        }

        .collage-photo-top {
            grid-column: 2 / 3;
            grid-row: 1 / 2;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            background: var(--pc-surface-secondary, #e9eef5);
            border: 1px solid var(--pc-border, #cbd5e1);
            position: relative;
        }

        .collage-photo-bottom {
            grid-column: 2 / 3;
            grid-row: 2 / 3;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            background: var(--pc-surface-secondary, #e9eef5);
            border: 1px solid var(--pc-border, #cbd5e1);
            position: relative;
        }

        .collage-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform 0.4s ease;
        }

        .collage-photo-main:hover .collage-img,
        .collage-photo-top:hover .collage-img,
        .collage-photo-bottom:hover .collage-img {
            transform: scale(1.02);
        }

        .collage-caption {
            position: absolute;
            bottom: 0.85rem;
            left: 0.85rem;
            background: rgba(15, 23, 42, 0.78);
            backdrop-filter: blur(8px);
            color: #ffffff;
            font-size: 0.75rem;
            font-weight: 500;
            padding: 4px 10px;
            border-radius: var(--pc-radius-sm, 6px);
            letter-spacing: 0.02em;
        }

        /* ── Integrated Search Continuation ────────────────────────────────── */
        .home-search-region {
            background: var(--pc-surface, #ffffff);
            border-bottom: 1px solid var(--pc-border, #cbd5e1);
            padding: 2.25rem 0;
        }

        .search-shell {
            background: var(--pc-background, #f8fafc);
            border: 1px solid var(--pc-border, #cbd5e1);
            border-radius: var(--pc-radius-lg, 14px);
            padding: 1.5rem 1.75rem;
        }

        .search-form-grid {
            display: grid;
            grid-template-columns: minmax(0, 1.3fr) minmax(0, 1.1fr) minmax(0, 1fr) auto;
            gap: 1.25rem;
            align-items: flex-end;
        }

        .search-field-unit {
            display: flex;
            flex-direction: column;
            gap: 0.45rem;
        }

        .search-field-label {
            font-size: 0.82rem;
            font-weight: 600;
            color: var(--pc-navy, #0f172a);
            letter-spacing: 0.01em;
        }

        .search-field-input {
            width: 100%;
            height: 44px;
            padding: 0 1rem;
            background: var(--pc-surface, #ffffff);
            border: 1px solid var(--pc-border, #cbd5e1);
            border-radius: var(--pc-radius-sm, 6px);
            color: var(--pc-navy, #0f172a);
            font-size: 0.92rem;
            font-family: inherit;
            transition: border-color 0.15s ease, box-shadow 0.15s ease;
        }

        .search-field-input:focus {
            outline: none;
            border-color: var(--pc-primary, #2563eb);
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }

        .search-submit-btn {
            height: 44px;
            padding: 0 1.75rem;
            font-weight: 600;
            font-size: 0.92rem;
        }

        .advanced-filter-toggle {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            background: none;
            border: none;
            padding: 0;
            color: var(--pc-text-secondary, #64748b);
            font-size: 0.85rem;
            font-weight: 500;
            cursor: pointer;
            margin-top: 1rem;
            transition: color 0.15s ease;
        }

        .advanced-filter-toggle:hover {
            color: var(--pc-navy, #0f172a);
        }

        .advanced-filter-panel {
            display: none;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 1rem;
            margin-top: 1.25rem;
            padding-top: 1.25rem;
            border-top: 1px solid var(--pc-border, #cbd5e1);
        }

        .advanced-filter-panel.is-open {
            display: grid;
        }

        /* ── Section Header Primitives ─────────────────────────────────────── */
        .editorial-section {
            padding: clamp(4rem, 7vw, 6rem) 0;
            border-bottom: 1px solid var(--pc-border, #cbd5e1);
        }

        .section-header-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            margin-bottom: 2.75rem;
            gap: 1.5rem;
            flex-wrap: wrap;
        }

        .section-tag {
            font-size: 0.78rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--pc-primary, #2563eb);
            margin-bottom: 0.5rem;
            display: block;
        }

        .section-title {
            font-family: var(--pc-font-editorial, 'Playfair Display', Georgia, serif);
            font-size: clamp(1.85rem, 3.2vw, 2.5rem);
            font-weight: 700;
            letter-spacing: -0.02em;
            color: var(--pc-navy, #0f172a);
            margin: 0;
        }

        .section-view-all {
            font-size: 0.9rem;
            font-weight: 600;
            color: var(--pc-navy, #0f172a);
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            transition: color 0.15s ease;
        }

        .section-view-all:hover {
            color: var(--pc-primary, #2563eb);
        }

        /* ── Minimal Photographer Cards Grid ──────────────────────────────── */
        .photographer-showcase-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 2rem;
        }

        .photographer-clean-card {
            background: var(--pc-surface, #ffffff);
            border: 1px solid var(--pc-border, #cbd5e1);
            border-radius: var(--pc-radius-lg, 14px);
            overflow: hidden;
            text-decoration: none;
            color: inherit;
            display: flex;
            flex-direction: column;
            transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
        }

        .photographer-clean-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--pc-shadow-card-hover, 0 10px 24px rgba(15, 23, 42, 0.08));
            border-color: rgba(37, 99, 235, 0.35);
        }

        .clean-card-media {
            width: 100%;
            aspect-ratio: 16 / 11;
            background: var(--pc-surface-secondary, #e9eef5);
            overflow: hidden;
            position: relative;
        }

        .clean-card-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform 0.35s ease;
        }

        .photographer-clean-card:hover .clean-card-img {
            transform: scale(1.025);
        }

        .clean-card-content {
            padding: 1.25rem 1.5rem 1.5rem;
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }

        .clean-card-name-row {
            display: flex;
            justify-content: space-between;
            align-items: baseline;
            gap: 0.75rem;
        }

        .clean-card-name {
            font-size: 1.1rem;
            font-weight: 700;
            color: var(--pc-navy, #0f172a);
            margin: 0;
            letter-spacing: -0.015em;
        }

        .clean-card-rate {
            font-size: 0.88rem;
            font-weight: 600;
            color: var(--pc-navy, #0f172a);
            white-space: nowrap;
        }

        .clean-card-specialization {
            font-size: 0.85rem;
            color: var(--pc-text-secondary, #64748b);
            line-height: 1.4;
            margin: 0;
        }

        .clean-card-location-meta {
            display: flex;
            align-items: center;
            gap: 0.35rem;
            font-size: 0.82rem;
            color: var(--pc-text-secondary, #64748b);
            margin-top: 0.25rem;
        }

        /* ── Curated Photography Editorial Grid ────────────────────────────── */
        .curated-work-grid {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 1.5rem;
        }

        .curated-item-large {
            grid-column: span 8;
            height: 480px;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            position: relative;
            background: var(--pc-surface-secondary, #e9eef5);
        }

        .curated-item-portrait {
            grid-column: span 4;
            height: 480px;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            position: relative;
            background: var(--pc-surface-secondary, #e9eef5);
        }

        .curated-item-third {
            grid-column: span 4;
            height: 340px;
            border-radius: var(--pc-radius-md, 10px);
            overflow: hidden;
            position: relative;
            background: var(--pc-surface-secondary, #e9eef5);
        }

        .curated-work-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform 0.4s ease;
        }

        .curated-item-large:hover .curated-work-img,
        .curated-item-portrait:hover .curated-work-img,
        .curated-item-third:hover .curated-work-img {
            transform: scale(1.025);
        }

        .curated-work-caption {
            position: absolute;
            bottom: 1rem;
            left: 1rem;
            background: rgba(15, 23, 42, 0.82);
            backdrop-filter: blur(8px);
            color: #ffffff;
            font-size: 0.78rem;
            font-weight: 500;
            padding: 5px 12px;
            border-radius: var(--pc-radius-sm, 6px);
        }

        /* ── How PhotoConnect Works (3 Understated Steps) ───────────────────── */
        .process-steps-row {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 3rem;
        }

        .process-step-item {
            display: flex;
            flex-direction: column;
            border-top: 1px solid var(--pc-border, #cbd5e1);
            padding-top: 1.75rem;
        }

        .step-index-label {
            font-size: 0.85rem;
            font-weight: 700;
            color: var(--pc-primary, #2563eb);
            margin-bottom: 0.85rem;
            letter-spacing: 0.04em;
        }

        .step-title {
            font-family: var(--pc-font, 'Inter', sans-serif);
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--pc-navy, #0f172a);
            margin: 0 0 0.65rem 0;
            letter-spacing: -0.015em;
        }

        .step-description {
            font-size: 0.92rem;
            line-height: 1.65;
            color: var(--pc-text-secondary, #64748b);
            margin: 0;
        }

        /* ── Clean Editorial Footer ────────────────────────────────────────── */
        .pc-site-footer {
            background: var(--pc-surface, #ffffff);
            border-top: 1px solid var(--pc-border, #cbd5e1);
            padding: 4rem 0 2.5rem;
        }

        .footer-main-grid {
            display: grid;
            grid-template-columns: 2fr 1fr 1fr 1fr;
            gap: 3rem;
            margin-bottom: 3.5rem;
        }

        .footer-brand-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--pc-navy, #0f172a);
            margin: 0 0 0.75rem 0;
            letter-spacing: -0.02em;
        }

        .footer-brand-desc {
            font-size: 0.9rem;
            line-height: 1.6;
            color: var(--pc-text-secondary, #64748b);
            max-width: 320px;
            margin: 0;
        }

        .footer-col-title {
            font-size: 0.82rem;
            font-weight: 700;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            color: var(--pc-navy, #0f172a);
            margin: 0 0 1.25rem 0;
        }

        .footer-links-list {
            list-style: none;
            padding: 0;
            margin: 0;
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
        }

        .footer-link-item a {
            color: var(--pc-text-secondary, #64748b);
            text-decoration: none;
            font-size: 0.88rem;
            transition: color 0.15s ease;
        }

        .footer-link-item a:hover {
            color: var(--pc-navy, #0f172a);
        }

        .footer-bottom-bar {
            padding-top: 2rem;
            border-top: 1px solid var(--pc-border, #cbd5e1);
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.85rem;
            color: var(--pc-text-secondary, #64748b);
            flex-wrap: wrap;
            gap: 1rem;
        }

        /* ── Responsive Viewports ──────────────────────────────────────────── */
        @media (max-width: 991px) {
            .hero-layout-grid {
                grid-template-columns: 1fr;
                gap: 3rem;
            }
            .hero-content-col {
                max-width: 100%;
            }
            .hero-photo-collage {
                grid-template-rows: 200px 200px;
            }
            .search-form-grid {
                grid-template-columns: 1fr 1fr;
            }
            .search-submit-btn {
                grid-column: span 2;
            }
            .curated-work-grid {
                grid-template-columns: 1fr;
            }
            .curated-item-large,
            .curated-item-portrait,
            .curated-item-third {
                grid-column: span 1;
                height: 320px;
            }
            .process-steps-row {
                grid-template-columns: 1fr;
                gap: 2rem;
            }
            .footer-main-grid {
                grid-template-columns: 1fr 1fr;
                gap: 2rem;
            }
        }

        @media (max-width: 640px) {
            .hero-photo-collage {
                grid-template-columns: 1fr;
                grid-template-rows: auto;
            }
            .collage-photo-main {
                grid-column: 1 / 2;
                grid-row: auto;
                height: 280px;
            }
            .collage-photo-top,
            .collage-photo-bottom {
                display: none;
            }
            .search-form-grid {
                grid-template-columns: 1fr;
            }
            .search-submit-btn {
                grid-column: span 1;
            }
            .footer-main-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<main>
    <!-- ── 01 / Photography-First Hero ────────────────────────────────── -->
    <section class="home-hero-wrap">
        <div class="pc-container">
            <div class="hero-layout-grid">
                <!-- Left: Focused Narrative -->
                <div class="hero-content-col">
                    <h1 class="hero-editorial-headline">
                        Find your perfect photographer.
                    </h1>
                    <p class="hero-supporting-text">
                        Explore inspiring portfolios, connect with talented photographers, and book your next shoot.
                    </p>
                    <div class="hero-actions d-flex flex-wrap align-items-center gap-3">
                        <a href="${pageContext.request.contextPath}/photographers" class="pc-btn-primary" style="padding: 13px 26px;">
                            Explore photographers &rarr;
                        </a>
                        <a href="${pageContext.request.contextPath}/feed" class="pc-btn-secondary" style="padding: 13px 24px;">
                            View collections
                        </a>
                    </div>
                </div>

                <!-- Right: Editorial Photo Composition -->
                <div class="hero-photo-collage">
                    <div class="collage-photo-main">
                        <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80"
                             alt="Natural daylight portraiture"
                             class="collage-img"
                             loading="eager">
                        <div class="collage-caption">Editorial Portraiture &middot; Hanoi</div>
                    </div>
                    <div class="collage-photo-top">
                        <img src="https://images.unsplash.com/photo-1606800052052-a08af7148866?auto=format&fit=crop&w=800&q=80"
                             alt="Intimate wedding moments"
                             class="collage-img"
                             loading="eager">
                        <div class="collage-caption">Wedding &middot; Da Nang</div>
                    </div>
                    <div class="collage-photo-bottom">
                        <img src="https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80"
                             alt="Contemporary fashion lookbook"
                             class="collage-img"
                             loading="eager">
                        <div class="collage-caption">Lookbook &middot; Saigon</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ── 02 / Integrated Search Experience ─────────────────────────── -->
    <section class="home-search-region">
        <div class="pc-container">
            <div class="search-shell">
                <form action="${pageContext.request.contextPath}/photographers" method="get" id="homeSearchForm">
                    <div class="search-form-grid">
                        <!-- Style Field -->
                        <div class="search-field-unit">
                            <label for="homeKeyword" class="search-field-label">Photography Style</label>
                            <input type="text"
                                   id="homeKeyword"
                                   name="keyword"
                                   class="search-field-input"
                                   placeholder="Portrait, wedding, fashion, commercial..."
                                   maxlength="100">
                        </div>

                        <!-- Location Field -->
                        <div class="search-field-unit">
                            <label for="homeCity" class="search-field-label">Location</label>
                            <input type="text"
                                   id="homeCity"
                                   name="city"
                                   class="search-field-input"
                                   placeholder="Hanoi, Ho Chi Minh City, Da Nang..."
                                   maxlength="100">
                        </div>

                        <!-- Date Field -->
                        <div class="search-field-unit">
                            <label for="homeDate" class="search-field-label">Shoot Date</label>
                            <input type="date"
                                   id="homeDate"
                                   name="date"
                                   class="search-field-input">
                        </div>

                        <!-- Search Action Button -->
                        <button type="submit" class="pc-btn-primary search-submit-btn">
                            Search Photographers
                        </button>
                    </div>

                    <!-- Collapsible Advanced Filters -->
                    <button type="button" class="advanced-filter-toggle" id="advancedFilterToggleBtn" aria-expanded="false">
                        <span>Adjust budget &amp; filters</span> ▾
                    </button>

                    <div class="advanced-filter-panel" id="advancedFilterPanel">
                        <div class="search-field-unit">
                            <label for="homeMinPrice" class="search-field-label">Min Budget (VND)</label>
                            <input type="number"
                                   id="homeMinPrice"
                                   name="minPrice"
                                   min="0"
                                   step="100000"
                                   class="search-field-input"
                                   placeholder="0">
                        </div>
                        <div class="search-field-unit">
                            <label for="homeMaxPrice" class="search-field-label">Max Budget (VND)</label>
                            <input type="number"
                                   id="homeMaxPrice"
                                   name="maxPrice"
                                   min="0"
                                   step="100000"
                                   class="search-field-input"
                                   placeholder="Any">
                        </div>
                        <div class="search-field-unit">
                            <label for="homeExperience" class="search-field-label">Min Experience (Years)</label>
                            <input type="number"
                                   id="homeExperience"
                                   name="minExperience"
                                   min="0"
                                   max="50"
                                   class="search-field-input"
                                   placeholder="0">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </section>

    <!-- ── 03 / Featured Photographers ───────────────────────────────── -->
    <section class="editorial-section">
        <div class="pc-container">
            <div class="section-header-row d-flex justify-content-between align-items-center">
                <div>
                    <span class="section-tag">Featured Talent</span>
                    <h2 class="section-title">Photographers in the Spotlight</h2>
                </div>
                <a href="${pageContext.request.contextPath}/photographers" class="section-view-all">
                    View all photographers &rarr;
                </a>
            </div>

            <c:choose>
                <c:when test="${empty featuredPhotographers}">
                    <div class="pc-empty-state" style="text-align: center; padding: 4rem 1rem;">
                        <p style="color: var(--pc-text-secondary); margin-bottom: 1.5rem;">Photographer profiles will appear here shortly.</p>
                        <a href="${pageContext.request.contextPath}/photographers" class="pc-btn-primary">Browse Directory</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="photographer-showcase-grid">
                        <c:forEach var="p" items="${featuredPhotographers}">
                            <a href="${pageContext.request.contextPath}/photographers/${p.id}" class="photographer-clean-card" id="featured-photographer-${p.id}">
                                <div class="clean-card-media">
                                    <c:choose>
                                        <c:when test="${not empty p.coverImageUrl}">
                                            <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio" class="clean-card-img" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #e2e8f0; color: #64748b; font-weight: 600;">
                                                <c:out value="${p.displayName}"/>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="clean-card-content">
                                    <div class="clean-card-name-row">
                                        <h3 class="clean-card-name"><c:out value="${p.displayName}"/></h3>
                                        <span class="clean-card-rate">
                                            <c:choose>
                                                <c:when test="${not empty p.priceFrom}">
                                                    From <fmt:formatNumber value="${p.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                                </c:when>
                                                <c:otherwise>Rates on request</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>

                                    <p class="clean-card-specialization">
                                        <c:choose>
                                            <c:when test="${not empty p.specialtyDisplayNames}">
                                                <c:forEach var="spec" items="${p.specialtyDisplayNames}" varStatus="status">
                                                    <c:out value="${spec}"/><c:if test="${!status.last}"> &bull; </c:if>
                                                </c:forEach>
                                            </c:when>
                                            <c:when test="${not empty p.headline}">
                                                <c:out value="${p.headline}"/>
                                            </c:when>
                                            <c:otherwise>Editorial &middot; Visual Storytelling</c:otherwise>
                                        </c:choose>
                                    </p>

                                    <div class="clean-card-location-meta">
                                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                        <span><c:out value="${not empty p.city ? p.city : 'Vietnam'}"/></span>
                                        <c:if test="${not empty p.averageRating && p.averageRating > 0}">
                                            <span style="margin-left: auto; color: var(--pc-navy); font-weight: 600;">★ <fmt:formatNumber value="${p.averageRating}" maxFractionDigits="1"/></span>
                                        </c:if>
                                    </div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <!-- ── 04 / Curated Photography Grid ─────────────────────────────── -->
    <section class="editorial-section">
        <div class="pc-container">
            <div class="section-header-row">
                <div>
                    <span class="section-tag">Visual Archives</span>
                    <h2 class="section-title">Curated Work Across Vietnam</h2>
                </div>
                <a href="${pageContext.request.contextPath}/feed" class="section-view-all">
                    Explore visual feed &rarr;
                </a>
            </div>

            <div class="curated-work-grid">
                <div class="curated-item-large">
                    <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80"
                         alt="Editorial portrait in studio"
                         class="curated-work-img"
                         loading="lazy">
                    <div class="curated-work-caption">Studio Portraiture &middot; Hanoi</div>
                </div>
                <div class="curated-item-portrait">
                    <img src="https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80"
                         alt="Documentary portrait"
                         class="curated-work-img"
                         loading="lazy">
                    <div class="curated-work-caption">Creator Session &middot; Hue</div>
                </div>
                <div class="curated-item-third">
                    <img src="https://images.unsplash.com/photo-1606800052052-a08af7148866?auto=format&fit=crop&w=800&q=80"
                         alt="Intimate wedding ceremony"
                         class="curated-work-img"
                         loading="lazy">
                    <div class="curated-work-caption">Documentary Wedding &middot; Da Nang</div>
                </div>
                <div class="curated-item-third">
                    <img src="https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80"
                         alt="Editorial fashion lookbook"
                         class="curated-work-img"
                         loading="lazy">
                    <div class="curated-work-caption">Lookbook &middot; Saigon</div>
                </div>
                <div class="curated-item-third">
                    <img src="https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80"
                         alt="Seaside golden hour celebration"
                         class="curated-work-img"
                         loading="lazy">
                    <div class="curated-work-caption">Destination &middot; Nha Trang</div>
                </div>
            </div>
        </div>
    </section>

    <!-- ── 05 / How PhotoConnect Works ───────────────────────────────── -->
    <section class="editorial-section" style="background: var(--pc-background, #f8fafc);">
        <div class="pc-container">
            <div style="max-width: 640px; margin-bottom: 3.5rem;">
                <span class="section-tag">How It Works</span>
                <h2 class="section-title">A Clear Path from Vision to Session</h2>
            </div>

            <div class="process-steps-row">
                <div class="process-step-item">
                    <span class="step-index-label">Step 01</span>
                    <h3 class="step-title">Discover photographers</h3>
                    <p class="step-description">
                        Browse verified portfolios filtered by photography style, city, and rate. Examine real work and client evaluations.
                    </p>
                </div>
                <div class="process-step-item">
                    <span class="step-index-label">Step 02</span>
                    <h3 class="step-title">Choose a service &amp; book</h3>
                    <p class="step-description">
                        Select your preferred date, location, and concept details. Send booking requests directly with transparent terms.
                    </p>
                </div>
                <div class="process-step-item">
                    <span class="step-index-label">Step 03</span>
                    <h3 class="step-title">Connect &amp; complete your shoot</h3>
                    <p class="step-description">
                        Coordinate shot lists via built-in messaging, meet on set, and receive your high-resolution finished gallery.
                    </p>
                </div>
            </div>
        </div>
    </section>
</main>

<!-- ── Editorial Footer ──────────────────────────────────────────────── -->
<footer class="pc-site-footer">
    <div class="pc-container">
        <div class="footer-main-grid">
            <div>
                <h4 class="footer-brand-title">PhotoConnect</h4>
                <p class="footer-brand-desc">
                    A contemporary photography marketplace connecting clients with exceptional visual talent across Vietnam.
                </p>
            </div>
            <div>
                <h5 class="footer-col-title">Explore</h5>
                <ul class="footer-links-list">
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/photographers">Photographers</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/feed">Collections</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/photographers?keyword=Wedding">Weddings</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/photographers?keyword=Portrait">Portraits</a></li>
                </ul>
            </div>
            <div>
                <h5 class="footer-col-title">For Photographers</h5>
                <ul class="footer-links-list">
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/register">Join as Artist</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/photographer/dashboard">Studio Console</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/photographer/portfolio">Manage Portfolio</a></li>
                </ul>
            </div>
            <div>
                <h5 class="footer-col-title">Account</h5>
                <ul class="footer-links-list">
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/login">Sign In</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/register">Create Account</a></li>
                    <li class="footer-link-item"><a href="${pageContext.request.contextPath}/bookings">My Bookings</a></li>
                </ul>
            </div>
        </div>

        <div class="footer-bottom-bar">
            <div>&copy; 2026 PhotoConnect. All rights reserved.</div>
            <div>Professional Creative Platform</div>
        </div>
    </div>
</footer>

<script>
    (function() {
        const toggleBtn = document.getElementById('advancedFilterToggleBtn');
        const panel = document.getElementById('advancedFilterPanel');
        if (toggleBtn && panel) {
            toggleBtn.addEventListener('click', function() {
                const isOpen = panel.classList.toggle('is-open');
                toggleBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
                toggleBtn.innerHTML = isOpen ? '<span>Hide budget &amp; filters</span> ▴' : '<span>Adjust budget &amp; filters</span> ▾';
            });
        }
    })();
</script>
