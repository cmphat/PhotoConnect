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
                                <select id="packageId" name="packageId" class="form-input" style="padding: 0.65rem 0.85rem;" required>
                                    <option value="">-- Select an active package --</option>
                                    <c:forEach var="pkg" items="${packages}">
                                        <option value="${pkg.id}"
                                                data-included-travel-km="${pkg.includedTravelKm}"
                                                data-night-rate="${pkg.nightSurchargeRate}"
                                                ${bookingRequest.packageId == pkg.id ? 'selected' : ''}>
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
                                <span><i class="bi bi-clock-history me-1"></i> Available Time Slots</span>
                                <span id="availability-status-badge" class="badge bg-primary" style="font-size: 0.75rem;">Loading...</span>
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
                            <label for="notes" class="form-label">Notes &amp; Requirements (Optional)</label>
                            <textarea id="notes"
                                      name="notes"
                                      rows="3"
                                      maxlength="1000"
                                      placeholder="Share shoot concept, special requests, etc..."
                                      class="form-input"><c:out value='${bookingRequest.notes}'/></textarea>
                        </div>

                        <!-- V6 Surcharges & Extras Configuration -->
                        <div class="card mb-4 border" style="background: var(--surface); border-color: rgba(37,99,235,0.2);">
                            <div class="card-header bg-transparent py-3 border-bottom" style="border-color: rgba(37,99,235,0.15);">
                                <h3 class="h6 fw-bold mb-0 text-dark">
                                    <i class="bi bi-sliders me-1 text-primary"></i> Surcharges &amp; Extra Add-ons
                                </h3>
                                <small class="text-muted">Customize travel distance, night session surcharge, and extra services</small>
                            </div>
                            <div class="card-body p-3">
                                <div class="row g-3 mb-3">
                                    <div class="col-12 col-md-6">
                                        <label for="travelDistanceKm" class="form-label fw-semibold" style="font-size: 0.88rem;">
                                            Travel Distance (km)
                                        </label>
                                        <div class="input-group">
                                            <input type="number" id="travelDistanceKm" name="travelDistanceKm" min="0" max="500"
                                                   class="form-input" placeholder="0" value="<c:out value='${bookingRequest.travelDistanceKm != null ? bookingRequest.travelDistanceKm : 0}'/>">
                                            <span class="input-group-text bg-light">km</span>
                                        </div>
                                        <div class="form-text" style="font-size: 0.78rem; color: #2563eb;">
                                            <i class="bi bi-info-circle me-1"></i> Rates are defined by the selected package and verified on the server.
                                        </div>
                                    </div>
                                    <div class="col-12 col-md-6 d-flex align-items-center">
                                        <div class="form-check mt-3">
                                            <input class="form-check-input" type="checkbox" id="nightShoot" name="nightShoot" value="true"
                                                   ${bookingRequest.nightShoot ? 'checked' : ''}>
                                            <label class="form-check-label fw-semibold" for="nightShoot" style="font-size: 0.88rem;">
                                                Night / Low-Light Shoot (per package rate)
                                            </label>
                                            <div class="form-text" style="font-size: 0.78rem;">
                                                Applies between 18:00 and 06:00 or when specialized lighting is requested.
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="row g-3">
                                    <div class="col-12 col-md-4">
                                        <label for="extraHours" class="form-label fw-semibold" style="font-size: 0.85rem;">
                                            Extra Shoot Hours (+200,000 VND/hr)
                                        </label>
                                        <input type="number" id="extraHours" name="extraHours" min="0" max="10"
                                               class="form-input" placeholder="0" value="<c:out value='${bookingRequest.extraHours != null ? bookingRequest.extraHours : 0}'/>">
                                    </div>
                                    <div class="col-12 col-md-4">
                                        <label for="extraPhotos" class="form-label fw-semibold" style="font-size: 0.85rem;">
                                            Extra Edited Photos (+50,000 VND/ea)
                                        </label>
                                        <input type="number" id="extraPhotos" name="extraPhotos" min="0" max="100"
                                               class="form-input" placeholder="0" value="<c:out value='${bookingRequest.extraPhotos != null ? bookingRequest.extraPhotos : 0}'/>">
                                    </div>
                                    <div class="col-12 col-md-4 d-flex align-items-center">
                                        <div class="form-check mt-3">
                                            <input class="form-check-input" type="checkbox" id="additionalPhotographer" name="additionalPhotographer" value="true"
                                                   ${bookingRequest.additionalPhotographer ? 'checked' : ''}>
                                            <label class="form-check-label fw-semibold" for="additionalPhotographer" style="font-size: 0.85rem;">
                                                Second Shooter / Assistant (+500,000 VND)
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- V6 Authoritative Live Price Breakdown Card -->
                        <div class="card mb-4 border" style="background: #f8fafc; border-color: #cbd5e1;">
                            <div class="card-header bg-transparent py-3 border-bottom d-flex justify-content-between align-items-center">
                                <h3 class="h6 fw-bold mb-0 text-dark">
                                    <i class="bi bi-receipt me-1 text-primary"></i> Price Breakdown &amp; 30% Escrow Deposit
                                </h3>
                                <span class="badge bg-primary" style="color: #ffffff !important; font-weight: 600; background-color: #2563eb !important;">Server Verified</span>
                            </div>
                            <div class="card-body p-3">
                                <div class="d-flex justify-content-between py-1" style="font-size: 0.9rem;">
                                    <span class="text-muted">Base Package Rate:</span>
                                    <strong id="displayBasePrice">0 VND</strong>
                                </div>
                                <div class="d-flex justify-content-between py-1" id="rowNightFee" style="font-size: 0.9rem; display: none;">
                                    <span class="text-muted">Night Shoot Surcharge (+20%):</span>
                                    <span id="displayNightFee" class="text-primary">+0 VND</span>
                                </div>
                                <div class="d-flex justify-content-between py-1" id="rowTravelFee" style="font-size: 0.9rem; display: none;">
                                    <span class="text-muted">Travel Surcharge (> 30 km):</span>
                                    <span id="displayTravelFee" class="text-primary">+0 VND</span>
                                </div>
                                <div class="d-flex justify-content-between py-1" id="rowExtrasFee" style="font-size: 0.9rem; display: none;">
                                    <span class="text-muted">Add-on Options (Hours/Photos/Assistant):</span>
                                    <span id="displayExtrasFee" class="text-primary">+0 VND</span>
                                </div>
                                <hr class="my-2">
                                <div class="d-flex justify-content-between py-1" style="font-size: 1.05rem;">
                                    <span class="fw-bold text-dark">Total Agreed Price:</span>
                                    <strong id="displayTotalPrice" class="text-dark">0 VND</strong>
                                </div>
                                <div class="p-3 my-2 rounded" style="background: rgba(37,99,235,0.08); border: 1px solid rgba(37,99,235,0.2);">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <span class="fw-bold text-primary" style="font-size: 1rem;">
                                            <i class="bi bi-shield-check me-1"></i> Required Escrow Deposit (30%):
                                        </span>
                                        <span class="badge bg-primary fs-6 px-3 py-2" id="displayDepositAmount" style="color: #ffffff !important; font-weight: 700; background-color: #2563eb !important;">0 VND</span>
                                    </div>
                                    <small class="text-muted d-block" style="font-size: 0.78rem;">
                                        Payable via VietQR once the photographer accepts. Funds are held in escrow.
                                    </small>
                                </div>
                                <div class="d-flex justify-content-between py-1 text-muted" style="font-size: 0.88rem;">
                                    <span>Remaining Balance Due at Session (70%):</span>
                                    <strong id="displayRemainingBalance">0 VND</strong>
                                </div>
                            </div>
                        </div>

                        <!-- Structured Shoot Brief (Phase 7) -->
                        <div class="card mb-4 border" style="background: var(--surface); border-color: var(--border);">
                            <div class="card-header bg-transparent py-3" style="cursor: pointer;" onclick="var b = document.getElementById('shootBriefBody'); b.style.display = b.style.display === 'none' ? 'block' : 'none';">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <h3 class="h6 fw-bold mb-0 text-dark"><i class="bi bi-camera me-1 text-primary"></i> Shoot Brief &amp; Creative Vision (Optional)</h3>
                                        <small class="text-muted">Specify concept details, outfits, moodboards, and desired shot list</small>
                                    </div>
                                    <span class="badge bg-light text-primary border">Details</span>
                                </div>
                            </div>
                            <div class="card-body" id="shootBriefBody">
                                <div class="form-group mb-3">
                                    <label for="conceptDescription" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Concept Description</label>
                                    <textarea id="conceptDescription" name="conceptDescription" rows="3" maxlength="1000"
                                              placeholder="Share concept details, lighting style, artistic mood..."
                                              class="form-input"><c:out value='${bookingRequest.conceptDescription}'/></textarea>
                                </div>

                                <div class="row g-3 mb-3">
                                    <div class="col-12 col-md-6">
                                        <label for="moodboardUrl" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Moodboard / Reference Link</label>
                                        <input type="url" id="moodboardUrl" name="moodboardUrl" maxlength="500"
                                               placeholder="https://pinterest.com/... or Google Drive folder link"
                                               class="form-input" value="<c:out value='${bookingRequest.moodboardUrl}'/>">
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label for="backupLocation" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Backup Location (Rain / Weather)</label>
                                        <input type="text" id="backupLocation" name="backupLocation" maxlength="255"
                                               placeholder="Alternative indoor/sheltered location in case of weather..."
                                               class="form-input" value="<c:out value='${bookingRequest.backupLocation}'/>">
                                    </div>
                                </div>

                                <div class="row g-3 mb-3">
                                    <div class="col-12 col-md-6">
                                        <label for="outfitCount" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Number of Outfits</label>
                                        <input type="number" id="outfitCount" name="outfitCount" min="1" max="20"
                                               placeholder="e.g. 2 outfits"
                                               class="form-input" value="<c:out value='${bookingRequest.outfitCount}'/>">
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label for="participantCount" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Number of Participants</label>
                                        <input type="number" id="participantCount" name="participantCount" min="1" max="50"
                                               placeholder="e.g. 1 or 2 people"
                                               class="form-input" value="<c:out value='${bookingRequest.participantCount}'/>">
                                    </div>
                                </div>

                                <div class="row g-3">
                                    <div class="col-12 col-md-6">
                                        <label for="shotList" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Shot List &amp; Key Poses</label>
                                        <textarea id="shotList" name="shotList" rows="2" maxlength="1500"
                                                  placeholder="e.g. Close-up portraits, wide environmental shots, 3/4 profiles..."
                                                  class="form-input"><c:out value='${bookingRequest.shotList}'/></textarea>
                                    </div>
                                    <div class="col-12 col-md-6">
                                        <label for="propsNeeded" class="form-label" style="font-size: 0.85rem; font-weight: 600;">Props &amp; Accessories</label>
                                        <textarea id="propsNeeded" name="propsNeeded" rows="2" maxlength="500"
                                                  placeholder="e.g. Floral bouquet, vintage film camera, straw hat..."
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
            statusBadge.textContent = 'Checking...';
            slotsContainer.innerHTML = '<span style="font-size: 0.82rem; color: var(--text-muted);">Loading creator schedule...</span>';
            noticeEl.textContent = '';

            fetch('${pageContext.request.contextPath}/api/photographers/' + photographerId + '/availability?date=' + encodeURIComponent(dateVal))
                .then(res => res.json())
                .then(data => {
                    if (!data.wholeDayAvailable) {
                        statusBadge.className = 'badge bg-danger';
                        statusBadge.textContent = 'Unavailable';
                        slotsContainer.innerHTML = '<span style="font-size: 0.82rem; color: #dc2626;">Photographer is unavailable or has blocked this date. Please choose another date.</span>';
                        noticeEl.textContent = 'You may still submit a request if discussed in advance.';
                        return;
                    }

                    const slots = data.slots || [];
                    if (slots.length === 0) {
                        statusBadge.className = 'badge bg-warning text-dark';
                        statusBadge.textContent = 'Open Schedule';
                        slotsContainer.innerHTML = '<span style="font-size: 0.82rem; color: var(--text-muted);">No fixed time constraints. You may choose any preferred session time.</span>';
                        return;
                    }

                    statusBadge.className = 'badge bg-success';
                    statusBadge.textContent = 'Available';
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
                            btn.title = slot.reason || 'Slot already booked';
                            btn.disabled = true;
                            btn.style.opacity = '0.5';
                        }
                        slotsContainer.appendChild(btn);
                    });

                    if (availableCount > 0) {
                        noticeEl.textContent = 'Click an available time slot above to auto-fill the session start time.';
                    } else {
                        statusBadge.className = 'badge bg-danger';
                        statusBadge.textContent = 'Fully Booked';
                        noticeEl.textContent = 'All standard time slots on this date are reserved.';
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

        // V6 Authoritative Pricing Preview
        const packageSelect = document.getElementById('packageId');
        const travelInput = document.getElementById('travelDistanceKm');
        const nightInput = document.getElementById('nightShoot');
        const extraHoursInput = document.getElementById('extraHours');
        const extraPhotosInput = document.getElementById('extraPhotos');
        const addPhotographerInput = document.getElementById('additionalPhotographer');

        function formatVnd(val) {
            return new Intl.NumberFormat('vi-VN').format(val || 0) + ' VND';
        }

        function updatePriceCalculation() {
            const pkgId = packageSelect ? packageSelect.value : '';
            if (!pkgId) {
                return;
            }
            const tTime = timeInput ? timeInput.value : '';
            const tDist = travelInput ? travelInput.value : '0';
            const nShoot = nightInput ? nightInput.checked : false;
            const eHours = extraHoursInput ? extraHoursInput.value : '0';
            const ePhotos = extraPhotosInput ? extraPhotosInput.value : '0';
            const aPhoto = addPhotographerInput ? addPhotographerInput.checked : false;

            const url = '${pageContext.request.contextPath}/api/pricing/calculate?photographerId=' + encodeURIComponent(photographerId)
                + '&packageId=' + encodeURIComponent(pkgId)
                + (tTime ? '&bookingTime=' + encodeURIComponent(tTime) : '')
                + '&travelDistanceKm=' + encodeURIComponent(tDist)
                + '&nightShoot=' + encodeURIComponent(nShoot)
                + '&extraHours=' + encodeURIComponent(eHours)
                + '&extraPhotos=' + encodeURIComponent(ePhotos)
                + '&additionalPhotographer=' + encodeURIComponent(aPhoto);

            fetch(url)
                .then(r => r.json())
                .then(data => {
                    document.getElementById('displayBasePrice').textContent = formatVnd(data.basePrice);

                    const rowNight = document.getElementById('rowNightFee');
                    if (data.nightSurcharge > 0) {
                        rowNight.style.display = 'flex';
                        document.getElementById('displayNightFee').textContent = '+' + formatVnd(data.nightSurcharge);
                    } else {
                        rowNight.style.display = 'none';
                    }

                    const rowTravel = document.getElementById('rowTravelFee');
                    if (data.travelSurcharge > 0) {
                        rowTravel.style.display = 'flex';
                        document.getElementById('displayTravelFee').textContent = '+' + formatVnd(data.travelSurcharge);
                    } else {
                        rowTravel.style.display = 'none';
                    }

                    const rowExtras = document.getElementById('rowExtrasFee');
                    if (data.extrasFee > 0) {
                        rowExtras.style.display = 'flex';
                        document.getElementById('displayExtrasFee').textContent = '+' + formatVnd(data.extrasFee);
                    } else {
                        rowExtras.style.display = 'none';
                    }

                    document.getElementById('displayTotalPrice').textContent = formatVnd(data.totalPrice);
                    document.getElementById('displayDepositAmount').textContent = formatVnd(data.depositAmount);
                    document.getElementById('displayRemainingBalance').textContent = formatVnd(data.remainingBalance);
                })
                .catch(err => console.debug('Pricing preview error', err));
        }

        [packageSelect, timeInput, travelInput, nightInput, extraHoursInput, extraPhotosInput, addPhotographerInput].forEach(el => {
            if (el) {
                el.addEventListener('change', updatePriceCalculation);
                el.addEventListener('input', updatePriceCalculation);
            }
        });

        updatePriceCalculation();
    });
    </script>

    <footer class="pc-footer">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>
