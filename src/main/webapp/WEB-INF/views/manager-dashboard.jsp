<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head><title>Trung tâm điều hành — PhotoConnect</title></head>
<main class="pc-page"><div class="editorial-container pc-container-admin">
    <jsp:include page="fragments/manager-navbar.jsp"/>
    <div class="pc-page-header"><div><span class="section-index">Operations</span><h1 class="editorial-title">Trung tâm điều hành</h1><p class="text-muted">Công việc cần xử lý dựa trên dữ liệu hiện tại.</p></div></div>
    <div class="row g-3 mb-4">
        <div class="col-6 col-lg"><a class="pc-card d-block h-100" href="${pageContext.request.contextPath}/manager/photographers?status=PENDING"><strong>${stats.pendingPhotographerApplications}</strong><br/>Hồ sơ chờ duyệt</a></div>
        <div class="col-6 col-lg"><a class="pc-card d-block h-100" href="${pageContext.request.contextPath}/manager/reports"><strong>${stats.openReports}</strong><br/>Báo cáo đang mở</a></div>
        <div class="col-6 col-lg"><a class="pc-card d-block h-100" href="${pageContext.request.contextPath}/manager/bookings?status=DISPUTED"><strong>${stats.unresolvedDisputes}</strong><br/>Tranh chấp</a></div>
        <div class="col-6 col-lg"><a class="pc-card d-block h-100" href="${pageContext.request.contextPath}/manager/support"><strong>${stats.openSupportTickets}</strong><br/>Yêu cầu hỗ trợ</a></div>
        <div class="col-6 col-lg"><div class="pc-card h-100"><strong>${stats.totalBookings}</strong><br/>Tổng booking</div></div>
    </div>
    <h2 class="editorial-heading h4">Hoạt động gần đây</h2>
    <div class="admin-table-shell"><table class="admin-table"><thead><tr><th>Thời gian</th><th>Người thực hiện</th><th>Hành động</th><th>Đối tượng</th><th>Kết quả</th></tr></thead><tbody>
    <c:forEach var="log" items="${activity}"><tr><td><c:out value="${log.createdAt}"/></td><td><c:out value="${log.actor.fullName}"/></td><td><c:out value="${log.action}"/></td><td><c:out value="${log.targetType}"/> #<c:out value="${log.targetId}"/></td><td><c:out value="${log.outcome}"/></td></tr></c:forEach>
    <c:if test="${empty activity}"><tr><td colspan="5" class="text-muted">Chưa có hoạt động điều hành.</td></tr></c:if>
    </tbody></table></div>
</div></main>
