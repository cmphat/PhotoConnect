<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="nav-container">
    <div class="nav-left">
        <a class="nav-brand" href="/">PhotoConnect</a>
        <nav class="nav-links">
            <a class="nav-link" href="/">Home</a>
            <a class="nav-link" href="/photographers">Explore</a>
            <c:if test="${not empty sessionScope.userId}">
                <c:choose>
                    <c:when test="${sessionScope.userRole == 'ADMIN'}">
                        <a class="nav-link" href="/admin/dashboard">Admin</a>
                    </c:when>
                    <c:when test="${sessionScope.userRole == 'PHOTOGRAPHER'}">
                        <a class="nav-link" href="/photographer/portfolio">Portfolio</a>
                        <a class="nav-link" href="/photographer/schedule">Schedule</a>
                        <a class="nav-link" href="/photographer/bookings">Booking Requests</a>
                        <a class="nav-link" href="/photographer/onboarding-status">Status</a>
                    </c:when>
                    <c:otherwise>
                        <a class="nav-link" href="/become-photographer">Join Roster</a>
                        <a class="nav-link" href="/bookings">My Bookings</a>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </nav>
    </div>
    <div class="nav-right">
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <span class="nav-user" style="margin-right: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">Account</span>
                <form action="/logout" method="post" style="display:inline; margin:0; padding:0;">
                    <button type="submit" class="nav-link" style="background:none; border:none; padding:0; cursor:pointer; font-family:var(--font-primary);">Sign Out</button>
                </form>
            </c:when>
            <c:otherwise>
                <a class="nav-link" href="/login">Sign In</a>
                <a class="primary-link" href="/register">Join PhotoConnect</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>
