<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head><title>Photographer Approvals — Manager</title></head>
<main class="pc-page"><div class="editorial-container pc-container-admin">
<jsp:include page="fragments/manager-navbar.jsp"/><h1 class="editorial-title">Photographer Approvals</h1><jsp:include page="fragments/manager-messages.jsp"/>
<form method="get" class="admin-filter-panel"><label for="status">Status</label><select id="status" name="status" class="form-select"><option value="ALL">All Statuses</option><c:forEach var="s" items="${statuses}"><option value="${s}" ${selectedStatus == s.name() ? 'selected' : ''}>${s}</option></c:forEach></select><button class="btn btn-primary">Filter</button></form>
<div class="admin-table-shell"><table class="admin-table"><thead><tr><th>ID</th><th>Applicant</th><th>Location</th><th>Status</th><th>Submitted</th><th>Actions</th></tr></thead><tbody>
<c:forEach var="p" items="${profiles}"><tr><td>#${p.id}</td><td><c:out value="${p.displayName}"/><br/><small><c:out value="${p.user.email}"/></small></td><td><c:out value="${p.city}"/></td><td><c:out value="${p.verificationStatus}"/></td><td><c:out value="${p.createdAt}"/></td><td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/manager/photographers/${p.id}">Review</a></td></tr></c:forEach>
<c:if test="${empty profiles}"><tr><td colspan="6" class="text-muted">No matching applications found.</td></tr></c:if></tbody></table></div>
<nav class="d-flex justify-content-between mt-3" aria-label="Pagination"><c:if test="${currentPage > 0}"><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/manager/photographers?status=${selectedStatus}&page=${currentPage - 1}">Previous</a></c:if><span>Page ${currentPage + 1} of ${totalPages}</span><c:if test="${currentPage + 1 < totalPages}"><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/manager/photographers?status=${selectedStatus}&page=${currentPage + 1}">Next</a></c:if></nav>
</div></main>
