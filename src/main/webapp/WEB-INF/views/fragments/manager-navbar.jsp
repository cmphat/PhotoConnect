<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="admin-subnav" aria-label="Điều hành Manager">
    <a class="${activeTab == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/dashboard">Tổng quan</a>
    <a class="${activeTab == 'photographers' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/photographers?status=PENDING">Duyệt nhiếp ảnh gia</a>
    <a class="${activeTab == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/reports">Báo cáo vi phạm</a>
    <a class="${activeTab == 'bookings' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/bookings?status=DISPUTED">Tranh chấp</a>
    <a class="${activeTab == 'support' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/support">Yêu cầu hỗ trợ</a>
    <a class="${activeTab == 'audit' ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/audit">Nhật ký hoạt động</a>
</nav>
