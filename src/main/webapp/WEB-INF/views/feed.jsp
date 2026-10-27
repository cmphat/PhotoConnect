<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Visual Feed — PhotoConnect</title>
    <meta name="description" content="Explore the latest photographic works and stories from top verified photographers on PhotoConnect.">

    <style>

        .feed-page-region {
            padding: 2rem 0 4rem;
            background: var(--background);
        }

        .feed-container {
            max-width: 1040px;
            margin: 0 auto;
            padding: 0 1rem;
        }

        /* ── Stories Row ────────────────────────────────────────── */
        .stories-container {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--pc-radius-lg, 14px);
            padding: 1.25rem 1rem;
            margin-bottom: 1.75rem;
            box-shadow: var(--pc-shadow-card);
            overflow-x: auto;
            scrollbar-width: none;
            -ms-overflow-style: none;
        }
        .stories-container::-webkit-scrollbar {
            display: none;
        }

        .stories-track {
            display: flex;
            gap: 1.25rem;
            align-items: center;
        }

        .story-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-decoration: none;
            color: var(--text);
            gap: 0.4rem;
            flex-shrink: 0;
            width: 72px;
            cursor: pointer;
            transition: transform var(--transition-fast);
        }
        .story-item:hover {
            transform: translateY(-2px);
        }

        .story-avatar-ring {
            width: 62px;
            height: 62px;
            border-radius: 50%;
            padding: 2.5px;
            background: linear-gradient(45deg, #f09433, #e6683c, #dc2743, #cc2366, #bc1888);
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }

        .story-avatar-img {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid var(--surface);
            background: #1e293b;
        }

        .story-initial-avatar {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            border: 2px solid var(--surface);
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 1.15rem;
        }

        .story-name {
            font-size: 0.72rem;
            font-weight: 700;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width: 100%;
            text-align: center;
            color: var(--text);
        }

        /* ── Category Filter Pills ──────────────────────────────── */
        .category-pills-row {
            display: flex;
            gap: 0.5rem;
            overflow-x: auto;
            scrollbar-width: none;
            margin-bottom: 2rem;
            padding-bottom: 0.25rem;
        }
        .category-pills-row::-webkit-scrollbar {
            display: none;
        }

        .cat-pill {
            display: inline-flex;
            align-items: center;
            padding: 0.45rem 0.95rem;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--pc-radius-sm, 6px);
            font-size: 0.84rem;
            font-weight: 500;
            color: var(--muted);
            text-decoration: none;
            white-space: nowrap;
            transition: all var(--transition-fast);
        }
        .cat-pill:hover {
            color: var(--text);
            border-color: var(--accent);
            background: var(--surface-elevated);
        }
        .cat-pill.active {
            background: var(--accent);
            border-color: var(--accent);
            color: #ffffff;
        }

        /* ── Post Card ──────────────────────────────────────────── */
        .feed-post-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--pc-radius-lg, 14px);
            overflow: hidden;
            margin-bottom: 2rem;
            box-shadow: var(--pc-shadow-card);
            transition: box-shadow var(--transition-fast);
        }
        .feed-post-card:hover {
            box-shadow: var(--pc-shadow-card-hover);
        }

        .post-header {
            padding: 1rem 1.25rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .post-author-info {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            text-decoration: none;
            color: inherit;
        }

        .post-author-avatar {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid var(--border-subtle);
        }

        .post-author-initial {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: linear-gradient(135deg, #3b82f6, #1d4ed8);
            color: #ffffff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 1.1rem;
        }

        .post-author-name {
            font-size: 0.96rem;
            font-weight: 800;
            color: var(--text);
            display: flex;
            align-items: center;
            gap: 0.35rem;
        }

        .post-meta-sub {
            font-size: 0.78rem;
            color: var(--muted);
            display: flex;
            align-items: center;
            gap: 0.4rem;
        }

        .post-caption-box {
            padding: 0 1.25rem 0.85rem;
            font-size: 0.94rem;
            color: var(--text);
            line-height: 1.6;
            font-weight: 400;
        }

        .post-category-tag {
            font-weight: 700;
            color: var(--accent);
            text-decoration: none;
            margin-right: 0.35rem;
        }

        .post-media-frame {
            position: relative;
            background: #0f172a;
            overflow: hidden;
            user-select: none;
        }

        .post-media-img {
            width: 100%;
            max-height: 640px;
            object-fit: cover;
            display: block;
        }

        /* ── Post Action Bar ────────────────────────────────────── */
        .post-actions-bar {
            padding: 0.85rem 1.25rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid var(--border-subtle);
        }

        .social-left-actions {
            display: flex;
            align-items: center;
            gap: 1.25rem;
        }

        .action-icon-btn {
            background: none;
            border: none;
            padding: 0;
            display: inline-flex;
            align-items: center;
            gap: 0.45rem;
            font-size: 0.9rem;
            font-weight: 700;
            color: var(--muted);
            cursor: pointer;
            transition: all var(--transition-fast);
        }
        .action-icon-btn:hover {
            color: var(--text);
        }

        .like-btn.liked {
            color: #ef4444;
        }
        .like-btn.liked svg {
            fill: #ef4444;
            stroke: #ef4444;
            animation: heartPulse 0.35s ease;
        }

        @keyframes heartPulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.35); }
            100% { transform: scale(1); }
        }

        .btn-book-cta {
            background: var(--pc-primary);
            color: #ffffff !important;
            padding: 0.45rem 1.15rem;
            font-size: 0.86rem;
            font-weight: 600;
            border-radius: var(--pc-radius-sm, 6px);
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.45rem;
            transition: background-color var(--transition-fast), transform var(--transition-fast);
        }
        .btn-book-cta:hover {
            background: var(--pc-primary-hover);
            transform: translateY(-1px);
        }

        /* ── Comments Box ───────────────────────────────────────── */
        .post-comments-section {
            padding: 0.85rem 1.25rem 1.1rem;
            background: var(--surface-hover);
        }

        .comment-item {
            display: flex;
            gap: 0.65rem;
            margin-bottom: 0.65rem;
            font-size: 0.85rem;
        }

        .comment-bubble {
            background: var(--surface);
            border: 1px solid var(--border-subtle);
            border-radius: 12px;
            padding: 0.45rem 0.85rem;
            max-width: 90%;
        }

        .comment-user {
            font-weight: 700;
            color: var(--text);
            margin-right: 0.35rem;
        }

        .comment-input-wrap {
            display: flex;
            gap: 0.5rem;
            margin-top: 0.75rem;
        }

        .comment-input-field {
            flex-grow: 1;
            height: 38px;
            border-radius: 20px;
            border: 1px solid var(--border);
            padding: 0 1rem;
            font-size: 0.85rem;
            background: var(--surface);
            color: var(--text);
            font-weight: 400;
        }
        .comment-input-field:focus {
            outline: none;
            border-color: var(--accent);
        }

        /* ── Sticky Sidebar ─────────────────────────────────────── */
        .sidebar-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--pc-radius-lg, 14px);
            padding: 1.25rem;
            margin-bottom: 1.5rem;
            box-shadow: var(--pc-shadow-card);
        }

        .sidebar-title {
            font-size: 0.95rem;
            font-weight: 800;
            color: var(--text);
            margin-bottom: 1rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .creator-mini-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0.5rem 0;
            border-bottom: 1px solid var(--border-subtle);
        }
        .creator-mini-row:last-child {
            border-bottom: none;
        }

        /* Floating Heart Animation */
        .heart-float {
            position: absolute;
            pointer-events: none;
            color: #ef4444;
            font-size: 3rem;
            animation: floatUp 0.8s ease-out forwards;
            opacity: 0;
        }
        @keyframes floatUp {
            0% { transform: translate(-50%, -50%) scale(0.5); opacity: 1; }
            50% { transform: translate(-50%, -80%) scale(1.3); opacity: 0.9; }
            100% { transform: translate(-50%, -120%) scale(1); opacity: 0; }
        }
    </style>
