<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Discover Photographers — PhotoConnect</title>
    <meta name="description" content="Discover and connect with top-tier verified professional photographers on PhotoConnect. Search by location, style, price, and experience.">

    <style>
        .marketplace-header-region {
            padding: clamp(3rem, 5.5vw, 4.5rem) 0 1.5rem;
        }

        .search-toolbar-shell {
            background: var(--surface);
            border: 1px solid var(--border);
            padding: 1.5rem 1.75rem;
            border-radius: var(--radius-sm);
            margin-bottom: 2.5rem;
            box-shadow: var(--shadow-subtle);
        }

        .search-toolbar-grid {
            display: grid;
            grid-template-columns: minmax(180px, 1.3fr) minmax(140px, 1fr) minmax(120px, 0.9fr) minmax(120px, 0.9fr) auto;
            gap: 1.25rem;
            align-items: flex-end;
        }

        .search-field-group {
            display: flex;
            flex-direction: column;
            gap: 0.4rem;
        }

        .search-field-group label {
            font-size: 0.74rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--muted);
        }

        .search-field-input {
            width: 100%;
            height: 44px;
            padding: 0 0.85rem;
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            background: var(--surface-raised);
            color: var(--text);
            font-family: var(--font-primary);
            font-size: 0.9rem;
            transition: border-color var(--transition-fast);
        }

        .search-field-input:focus {
            outline: none;
            border-color: var(--text);
            box-shadow: 0 0 0 1px var(--text);
        }

        .artist-card-spec {
            font-size: 0.82rem;
            color: var(--muted);
            margin-top: 0.25rem;
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.5rem;
        }

        .artist-rate-label {
            font-family: var(--font-primary);
            font-size: 0.92rem;
            font-weight: 600;
            color: var(--text);
            margin-top: 0.35rem;
        }

        .artist-rating-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
            font-size: 0.82rem;
            font-weight: 600;
            color: var(--warning);
        }

        @media (max-width: 991px) {
            .search-toolbar-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
            .search-toolbar-grid .toolbar-actions-cell {
                grid-column: span 2;
                display: flex;
                gap: 1rem;
                justify-content: flex-start;
                margin-top: 0.5rem;
            }
        }

        @media (max-width: 640px) {
            .search-toolbar-grid {
                grid-template-columns: 1fr;
            }
            .search-toolbar-grid .toolbar-actions-cell {
                grid-column: span 1;
                flex-direction: column;
            }
            .search-toolbar-grid .toolbar-actions-cell button,
            .search-toolbar-grid .toolbar-actions-cell a {
                width: 100%;
            }
        }
    </style>
