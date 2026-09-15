<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<style>
.admin-subnav {
    display: flex;
    gap: 2rem;
    margin-bottom: 2.5rem;
    border-bottom: 1px solid var(--border-dark);
    padding-bottom: 1rem;
    overflow-x: auto;
}
.admin-subnav-link {
    color: var(--text-muted);
    font-weight: 400;
    text-transform: uppercase;
    font-size: 0.85rem;
    letter-spacing: 0.05em;
    padding-bottom: 0.5rem;
    border-bottom: 2px solid transparent;
    text-decoration: none;
    transition: color 0.2s ease, border-color 0.2s ease;
}
.admin-subnav-link:hover {
    color: var(--text-on-dark);
}
.admin-subnav-link.active {
    color: var(--text-on-dark);
    font-weight: 600;
    border-bottom: 2px solid var(--text-on-dark);
}
</style>
<div class="admin-subnav">
    <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-subnav-link <c:if test="${activeTab == 'dashboard'}">active</c:if>">Dashboard</a>
    <a href="${pageContext.request.contextPath}/admin/users" class="admin-subnav-link <c:if test="${activeTab == 'users'}">active</c:if>">Users</a>
    <a href="${pageContext.request.contextPath}/admin/photographers" class="admin-subnav-link <c:if test="${activeTab == 'photographers'}">active</c:if>">Photographers</a>
    <a href="${pageContext.request.contextPath}/admin/bookings" class="admin-subnav-link <c:if test="${activeTab == 'bookings'}">active</c:if>">Bookings</a>
    <a href="${pageContext.request.contextPath}/admin/reviews" class="admin-subnav-link <c:if test="${activeTab == 'reviews'}">active</c:if>">Reviews</a>
</div>