</head>

<main class="feed-page-region">
    <div class="feed-container">

        <!-- Top Header & View Mode Switcher -->
        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
            <div>
                <span class="section-index">Live Photography Feed</span>
                <h1 style="font-size: 1.85rem; font-weight: 800; margin: 0; color: var(--text);">
                    Recent Photographic Works
                </h1>
            </div>

            <!-- View Mode Switcher -->
            <div class="view-mode-tabs" style="display: inline-flex; background: var(--surface); border: 1px solid var(--border); padding: 4px; border-radius: var(--pc-radius-md, 10px);">
                <a href="${pageContext.request.contextPath}/photographers" class="view-mode-tab" style="padding: 0.4rem 1rem; font-size: 0.85rem; font-weight: 600; border-radius: var(--pc-radius-sm, 6px); color: var(--muted); text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem;">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                    Photographer Roster
                </a>
                <a href="${pageContext.request.contextPath}/feed" class="view-mode-tab active" style="padding: 0.4rem 1rem; font-size: 0.85rem; font-weight: 600; border-radius: var(--pc-radius-sm, 6px); background: var(--accent); color: #fff; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem;">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                    Visual Feed
                </a>
            </div>
        </div>

        <!-- ── Top Stories / Featured Creators Row ────────────────── -->
        <div class="stories-container">
            <div class="stories-track">
                <c:forEach var="c" items="${featuredCreators}">
                    <a href="${pageContext.request.contextPath}/photographers/${c.photographerId}" class="story-item" title="View <c:out value='${c.displayName}'/>'s profile">
                        <div class="story-avatar-ring">
                            <c:choose>
                                <c:when test="${not empty c.avatarUrl}">
                                    <img src="<c:out value='${c.avatarUrl}'/>" alt="<c:out value='${c.displayName}'/>" class="story-avatar-img">
                                </c:when>
                                <c:otherwise>
                                    <div class="story-initial-avatar">
                                        <c:out value="${fn:toUpperCase(fn:substring(c.displayName, 0, 1))}"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <span class="story-name"><c:out value="${c.displayName}"/></span>
                    </a>
                </c:forEach>
            </div>
        </div>

        <!-- ── Category Filter Pills ──────────────────────────────── -->
        <div class="category-pills-row">
            <a href="${pageContext.request.contextPath}/feed" class="cat-pill <c:if test="${activeCategory == 'ALL'}">active</c:if>">
                All Works
            </a>
            <c:forEach var="cat" items="${categories}">
                <a href="${pageContext.request.contextPath}/feed?category=${cat.name()}" class="cat-pill <c:if test="${activeCategory == cat.name()}">active</c:if>">
                    <c:out value="${cat.displayName}"/>
                </a>
            </c:forEach>
        </div>

        <!-- ── Main Grid Layout: Feed on Left, Sticky Sidebar on Right ── -->
        <div class="row g-4">
            <!-- Left Main Feed Column -->
            <div class="col-12 col-lg-8">
                <c:choose>
                    <c:when test="${empty posts}">
                        <div class="pc-empty-state text-center p-5 bg-surface border rounded-4">
                            <h3 style="font-weight: 800;">No works found in this category</h3>
                            <p class="text-muted">Explore our photographers directory to discover creative portfolios.</p>
                            <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary mt-3">Explore Photographers</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="post" items="${posts}">
                            <article class="feed-post-card" id="post-<c:out value='${post.imageId}'/>">
                                <div class="post-header-bar">
                                    <a href="${pageContext.request.contextPath}/photographers/${post.photographerId}" class="post-author-info">
                                         <c:choose>
                                             <c:when test="${not empty post.photographerAvatarUrl}">
                                                 <img src="<c:out value='${post.photographerAvatarUrl}'/>" alt="<c:out value='${post.photographerName}'/>" class="post-author-avatar">
                                             </c:when>
                                             <c:otherwise>
                                                 <div class="post-author-initial">
                                                     <c:out value="${fn:toUpperCase(fn:substring(post.photographerName, 0, 1))}"/>
                                                 </div>
                                             </c:otherwise>
                                         </c:choose>
                                         <div>
                                             <div class="post-author-name">
                                                 <c:out value="${post.photographerName}"/>
                                                 <span class="verified-badge" title="Verified Creator">
                                                     <svg width="15" height="15" viewBox="0 0 24 24" fill="#2563eb"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
                                                 </span>
                                             </div>
                                             <div class="post-meta-sub">
                                                 <span><c:out value="${post.photographerCity}"/></span>
                                                 <span>•</span>
                                                 <span><c:out value="${post.timeAgo}"/></span>
                                             </div>
                                         </div>
                                     </a>

                                     <div class="d-flex align-items-center gap-2">
                                         <c:if test="${post.isSaved}">
                                             <span class="badge bg-danger-subtle text-danger" style="font-size: 0.72rem;">♥ Saved</span>
                                         </c:if>
                                         <a href="${pageContext.request.contextPath}/photographers/${post.photographerId}" class="btn btn-sm btn-ghost" title="View Profile">
                                             <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="1"></circle><circle cx="19" cy="12" r="1"></circle><circle cx="5" cy="12" r="1"></circle></svg>
                                         </a>
                                     </div>
                                 </div>

                                 <!-- Post Caption -->
                                 <div class="post-caption-box">
                                     <c:if test="${not empty post.categoryDisplayName}">
                                         <a href="${pageContext.request.contextPath}/feed?category=${post.category}" class="post-category-tag">
                                             #<c:out value="${post.categoryDisplayName}"/>
                                         </a>
                                     </c:if>
                                     <div><c:out value="${post.caption}"/></div>
                                 </div>

                                 <!-- Post Photography Image (Double-click to Like) -->
                                 <div class="post-media-frame post-media-dblclick" data-image-id="<c:out value='${post.imageId}'/>">
                                     <img src="<c:out value='${post.imageUrl}'/>" alt="<c:out value='${post.caption}'/>" class="post-media-img" loading="lazy">
                                     <div class="heart-float" id="heart-anim-<c:out value='${post.imageId}'/>">❤️</div>
                                 </div>

                                 <!-- Social Interaction Bar -->
                                 <div class="post-actions-bar">
                                     <div class="social-left-actions">
                                         <!-- Like Button -->
                                         <button type="button" class="action-icon-btn like-btn action-toggle-like" id="like-btn-<c:out value='${post.imageId}'/>" data-image-id="<c:out value='${post.imageId}'/>">
                                             <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                             <span id="like-count-<c:out value='${post.imageId}'/>"><c:out value="${post.likeCount}"/></span>
                                         </button>

                                         <!-- Comment Toggle Button -->
                                         <button type="button" class="action-icon-btn action-toggle-comments" data-image-id="<c:out value='${post.imageId}'/>">
                                             <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                                             <span><c:out value="${post.commentCount}"/></span>
                                         </button>

                                         <!-- Share Button -->
                                         <button type="button" class="action-icon-btn action-share-post" data-photographer-id="<c:out value='${post.photographerId}'/>" title="Share Link">
                                             <svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
                                         </button>
                                     </div>

                                     <!-- Direct Booking Call-To-Action -->
                                     <a href="${pageContext.request.contextPath}/bookings/new?photographerId=${post.photographerId}" class="btn-book-cta">
                                         <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                         Book Shoot • From <fmt:formatNumber value="${post.startingPrice}" type="number" maxFractionDigits="0"/> VND
                                     </a>
                                 </div>

                                 <!-- Comments Section -->
                                 <div class="post-comments-section" id="comments-section-<c:out value='${post.imageId}'/>">
                                     <div id="dynamic-comments-<c:out value='${post.imageId}'/>"></div>

                                     <div class="comment-input-wrap">
                                         <input type="text"
                                                class="comment-input-field"
                                                id="comment-input-<c:out value='${post.imageId}'/>"
                                                data-image-id="<c:out value='${post.imageId}'/>"
                                                placeholder="Write a comment on this work...">
                                         <button type="button" class="btn btn-sm btn-primary rounded-2 px-3 action-submit-comment" data-image-id="<c:out value='${post.imageId}'/>">
                                             Post
                                         </button>
                                     </div>
                                 </div>
                             </article>
                         </c:forEach>
                     </c:otherwise>
                 </c:choose>
             </div>

             <!-- Right Sticky Sidebar -->
             <div class="col-12 col-lg-4">
                 <div style="position: sticky; top: 85px;">
                     <!-- Suggested Creators Card -->
                     <div class="sidebar-card">
                         <div class="sidebar-title">
                             <span>Featured Photographers</span>
                             <a href="${pageContext.request.contextPath}/photographers" class="text-primary small text-decoration-none fw-semibold">View all</a>
                         </div>
                         <c:forEach var="sc" items="${featuredCreators}" end="4">
                             <div class="creator-mini-row">
                                 <a href="${pageContext.request.contextPath}/photographers/${sc.photographerId}" class="d-flex align-items-center gap-2 text-decoration-none color-inherit">
                                     <div style="width: 36px; height: 36px; border-radius: 50%; overflow: hidden; background: #2563eb; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem;">
                                         <c:choose>
                                             <c:when test="${not empty sc.avatarUrl}">
                                                 <img src="<c:out value='${sc.avatarUrl}'/>" alt="<c:out value='${sc.displayName}'/>" style="width: 100%; height: 100%; object-fit: cover;">
                                             </c:when>
                                             <c:otherwise>
                                                 <c:out value="${fn:toUpperCase(fn:substring(sc.displayName, 0, 1))}"/>
                                             </c:otherwise>
                                         </c:choose>
                                     </div>
                                    <div>
                                        <div style="font-weight: 700; font-size: 0.88rem; color: var(--text);"><c:out value="${sc.displayName}"/></div>
                                        <div style="font-size: 0.74rem; color: var(--muted);"><c:out value="${sc.city}"/></div>
                                    </div>
                                </a>
                                <a href="${pageContext.request.contextPath}/photographers/${sc.photographerId}" class="btn btn-sm btn-outline-primary rounded-pill" style="font-size: 0.76rem; padding: 0.2rem 0.65rem;">
                                    Profile
                                </a>
                            </div>
                        </c:forEach>
                    </div>


                    <div class="text-center text-muted small mt-3">
                        &copy; 2026 PhotoConnect Feed • Professional Creative Network
                    </div>
                </div>
            </div>
        </div>

    </div>
