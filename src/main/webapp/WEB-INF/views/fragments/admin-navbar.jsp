<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="admin-subnav" aria-label="Administration sections">
    <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-subnav-link <c:if test="${activeTab == 'dashboard'}">active</c:if>">Dashboard</a>
    <a href="${pageContext.request.contextPath}/admin/users" class="admin-subnav-link <c:if test="${activeTab == 'users'}">active</c:if>">Users</a>
    <a href="${pageContext.request.contextPath}/admin/photographers" class="admin-subnav-link <c:if test="${activeTab == 'photographers'}">active</c:if>">Photographers</a>
    <a href="${pageContext.request.contextPath}/admin/bookings" class="admin-subnav-link <c:if test="${activeTab == 'bookings'}">active</c:if>">Bookings</a>
    <a href="${pageContext.request.contextPath}/admin/reviews" class="admin-subnav-link <c:if test="${activeTab == 'reviews'}">active</c:if>">Reviews</a>
</nav>
