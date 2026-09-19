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
    <title>My Portfolio — PhotoConnect Creator Studio</title>

    <!-- Google Fonts: Plus Jakarta Sans & Playfair Display -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">

    <style>
        .portfolio-header-strip {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            margin-bottom: 2.5rem;
            padding-bottom: 2rem;
            border-bottom: 1px solid var(--border);
            flex-wrap: wrap;
            gap: 1.5rem;
        }

        /* ── Expandable Upload Panel ────────────────────────────────────────── */
        .upload-expandable-panel {
            max-height: 0;
            overflow: hidden;
            opacity: 0;
            transition: max-height 450ms cubic-bezier(0.16, 1, 0.3, 1),
                        opacity 350ms ease,
                        margin-bottom 350ms ease;
            margin-bottom: 0;
        }

        .upload-expandable-panel.is-expanded {
            max-height: 600px;
            opacity: 1;
            margin-bottom: 3.5rem;
        }

        .upload-card-inner {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: clamp(1.75rem, 4vw, 2.5rem);
            box-shadow: var(--shadow-subtle);
        }

        /* ── Editorial Masonry Gallery ──────────────────────────────────────── */
        .portfolio-editorial-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 1.75rem;
            align-items: start;
        }

        .portfolio-card-frame {
            position: relative;
            background: var(--surface-subtle);
            overflow: hidden;
            aspect-ratio: 4/5;
            border-radius: var(--radius-sm);
            border: 1px solid var(--border);
            box-shadow: var(--shadow-subtle);
        }

        /* Dynamic aspect ratios for visual variety */
        .portfolio-card-frame:nth-child(5n+1) {
            aspect-ratio: 3/4;
        }

        .portfolio-card-frame:nth-child(5n+3) {
            aspect-ratio: 1/1;
        }

        .portfolio-card-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform var(--transition-smooth);
        }

        .portfolio-card-frame:hover .portfolio-card-img,
        .portfolio-card-frame:focus-within .portfolio-card-img {
            transform: scale(1.03);
        }

        .portfolio-hover-scrim {
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, rgba(7, 11, 18, 0.25) 0%, rgba(7, 11, 18, 0.88) 100%);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 1.5rem;
            opacity: 0;
            transition: opacity var(--transition-fast);
            z-index: 2;
        }

        .portfolio-card-frame:hover .portfolio-hover-scrim,
        .portfolio-card-frame:focus-within .portfolio-hover-scrim {
            opacity: 1;
        }

        .scrim-caption-text {
            color: #F8FAFC;
            font-size: 0.92rem;
            font-weight: 400;
            line-height: 1.4;
        }

        .scrim-top-actions {
            display: flex;
            justify-content: flex-end;
        }

        @media (hover: none), (max-width: 640px) {
            .portfolio-hover-scrim {
                opacity: 1;
                background: linear-gradient(180deg, transparent 40%, rgba(7, 11, 18, 0.92) 100%);
            }
            .scrim-top-actions {
                background: rgba(7, 11, 18, 0.7);
                border-radius: var(--radius-sm);
                padding: 0.35rem;
                align-self: flex-end;
            }
        }
    </style>
