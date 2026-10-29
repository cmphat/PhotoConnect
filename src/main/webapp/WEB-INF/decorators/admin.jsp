<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <c:if test="${not empty _csrf}">
        <meta name="csrf-token" content="${_csrf.token}">
        <meta name="csrf-header" content="${_csrf.headerName}">
    </c:if>
    <title><sitemesh:write property='title'>Admin Console — PhotoConnect</sitemesh:write></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <sitemesh:write property='head'/>
</head>
<body class="admin-layout">
    <jsp:include page="/WEB-INF/views/fragments/navbar.jsp" />
<aside class="admin-sidebar" id="adminSidebar" aria-label="Admin Navigation">
    <a class="admin-brand" href="${pageContext.request.contextPath}/admin/dashboard" title="PhotoConnect Admin">
        <span class="admin-brand-mark">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="14.31" y1="8" x2="20.05" y2="17.94"></line>
                <line x1="9.69" y1="8" x2="21.17" y2="8"></line>
                <line x1="7.38" y1="12" x2="13.12" y2="2.06"></line>
                <line x1="9.69" y1="16" x2="3.95" y2="6.06"></line>
                <line x1="14.31" y1="16" x2="2.83" y2="16"></line>
                <line x1="16.62" y1="12" x2="10.88" y2="21.94"></line>
            </svg>
        </span>
        <span class="admin-brand-copy"><strong>PhotoConnect</strong><small>Admin Console</small></span>
    </a>
    <nav class="admin-sidebar-nav">
        <span class="admin-nav-label">Platform Operations</span>
        <a class="admin-nav-item ${activeTab == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/dashboard" title="System Dashboard">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
            </span>
            <span>Dashboard</span>
        </a>
        <a class="admin-nav-item" href="${pageContext.request.contextPath}/admin/dashboard#analytics" title="Platform Analytics">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>
            </span>
            <span>Analytics</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'users' && selectedRole != 'MANAGER' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users" title="User Management">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            </span>
            <span>Users &amp; Roles</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'photographers' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/photographers?status=ALL" title="Photographer Directory">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
            </span>
            <span>Photographers</span>
        </a>
        <a class="admin-nav-item ${selectedRole == 'MANAGER' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users?role=MANAGER" title="Manager Staff Access">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
            </span>
            <span>Manager Access</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'bookings' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/bookings" title="Booking Monitor">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            </span>
            <span>Bookings</span>
        </a>

        <span class="admin-nav-label">Finance &amp; Governance</span>
        <a class="admin-nav-item ${activeTab == 'payments' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/payments" title="Escrow Deposits">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
            </span>
            <span>Escrow Deposits</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'platform-fees' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/platform-fees" title="Platform Fees &amp; Ledger">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
            </span>
            <span>Platform Fees</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'reviews' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews" title="Reviews &amp; Safety Moderation">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
            </span>
            <span>Trust &amp; Safety</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'audit' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/audit" title="Operational Audit Logs">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
            </span>
            <span>Audit Logs</span>
        </a>
        <a class="admin-nav-item ${activeTab == 'settings' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/settings" title="System Settings">
            <span class="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
            </span>
            <span>Settings</span>
        </a>
    </nav>
    <div class="admin-sidebar-footer">
        <span class="admin-avatar">AD</span>
        <div class="admin-profile-copy">
            <strong><c:out value="${sessionScope.userFullName != null ? sessionScope.userFullName : 'Administrator'}"/></strong>
            <small>Super Administrator</small>
        </div>
    </div>
</aside>

<div class="admin-workspace">
    <header class="admin-topbar">
        <div class="admin-topbar-left">
            <button class="admin-icon-button" id="adminSidebarToggle" type="button" aria-label="Toggle navigation menu" title="Toggle Sidebar">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
            </button>
            <form class="admin-global-search" action="${pageContext.request.contextPath}/admin/users" method="get" role="search">
                <span aria-hidden="true">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                </span>
                <input name="search" type="search" placeholder="Search users, email, ID… (Ctrl + K)" aria-label="Search users and resources">
            </form>
        </div>
        <div class="admin-topbar-actions">
            <a class="admin-icon-button" href="${pageContext.request.contextPath}/admin/reviews?tab=reports" aria-label="Disputes &amp; Reports" title="Disputes &amp; Safety Reports">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
            </a>
            <button class="admin-icon-button" id="adminThemeToggle" type="button" aria-label="Toggle color scheme" title="Toggle Light / Dark Mode">
                <svg id="themeIconSun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>
            </button>
            <form action="${pageContext.request.contextPath}/logout" method="post" style="margin: 0;">
                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"></c:if>
                <button class="admin-signout-btn" type="submit">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                    Sign Out
                </button>
            </form>
        </div>
    </header>
    <div class="admin-content">
        <sitemesh:write property='body'/>
    </div>
</div>

<script>
(() => {
    const body = document.body;
    const collapseKey = 'pc-admin-sidebar-collapsed';
    const themeKey = 'pc-admin-theme';
    if (localStorage.getItem(collapseKey) === 'true') body.classList.add('admin-sidebar-collapsed');
    if (localStorage.getItem(themeKey) === 'dark') body.classList.add('admin-dark');

    document.getElementById('adminSidebarToggle')?.addEventListener('click', () => {
        if (window.innerWidth < 992) {
            body.classList.toggle('admin-sidebar-open');
        } else {
            body.classList.toggle('admin-sidebar-collapsed');
            localStorage.setItem(collapseKey, String(body.classList.contains('admin-sidebar-collapsed')));
        }
    });

    document.getElementById('adminThemeToggle')?.addEventListener('click', () => {
        body.classList.toggle('admin-dark');
        localStorage.setItem(themeKey, body.classList.contains('admin-dark') ? 'dark' : 'light');
    });

    // Global keyboard shortcut: Ctrl+K or / focuses search
    window.addEventListener('keydown', (e) => {
        if ((e.ctrlKey && e.key === 'k') || (e.key === '/' && document.activeElement.tagName !== 'INPUT')) {
            e.preventDefault();
            document.querySelector('.admin-global-search input')?.focus();
        }
    });
})();
</script>
</body>
</html>
