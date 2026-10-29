<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Discover Photographers — PhotoConnect</title>
    <meta name="description" content="Discover and connect with top-tier verified professional photographers on PhotoConnect. Filter by location, aesthetic style, and budget.">

    <style>

        /* ── Hero Editorial Header ─────────────────────────────────── */
        .marketplace-hero-section {
            position: relative;
            padding: clamp(3rem, 5vw, 4.5rem) 0 2rem;
            background: radial-gradient(circle at 50% 0%, rgba(37, 99, 235, 0.08) 0%, transparent 70%);
            border-bottom: 1px solid var(--border-subtle);
            margin-bottom: 2.5rem;
        }

        .hero-editorial-title {
            font-family: var(--pc-font-editorial, 'Playfair Display', Georgia, serif);
            font-size: clamp(2.4rem, 5vw, 3.5rem);
            font-weight: 700;
            line-height: 1.15;
            color: var(--pc-navy);
            margin-bottom: 0.85rem;
            letter-spacing: -0.02em;
        }

        .hero-editorial-desc {
            font-size: clamp(1rem, 1.25vw, 1.15rem);
            color: var(--muted);
            max-width: 720px;
            line-height: 1.65;
            margin-bottom: 1.75rem;
            font-weight: 400;
        }

        /* ── Mode Switcher ────────────────────────────────────────── */
        .mode-switcher-container {
            display: inline-flex;
            background: var(--surface);
            border: 1px solid var(--border);
            padding: 4px;
            border-radius: var(--pc-radius-md, 10px);
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
            margin-bottom: 1rem;
        }

        .mode-switch-btn {
            padding: 0.5rem 1.25rem;
            font-size: 0.88rem;
            font-weight: 600;
            border-radius: var(--pc-radius-sm, 6px);
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            transition: all var(--transition-fast);
            color: var(--muted);
        }

        .mode-switch-btn.active {
            background: var(--accent);
            color: #ffffff !important;
        }

        .mode-switch-btn:hover:not(.active) {
            color: var(--text);
            background: var(--surface-elevated);
        }

        /* ── Floating Unified Search Toolbar ──────────────────────── */
        .search-hero-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--pc-radius-lg, 14px);
            padding: 1.5rem 1.75rem;
            margin-bottom: 2rem;
            box-shadow: var(--pc-shadow-card);
            transition: box-shadow var(--transition-fast), border-color var(--transition-fast);
        }

        .search-hero-card:focus-within {
            border-color: rgba(37, 99, 235, 0.4);
            box-shadow: 0 16px 42px -6px rgba(37, 99, 235, 0.09), 0 0 0 2px rgba(37, 99, 235, 0.15);
        }

        .search-input-unit {
            display: flex;
            flex-direction: column;
            gap: 0.35rem;
        }

        .search-input-unit label {
            font-size: 0.74rem;
            font-weight: 700;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            color: var(--muted);
            display: flex;
            align-items: center;
            gap: 0.35rem;
        }

        .search-input-control {
            width: 100%;
            height: 48px;
            padding: 0.5rem 0.95rem;
            border: 1px solid var(--border);
            border-radius: 10px;
            background: var(--surface-elevated);
            color: var(--text);
            font-size: 0.94rem;
            font-weight: 400;
            transition: all var(--transition-fast);
            box-sizing: border-box;
        }

        .search-input-control:focus {
            outline: none;
            background: var(--surface);
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }

        .btn-search-submit {
            height: 48px;
            background: var(--accent);
            color: #ffffff;
            font-weight: 700;
            font-size: 0.95rem;
            border-radius: 10px;
            border: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.55rem;
            padding: 0 1.5rem;
            cursor: pointer;
            box-shadow: 0 4px 14px rgba(37, 99, 235, 0.28);
            transition: all var(--transition-fast);
        }

        .btn-search-submit:hover {
            background: var(--accent-hover);
            transform: translateY(-1px);
            box-shadow: 0 6px 18px rgba(37, 99, 235, 0.36);
        }

        .btn-search-reset {
            height: 48px;
            width: 48px;
            background: var(--surface-elevated);
            color: var(--muted);
            border: 1px solid var(--border);
            border-radius: 10px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            transition: all var(--transition-fast);
        }

        .btn-search-reset:hover {
            color: var(--danger);
            border-color: var(--danger-border);
            background: var(--danger-bg);
        }

        /* ── Quick Tags Scroller ──────────────────────────────────── */
        .quick-tags-scroller {
            display: flex;
            align-items: center;
            gap: 0.55rem;
            overflow-x: auto;
            scrollbar-width: none;
            margin-top: 1.15rem;
            padding-top: 1rem;
            border-top: 1px solid var(--border-subtle);
        }

        .quick-tags-scroller::-webkit-scrollbar {
            display: none;
        }

        .quick-tag-label {
            font-size: 0.76rem;
            font-weight: 700;
            color: var(--muted);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            white-space: nowrap;
            margin-right: 0.25rem;
        }

        .quick-filter-chip {
            padding: 0.35rem 0.85rem;
            font-size: 0.82rem;
            font-weight: 500;
            border-radius: var(--pc-radius-sm, 6px);
            background: var(--surface-elevated);
            color: var(--muted);
            border: 1px solid var(--border);
            text-decoration: none;
            white-space: nowrap;
            cursor: pointer;
            transition: all var(--transition-fast);
        }

        .quick-filter-chip:hover {
            color: var(--text);
            border-color: var(--accent);
            background: var(--surface);
        }

        .quick-filter-chip.active {
            background: var(--accent);
            color: #ffffff;
            border-color: var(--accent);
        }

        /* ── Photographer Cards Grid ──────────────────────────────── */
        .photographer-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
            gap: 2rem;
        }

        .photographer-editorial-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--pc-radius-lg, 14px);
            overflow: hidden;
            text-decoration: none;
            color: inherit;
            display: flex;
            flex-direction: column;
            transition: transform 0.32s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.32s ease, border-color 0.2s ease;
            box-shadow: var(--pc-shadow-card);
            position: relative;
        }

        .photographer-editorial-card:hover {
            transform: translateY(-6px);
            box-shadow: var(--pc-shadow-card-hover);
            border-color: rgba(37, 99, 235, 0.35);
        }

        .card-cover-wrapper {
            position: relative;
            aspect-ratio: 16/10;
            overflow: hidden;
            background: #0f172a;
        }

        .card-cover-wrapper img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
        }

        .photographer-editorial-card:hover .card-cover-wrapper img {
            transform: scale(1.07);
        }

        .card-cover-gradient {
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, rgba(15, 23, 42, 0.35) 0%, transparent 40%, rgba(15, 23, 42, 0.85) 100%);
            pointer-events: none;
        }

        .card-photo-top-bar {
            position: absolute;
            top: 0.9rem;
            left: 0.9rem;
            right: 0.9rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            z-index: 2;
        }

        .badge-frosted-glass {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            padding: 0.35rem 0.75rem;
            border-radius: 50px;
            font-size: 0.78rem;
            font-weight: 700;
            background: rgba(15, 23, 42, 0.75);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            color: #ffffff;
            border: 1px solid rgba(255, 255, 255, 0.18);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }

        .badge-frosted-glass.rating {
            background: rgba(245, 158, 11, 0.9);
            color: #ffffff;
            border: none;
        }

        .card-photo-bottom-bar {
            position: absolute;
            bottom: 0.9rem;
            left: 0.9rem;
            right: 0.9rem;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            z-index: 2;
        }

        .badge-price-tag {
            background: rgba(15, 23, 42, 0.85);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            color: #ffffff;
            padding: 0.4rem 0.85rem;
            border-radius: 8px;
            font-size: 0.88rem;
            font-weight: 700;
        }

        .card-details-body {
            padding: 1.35rem 1.4rem;
            display: flex;
            flex-direction: column;
            flex-grow: 1;
            background: var(--surface);
        }

        .artist-title-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 0.5rem;
            margin-bottom: 0.35rem;
        }

        .artist-name {
            font-size: 1.25rem;
            font-weight: 800;
            color: var(--text);
            margin: 0;
            letter-spacing: -0.01em;
            display: flex;
            align-items: center;
            gap: 0.4rem;
        }

        .verified-badge-icon {
            color: #2563eb;
            flex-shrink: 0;
        }

        .artist-headline-text {
            font-size: 0.88rem;
            color: var(--muted);
            line-height: 1.5;
            margin-bottom: 0.85rem;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            min-height: 2.6rem;
            font-weight: 400;
        }

        .specialty-tags-row {
            display: flex;
            flex-wrap: wrap;
            gap: 0.35rem;
            margin-bottom: 1.15rem;
        }

        .specialty-pill {
            font-size: 0.72rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
            padding: 0.25rem 0.6rem;
            border-radius: 6px;
            background: var(--surface-elevated);
            color: var(--muted);
            border: 1px solid var(--border-subtle);
        }

        .card-footer-action {
            margin-top: auto;
            padding-top: 1rem;
            border-top: 1px solid var(--border-subtle);
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .action-link-label {
            font-size: 0.88rem;
            font-weight: 700;
            color: var(--accent);
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            transition: gap var(--transition-fast);
        }

        .photographer-editorial-card:hover .action-link-label {
            gap: 0.65rem;
            color: var(--accent-hover);
        }

        .btn-quick-book {
            padding: 0.4rem 0.95rem;
            font-size: 0.82rem;
            font-weight: 700;
            border-radius: 8px;
            background: var(--accent-subtle);
            color: var(--accent);
            border: 1px solid transparent;
            transition: all var(--transition-fast);
        }

        .photographer-editorial-card:hover .btn-quick-book {
            background: var(--accent);
            color: #ffffff;
        }

        .empty-gallery-state {
            background: var(--surface);
            border: 1px dashed var(--border);
            border-radius: 20px;
            padding: 4rem 2rem;
            text-align: center;
            max-width: 600px;
            margin: 2rem auto;
        }

        .empty-icon-circle {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            background: var(--surface-elevated);
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: var(--muted);
            margin-bottom: 1.25rem;
        }
    </style>
</head>

<main>
    <!-- ── Hero Editorial Header ─────────────────────────────────── -->
    <section class="marketplace-hero-section">
        <div class="editorial-container">
            <div class="row align-items-center justify-content-between g-4">
                <div class="col-12 col-lg-8">
                    <h1 class="hero-editorial-title">
                        Discover Exceptional Photographers
                    </h1>
                    <p class="hero-editorial-desc">
                        Connect directly with Vietnam&rsquo;s most gifted professional photographers. Browse verified portfolios, compare aesthetic styles, explore transparent rates, and book shoots securely.
                    </p>

                    <!-- View Mode Switcher -->
                    <div class="mode-switcher-container">
                        <a href="${pageContext.request.contextPath}/photographers" class="mode-switch-btn active">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                            Photographer Roster
                        </a>
                        <a href="${pageContext.request.contextPath}/feed" class="mode-switch-btn">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                            Visual Feed
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ── Main Discovery Content ────────────────────────────────── -->
    <div class="editorial-container" style="padding-bottom: 5rem;">

        <!-- Floating Unified Search Card -->
        <div class="search-hero-card">
            <form action="${pageContext.request.contextPath}/photographers" method="get" id="search-form">
                <!-- Primary Filter Row -->
                <div class="row g-3 align-items-end mb-3">
                    <!-- Keyword Search -->
                    <div class="col-12 col-md-3">
                        <div class="search-input-unit">
                            <label for="keyword" class="form-label">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                                Search Photographer
                            </label>
                            <input type="text"
                                   id="keyword"
                                   name="keyword"
                                   maxlength="100"
                                   class="form-control search-input-control"
                                   placeholder="Name, style, bio..."
                                   value="<c:out value='${searchRequest.keyword}'/>">
                        </div>
                    </div>

                    <!-- City Selection -->
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="search-input-unit">
                            <label for="city" class="form-label">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                City / Region
                            </label>
                            <input type="text"
                                   id="city"
                                   name="city"
                                   list="vietnam-cities"
                                   maxlength="100"
                                   class="form-control search-input-control"
                                   placeholder="Hanoi, Ho Chi Minh City, Da Nang..."
                                   value="<c:out value='${searchRequest.city}'/>">
                            <datalist id="vietnam-cities">
                                <option value="Hanoi">
                                <option value="Ho Chi Minh City">
                                <option value="Da Nang">
                                <option value="Can Tho">
                                <option value="Hai Phong">
                                <option value="Nha Trang">
                                <option value="Da Lat">
                                <option value="Hue">
                                <option value="Vung Tau">
                                <option value="Hoi An">
                            </datalist>
                        </div>
                    </div>

                    <!-- Category / Specialization -->
                    <div class="col-6 col-sm-3 col-md-2">
                        <div class="search-input-unit">
                            <label for="category" class="form-label">Category</label>
                            <select id="category" name="category" class="form-control search-input-control">
                                <option value="" <c:if test="${empty searchRequest.category}">selected</c:if>>All Specialties</option>
                                <option value="PORTRAIT" <c:if test="${searchRequest.category == 'PORTRAIT'}">selected</c:if>>Portrait</option>
                                <option value="WEDDING" <c:if test="${searchRequest.category == 'WEDDING'}">selected</c:if>>Wedding</option>
                                <option value="FASHION" <c:if test="${searchRequest.category == 'FASHION'}">selected</c:if>>Fashion</option>
                                <option value="LIFESTYLE" <c:if test="${searchRequest.category == 'LIFESTYLE'}">selected</c:if>>Lifestyle</option>
                                <option value="EVENT" <c:if test="${searchRequest.category == 'EVENT'}">selected</c:if>>Event</option>
                                <option value="COMMERCIAL" <c:if test="${searchRequest.category == 'COMMERCIAL'}">selected</c:if>>Commercial</option>
                                <option value="PRODUCT" <c:if test="${searchRequest.category == 'PRODUCT'}">selected</c:if>>Product</option>
                                <option value="TRAVEL" <c:if test="${searchRequest.category == 'TRAVEL'}">selected</c:if>>Travel</option>
                                <option value="ARCHITECTURE" <c:if test="${searchRequest.category == 'ARCHITECTURE'}">selected</c:if>>Architecture</option>
                                <option value="DOCUMENTARY" <c:if test="${searchRequest.category == 'DOCUMENTARY'}">selected</c:if>>Documentary</option>
                            </select>
                        </div>
                    </div>

                    <!-- Min Rating Filter -->
                    <div class="col-6 col-sm-3 col-md-2">
                        <div class="search-input-unit">
                            <label for="minRating" class="form-label">Rating</label>
                            <select id="minRating" name="minRating" class="form-control search-input-control">
                                <option value="" <c:if test="${empty searchRequest.minRating}">selected</c:if>>Any Rating</option>
                                <option value="4.5" <c:if test="${searchRequest.minRating == 4.5}">selected</c:if>>4.5 &amp; Up ★</option>
                                <option value="4.0" <c:if test="${searchRequest.minRating == 4.0}">selected</c:if>>4.0 &amp; Up ★</option>
                                <option value="3.5" <c:if test="${searchRequest.minRating == 3.5}">selected</c:if>>3.5 &amp; Up ★</option>
                            </select>
                        </div>
                    </div>

                    <!-- Submit & Reset Action -->
                    <div class="col-12 col-md-2 d-flex gap-2">
                        <button type="submit" class="btn-search-submit w-100" id="btn-apply-filters" title="Search">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                            <span>Search</span>
                        </button>
                        <c:if test="${hasFilters}">
                            <a href="${pageContext.request.contextPath}/photographers" class="btn-search-reset" id="btn-clear-filters" title="Clear filters">
                                ✕
                            </a>
                        </c:if>
                    </div>
                </div>

                <!-- Secondary Filter & Sort Row -->
                <div class="row g-3 align-items-end">
                    <!-- Price Min -->
                    <div class="col-6 col-md-3">
                        <div class="search-input-unit">
                            <label for="minPrice" class="form-label">Price From (VND)</label>
                            <input type="number"
                                   id="minPrice"
                                   name="minPrice"
                                   min="0"
                                   step="100000"
                                   class="form-control search-input-control"
                                   placeholder="0"
                                   value="<c:out value='${searchRequest.minPrice}'/>">
                        </div>
                    </div>

                    <!-- Price Max -->
                    <div class="col-6 col-md-3">
                        <div class="search-input-unit">
                            <label for="maxPrice" class="form-label">Price To (VND)</label>
                            <input type="number"
                                   id="maxPrice"
                                   name="maxPrice"
                                   min="0"
                                   step="100000"
                                   class="form-control search-input-control"
                                   placeholder="Any max"
                                   value="<c:out value='${searchRequest.maxPrice}'/>">
                        </div>
                    </div>

                    <!-- Shoot Date Availability -->
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="search-input-unit">
                            <label for="availableDate" class="form-label">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                Check Availability Date
                            </label>
                            <input type="date"
                                   id="availableDate"
                                   name="availableDate"
                                   class="form-control search-input-control"
                                   value="<c:out value='${searchRequest.availableDate}'/>">
                        </div>
                    </div>

                    <!-- Sort By -->
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="search-input-unit">
                            <label for="sortBy" class="form-label">Sort Results By</label>
                            <select id="sortBy" name="sortBy" class="form-control search-input-control" onchange="this.form.submit()">
                                <option value="RECOMMENDED" <c:if test="${empty searchRequest.sortBy or searchRequest.sortBy == 'RECOMMENDED'}">selected</c:if>>Top Recommended (AI Score)</option>
                                <option value="RATING_DESC" <c:if test="${searchRequest.sortBy == 'RATING_DESC'}">selected</c:if>>Highest Customer Rating</option>
                                <option value="PRICE_ASC" <c:if test="${searchRequest.sortBy == 'PRICE_ASC'}">selected</c:if>>Price: Low to High</option>
                                <option value="PRICE_DESC" <c:if test="${searchRequest.sortBy == 'PRICE_DESC'}">selected</c:if>>Price: High to Low</option>
                                <option value="REVIEWS_DESC" <c:if test="${searchRequest.sortBy == 'REVIEWS_DESC'}">selected</c:if>>Most Reviewed</option>
                                <option value="COMPLETED_DESC" <c:if test="${searchRequest.sortBy == 'COMPLETED_DESC'}">selected</c:if>>Most Booked Shoots</option>
                                <option value="NEWEST" <c:if test="${searchRequest.sortBy == 'NEWEST'}">selected</c:if>>Newest Photographers</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- Quick Genre Filter Chips -->
                <div class="quick-tags-scroller mt-3">
                    <span class="quick-tag-label">Specializations:</span>
                    <a href="${pageContext.request.contextPath}/photographers" class="quick-filter-chip <c:if test='${empty searchRequest.category and empty searchRequest.keyword}'>active</c:if>">All Styles</a>
                    <a href="${pageContext.request.contextPath}/photographers?category=PORTRAIT" class="quick-filter-chip <c:if test='${searchRequest.category == \"PORTRAIT\"}'>active</c:if>">Portrait</a>
                    <a href="${pageContext.request.contextPath}/photographers?category=WEDDING" class="quick-filter-chip <c:if test='${searchRequest.category == \"WEDDING\"}'>active</c:if>">Wedding &amp; Couples</a>
                    <a href="${pageContext.request.contextPath}/photographers?category=FASHION" class="quick-filter-chip <c:if test='${searchRequest.category == \"FASHION\"}'>active</c:if>">Fashion &amp; Editorial</a>
                    <a href="${pageContext.request.contextPath}/photographers?category=LIFESTYLE" class="quick-filter-chip <c:if test='${searchRequest.category == \"LIFESTYLE\"}'>active</c:if>">Lifestyle</a>
                    <a href="${pageContext.request.contextPath}/photographers?category=EVENT" class="quick-filter-chip <c:if test='${searchRequest.category == \"EVENT\"}'>active</c:if>">Events</a>
                    <a href="${pageContext.request.contextPath}/photographers?category=COMMERCIAL" class="quick-filter-chip <c:if test='${searchRequest.category == \"COMMERCIAL\"}'>active</c:if>">Commercial &amp; Product</a>
                </div>

                <!-- Quick Budget Chips -->
                <div class="quick-tags-scroller mt-2">
                    <span class="quick-tag-label">Budget:</span>
                    <a href="${pageContext.request.contextPath}/photographers?maxPrice=2000000" class="quick-filter-chip <c:if test='${searchRequest.maxPrice == 2000000 and empty searchRequest.minPrice}'>active</c:if>">&lt; 2M VND</a>
                    <a href="${pageContext.request.contextPath}/photographers?minPrice=2000000&maxPrice=5000000" class="quick-filter-chip <c:if test='${searchRequest.minPrice == 2000000 and searchRequest.maxPrice == 5000000}'>active</c:if>">2M - 5M VND</a>
                    <a href="${pageContext.request.contextPath}/photographers?minPrice=5000000&maxPrice=10000000" class="quick-filter-chip <c:if test='${searchRequest.minPrice == 5000000 and searchRequest.maxPrice == 10000000}'>active</c:if>">5M - 10M VND</a>
                    <a href="${pageContext.request.contextPath}/photographers?minPrice=10000000" class="quick-filter-chip <c:if test='${searchRequest.minPrice == 10000000 and empty searchRequest.maxPrice}'>active</c:if>">&gt; 10M VND</a>
                </div>
            </form>
        </div>

        <!-- Error Banner -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger rounded-3 mb-4" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <!-- Results Meta Bar -->
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-4">
            <div style="font-size: 0.95rem; color: var(--muted);">
                Showing <strong style="color: var(--text);"><c:out value="${resultCount != null ? resultCount : fn:length(photographers)}"/></strong>
                verified professional photographer<c:if test="${(resultCount != null ? resultCount : fn:length(photographers)) != 1}">s</c:if>
                <c:if test="${not empty searchRequest.category}">
                    in <strong style="color: var(--accent);">&ldquo;<c:out value='${searchRequest.category}'/>&rdquo;</strong>
                </c:if>
                <c:if test="${not empty searchRequest.city}">
                    in <strong style="color: var(--accent);">&ldquo;<c:out value='${searchRequest.city}'/>&rdquo;</strong>
                </c:if>
                <c:if test="${not empty searchRequest.keyword}">
                    matching <strong style="color: var(--accent);">&ldquo;<c:out value='${searchRequest.keyword}'/>&rdquo;</strong>
                </c:if>
                <c:if test="${not empty searchRequest.availableDate}">
                    available on <strong style="color: #34d399;">${searchRequest.availableDate}</strong>
                </c:if>
            </div>

            <div style="font-size: 0.85rem; color: var(--muted);">
                Sort by: <strong style="color: var(--text);">
                    <c:choose>
                        <c:when test="${searchRequest.sortBy == 'RATING_DESC'}">Highest Rated</c:when>
                        <c:when test="${searchRequest.sortBy == 'PRICE_ASC'}">Price: Low to High</c:when>
                        <c:when test="${searchRequest.sortBy == 'PRICE_DESC'}">Price: High to Low</c:when>
                        <c:when test="${searchRequest.sortBy == 'REVIEWS_DESC'}">Most Reviewed</c:when>
                        <c:when test="${searchRequest.sortBy == 'COMPLETED_DESC'}">Most Booked</c:when>
                        <c:when test="${searchRequest.sortBy == 'NEWEST'}">Newest First</c:when>
                        <c:otherwise>Top Recommended</c:otherwise>
                    </c:choose>
                </strong>
            </div>
        </div>

        <!-- ── Editorial Photographer Cards Grid ─────────────────────── -->
        <c:choose>
            <c:when test="${empty photographers}">
                <div class="empty-gallery-state">
                    <div class="empty-icon-circle">
                        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                    </div>
                    <h3 style="font-size: 1.6rem; font-weight: 800; margin-bottom: 0.5rem; color: var(--text);">
                        No Photographers Found
                    </h3>
                    <p style="color: var(--muted); font-size: 0.95rem; max-width: 440px; margin: 0 auto 1.5rem;">
                        Try broadening your budget range or resetting your keywords to discover more artists.
                    </p>
                    <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary" style="padding: 0.6rem 1.75rem; font-weight: 700; border-radius: 50px;">
                        Reset All Filters
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="photographer-grid">
                    <c:forEach var="p" items="${photographers}">
                        <a href="${pageContext.request.contextPath}/photographers/${p.id}" class="photographer-editorial-card" id="view-profile-${p.id}">
                            <!-- Photo Cover Frame -->
                            <div class="card-cover-wrapper">
                                <c:choose>
                                    <c:when test="${not empty p.coverImageUrl}">
                                        <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio" loading="lazy">
                                    </c:when>
                                    <c:otherwise>
                                        <div style="width: 100%; height: 100%; background: linear-gradient(135deg, #1e293b, #0f172a); display: flex; align-items: center; justify-content: center; color: #94a3b8; font-weight: 700; font-size: 2rem;">
                                            <c:out value="${fn:toUpperCase(fn:substring(p.displayName, 0, 1))}"/>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="card-cover-gradient"></div>

                                <!-- Top Badges -->
                                <div class="card-photo-top-bar">
                                    <div class="badge-frosted-glass">
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                        <c:choose>
                                            <c:when test="${not empty p.city}"><c:out value="${p.city}"/></c:when>
                                            <c:otherwise>Nationwide</c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="d-flex gap-1 align-items-center">
                                        <c:if test="${not empty p.recommendationScore and p.recommendationScore > 0}">
                                            <div class="badge-frosted-glass" style="background: rgba(37, 99, 235, 0.9); border: none;" title="Rule-based Recommendation Match">
                                                ★ <c:out value="${p.recommendationPercent}"/>% Match
                                            </div>
                                        </c:if>
                                        <div class="badge-frosted-glass rating">
                                            <svg width="12" height="12" viewBox="0 0 24 24" fill="#ffffff" stroke="none"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty p.averageRating and p.averageRating > 0 and not p.newTalent}">
                                                        <fmt:formatNumber value="${p.averageRating}" maxFractionDigits="1" minFractionDigits="1"/>
                                                        <c:if test="${not empty p.reviewCount and p.reviewCount > 0}">
                                                             (${p.reviewCount})
                                                        </c:if>
                                                    </c:when>
                                                    <c:otherwise>New Creator</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <!-- Bottom Price Tag -->
                                <div class="card-photo-bottom-bar">
                                    <div class="badge-price-tag">
                                        <span style="font-size: 0.7rem; opacity: 0.85; font-weight: 700; display: block; text-transform: uppercase; letter-spacing: 0.05em;">Starting From</span>
                                        <c:choose>
                                            <c:when test="${not empty p.priceFrom}">
                                                <fmt:formatNumber value="${p.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                            </c:when>
                                            <c:otherwise>Negotiable</c:otherwise>
                                        </c:choose>
                                    </div>

                                    <c:if test="${not empty savedPhotographerIds and savedPhotographerIds.contains(p.id)}">
                                        <div class="badge-frosted-glass" style="background: rgba(239, 68, 68, 0.9); border: none;">
                                            ♥ Saved
                                        </div>
                                    </c:if>
                                </div>
                            </div>

                            <!-- Card Details Body -->
                            <div class="card-details-body">
                                <div class="artist-title-row">
                                    <h2 class="artist-name">
                                        <c:out value="${p.displayName}"/>
                                        <svg class="verified-badge-icon" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                                        </svg>
                                    </h2>
                                </div>

                                <div class="d-flex align-items-center gap-2 flex-wrap mb-2" style="font-size: 0.76rem;">
                                    <c:if test="${not empty p.completedBookingsCount and p.completedBookingsCount > 0}">
                                        <span style="color: #34d399; font-weight: 600; display: inline-flex; align-items: center; gap: 0.25rem;">
                                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                            <c:out value="${p.completedBookingsCount}"/> bookings fulfilled
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty searchRequest.availableDate}">
                                        <c:choose>
                                            <c:when test="${p.availableOnRequestedDate}">
                                                <span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #10b981; font-weight: 600; font-size: 0.72rem; padding: 0.2rem 0.5rem; border-radius: 4px;">
                                                    Available on ${searchRequest.availableDate}
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444; font-weight: 600; font-size: 0.72rem; padding: 0.2rem 0.5rem; border-radius: 4px;">
                                                    Busy on ${searchRequest.availableDate}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </div>

                                <div class="artist-headline-text">
                                    <c:choose>
                                        <c:when test="${not empty p.headline}">
                                            <c:out value="${p.headline}"/>
                                        </c:when>
                                        <c:otherwise>
                                            Verified professional photographer based in <c:out value="${not empty p.city ? p.city : 'Vietnam'}"/>. Passionate about authentic visual storytelling.
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Specialty Pills -->
                                <div class="specialty-tags-row">
                                    <c:choose>
                                        <c:when test="${not empty p.specialtyDisplayNames}">
                                            <c:forEach var="spec" items="${p.specialtyDisplayNames}" end="2">
                                                <span class="specialty-pill"><c:out value="${spec}"/></span>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="specialty-pill">Portrait</span>
                                            <span class="specialty-pill">Fine Art</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Card Action Footer -->
                                <div class="card-footer-action">
                                    <span class="action-link-label">
                                        View Portfolio
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                                    </span>
                                    <span class="btn-quick-book">Book Now</span>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </div>

                <!-- Pagination Controls -->
                <c:if test="${totalPages > 1}">
                    <nav aria-label="Photographer results pages" class="pc-pagination mt-5 d-flex justify-content-center align-items-center gap-3">
                        <c:if test="${hasPreviousPage}">
                            <c:url var="previousPageUrl" value="/photographers">
                                <c:param name="keyword" value="${searchRequest.keyword}" />
                                <c:param name="city" value="${searchRequest.city}" />
                                <c:param name="minPrice" value="${searchRequest.minPrice}" />
                                <c:param name="maxPrice" value="${searchRequest.maxPrice}" />
                                <c:param name="minExperience" value="${searchRequest.minExperience}" />
                                <c:param name="page" value="${currentPage - 1}" />
                            </c:url>
                            <a class="btn btn-secondary rounded-pill px-4" href="${previousPageUrl}" rel="prev">&larr; Previous</a>
                        </c:if>
                        <span style="color:var(--muted); font-size: 0.92rem; font-weight: 700;">
                            Page <strong style="color:var(--text);">${currentPage + 1}</strong> of ${totalPages}
                        </span>
                        <c:if test="${hasNextPage}">
                            <c:url var="nextPageUrl" value="/photographers">
                                <c:param name="keyword" value="${searchRequest.keyword}" />
                                <c:param name="city" value="${searchRequest.city}" />
                                <c:param name="minPrice" value="${searchRequest.minPrice}" />
                                <c:param name="maxPrice" value="${searchRequest.maxPrice}" />
                                <c:param name="minExperience" value="${searchRequest.minExperience}" />
                                <c:param name="page" value="${currentPage + 1}" />
                            </c:url>
                            <a class="btn btn-secondary rounded-pill px-4" href="${nextPageUrl}" rel="next">Next &rarr;</a>
                        </c:if>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</main>
