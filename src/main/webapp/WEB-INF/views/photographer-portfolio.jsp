<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>My Portfolio — PhotoConnect Creator Studio</title>
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
            max-height: 850px;
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

        /* ── Local Upload Preview Box ───────────────────────────────────────── */
        .upload-preview-box {
            display: none;
            background: var(--surface-subtle);
            border: 1px dashed var(--border-strong);
            border-radius: var(--radius-sm);
            padding: 1.25rem;
            margin-top: 1.25rem;
            margin-bottom: 1.25rem;
            align-items: center;
            gap: 1.5rem;
        }

        .upload-preview-box.has-preview {
            display: flex;
        }

        .upload-preview-thumb {
            width: 100px;
            height: 100px;
            object-fit: cover;
            border-radius: var(--radius-sm);
            border: 1px solid var(--border);
            flex-shrink: 0;
        }

        .upload-preview-info {
            display: flex;
            flex-direction: column;
            gap: 0.35rem;
            min-width: 0;
        }

        .upload-preview-filename {
            font-size: 0.95rem;
            font-weight: 600;
            color: var(--text);
            word-break: break-all;
        }

        /* ── Dedicated Cover Hero Showcase ──────────────────────────────────── */
        .portfolio-cover-showcase {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: clamp(1.5rem, 3vw, 2.25rem);
            margin-bottom: 3.5rem;
            box-shadow: var(--shadow-subtle);
        }

        .cover-showcase-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1.25rem;
            flex-wrap: wrap;
            gap: 0.75rem;
        }

        .cover-frame {
            position: relative;
            width: 100%;
            height: clamp(240px, 32vw, 420px);
            overflow: hidden;
            border-radius: var(--radius-sm);
            background: var(--surface-subtle);
            border: 1px solid var(--border);
        }

        .cover-frame-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .cover-meta-strip {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 1.25rem;
            flex-wrap: wrap;
            gap: 1rem;
        }

        /* ── Category Filter Bar ────────────────────────────────────────────── */
        .category-filter-bar {
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            margin-bottom: 2rem;
            align-items: center;
        }

        .filter-tab-btn {
            background: var(--surface);
            color: var(--muted);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: 0.45rem 1rem;
            font-size: 0.88rem;
            font-weight: 500;
            cursor: pointer;
            transition: all var(--transition-fast);
        }

        .filter-tab-btn:hover {
            color: var(--text);
            border-color: var(--border-strong);
            background: var(--surface-hover);
        }

        .filter-tab-btn.is-active {
            background: var(--deep-navy);
            color: #FFFFFF;
            border-color: var(--deep-navy);
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
            transition: transform var(--transition-smooth), box-shadow var(--transition-smooth);
        }

        .portfolio-card-frame:hover {
            box-shadow: var(--shadow-medium);
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
            background: linear-gradient(180deg, rgba(7, 11, 18, 0.45) 0%, rgba(7, 11, 18, 0.92) 100%);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 1.25rem;
            opacity: 0;
            transition: opacity var(--transition-fast);
            z-index: 2;
        }

        .portfolio-card-frame:hover .portfolio-hover-scrim,
        .portfolio-card-frame:focus-within .portfolio-hover-scrim {
            opacity: 1;
        }

        .scrim-top-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 0.5rem;
        }

        .scrim-actions-row {
            display: flex;
            gap: 0.5rem;
            align-items: center;
        }

        .scrim-caption-text {
            color: #F8FAFC;
            font-size: 0.92rem;
            font-weight: 400;
            line-height: 1.4;
            margin-bottom: 0.75rem;
        }

        /* ── Badge Indicators ───────────────────────────────────────────────── */
        .pc-badge-category {
            background: rgba(37, 99, 235, 0.85);
            color: #FFFFFF;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.25rem 0.65rem;
            border-radius: var(--radius-sm);
        }

        .pc-badge-cover-tag {
            background: rgba(16, 185, 129, 0.9);
            color: #FFFFFF;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.25rem 0.65rem;
            border-radius: var(--radius-sm);
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
        }

        @media (hover: none), (max-width: 640px) {
            .portfolio-hover-scrim {
                opacity: 1;
                background: linear-gradient(180deg, rgba(7, 11, 18, 0.35) 0%, rgba(7, 11, 18, 0.92) 100%);
            }
        }
    </style>