</head>

    <main>
        <!-- ── Marketplace Editorial Header ──────────────────────────── -->
        <section class="marketplace-header-region">
            <div class="editorial-container">
                <span class="section-index">Verified Creator Roster</span>
                <h1 class="editorial-title" style="font-size: clamp(2.5rem, 5vw, 4.25rem); margin-bottom: 0.75rem;">
                    Discover Photographers
                </h1>
                <p class="pc-lead">
                    Explore our curated community of visual artists specializing in editorial portraits, intimate wedding documentation, and commercial brand narratives.
                </p>
            </div>
        </section>

        <!-- ── Main Discovery Content ────────────────────────────────── -->
        <div class="editorial-container" style="padding-bottom: 5rem;">

            <!-- Deliberate Search & Filter Toolbar -->
            <div class="search-toolbar-shell">
                <form action="${pageContext.request.contextPath}/photographers" method="get" id="search-form">
                    <div class="search-toolbar-grid row g-3 align-items-end">
                        <div class="search-field-group col-12 col-md-3">
                            <label for="keyword" class="form-label">Search Artist</label>
                            <input type="text"
                                   id="keyword"
                                   name="keyword"
                                   maxlength="100"
                                   class="search-field-input form-control"
                                   placeholder="Name, style, or discipline…"
                                   value="<c:out value='${searchRequest.keyword}'/>">
                        </div>
                        <div class="search-field-group col-12 col-md-2">
                            <label for="city" class="form-label">City</label>
                            <input type="text"
                                   id="city"
                                   name="city"
                                   maxlength="100"
                                   class="search-field-input form-control"
                                   placeholder="e.g. Hanoi, Saigon…"
                                   value="<c:out value='${searchRequest.city}'/>">
                        </div>
                        <div class="search-field-group col-12 col-md-2">
                            <label for="minPrice" class="form-label">Min Rate (VND)</label>
                            <input type="number"
                                   id="minPrice"
                                   name="minPrice"
                                   min="0"
                                   step="100000"
                                   class="search-field-input form-control"
                                   placeholder="0"
                                   value="<c:out value='${searchRequest.minPrice}'/>">
                        </div>
                        <div class="search-field-group col-12 col-md-2">
                            <label for="maxPrice" class="form-label">Max Rate (VND)</label>
                            <input type="number"
                                   id="maxPrice"
                                   name="maxPrice"
                                   min="0"
                                   step="100000"
                                   class="search-field-input form-control"
                                   placeholder="Any"
                                   value="<c:out value='${searchRequest.maxPrice}'/>">
                        </div>
                        <div class="toolbar-actions-cell pc-actions col-12 col-md-auto" style="margin-bottom: 2px;">
                            <button type="submit" class="btn btn-primary" id="btn-apply-filters">
                                Apply Filters
                            </button>
                            <c:if test="${hasFilters}">
                                <a href="${pageContext.request.contextPath}/photographers" class="btn btn-secondary" id="btn-clear-filters">
                                    Clear
                                </a>
                            </c:if>
                        </div>
                    </div>
                </form>
            </div>

            <!-- Error Banner -->
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger alert alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- Results Meta Header -->
            <div class="results-meta">
                <div>
                    Showing <strong><c:out value="${resultCount != null ? resultCount : fn:length(photographers)}"/></strong>
                    verified photographer<c:if test="${(resultCount != null ? resultCount : fn:length(photographers)) != 1}">s</c:if>
                    <c:if test="${not empty searchRequest.city}">
                        in <strong>&ldquo;<c:out value='${searchRequest.city}'/>&rdquo;</strong>
                    </c:if>
                    <c:if test="${not empty searchRequest.keyword}">
                        matching <strong>&ldquo;<c:out value='${searchRequest.keyword}'/>&rdquo;</strong>
                    </c:if>
                </div>
            </div>

            <!-- ── Photography-First Artist Cards Grid ─────────────────── -->
            <c:choose>
                <c:when test="${empty photographers}">
                    <div class="pc-empty-state" style="max-width: 580px; margin-inline: auto;">
                        <h3 class="editorial-heading" style="font-size: 1.6rem; margin-bottom: 0.85rem;">No Photographers Found</h3>
                        <p>
                            <c:choose>
                                <c:when test="${hasFilters}">
                                    We couldn&rsquo;t find any verified creators matching your current filter criteria. Try expanding your price range or resetting keywords.
                                </c:when>
                                <c:otherwise>
                                    Our verified photographer roster is currently undergoing curation. Check back soon for newly approved artists.
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <c:if test="${hasFilters}">
                            <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary" style="margin-top: 1rem;">
                                Reset All Filters
                            </a>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="photographer-grid">
                        <c:forEach var="p" items="${photographers}">
                            <a href="${pageContext.request.contextPath}/photographers/${p.id}" class="photographer-card" id="view-profile-${p.id}">
                                <div class="photo-frame">
                                    <c:choose>
                                        <c:when test="${not empty p.coverImageUrl}">
                                            <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio photograph" loading="lazy">
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
                                        <div class="d-flex align-items-center justify-content-between gap-2">
                                            <h2 class="card-name" style="font-size: 1.3rem; margin: 0;"><c:out value="${p.displayName}"/></h2>
                                            <c:if test="${not empty savedPhotographerIds && savedPhotographerIds.contains(p.id)}">
                                                <span class="badge bg-secondary" style="font-size: 0.7rem; padding: 0.2rem 0.45rem; font-weight: 500;" title="Saved in your favorites">Saved</span>
                                            </c:if>
                                        </div>
                                        <c:if test="${not empty p.headline}">
                                            <div style="font-size: 0.84rem; color: var(--muted); margin-bottom: 0.25rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                <c:out value="${p.headline}"/>
                                            </div>
                                        </c:if>
                                        <div class="artist-card-spec">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty p.city}"><c:out value="${p.city}"/><c:if test="${not empty p.country}">, <c:out value="${p.country}"/></c:if></c:when>
                                                    <c:otherwise>Vietnam</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <c:if test="${not empty p.experienceYears}">
                                                <span>&middot;</span>
                                                <span><c:out value="${p.experienceYears}"/> yrs exp</span>
                                            </c:if>
                                            <c:if test="${not empty p.averageRating && p.averageRating > 0}">
                                                <span>&middot;</span>
                                                <span class="artist-rating-badge">
                                                    ★ <fmt:formatNumber value="${p.averageRating}" maxFractionDigits="1" minFractionDigits="1"/>
                                                </span>
                                            </c:if>
                                        </div>
                                        <c:if test="${not empty p.specialtyDisplayNames}">
                                            <div class="d-flex flex-wrap gap-1 my-1">
                                                <c:forEach var="spec" items="${p.specialtyDisplayNames}" end="2">
                                                    <span class="badge bg-light text-dark border" style="font-size: 0.72rem; padding: 0.2rem 0.45rem;"><c:out value="${spec}"/></span>
                                                </c:forEach>
                                            </div>
                                        </c:if>
                                        <div class="artist-rate-label">
                                            <c:choose>
                                                <c:when test="${not empty p.priceFrom}">
                                                    From <fmt:formatNumber value="${p.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                                </c:when>
                                                <c:otherwise>Rates on agreement</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="card-arrow">&rarr;</div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>

                    <!-- Pagination Controls -->
                    <c:if test="${totalPages > 1}">
                        <nav aria-label="Photographer results pages" class="pc-pagination">
                            <c:if test="${hasPreviousPage}">
                                <c:url var="previousPageUrl" value="/photographers">
                                    <c:param name="keyword" value="${searchRequest.keyword}" />
                                    <c:param name="city" value="${searchRequest.city}" />
                                    <c:param name="minPrice" value="${searchRequest.minPrice}" />
                                    <c:param name="maxPrice" value="${searchRequest.maxPrice}" />
                                    <c:param name="minExperience" value="${searchRequest.minExperience}" />
                                    <c:param name="page" value="${currentPage - 1}" />
                                </c:url>
                                <a class="btn btn-secondary" href="${previousPageUrl}" rel="prev">&larr; Previous</a>
                            </c:if>
                            <span style="color:var(--muted); font-size: 0.9rem;">
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
                                <a class="btn btn-secondary" href="${nextPageUrl}" rel="next">Next &rarr;</a>
                            </c:if>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>

        </div>
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
                    <a href="${pageContext.request.contextPath}/" class="text-link">Home</a>
                    <a href="${pageContext.request.contextPath}/become-photographer" class="text-link">Join Roster</a>
                </div>
            </div>
        </div>
    </footer>