</head>
<body>

    <!-- Global Professional Navigation -->
    <jsp:include page="fragments/navbar.jsp" />

    <main class="pc-page">
        <div class="editorial-container pc-container-wide">

            <!-- Breadcrumbs / Back link -->
            <div style="margin-bottom: 1.5rem;">
                <a href="${pageContext.request.contextPath}/photographer/onboarding-status" class="text-link" style="font-size: 0.88rem;">
                    &larr; Application Status
                </a>
            </div>

            <!-- Page Header Strip with Add Photograph CTA -->
            <div class="portfolio-header-strip">
                <div>
                    <span class="section-index">Creator Studio</span>
                    <h1 class="editorial-title" style="margin: 0 0 0.4rem 0;">My Portfolio</h1>
                    <p class="pc-lead" style="font-size: 1.05rem;">
                        Manage the work clients see first. Your portfolio defines your presence on the public marketplace.
                    </p>
                </div>
                <div>
                    <button type="button" class="btn btn-primary btn-lg" id="btnToggleUpload" aria-expanded="false">
                        + Add Photograph
                    </button>
                </div>
            </div>

            <!-- Feedback Alerts -->
            <c:if test="${not empty successMessage}">
                <div class="pc-alert pc-alert-success" role="status">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- Expandable Upload Drawer Panel (Collapsed by default) -->
            <div class="upload-expandable-panel" id="uploadDrawerPanel" aria-hidden="true">
                <div class="upload-card-inner">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                        <h2 class="editorial-heading" style="font-size: 1.4rem; margin: 0;">Upload New Photograph</h2>
                        <button type="button" class="btn btn-ghost btn-sm" id="btnCloseUpload">Close ✕</button>
                    </div>

                    <form action="${pageContext.request.contextPath}/photographer/portfolio/upload"
                          method="post"
                          enctype="multipart/form-data"
                          id="upload-form">
                        <div class="pc-upload-grid">
                            <div>
                                <label for="imageFile" class="pc-label">Image File *</label>
                                <input type="file"
                                       id="imageFile"
                                       name="imageFile"
                                       accept="image/jpeg,image/png,image/webp"
                                       class="pc-input"
                                       required>
                                <div class="form-help">JPEG, PNG, or WEBP &middot; Max 10 MB file size</div>
                            </div>
                            <div>
                                <label for="caption" class="pc-label">Photograph Caption (optional)</label>
                                <input type="text"
                                       id="caption"
                                       name="caption"
                                       maxlength="500"
                                       placeholder="Describe context, location, or equipment…"
                                       class="pc-input">
                            </div>
                            <div>
                                <button type="submit" class="btn btn-primary" id="btn-upload-submit" style="min-height: 46px;">
                                    Upload Photograph
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Gallery Dominant Content -->
            <c:choose>
                <c:when test="${empty images}">
                    <!-- Intentional Empty State -->
                    <div class="pc-empty-state" style="max-width: 620px; margin: 2rem auto;">
                        <div style="font-size: 2.5rem; margin-bottom: 1rem; color: var(--accent);">✦</div>
                        <h2 class="editorial-heading" style="font-size: 1.75rem; margin-bottom: 0.75rem;">Your portfolio starts here.</h2>
                        <p style="font-size: 1.05rem; margin-bottom: 2rem;">
                            Upload your first photograph to show prospective clients what makes your eye, craft, and vision distinctive.
                        </p>
                        <button type="button" class="btn btn-primary btn-lg" id="btnEmptyStateAdd">
                            + Add Your First Photograph
                        </button>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Dominant Editorial Masonry Grid -->
                    <div class="portfolio-editorial-grid">
                        <c:forEach var="img" items="${images}">
                            <div class="portfolio-card-frame">
                                <img src="<c:out value='${img.imageUrl}'/>"
                                     alt="<c:out value='${not empty img.caption ? img.caption : \"Portfolio image\"}'/>"
                                     class="portfolio-card-img"
                                     loading="lazy">
                                <div class="portfolio-hover-scrim">
                                    <div class="scrim-top-actions">
                                        <form action="${pageContext.request.contextPath}/photographer/portfolio/${img.id}/delete"
                                              method="post"
                                              onsubmit="return confirm('Delete this photograph from your public portfolio? This cannot be undone.');"
                                              style="margin: 0;">
                                            <button type="submit" class="btn btn-danger btn-sm" id="btn-delete-${img.id}">
                                                Delete
                                            </button>
                                        </form>
                                    </div>
                                    <div class="scrim-caption-text">
                                        <c:choose>
                                            <c:when test="${not empty img.caption}">
                                                <c:out value="${img.caption}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <em style="opacity: 0.7;">No caption provided</em>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>

    <!-- Editorial Footer -->
    <footer class="pc-footer">
        <div class="editorial-container">
            <div style="font-family: var(--font-editorial); font-size: 1.15rem; color: var(--text); margin-bottom: 0.5rem;">
                PhotoConnect Creator Studio
            </div>
            <div style="color: var(--muted); font-size: 0.85rem;">
                &copy; 2026 PhotoConnect. Portfolio images are displayed on your public verified profile.
            </div>
        </div>
    </footer>

    <!-- Plain JavaScript for Expandable Upload Drawer -->
    <script>
        (function() {
            const toggleBtn = document.getElementById('btnToggleUpload');
            const emptyAddBtn = document.getElementById('btnEmptyStateAdd');
            const closeBtn = document.getElementById('btnCloseUpload');
            const panel = document.getElementById('uploadDrawerPanel');
            const fileInput = document.getElementById('imageFile');

            function openPanel() {
                if (!panel) return;
                panel.classList.add('is-expanded');
                panel.setAttribute('aria-hidden', 'false');
                if (toggleBtn) {
                    toggleBtn.setAttribute('aria-expanded', 'true');
                    toggleBtn.textContent = '✕ Close Upload';
                }
                setTimeout(function() {
                    if (fileInput) fileInput.focus();
                }, 300);
            }

            function closePanel() {
                if (!panel) return;
                panel.classList.remove('is-expanded');
                panel.setAttribute('aria-hidden', 'true');
                if (toggleBtn) {
                    toggleBtn.setAttribute('aria-expanded', 'false');
                    toggleBtn.textContent = '+ Add Photograph';
                }
            }

            if (toggleBtn) {
                toggleBtn.addEventListener('click', function() {
                    if (panel.classList.contains('is-expanded')) {
                        closePanel();
                    } else {
                        openPanel();
                    }
                });
            }

            if (emptyAddBtn) {
                emptyAddBtn.addEventListener('click', function() {
                    openPanel();
                });
            }

            if (closeBtn) {
                closeBtn.addEventListener('click', closePanel);
            }
        })();
    </script>
</body>
</html>