</head>

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
                        Manage the work clients see on your public profile. Select categories, establish your signature cover photograph, and build your visual presence.
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
                        <div>
                            <h2 class="editorial-heading" style="font-size: 1.4rem; margin: 0 0 0.25rem 0;">Upload New Photograph</h2>
                            <div style="color: var(--muted); font-size: 0.88rem;">
                                Binaries are stored securely on Cloudinary CDN; metadata and category are recorded in PhotoConnect.
                            </div>
                        </div>
                        <button type="button" class="btn btn-ghost btn-sm" id="btnCloseUpload">Close ✕</button>
                    </div>

                    <form action="${pageContext.request.contextPath}/photographer/portfolio/upload"
                          method="post"
                          enctype="multipart/form-data"
                          id="upload-form">
                        <%@ include file="fragments/csrf-input.jsp" %>
                        <div class="row g-3">
                            <!-- Image file selection -->
                            <div class="col-12 col-md-4">
                                <label for="imageFile" class="pc-label">Photograph File *</label>
                                <input type="file"
                                       id="imageFile"
                                       name="imageFile"
                                       accept="image/jpeg,image/png,image/webp"
                                       class="form-control pc-input"
                                       required>
                                <div class="form-help">JPEG, PNG, or WEBP &middot; Max 10 MB</div>
                            </div>

                            <!-- Photography Category Selection -->
                            <div class="col-12 col-md-4">
                                <label for="category" class="pc-label">Photography Category *</label>
                                <select id="category" name="category" class="form-select pc-input" required>
                                    <option value="" disabled selected>-- Select Category --</option>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat.name()}"><c:out value="${cat.displayName}"/></option>
                                    </c:forEach>
                                </select>
                                <div class="form-help">Curated style discipline for discovery</div>
                            </div>

                            <!-- Optional Caption -->
                            <div class="col-12 col-md-4">
                                <label for="caption" class="pc-label">Caption (optional)</label>
                                <input type="text"
                                       id="caption"
                                       name="caption"
                                       maxlength="500"
                                       placeholder="Describe context, location, or lens…"
                                       class="form-control pc-input">
                                <div class="form-help">Up to 500 characters</div>
                            </div>
                        </div>

                        <!-- Local Image Preview Box (Vanilla JavaScript) -->
                        <div class="upload-preview-box" id="uploadPreviewRegion">
                            <img src="" alt="Photograph preview" class="upload-preview-thumb" id="uploadPreviewImg">
                            <div class="upload-preview-info">
                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                    <span class="pc-badge-category" id="previewCategoryBadge">Category</span>
                                    <span style="font-size: 0.78rem; color: var(--muted);">Ready to upload</span>
                                </div>
                                <div class="upload-preview-filename" id="previewFileName">filename.jpg</div>
                                <div style="font-size: 0.82rem; color: var(--muted);" id="previewFileSize"></div>
                            </div>
                        </div>

                        <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
                            <button type="submit" class="btn btn-primary" id="btn-upload-submit" style="min-height: 44px; padding-inline: 2rem;">
                                Upload Photograph
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- ── COVER AREA ───────────────────────────────────────────────── -->
            <c:if test="${not empty currentCover}">
                <section class="portfolio-cover-showcase">
                    <div class="cover-showcase-header">
                        <div>
                            <span class="section-index" style="font-size: 0.78rem;">Public Presentation</span>
                            <h2 class="editorial-heading" style="font-size: 1.5rem; margin: 0;">Portfolio Cover</h2>
                        </div>
                        <div>
                            <span class="pc-badge-cover-tag">★ Current Cover</span>
                        </div>
                    </div>

                    <div class="cover-frame">
                        <img src="<c:out value='${currentCover.coverTransformedUrl}'/>"
                             alt="<c:out value='${not empty currentCover.caption ? currentCover.caption : \"Portfolio Cover\"}'/>"
                             class="cover-frame-img"
                             loading="eager">
                    </div>

                    <div class="cover-meta-strip">
                        <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                            <span class="pc-badge-category">
                                <c:out value="${currentCover.categoryDisplayName}"/>
                            </span>
                            <c:choose>
                                <c:when test="${not empty currentCover.caption}">
                                    <span style="font-size: 0.95rem; color: var(--text); font-weight: 500;">
                                        &ldquo;<c:out value="${currentCover.caption}"/>&rdquo;
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span style="font-size: 0.85rem; color: var(--muted); font-style: italic;">
                                        No caption set
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div style="font-size: 0.82rem; color: var(--muted);">
                            Displayed on your public profile header &amp; search directory card.
                        </div>
                    </div>
                </section>
            </c:if>

            <!-- ── GALLERY SECTION ──────────────────────────────────────────── -->
            <c:choose>
                <c:when test="${empty images}">
                    <!-- Intentional Empty State -->
                    <div class="pc-empty-state" style="max-width: 620px; margin: 3rem auto;">
                        <div style="font-size: 2.5rem; margin-bottom: 1rem; color: var(--accent);">✦</div>
                        <h2 class="editorial-heading" style="font-size: 1.75rem; margin-bottom: 0.75rem;">Your portfolio is empty.</h2>
                        <p style="font-size: 1.05rem; margin-bottom: 2rem;">
                            Upload your first photograph to start building your profile. Your first photograph will automatically serve as your portfolio cover.
                        </p>
                        <button type="button" class="btn btn-primary btn-lg" id="btnEmptyStateAdd">
                            + Add Your First Photograph
                        </button>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Category Filter Tabs -->
                    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 1rem; flex-wrap: wrap; gap: 1rem;">
                        <h2 class="editorial-heading" style="font-size: 1.6rem; margin: 0;">
                            Gallery (<span id="visibleCount"><c:out value="${fn:length(images)}"/></span> of <c:out value="${fn:length(images)}"/>)
                        </h2>
                    </div>

                    <div class="category-filter-bar" role="tablist" aria-label="Filter gallery by category">
                        <button type="button" class="filter-tab-btn is-active" data-filter="ALL" id="filter-all">
                            All
                        </button>
                        <c:forEach var="cat" items="${categories}">
                            <button type="button" class="filter-tab-btn" data-filter="${cat.name()}" id="filter-${fn:toLowerCase(cat.name())}">
                                <c:out value="${cat.displayName}"/>
                            </button>
                        </c:forEach>
                    </div>

                    <!-- Category empty notice (hidden by default) -->
                    <div id="categoryEmptyNotice" class="pc-empty-state" style="display: none; padding: 2.5rem 1rem; margin-bottom: 2rem;">
                        <p style="color: var(--muted); margin: 0;">No photographs found in this category.</p>
                    </div>

                    <!-- Dominant Editorial Grid -->
                    <div class="portfolio-editorial-grid" id="portfolioGrid">
                        <c:forEach var="img" items="${images}">
                            <div class="portfolio-card-frame portfolio-card-item"
                                 data-category="${img.category != null ? img.category.name() : 'OTHER'}"
                                 id="portfolio-card-${img.id}">
                                <img src="<c:out value='${img.thumbnailUrl}'/>"
                                     alt="<c:out value='${not empty img.caption ? img.caption : \"Portfolio image\"}'/>"
                                     class="portfolio-card-img"
                                     loading="lazy">
                                <div class="portfolio-hover-scrim">
                                    <div class="scrim-top-row">
                                        <span class="pc-badge-category">
                                            <c:out value="${img.categoryDisplayName}"/>
                                        </span>
                                        <c:if test="${img.isCover}">
                                            <span class="pc-badge-cover-tag">★ Cover</span>
                                        </c:if>
                                    </div>

                                    <div>
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

                                        <div class="scrim-actions-row">
                                            <!-- Set as Cover Action (only if not already cover) -->
                                            <c:choose>
                                                <c:when test="${!img.isCover}">
                                                    <form action="${pageContext.request.contextPath}/photographer/portfolio/${img.id}/cover"
                                                          method="post"
                                                          style="margin: 0;">
                                                        <%@ include file="fragments/csrf-input.jsp" %>
                                                        <button type="submit" class="btn btn-outline-light btn-sm" id="btn-cover-${img.id}">
                                                            Set as Cover
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-white-50" style="font-size: 0.8rem; font-weight: 600; padding: 0.25rem 0.5rem;">
                                                        Current Cover
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>

                                            <!-- Delete Action -->
                                            <form action="${pageContext.request.contextPath}/photographer/portfolio/${img.id}/delete"
                                                  method="post"
                                                  onsubmit="return confirm('Delete this photograph from your portfolio? This removes the Cloudinary asset and cannot be undone.');"
                                                  style="margin: 0;">
                                                <%@ include file="fragments/csrf-input.jsp" %>
                                                <button type="submit" class="btn btn-danger btn-sm" id="btn-delete-${img.id}">
                                                    Delete
                                                </button>
                                            </form>
                                        </div>
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
                &copy; 2026 PhotoConnect. Portfolio images and cover photographs are verified and displayed across the public marketplace.
            </div>
        </div>
    </footer>

    <!-- Plain JavaScript for Expandable Upload Drawer, Live Preview, and Category Filter -->
    <script>
        (function() {
            // ── Upload Drawer Controls ──
            const toggleBtn = document.getElementById('btnToggleUpload');
            const emptyAddBtn = document.getElementById('btnEmptyStateAdd');
            const closeBtn = document.getElementById('btnCloseUpload');
            const panel = document.getElementById('uploadDrawerPanel');
            const fileInput = document.getElementById('imageFile');
            const categorySelect = document.getElementById('category');

            // ── Live Upload Preview ──
            const previewBox = document.getElementById('uploadPreviewRegion');
            const previewImg = document.getElementById('uploadPreviewImg');
            const previewFileName = document.getElementById('previewFileName');
            const previewFileSize = document.getElementById('previewFileSize');
            const previewCategoryBadge = document.getElementById('previewCategoryBadge');

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

            function updateCategoryPreview() {
                if (!categorySelect || !previewCategoryBadge) return;
                const selectedOpt = categorySelect.options[categorySelect.selectedIndex];
                if (selectedOpt && selectedOpt.value) {
                    previewCategoryBadge.textContent = selectedOpt.textContent;
                } else {
                    previewCategoryBadge.textContent = 'Category';
                }
            }

            if (categorySelect) {
                categorySelect.addEventListener('change', updateCategoryPreview);
            }

            if (fileInput) {
                fileInput.addEventListener('change', function(e) {
                    const file = e.target.files && e.target.files[0];
                    if (file) {
                        previewFileName.textContent = file.name;
                        const sizeMb = (file.size / (1024 * 1024)).toFixed(2);
                        previewFileSize.textContent = sizeMb + ' MB';
                        updateCategoryPreview();

                        const reader = new FileReader();
                        reader.onload = function(evt) {
                            if (previewImg) {
                                previewImg.src = evt.target.result;
                            }
                            if (previewBox) {
                                previewBox.classList.add('has-preview');
                            }
                        };
                        reader.readAsDataURL(file);
                    } else {
                        if (previewBox) {
                            previewBox.classList.remove('has-preview');
                        }
                    }
                });
            }

            // ── Gallery Category Filtering ──
            const filterBtns = document.querySelectorAll('.filter-tab-btn');
            const cardItems = document.querySelectorAll('.portfolio-card-item');
            const emptyNotice = document.getElementById('categoryEmptyNotice');
            const visibleCountElem = document.getElementById('visibleCount');

            filterBtns.forEach(function(btn) {
                btn.addEventListener('click', function() {
                    const targetCategory = this.getAttribute('data-filter');

                    filterBtns.forEach(function(b) {
                        b.classList.remove('is-active');
                    });
                    this.classList.add('is-active');

                    let visibleCount = 0;
                    cardItems.forEach(function(card) {
                        const itemCategory = card.getAttribute('data-category');
                        if (targetCategory === 'ALL' || itemCategory === targetCategory) {
                            card.style.display = '';
                            visibleCount++;
                        } else {
                            card.style.display = 'none';
                        }
                    });

                    if (visibleCountElem) {
                        visibleCountElem.textContent = visibleCount;
                    }

                    if (emptyNotice) {
                        emptyNotice.style.display = (visibleCount === 0) ? 'block' : 'none';
                    }
                });
            });
        })();
    </script>
