<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head><title>Duyệt nhiếp ảnh gia — Manager</title></head>
<main class="pc-page"><div class="editorial-container pc-container-admin">
<jsp:include page="fragments/manager-navbar.jsp"/><h1 class="editorial-title">Duyệt nhiếp ảnh gia</h1><jsp:include page="fragments/manager-messages.jsp"/>
<form method="get" class="admin-filter-panel"><label for="status">Trạng thái</label><select id="status" name="status" class="form-select"><option value="ALL">Tất cả</option><c:forEach var="s" items="${statuses}"><option value="${s}" ${selectedStatus == s.name() ? 'selected' : ''}>${s}</option></c:forEach></select><button class="btn btn-primary">Lọc</button></form>
<div class="admin-table-shell"><table class="admin-table"><thead><tr><th>ID</th><th>Ứng viên</th><th>Địa điểm</th><th>Trạng thái</th><th>Gửi lúc</th><th></th></tr></thead><tbody>
<c:forEach var="p" items="${profiles}"><tr><td>#${p.id}</td><td><c:out value="${p.displayName}"/><br/><small><c:out value="${p.user.email}"/></small></td><td><c:out value="${p.city}"/></td><td><c:out value="${p.verificationStatus}"/></td><td><c:out value="${p.createdAt}"/></td><td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/manager/photographers/${p.id}">Xem</a></td></tr></c:forEach>
<c:if test="${empty profiles}"><tr><td colspan="6" class="text-muted">Không có hồ sơ phù hợp.</td></tr></c:if></tbody></table></div>
<nav class="d-flex justify-content-between mt-3" aria-label="Phân trang"><c:if test="${currentPage > 0}"><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/manager/photographers?status=${selectedStatus}&page=${currentPage - 1}">Trang trước</a></c:if><span>Trang ${currentPage + 1}/${totalPages}</span><c:if test="${currentPage + 1 < totalPages}"><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/manager/photographers?status=${selectedStatus}&page=${currentPage + 1}">Trang sau</a></c:if></nav>
</div></main>
