<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!-- Bootstrap 5.3.3 JS Bundle (includes Popper) -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function() {
        try {
            var saved = localStorage.getItem('pc-theme');
            var theme = saved || ((window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'light');
            document.documentElement.setAttribute('data-theme', theme);
            document.documentElement.setAttribute('data-bs-theme', theme);
        } catch(e) {}
    })();
</script>
<header class="nav-container" id="site-header">
    <div class="nav-left">
        <a class="nav-brand" href="${pageContext.request.contextPath}/" aria-label="PhotoConnect Home">
            PhotoConnect
        </a>
        <button type="button" class="nav-menu-toggle btn btn-secondary btn-sm" id="navToggleBtn" aria-label="Toggle navigation" aria-expanded="false" data-bs-toggle="collapse" data-bs-target="#navLinksMenu" aria-controls="navLinksMenu">
            Menu ▾
        </button>
        <nav class="nav-links" id="navLinksMenu" aria-label="Primary navigation">
            <c:choose>
                <c:when test="${not empty sessionScope.userId}">
                    <c:choose>
                    <c:when test="${sessionScope.userRole == 'ADMIN'}">
                        <a class="nav-link" href="${pageContext.request.contextPath}/">Home</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/photographers">Explore</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/dashboard">Admin</a>
                    </c:when>
                    <c:when test="${sessionScope.userRole == 'PHOTOGRAPHER'}">
                        <a class="nav-link" href="${pageContext.request.contextPath}/photographer/dashboard">Studio</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/photographer/bookings">Bookings</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/photographer/portfolio">Portfolio</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/photographer/schedule">Availability</a>
                    </c:when>
                    <c:otherwise>
                        <a class="nav-link" href="${pageContext.request.contextPath}/customer/dashboard">Dashboard</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/photographers">Explore</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/customer/saved-photographers">Saved</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/bookings">Bookings</a>
                    </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <a class="nav-link" href="${pageContext.request.contextPath}/">Home</a>
                    <a class="nav-link" href="${pageContext.request.contextPath}/photographers">Explore</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
    <div class="nav-right" id="navRightSection">
        <button type="button" class="theme-toggle-btn btn btn-ghost" id="themeToggleBtn" aria-label="Toggle visual theme" title="Toggle theme">
            <!-- Sun icon for switching to light mode -->
            <svg class="theme-icon-sun" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="5"></circle>
                <line x1="12" y1="21" x2="12" y2="23"></line>
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
                <line x1="1" y1="12" x2="3" y2="12"></line>
                <line x1="21" y1="12" x2="23" y2="12"></line>
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
            </svg>
            <!-- Moon icon for switching to dark mode -->
            <svg class="theme-icon-moon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
        </button>
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <span class="nav-user">Account</span>
                <a class="nav-link" href="${pageContext.request.contextPath}/auth/google/link">Link Google</a>
                <form action="${pageContext.request.contextPath}/logout" method="post" class="nav-form">
                    <%@ include file="csrf-input.jsp" %>
                    <button type="submit" class="nav-link nav-action">Sign Out</button>
                </form>
            </c:when>
            <c:otherwise>
                <a class="nav-link" href="${pageContext.request.contextPath}/login">Sign In</a>
                <a class="primary-link" href="${pageContext.request.contextPath}/register">Join PhotoConnect</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<script>
    (function() {
        const toggleBtn = document.getElementById('navToggleBtn');
        const navLinks = document.getElementById('navLinksMenu');
        const navRight = document.getElementById('navRightSection');
        if (toggleBtn && navLinks) {
            toggleBtn.addEventListener('click', function() {
                const isOpen = navLinks.classList.toggle('is-open');
                if (navRight) navRight.classList.toggle('is-open', isOpen);
                toggleBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
                toggleBtn.textContent = isOpen ? 'Close ✕' : 'Menu ▾';
            });
        }

        const themeBtn = document.getElementById('themeToggleBtn');
        if (themeBtn) {
            function updateThemeBtn() {
                const current = document.documentElement.getAttribute('data-theme') || 'light';
                themeBtn.setAttribute('aria-label', current === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
                themeBtn.setAttribute('title', current === 'dark' ? 'Light mode' : 'Dark mode');
            }
            updateThemeBtn();
            themeBtn.addEventListener('click', function() {
                const current = document.documentElement.getAttribute('data-theme') || 'light';
                const next = current === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-theme', next);
                document.documentElement.setAttribute('data-bs-theme', next);
                try { localStorage.setItem('pc-theme', next); } catch(e) {}
                updateThemeBtn();
            });
        }
    })();
</script>
