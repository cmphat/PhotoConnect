<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Application Detail – Admin – PhotoConnect</title>
</head>

    <main class="pc-page">
        <div class="editorial-container pc-container-narrow">
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/admin/photographers" class="text-link" style="color: var(--text-muted); font-size: 0.9rem;">← Back to Applications</a>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="pc-alert pc-alert-success" role="status">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty profile}">
                    <div class="pc-panel">
                        <div class="pc-page-header pc-stack-mobile">
                            <h1 class="editorial-title" style="margin-bottom: 0; font-size: 2.5rem;"><c:out value="${profile.displayName}"/></h1>
                            <span class="status-badge ${profile.verificationStatus}">
                                <c:out value="${profile.verificationStatus}"/>
                            </span>
                        </div>

                        <table class="detail-table">
                            <tbody>
                                <tr>
                                    <th>Account Email</th>
                                    <td><c:out value="${profile.user.email}"/></td>
                                </tr>
                                <tr>
                                    <th>Full Name</th>
                                    <td><c:out value="${profile.user.fullName}"/></td>
                                </tr>
                                <tr>
                                    <th>City</th>
                                    <td><c:out value="${profile.city}"/></td>
                                </tr>
                                <tr>
                                    <th>Experience</th>
                                    <td><c:out value="${profile.experienceYears}"/> years</td>
                                </tr>
                                <tr>
                                    <th>Starting Rate</th>
                                    <td><fmt:formatNumber value="${profile.priceFrom}" pattern="#,##0" /> VND</td>
                                </tr>
                                <tr>
                                    <th>Submitted</th>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty profile.createdAt}">
                                                <fmt:parseDate value="${fn:substring(profile.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedProfileDate" type="date" />
                                                <fmt:formatDate value="${parsedProfileDate}" pattern="MMM d, yyyy" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </tbody>
                        </table>

                        <h2 class="editorial-heading" style="font-size: 1.25rem; margin-top: 3rem; margin-bottom: 1.5rem;">Bio</h2>
                        <div style="color: var(--text-muted); line-height: 1.7; margin-bottom: 4rem;">
                            <c:out value="${profile.bio}"/>
                        </div>

                        <c:if test="${profile.verificationStatus == 'PENDING'}">
                            <div class="row g-4" style="border-top: 1px solid var(--border); padding-top: 2rem;">
                                <div class="col-md-6">
                                    <div class="p-3 border rounded-3 bg-light">
                                        <h3 class="h6 fw-bold mb-2 text-success">Phê duyệt hồ sơ</h3>
                                        <p class="small text-muted mb-3">Hồ sơ sẽ được hiển thị công khai trên marketplace và có thể nhận lịch đặt.</p>
                                        <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/approve" method="post" style="margin: 0;">
                                            <%@ include file="fragments/csrf-input.jsp" %>
                                            <div class="mb-2">
                                                <input type="text" name="adminNotes" class="form-control form-control-sm" placeholder="Ghi chú nội bộ admin (tùy chọn)">
                                            </div>
                                            <button type="submit" class="btn btn-primary btn-sm" onclick="return confirm('Xác nhận phê duyệt hồ sơ nhiếp ảnh gia này?')">
                                                Phê duyệt hồ sơ
                                            </button>
                                        </form>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 border rounded-3 bg-light">
                                        <h3 class="h6 fw-bold mb-2 text-danger">Từ chối hồ sơ</h3>
                                        <p class="small text-muted mb-3">Nhiếp ảnh gia sẽ nhận được lý do từ chối và có thể cập nhật để nộp lại.</p>
                                        <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/reject" method="post" style="margin: 0;">
                                            <%@ include file="fragments/csrf-input.jsp" %>
                                            <div class="mb-2">
                                                <textarea name="rejectionReason" class="form-control form-control-sm" rows="2" placeholder="Lý do từ chối (gửi tới người nộp)" required></textarea>
                                            </div>
                                            <div class="mb-2">
                                                <input type="text" name="adminNotes" class="form-control form-control-sm" placeholder="Ghi chú nội bộ admin (tùy chọn)">
                                            </div>
                                            <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Xác nhận từ chối hồ sơ này?')">
                                                Từ chối hồ sơ
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <c:if test="${profile.verificationStatus != 'PENDING'}">
                            <div class="border-top pt-4 mt-3" style="font-size: 0.9rem;">
                                <div class="d-flex align-items-center gap-2 mb-2">
                                    <span class="text-muted">Trạng thái xử lý:</span>
                                    <span class="status-badge ${profile.verificationStatus}"><c:out value="${profile.verificationStatus}"/></span>
                                </div>
                                <c:if test="${not empty profile.reviewedAt}">
                                    <div class="text-muted small mb-1">
                                        Thời gian duyệt: <fmt:parseDate value="${fn:substring(profile.reviewedAt, 0, 19)}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedReviewDate" type="both" />
                                        <fmt:formatDate value="${parsedReviewDate}" pattern="dd/MM/yyyy HH:mm" />
                                        <c:if test="${not empty profile.reviewedByAdminId}">
                                            &middot; Duyệt bởi Admin ID #${profile.reviewedByAdminId}
                                        </c:if>
                                    </div>
                                </c:if>
                                <c:if test="${not empty profile.rejectionReason}">
                                    <div class="alert alert-warning pc-alert pc-alert-warning mt-2 mb-2">
                                        <strong>Lý do từ chối:</strong> <c:out value="${profile.rejectionReason}"/>
                                    </div>
                                </c:if>
                                <c:if test="${not empty profile.adminNotes}">
                                    <div class="text-muted small">
                                        <em>Ghi chú nội bộ:</em> <c:out value="${profile.adminNotes}"/>
                                    </div>
                                </c:if>
                                <c:if test="${profile.verificationStatus == 'APPROVED'}">
                                    <div class="p-3 border rounded-3 bg-light mt-4">
                                        <h3 class="h6 fw-bold mb-2 text-warning">Tạm ngưng hoạt động nhiếp ảnh gia</h3>
                                        <p class="small text-muted mb-3">Tạm ngưng hiển thị hồ sơ và khả năng nhận booking khi có vi phạm chính sách hoặc sau khi điều tra báo cáo.</p>
                                        <form action="${pageContext.request.contextPath}/admin/photographers/${profile.id}/suspend" method="post" style="margin: 0;">
                                            <%@ include file="fragments/csrf-input.jsp" %>
                                            <div class="mb-2">
                                                <textarea name="suspensionReason" class="form-control form-control-sm" rows="2" placeholder="Lý do tạm ngưng (bắt buộc)" required></textarea>
                                            </div>
                                            <div class="mb-2">
                                                <input type="text" name="adminNotes" class="form-control form-control-sm" placeholder="Ghi chú nội bộ admin (tùy chọn)">
                                            </div>
                                            <button type="submit" class="btn btn-warning btn-sm" onclick="return confirm('Xác nhận tạm ngưng hoạt động của nhiếp ảnh gia này?')">
                                                Đình chỉ hoạt động
                                            </button>
                                        </form>
                                    </div>
                                </c:if>
                            </div>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem; color: #ef4444;">Application not found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">The requested photographer profile does not exist.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
