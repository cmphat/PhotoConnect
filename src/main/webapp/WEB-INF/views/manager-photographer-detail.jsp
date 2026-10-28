<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head><title>Hồ sơ nhiếp ảnh gia — Manager</title></head>
<main class="pc-page"><div class="editorial-container pc-container-admin"><jsp:include page="fragments/manager-navbar.jsp"/><jsp:include page="fragments/manager-messages.jsp"/>
<a href="${pageContext.request.contextPath}/manager/photographers">← Danh sách</a><h1 class="editorial-title"><c:out value="${profile.displayName}"/></h1>
<div class="pc-card mb-3"><p><strong>Email:</strong> <c:out value="${profile.user.email}"/></p><p><strong>Thành phố:</strong> <c:out value="${profile.city}"/></p><p><strong>Kinh nghiệm:</strong> <c:out value="${profile.experienceYears}"/> năm</p><p><strong>Chuyên môn:</strong> <c:out value="${profile.specialties}"/></p><p><strong>Thiết bị:</strong> <c:out value="${profile.equipmentSummary}"/></p><p><strong>Giới thiệu:</strong> <c:out value="${profile.bio}"/></p><p><strong>Trạng thái:</strong> <c:out value="${profile.verificationStatus}"/></p><c:if test="${not empty profile.rejectionReason}"><p><strong>Lý do trước:</strong> <c:out value="${profile.rejectionReason}"/></p></c:if></div>
<c:if test="${profile.verificationStatus == 'PENDING'}">
<div class="pc-card mb-3 border border-info">
  <h2 class="h5 fw-bold">AI hỗ trợ xem xét hồ sơ</h2>
  <p class="text-muted small mb-2">Chỉ kiểm tra mức độ đầy đủ của thông tin nghề nghiệp. Không xác minh danh tính hay tự động phê duyệt/từ chối. Manager chịu trách nhiệm quyết định cuối cùng.</p>
  <form method="post" action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/ai-review">
    <%@ include file="fragments/csrf-input.jsp" %>
    <button type="submit" class="btn btn-outline-info">Phân tích hồ sơ</button>
  </form>
  <c:if test="${not empty aiAdvice}">
    <div class="mt-3" role="status">
      <strong>Độ đầy đủ thông tin: <c:out value="${aiAdvice.completed}"/>/<c:out value="${aiAdvice.total}"/> mục</strong>
      <p class="mb-1"><strong>Gợi ý bước tiếp theo:</strong> <c:out value="${aiAdvice.suggestedAction}"/></p>
      <p class="small text-muted mt-1"><c:out value="${aiAdvice.aiStatus}"/></p>
      <c:if test="${aiAdvice.aiGenerated}">
        <div class="alert alert-info"><strong>Nhận xét AI:</strong> <c:out value="${aiAdvice.aiSummary}"/></div>
      </c:if>
      <div class="row g-2">
        <div class="col-md-6">
          <h3 class="h6">Đã khai báo</h3>
          <ul><c:forEach var="item" items="${aiAdvice.ready}"><li><c:out value="${item}"/></li></c:forEach></ul>
        </div>
        <div class="col-md-6">
          <h3 class="h6">Cần bổ sung / xác minh</h3>
          <ul><c:forEach var="item" items="${aiAdvice.missing}"><li><c:out value="${item}"/></li></c:forEach></ul>
        </div>
      </div>
    </div>
  </c:if>
</div>
</c:if>
<c:if test="${profile.verificationStatus == 'PENDING'}"><div class="row g-3"><div class="col-md-6"><form method="post" action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/approve" class="pc-card"><%@ include file="fragments/csrf-input.jsp" %><label for="approveNotes">Ghi chú nội bộ</label><textarea id="approveNotes" name="notes" class="form-control" maxlength="1000"></textarea><button class="btn btn-primary mt-2">Phê duyệt</button></form></div><div class="col-md-6"><form method="post" action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/reject" class="pc-card"><%@ include file="fragments/csrf-input.jsp" %><label for="reason">Lý do từ chối</label><textarea id="reason" name="reason" required maxlength="1000" class="form-control"></textarea><label for="rejectNotes">Ghi chú nội bộ</label><textarea id="rejectNotes" name="notes" maxlength="1000" class="form-control"></textarea><button class="btn btn-danger mt-2">Từ chối</button></form></div></div></c:if>
<c:if test="${profile.verificationStatus == 'APPROVED'}"><div class="row g-3 mt-1"><div class="col-md-12"><form method="post" action="${pageContext.request.contextPath}/manager/photographers/${profile.id}/suspend" class="pc-card"><%@ include file="fragments/csrf-input.jsp" %><h3 class="h6 fw-bold mb-2 text-warning">Tạm ngưng hoạt động</h3><label for="suspendReason">Lý do đình chỉ (bắt buộc)</label><textarea id="suspendReason" name="reason" required maxlength="1000" class="form-control mb-2" placeholder="Ví dụ: Vi phạm quy tắc cộng đồng hoặc có báo cáo tiêu cực được xác nhận..."></textarea><label for="suspendNotes">Ghi chú nội bộ</label><textarea id="suspendNotes" name="notes" maxlength="1000" class="form-control mb-2"></textarea><button class="btn btn-warning" onclick="return confirm('Xác nhận tạm ngưng hoạt động của nhiếp ảnh gia này?')">Đình chỉ hồ sơ</button></form></div></div></c:if>
</div></main>
