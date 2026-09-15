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
    <meta name="description" content="Discover and connect with top-tier verified professional photographers on PhotoConnect. Search by location, style, price, and experience.">
    <title>Discover Photographers – PhotoConnect</title>
    
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main>
        <!-- ── Editorial Hero ────────────────────────────────────────── -->
        <section style="padding: 6rem 0 2rem 0;">
            <div class="editorial-container">
                <h1 class="hero-display" style="margin-bottom: 1rem; font-size: clamp(3rem, 6vw, 6rem);">Discover<br>Photographers</h1>
                <p style="font-size: clamp(1rem, 2vw, 1.25rem); color: var(--text-muted); max-width: 600px; font-weight: 300;">
                    Explore a curated selection of professional visual artists specializing in editorial, portrait, and commercial storytelling.
                </p>
            </div>
        </section>

        <!-- ── Filter & Main Content ─────────────────────────────────── -->
        <div class="editorial-container pb-5">
            
            <!-- Search & Filter Panel -->
            <div class="filter-panel">
                <form action="${pageContext.request.contextPath}/photographers" method="get" id="search-form">
                    <div class="filter-grid">
                        <div class="form-group" style="margin-bottom:0;">
                            <label for="keyword" class="form-label">Keyword</label>
                            <input type="text" id="keyword" name="keyword" maxlength="100" class="form-input" placeholder="Name, specialty…" value="<c:out value='${searchRequest.keyword}'/>">
                        </div>
                        <div class="form-group" style="margin-bottom:0;">
                            <label for="city" class="form-label">City</label>
                            <input type="text" id="city" name="city" maxlength="100" class="form-input" placeholder="e.g. Hanoi…" value="<c:out value='${searchRequest.city}'/>">
                        </div>
                        <div class="form-group" style="margin-bottom:0;">
                            <label for="minPrice" class="form-label">Min Price (VND)</label>
                            <input type="number" id="minPrice" name="minPrice" min="0" step="100000" class="form-input" placeholder="0" value="<c:out value='${searchRequest.minPrice}'/>">
                        </div>
                        <div class="form-group" style="margin-bottom:0;">
                            <label for="maxPrice" class="form-label">Max Price (VND)</label>
                            <input type="number" id="maxPrice" name="maxPrice" min="0" step="100000" class="form-input" placeholder="Any" value="<c:out value='${searchRequest.maxPrice}'/>">
                        </div>
                        <div style="display: flex; gap: 0.5rem; align-items: flex-end;">
                            <button type="submit" class="filter-btn" style="flex:1;" id="btn-apply-filters">Filter</button>
                            <c:if test="${hasFilters}">
                                <a href="${pageContext.request.contextPath}/photographers" class="filter-btn-outline" id="btn-clear-filters">Clear</a>
                            </c:if>
                        </div>
                    </div>
                </form>
            </div>

            <c:if test="${not empty errorMessage}">
                <div style="padding: 1.5rem; background: rgba(220,53,69,0.1); border-left: 2px solid #dc3545; margin-bottom: 2rem;">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <div class="results-meta">
                <div>
                    Showing <strong><c:out value="${resultCount != null ? resultCount : fn:length(photographers)}"/></strong> 
                    verified artist<c:if test="${(resultCount != null ? resultCount : fn:length(photographers)) != 1}">s</c:if>
                    <c:if test="${not empty searchRequest.city}">
                        in <span style="color:var(--text-on-dark);">"<c:out value='${searchRequest.city}'/>"</span>
                    </c:if>
                    <c:if test="${not empty searchRequest.keyword}">
                        matching <span style="color:var(--text-on-dark);">"<c:out value='${searchRequest.keyword}'/>"</span>
                    </c:if>
                </div>
            </div>

            <!-- ── Photographer Grid ─────────────────────────────────── -->
            <c:choose>
                <c:when test="${empty photographers}">
                    <div style="padding: 6rem 0; text-align: center; max-width: 500px; margin: 0 auto;">
                        <h3 style="font-size: 1.5rem; margin-bottom: 1rem;">No Photographers Found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 2rem;">
                            <c:choose>
                                <c:when test="${hasFilters}">
                                    We couldn't find any verified photographers matching your current filter criteria. Try adjusting your price range or clearing keywords.
                                </c:when>
                                <c:otherwise>
                                    Our verified photographer roster is currently being curated. Check back soon for newly approved artists.
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <c:if test="${hasFilters}">
                            <a href="${pageContext.request.contextPath}/photographers" class="primary-link">Reset Filters</a>
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
                                            <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 4rem; font-weight: 300; color: var(--border-dark);">
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
                                            •
                                            <c:choose>
                                                <c:when test="${not empty p.priceFrom}">
                                                    From <fmt:formatNumber value="${p.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                                </c:when>
                                                <c:otherwise>Contact for rates</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="card-arrow" style="font-size: 1.25rem;">→</div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <nav aria-label="Photographer results pages" style="display:flex; justify-content:center; align-items:center; gap:1rem; margin-top:3rem;">
                            <c:if test="${hasPreviousPage}">
                                <c:url var="previousPageUrl" value="/photographers">
                                    <c:param name="keyword" value="${searchRequest.keyword}" />
                                    <c:param name="city" value="${searchRequest.city}" />
                                    <c:param name="minPrice" value="${searchRequest.minPrice}" />
                                    <c:param name="maxPrice" value="${searchRequest.maxPrice}" />
                                    <c:param name="minExperience" value="${searchRequest.minExperience}" />
                                    <c:param name="page" value="${currentPage - 1}" />
                                </c:url>
                                <a class="filter-btn-outline" href="${previousPageUrl}" rel="prev">Previous</a>
                            </c:if>
                            <span style="color:var(--text-muted);">Page <strong style="color:var(--text-on-dark);">${currentPage + 1}</strong> of ${totalPages}</span>
                            <c:if test="${hasNextPage}">
                                <c:url var="nextPageUrl" value="/photographers">
                                    <c:param name="keyword" value="${searchRequest.keyword}" />
                                    <c:param name="city" value="${searchRequest.city}" />
                                    <c:param name="minPrice" value="${searchRequest.minPrice}" />
                                    <c:param name="maxPrice" value="${searchRequest.maxPrice}" />
                                    <c:param name="minExperience" value="${searchRequest.minExperience}" />
                                    <c:param name="page" value="${currentPage + 1}" />
                                </c:url>
                                <a class="filter-btn-outline" href="${nextPageUrl}" rel="next">Next</a>
                            </c:if>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>

        </div>
    </main>

    <footer style="border-top: 1px solid var(--border-dark); padding: 3rem 0; margin-top: auto; text-align: center; font-size: 0.85rem; color: var(--text-muted);">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>

</body>
</html>
