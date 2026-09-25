<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Saved Photographers — PhotoConnect</title>
    <meta name="description" content="View and manage your saved and favorited professional photographers on PhotoConnect.">

    <style>
        .saved-header-region {
            padding: clamp(2.5rem, 5vw, 4rem) 0 1.5rem;
            border-bottom: 1px solid var(--border);
            margin-bottom: 2.5rem;
        }

        .saved-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 2rem;
            margin-bottom: 4rem;
        }

        .saved-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            transition: transform var(--transition-fast), box-shadow var(--transition-fast);
        }

        .saved-card:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-subtle);
        }

        .saved-photo-frame {
            position: relative;
            aspect-ratio: 16 / 10;
            background: var(--surface-raised);
            overflow: hidden;
        }

        .saved-photo-frame img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .saved-card-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: var(--surface-raised);
            color: var(--muted);
        }

        .saved-card-body {
            padding: 1.5rem;
            display: flex;
            flex-direction: column;
            flex-grow: 1;
        }

        .saved-card-name {
            font-family: var(--font-editorial);
            font-size: 1.35rem;
            margin: 0 0 0.35rem 0;
            color: var(--text);
        }

        .saved-card-headline {
            font-size: 0.88rem;
            color: var(--muted);
            margin-bottom: 0.75rem;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .saved-card-meta {
            font-size: 0.82rem;
            color: var(--muted);
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.5rem;
            margin-bottom: 0.75rem;
        }

        .saved-card-rate {
            font-family: var(--font-primary);
            font-size: 0.95rem;
            font-weight: 600;
            color: var(--text);
            margin-bottom: 1.25rem;
        }

        .saved-card-actions {
            margin-top: auto;
            padding-top: 1rem;
            border-top: 1px solid var(--border);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 0.75rem;
        }

        @media (max-width: 640px) {
            .saved-grid {
                grid-template-columns: 1fr;
            }
            .saved-card-actions {
                flex-direction: column;
            }
            .saved-card-actions a,
            .saved-card-actions form,
            .saved-card-actions button {
                width: 100%;
                text-align: center;
            }
        }
    </style>
</head>

    <main>
        <!-- ── Header Region ─────────────────────────────────────────────── -->
        <section class="saved-header-region">
            <div class="editorial-container">
                <span class="section-index">Personal Collection</span>
                <div class="d-flex justify-content-between align-items-baseline flex-wrap gap-2">
                    <h1 class="editorial-title" style="font-size: clamp(2.25rem, 4.5vw, 3.5rem); margin: 0;">
                        Saved Photographers
                    </h1>
                    <c:if test="${savedCount > 0}">
                        <div style="font-size: 0.92rem; color: var(--muted);">
                            <strong><c:out value="${savedCount}"/></strong> saved creator<c:if test="${savedCount != 1}">s</c:if>
                        </div>
                    </c:if>
                </div>
                <p class="pc-lead" style="margin-top: 0.5rem; max-width: 640px;">
                    Photographers you have bookmarked for upcoming shoots and creative productions.
                </p>
            </div>
        </section>

        <!-- ── Main Content ──────────────────────────────────────────────── -->
        <div class="editorial-container" style="padding-bottom: 5rem;">

            <!-- Flash Alerts -->
            <c:if test="${not empty successMessage}">
                <div class="pc-alert pc-alert-success alert alert-success mb-4" role="alert">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger alert alert-danger mb-4" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${empty savedPhotographers}">
                    <!-- Deliberate Empty State -->
                    <div class="pc-empty-state" style="max-width: 580px; margin: 3rem auto; padding: 4rem 1.5rem; text-align: center;">
                        <h3 class="editorial-heading" style="font-size: 1.6rem; margin-bottom: 0.85rem;">
                            No saved photographers yet.
                        </h3>
                        <p style="color: var(--muted); margin-bottom: 1.75rem; line-height: 1.6;">
                            Explore photographers and save the ones you want to revisit.
                        </p>
                        <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary" id="btn-explore-photographers">
                            Explore Photographers
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Saved Photographers Grid -->
                    <div class="saved-grid">
                        <c:forEach var="item" items="${savedPhotographers}">
                            <c:set var="p" value="${item.photographer}" />
                            <div class="saved-card" id="saved-card-${p.id}">
                                <div class="saved-photo-frame">
                                    <c:choose>
                                        <c:when test="${not empty p.coverImageUrl}">
                                            <img src="<c:out value='${p.coverImageUrl}'/>" alt="<c:out value='${p.displayName}'/> portfolio photograph" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="saved-card-placeholder">
                                                <div style="font-family: var(--font-editorial); font-size: 2rem; font-weight: 600;">
                                                    <c:out value="${fn:toUpperCase(fn:substring(p.displayName, 0, 1))}"/>
                                                </div>
                                                <div style="font-size: 0.75rem; letter-spacing: 0.08em; text-transform: uppercase; margin-top: 0.35rem;">
                                                    Verified Creator
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="saved-card-body">
                                    <h2 class="saved-card-name">
                                        <c:out value="${p.displayName}"/>
                                    </h2>
                                    <c:if test="${not empty p.headline}">
                                        <div class="saved-card-headline">
                                            <c:out value="${p.headline}"/>
                                        </div>
                                    </c:if>
                                    <div class="saved-card-meta">
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
                                            <span style="color: var(--warning); font-weight: 600;">
                                                ★ <fmt:formatNumber value="${p.averageRating}" maxFractionDigits="1" minFractionDigits="1"/>
                                            </span>
                                        </c:if>
                                    </div>
                                    <c:if test="${not empty p.specialtyDisplayNames}">
                                        <div class="d-flex flex-wrap gap-1 mb-3">
                                            <c:forEach var="spec" items="${p.specialtyDisplayNames}" end="2">
                                                <span class="badge bg-light text-dark border" style="font-size: 0.72rem; padding: 0.2rem 0.45rem;"><c:out value="${spec}"/></span>
                                            </c:forEach>
                                        </div>
                                    </c:if>
                                    <div class="saved-card-rate">
                                        <c:choose>
                                            <c:when test="${not empty p.priceFrom}">
                                                From <fmt:formatNumber value="${p.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                            </c:when>
                                            <c:otherwise>Rates on agreement</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="saved-card-actions">
                                        <a href="${pageContext.request.contextPath}/photographers/${p.id}" class="btn btn-primary btn-sm" id="btn-view-profile-${p.id}">
                                            View Profile
                                        </a>
                                        <form action="${pageContext.request.contextPath}/photographers/${p.id}/unsave?redirect=/customer/saved-photographers" method="post" style="margin: 0;">
                                            <%@ include file="fragments/csrf-input.jsp" %>
                                            <button type="submit" class="btn btn-outline-secondary btn-sm" id="btn-remove-saved-${p.id}">
                                                Remove
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>
