<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Book <c:out value="${photographer.displayName}"/> – PhotoConnect</title>
    <meta name="description" content="Request a photography booking on PhotoConnect.">
</head>

    <main class="pc-page pc-page-compact">
        <div class="editorial-container">

            <!-- Breadcrumb Navigation -->
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/photographers/${photographer.id}" class="text-link" style="font-size:0.88rem;">← Back to Profile</a>
            </div>

            <div class="pc-booking-layout">

                <!-- Left: Photographer Summary -->
                <div>
                    <div style="position: sticky; top: 120px;">
                        <div class="photo-frame" style="aspect-ratio: 4/5; margin-bottom: 2rem;">
                            <c:choose>
                                <c:when test="${not empty photographer.coverImageUrl}">
                                    <img src="<c:out value='${photographer.coverImageUrl}'/>"
                                         alt="<c:out value='${photographer.displayName}'/> cover"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="pc-card-placeholder">
                                        <div class="placeholder-initial"><c:out value="${fn:toUpperCase(fn:substring(photographer.displayName, 0, 1))}"/></div>
                                        <div class="placeholder-badge">Editorial Artist</div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
                            <h2 class="editorial-heading" style="margin: 0; font-size: 1.5rem;"><c:out value="${photographer.displayName}"/></h2>
                        </div>

                        <div style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 2rem;">
                            <c:if test="${not empty photographer.city}">
                                <span><c:out value="${photographer.city}"/></span>
                            </c:if>
                            <c:if test="${not empty photographer.experienceYears}">
                                <span style="margin-left: 0.5rem;">· <c:out value="${photographer.experienceYears}"/> yrs exp</span>
                            </c:if>
                        </div>

                        <!-- Price Snapshot Notice -->
                        <div style="border-top: 1px solid var(--border); padding-top: 1.5rem;">
                            <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Starting Rate</div>
                            <div style="font-size: 1.5rem; font-weight: 400;">
                                <c:choose>
                                    <c:when test="${not empty photographer.priceFrom}">
                                        <fmt:formatNumber value="${photographer.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                    </c:when>
                                    <c:otherwise>Rates upon agreement</c:otherwise>
                                </c:choose>
                            </div>
                            <div style="color: var(--text-muted); font-size: 0.8rem; margin-top: 0.5rem;">
                                Locked in at booking submission.
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right: Booking Form Panel -->
                <div>
                    <div style="margin-bottom: 3rem;">
                        <h1 class="editorial-title" style="margin-bottom: 1rem;">Request Shoot Booking</h1>
                        <p style="color: var(--text-muted); line-height: 1.6;">
                            Fill in your session preferences below to submit a formal booking request directly to <c:out value="${photographer.displayName}"/>.
                        </p>
                    </div>

                    <!-- Error alert -->
                    <c:if test="${not empty errorMessage}">
                        <div class="pc-alert pc-alert-danger" role="alert">
                            <c:out value="${errorMessage}"/>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/photographers/${photographer.id}/book" method="post" id="booking-form">
                        <%@ include file="fragments/csrf-input.jsp" %>

                        <!-- Service Package Selection -->
                        <c:if test="${not empty packages}">
                            <div class="form-group" style="margin-bottom: 1.5rem;">
                                <label for="packageId" class="form-label">Select Service Package</label>
                                <select id="packageId" name="packageId" class="form-input" style="padding: 0.65rem 0.85rem;">
                                    <option value="">-- Custom Shoot (Photographer Base Rate) --</option>
                                    <c:forEach var="pkg" items="${packages}">
                                        <option value="${pkg.id}" ${bookingRequest.packageId == pkg.id ? 'selected' : ''}>
                                            <c:out value="${pkg.name}"/> · <c:out value="${pkg.formattedPrice}"/> (<c:out value="${pkg.formattedDuration}"/>, <c:out value="${pkg.editedPhotoCount}"/> photos)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>

                        <!-- Date & Time Row -->
                        <div class="pc-booking-datetime-grid">
                            <div class="form-group">
                                <label for="bookingDate" class="form-label">Shoot Date *</label>
                                <input type="date"
                                       id="bookingDate"
                                       name="bookingDate"
                                       min="<c:out value='${minBookingDate}'/>"
                                       class="form-input"
                                       value="<c:out value='${bookingRequest.bookingDate}'/>"
                                       required>
                                <spring:bind path="bookingRequest.bookingDate">
                                    <c:if test="${status.error}">
                                        <div class="field-error"><c:out value="${status.errorMessage}"/></div>
                                    </c:if>
                                </spring:bind>
                            </div>

                            <div class="form-group">
                                <label for="bookingTime" class="form-label">Preferred Time *</label>
                                <input type="time"
                                       id="bookingTime"
                                       name="bookingTime"
                                       class="form-input"
                                       value="<c:out value='${bookingRequest.bookingTime}'/>"
                                       required>
                                <spring:bind path="bookingRequest.bookingTime">
                                    <c:if test="${status.error}">
                                        <div class="field-error"><c:out value="${status.errorMessage}"/></div>
                                    </c:if>
                                </spring:bind>
                            </div>
                        </div>

                        <!-- Availability Slots Feedback (Phase 6) -->
                        <div id="availability-feedback" style="display: none; margin-bottom: 2rem; padding: 1.25rem; border-radius: var(--radius-sm, 8px); background: rgba(37, 99, 235, 0.04); border: 1px solid rgba(37, 99, 235, 0.18);">
                            <div style="font-size: 0.88rem; font-weight: 600; color: #1e3a8a; margin-bottom: 0.75rem; display: flex; align-items: center; justify-content: space-between;">
                                <span><i class="bi bi-clock-history me-1"></i> Khung giờ khả dụng / Available Slots</span>
                                <span id="availability-status-badge" class="badge bg-primary" style="font-size: 0.75rem;">Đang tải...</span>
                            </div>
                            <div id="availability-slots-list" style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
                            </div>
                            <div id="availability-notice" style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem;"></div>
                        </div>

                        <!-- Location -->
                        <div class="form-group" style="margin-bottom: 2rem;">
                            <label for="location" class="form-label">Shoot Location / Address *</label>
                            <input type="text"
                                   id="location"
                                   name="location"
                                   maxlength="255"
                                   placeholder="e.g. Studio LightSpace, District 1..."
                                   class="form-input"
                                   value="<c:out value='${bookingRequest.location}'/>"
                                   required>
                            <spring:bind path="bookingRequest.location">
                                <c:if test="${status.error}">
                                    <div class="field-error"><c:out value="${status.errorMessage}"/></div>
                                </c:if>
                            </spring:bind>
                        </div>

                        <!-- Notes / Requirements -->
                        <div class="form-group" style="margin-bottom: 2rem;">
                            <label for="notes" class="form-label">Notes & Requirements (Optional)</label>
                            <textarea id="notes"
                                      name="notes"
                                      rows="3"
                                      maxlength="1000"
                                      placeholder="Share shoot concept, special requests, etc..."
                                      class="form-input"><c:out value='${bookingRequest.notes}'/></textarea>
                            <spring:bind path="bookingRequest.notes">
                                <c:if test="${status.error}">
                                    <div class="field-error"><c:out value="${status.errorMessage}"/></div>
                                </c:if>
                            </spring:bind>
                        </div>

                        <!-- Structured Shoot Brief (Phase 7) -->
                        <div class="card mb-4 border" style="background: var(--surface); border-color: var(--border);">
                            <div class="card-header bg-transparent py-3" style="cursor: pointer;" onclick="var b = document.getElementById('shootBriefBody'); b.style.display = b.style.display === 'none' ? 'block' : 'none';">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <h3 class="h6 fw-bold mb-0 text-dark"><i class="bi bi-camera me-1 text-primary"></i> Shoot Brief & Creative Vision (Optional)</h3>
                                        <small class="text-muted">Chi tiết concept, trang phục, moodboard và danh sách góc chụp mong muốn</small>
                                    </div>
                                    <span class="badge bg-light text-primary border">Chi tiết</span>
                                </div>
                            </div>
                            <div class="card-body" id="shootBriefBody">
                                <div class="form-group mb-3">
                                    <label for="conceptDescription" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Concept Description / Mô tả ý tưởng</label>
                                    <textarea id="conceptDescription" name="conceptDescription" rows="3" maxlength="1000"
                                              placeholder="Chia sẻ concept chi tiết, phong cách ánh sáng, vibe nghệ thuật..."
                                              class="form-input"><c:out value='${bookingRequest.conceptDescription}'/></textarea>
                                </div>

                                <div class="row g-3 mb-3">
                                    <div class="col-12 col-md-6">
                                        <label for="moodboardUrl" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Moodboard / Reference Link</label>
                                        <input type="url" id="moodboardUrl" name="moodboardUrl" maxlength="500"
                                               placeholder="https://pinterest.com/... hoặc link Google Drive"
                                               class="form-input" value="<c:out value='${bookingRequest.moodboardUrl}'/>">
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label for="backupLocation" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Backup Location / Địa điểm dự phòng</label>
                                        <input type="text" id="backupLocation" name="backupLocation" maxlength="255"
                                               placeholder="Phòng khi trời mưa hoặc đổi địa điểm..."
                                               class="form-input" value="<c:out value='${bookingRequest.backupLocation}'/>">
                                    </div>
                                </div>

                                <div class="row g-3 mb-3">
                                    <div class="col-12 col-md-6">
                                        <label for="outfitCount" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Number of Outfits / Số set đồ</label>
                                        <input type="number" id="outfitCount" name="outfitCount" min="1" max="20"
                                               placeholder="Ví dụ: 2 set"
                                               class="form-input" value="<c:out value='${bookingRequest.outfitCount}'/>">
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label for="participantCount" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Number of Participants / Số người chụp</label>
                                        <input type="number" id="participantCount" name="participantCount" min="1" max="50"
                                               placeholder="Ví dụ: 1 hoặc 2 người"
                                               class="form-input" value="<c:out value='${bookingRequest.participantCount}'/>">
                                    </div>
                                </div>

                                <div class="row g-3">
                                    <div class="col-12 col-md-6">
                                        <label for="shotList" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Shot List / Góc chụp mong muốn</label>
                                        <textarea id="shotList" name="shotList" rows="2" maxlength="1500"
                                                  placeholder="Ví dụ: Chân dung cận cảnh, toàn cảnh ngoại cảnh, góc nghiêng..."
                                                  class="form-input"><c:out value='${bookingRequest.shotList}'/></textarea>
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label for="propsNeeded" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Props & Accessories / Đạo cụ cần có</label>
                                        <textarea id="propsNeeded" name="propsNeeded" rows="2" maxlength="500"
                                                  placeholder="Ví dụ: Bó hoa tươi, máy ảnh film, mũ cói, đèn led mini..."
                                                  class="form-input"><c:out value='${bookingRequest.propsNeeded}'/></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 3rem;">
                            Note: Your request will be sent to the photographer for confirmation. No immediate payment is required until the photographer accepts.
                        </div>

                        <!-- Actions -->
                        <div class="pc-booking-actions">
                            <a href="${pageContext.request.contextPath}/photographers/${photographer.id}" class="btn btn-ghost">
                                Cancel
                            </a>
                            <button type="submit" class="btn btn-primary" id="btn-submit-booking" style="width: auto; padding: 1rem 3rem;">
                                Request Booking
                            </button>
                        </div>

                    </form>
                </div>

            </div>

        </div>
    </main>

    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const dateInput = document.getElementById('bookingDate');
        const timeInput = document.getElementById('bookingTime');
        const feedbackBox = document.getElementById('availability-feedback');
        const statusBadge = document.getElementById('availability-status-badge');
        const slotsContainer = document.getElementById('availability-slots-list');
        const noticeEl = document.getElementById('availability-notice');
        const photographerId = '${photographer.id}';

        function checkAvailability() {
            if (!dateInput || !feedbackBox) return;
            const dateVal = dateInput.value;
            if (!dateVal || !photographerId) {
                feedbackBox.style.display = 'none';
                return;
            }

            feedbackBox.style.display = 'block';
            statusBadge.className = 'badge bg-secondary';
            statusBadge.textContent = 'Kiểm tra...';
            slotsContainer.innerHTML = '<span style="font-size: 0.82rem; color: var(--text-muted);">Đang tải lịch của nhiếp ảnh gia...</span>';
            noticeEl.textContent = '';

            fetch('${pageContext.request.contextPath}/api/photographers/' + photographerId + '/availability?date=' + encodeURIComponent(dateVal))
                .then(res => res.json())
                .then(data => {
                    if (!data.wholeDayAvailable) {
                        statusBadge.className = 'badge bg-danger';
                        statusBadge.textContent = 'Bận cả ngày';
                        slotsContainer.innerHTML = '<span style="font-size: 0.82rem; color: #dc2626;">Nhiếp ảnh gia bận hoặc đã khóa lịch vào ngày này. Vui lòng chọn ngày khác.</span>';
                        noticeEl.textContent = 'Bạn vẫn có thể gửi yêu cầu nếu đã trao đổi trước.';
                        return;
                    }

                    const slots = data.slots || [];
                    if (slots.length === 0) {
                        statusBadge.className = 'badge bg-warning text-dark';
                        statusBadge.textContent = 'Lịch tự do';
                        slotsContainer.innerHTML = '<span style="font-size: 0.82rem; color: var(--text-muted);">Không có khung giờ giới hạn cố định. Bạn có thể chọn giờ tùy ý.</span>';
                        return;
                    }

                    statusBadge.className = 'badge bg-success';
                    statusBadge.textContent = 'Có thể đặt';
                    slotsContainer.innerHTML = '';
                    let availableCount = 0;

                    slots.forEach(slot => {
                        const btn = document.createElement('button');
                        btn.type = 'button';
                        btn.className = 'btn btn-sm ' + (slot.available ? 'btn-outline-primary' : 'btn-outline-secondary disabled');
                        btn.style.fontSize = '0.8rem';
                        btn.style.padding = '0.3rem 0.6rem';
                        btn.textContent = slot.startTime + ' - ' + slot.endTime;
                        if (slot.available) {
                            availableCount++;
                            btn.onclick = function() {
                                if (timeInput) timeInput.value = slot.startTime;
                                Array.from(slotsContainer.children).forEach(c => c.classList.remove('btn-primary', 'text-white'));
                                btn.classList.add('btn-primary', 'text-white');
                            };
                        } else {
                            btn.title = slot.reason || 'Khung giờ bị trùng lịch';
                            btn.disabled = true;
                            btn.style.opacity = '0.5';
                        }
                        slotsContainer.appendChild(btn);
                    });

                    if (availableCount > 0) {
                        noticeEl.textContent = 'Nhấn vào khung giờ phía trên để tự động điền giờ chụp.';
                    } else {
                        statusBadge.className = 'badge bg-danger';
                        statusBadge.textContent = 'Hết chỗ trống';
                        noticeEl.textContent = 'Tất cả các khung giờ tiêu chuẩn trong ngày đã có lịch hẹn.';
                    }
                })
                .catch(() => {
                    feedbackBox.style.display = 'none';
                });
        }

        if (dateInput) {
            dateInput.addEventListener('change', checkAvailability);
            if (dateInput.value) {
                checkAvailability();
            }
        }
    });
    </script>

    <footer class="pc-footer">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>
