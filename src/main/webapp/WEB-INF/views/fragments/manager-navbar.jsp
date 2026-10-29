<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="admin-subnav" aria-label="Staff Operations">
    <a class="admin-subnav-link ${activeTab == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/dashboard">Dashboard</a>
    <a class="admin-subnav-link ${activeTab == 'photographers' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/photographers?status=PENDING">Approvals</a>
    <a class="admin-subnav-link ${activeTab == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/reports">Safety Reports</a>
    <a class="admin-subnav-link ${activeTab == 'bookings' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/bookings?status=DISPUTED">Disputes</a>
    <a class="admin-subnav-link ${activeTab == 'support' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/support">Support Tickets</a>
</nav>