</main>

<script>
    // Double tap to like
    function handleDoubleTap(container, imageId) {
        const anim = document.getElementById('heart-anim-' + imageId);
        if (anim) {
            anim.style.animation = 'none';
            anim.offsetHeight; // trigger reflow
            anim.style.animation = 'floatUp 0.8s ease-out forwards';
        }
        const likeBtn = document.getElementById('like-btn-' + imageId);
        if (likeBtn && !likeBtn.classList.contains('liked')) {
            toggleLike(imageId);
        }
    }

    // Toggle Like
    function toggleLike(imageId) {
        const btn = document.getElementById('like-btn-' + imageId);
        const countSpan = document.getElementById('like-count-' + imageId);
        if (!btn || !countSpan) return;

        let count = parseInt(countSpan.textContent) || 0;
        if (btn.classList.contains('liked')) {
            btn.classList.remove('liked');
            countSpan.textContent = Math.max(0, count - 1);
        } else {
            btn.classList.add('liked');
            countSpan.textContent = count + 1;
        }
    }

    // Toggle Comments view
    function toggleComments(imageId) {
        const sec = document.getElementById('comments-section-' + imageId);
        if (sec) {
            sec.style.display = (sec.style.display === 'none') ? 'block' : 'none';
        }
    }

    // Submit live comment safely without innerHTML
    function submitComment(imageId) {
        const input = document.getElementById('comment-input-' + imageId);
        const list = document.getElementById('dynamic-comments-' + imageId);
        if (!input || !list) return;

        const text = input.value.trim();
        if (!text) return;

        const commentItem = document.createElement('div');
        commentItem.className = 'comment-item';

        const bubble = document.createElement('div');
        bubble.className = 'comment-bubble';
        bubble.style.background = 'var(--surface)';
        bubble.style.borderColor = 'var(--accent)';

        const userSpan = document.createElement('span');
        userSpan.className = 'comment-user';
        userSpan.textContent = 'You';

        const textSpan = document.createElement('span');
        textSpan.textContent = text;

        bubble.appendChild(userSpan);
        bubble.appendChild(document.createTextNode(' '));
        bubble.appendChild(textSpan);
        commentItem.appendChild(bubble);
        list.appendChild(commentItem);
        input.value = '';
    }

    // Share link
    function sharePost(photographerId) {
        const url = window.location.origin + '${pageContext.request.contextPath}/photographers/' + photographerId;
        if (navigator.clipboard) {
            navigator.clipboard.writeText(url).then(() => {
                alert('Copied photographer profile link to clipboard: ' + url);
            });
        } else {
            prompt('Photographer link:', url);
        }
    }

    // Unobtrusive Event Listeners (No inline handlers)
    document.addEventListener('DOMContentLoaded', function() {
        document.addEventListener('dblclick', function(e) {
            const frame = e.target.closest('.post-media-dblclick');
            if (frame && frame.dataset.imageId) {
                handleDoubleTap(frame, frame.dataset.imageId);
            }
        });

        document.addEventListener('click', function(e) {
            const likeBtn = e.target.closest('.action-toggle-like');
            if (likeBtn && likeBtn.dataset.imageId) {
                toggleLike(likeBtn.dataset.imageId);
                return;
            }

            const commentToggleBtn = e.target.closest('.action-toggle-comments');
            if (commentToggleBtn && commentToggleBtn.dataset.imageId) {
                toggleComments(commentToggleBtn.dataset.imageId);
                return;
            }

            const shareBtn = e.target.closest('.action-share-post');
            if (shareBtn && shareBtn.dataset.photographerId) {
                sharePost(shareBtn.dataset.photographerId);
                return;
            }

            const submitBtn = e.target.closest('.action-submit-comment');
            if (submitBtn && submitBtn.dataset.imageId) {
                submitComment(submitBtn.dataset.imageId);
                return;
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                const input = e.target.closest('.comment-input-field');
                if (input && input.dataset.imageId) {
                    e.preventDefault();
                    submitComment(input.dataset.imageId);
                }
            }
        });
    });
</script>
