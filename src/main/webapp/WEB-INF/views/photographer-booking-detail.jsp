<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Booking Request #${booking.id} — Creator Workspace — PhotoConnect</title>
    <style>
        .workspace-shell {
            max-width: 980px;
            margin: 0 auto;
        }

        .session-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: clamp(1.75rem, 4vw, 3rem);
            box-shadow: var(--shadow-subtle);
            margin-bottom: 2.5rem;
        }

        .session-header-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            flex-wrap: wrap;
            gap: 1.5rem;
            padding-bottom: 2rem;
            border-bottom: 1px solid var(--border);
            margin-bottom: 2rem;
        }

        .session-meta-spec {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 2rem;
            padding-bottom: 2.5rem;
            border-bottom: 1px solid var(--border);
            margin-bottom: 2.5rem;
        }

        .spec-item-label {
            font-size: 0.74rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--muted);
            margin-bottom: 0.4rem;
        }

        .spec-item-value {
            font-family: var(--font-primary);
            font-size: 1.15rem;
            font-weight: 500;
            color: var(--text);
        }

        @media (max-width: 640px) {
            .session-meta-spec {
                grid-template-columns: 1fr;
                gap: 1.5rem;
            }
            .session-header-row {
                flex-direction: column;
            }
        }
    </style>
</head>

    <main class="pc-page">
        <div class="editorial-container workspace-shell">

            <!-- Breadcrumbs -->
            <div style="margin-bottom: 1.75rem;">
                <a href="${pageContext.request.contextPath}/photographer/bookings" class="text-link" style="font-size: 0.88rem;">
                    &larr; Back to Booking Requests
                </a>
            </div>

            <!-- Workspace Title -->
            <div style="margin-bottom: 2rem;">
                <span class="section-index">Creator Workspace</span>
                <h1 class="editorial-title" style="font-size: clamp(2rem, 4vw, 3rem); margin: 0 0 0.5rem 0;">
                    Booking Request Details
                </h1>
                <div style="color: var(--muted); font-size: 0.9rem;">
                    Request Reference #${booking.id}
                </div>
            </div>

            <!-- Feedback Alerts -->
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

            <!-- ── Visual Progress Indicator derived strictly from existing data ── -->
            <c:choose>
                <c:when test="${booking.status == 'CANCELLED'}">
                    <div class="stepper-terminated">
                        <span style="font-size: 1.25rem;">✕</span>
                        <div>
                            <strong>Session Cancelled</strong> &mdash; This session is cancelled.
                            <c:if test="${not empty booking.cancelledByName}">
                                <div style="font-size: 0.88rem; margin-top: 0.35rem;">
                                    Cancelled by: <strong><c:out value="${booking.cancelledByName}"/></strong>
                                    <c:if test="${not empty booking.cancellationReason}">
                                        &middot; Reason: <em>&ldquo;<c:out value="${booking.cancellationReason}"/>&rdquo;</em>
                                    </c:if>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </c:when>
                <c:when test="${booking.status == 'REJECTED'}">
                    <div class="stepper-terminated">
                        <span style="font-size: 1.25rem;">✕</span>
                        <div>
                            <strong>Request Declined</strong> &mdash; You declined this booking request.
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Active Lifecycle Progress Stepper -->
                    <div class="booking-stepper">
                        <!-- Step 1: Requested -->
                        <div class="stepper-step completed">
                            <div class="stepper-indicator">✓</div>
                            <span class="stepper-label">Requested</span>
                        </div>

                        <!-- Step 2: Accepted -->
                        <c:choose>
                            <c:when test="${booking.status == 'ACCEPTED' || booking.status == 'COMPLETED'}">
                                <div class="stepper-step completed">
                                    <div class="stepper-indicator">✓</div>
                                    <span class="stepper-label">Accepted</span>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stepper-step current">
                                    <div class="stepper-indicator">2</div>
                                    <span class="stepper-label">Needs Decision</span>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <!-- Step 3: Deposit Paid -->
                        <c:choose>
                            <c:when test="${booking.status == 'COMPLETED'}">
                                <div class="stepper-step completed">
                                    <div class="stepper-indicator">✓</div>
                                    <span class="stepper-label">Deposit Paid</span>
                                </div>
                            </c:when>
                            <c:when test="${booking.status == 'ACCEPTED'}">
                                <div class="stepper-step current">
                                    <div class="stepper-indicator">3</div>
                                    <span class="stepper-label">Awaiting Client Deposit</span>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stepper-step">
                                    <div class="stepper-indicator">3</div>
                                    <span class="stepper-label">Deposit</span>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <!-- Step 4: Completed -->
                        <c:choose>
                            <c:when test="${booking.status == 'COMPLETED'}">
                                <div class="stepper-step completed">
                                    <div class="stepper-indicator">✓</div>
                                    <span class="stepper-label">Completed</span>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stepper-step">
                                    <div class="stepper-indicator">4</div>
                                    <span class="stepper-label">Completed</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>

            <!-- ── Main Session Card ─────────────────────────────────────── -->
            <div class="session-card">

                <div class="session-header-row pc-stack-mobile">
                    <div>
                        <div style="font-size: 0.8rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; color: var(--accent); margin-bottom: 0.35rem;">
                            Client Booking Request
                        </div>
                        <h2 class="editorial-heading" style="font-size: 1.75rem; margin: 0 0 0.35rem 0;">
                            from <c:out value="${booking.customerName}"/>
                            <c:choose>
                                <c:when test="${not empty booking.pastCompletedShootsWithClient and booking.pastCompletedShootsWithClient > 0}">
                                    <span class="badge bg-success" style="font-size: 0.78rem; vertical-align: middle; margin-left: 0.5rem;">
                                        ★ Khách quen (${booking.pastCompletedShootsWithClient} buổi đã hoàn thành)
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-dark" style="font-size: 0.78rem; vertical-align: middle; margin-left: 0.5rem; border: 1px solid var(--border);">
                                        Khách hàng mới
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </h2>
                        <c:if test="${not empty booking.createdAt}">
                            <fmt:parseDate value="${fn:substring(booking.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedCreatedAt" type="date" />
                            <p style="color: var(--muted); font-size: 0.88rem; margin: 0;">
                                Submitted on <fmt:formatDate value="${parsedCreatedAt}" pattern="MMMM d, yyyy" />
                            </p>
                        </c:if>
                    </div>

                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 0.85rem;">
                        <span class="status-badge ${booking.status}">
                            <c:out value="${booking.status}"/>
                        </span>
                        <a href="${pageContext.request.contextPath}/bookings/${booking.id}/chat" class="btn btn-secondary btn-sm" id="btn-open-chat">
                            Open Chat &rarr;
                        </a>
                    </div>
                </div>

                <!-- Structured Specification Grid -->
                <div class="session-meta-spec">
                    <div>
                        <div class="spec-item-label">Requested Date &amp; Time</div>
                        <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                        <div class="spec-item-value">
                            <fmt:formatDate value="${parsedBookingDate}" pattern="EEEE, MMMM d, yyyy" /> at ${booking.bookingTime}
                        </div>
                    </div>
                    <div>
                        <div class="spec-item-label">Agreed Rate</div>
                        <div class="spec-item-value" style="color: var(--text); font-weight: 600;">
                            <fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND
                        </div>
                    </div>
                    <c:if test="${not empty booking.packageName}">
                        <div style="grid-column: span 2;">
                            <div class="spec-item-label">Selected Service Package</div>
                            <div class="spec-item-value">
                                <strong><c:out value="${booking.packageName}"/></strong>
                                <span style="font-size: 0.88rem; color: var(--muted); margin-left: 0.5rem;">
                                    (<c:out value="${booking.durationMinutes}"/> mins · <c:out value="${not empty booking.editedPhotoCount ? booking.editedPhotoCount : 'Standard'}"/> photos · <c:out value="${not empty booking.deliveryDays ? booking.deliveryDays : 'Standard'}"/> days delivery)
                                </span>
                            </div>
                        </div>
                    </c:if>
                    <div style="grid-column: span 2;">
                        <div class="spec-item-label">Requested Location</div>
                        <div class="spec-item-value">
                            <c:out value="${booking.location}"/>
                        </div>
                    </div>
                    <c:if test="${not empty booking.notes}">
                        <div style="grid-column: span 2;">
                            <div class="spec-item-label">Client Notes</div>
                            <div style="font-size: 0.95rem; color: var(--muted); line-height: 1.6;">
                                <c:out value="${booking.notes}"/>
                            </div>
                        </div>
                    </c:if>

                    <!-- Structured Shoot Brief Card (Phase 7) -->
                    <c:if test="${not empty booking.conceptDescription or not empty booking.moodboardUrl or not empty booking.backupLocation or not empty booking.shotList or not empty booking.propsNeeded or not empty booking.outfitCount or not empty booking.participantCount}">
                        <div style="grid-column: span 2; margin-top: 1rem;">
                            <div style="background: rgba(37, 99, 235, 0.03); border: 1px solid rgba(37, 99, 235, 0.2); border-radius: var(--radius-sm, 8px); padding: 1.5rem;">
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid rgba(37, 99, 235, 0.12); padding-bottom: 0.75rem;">
                                    <div style="font-weight: 700; color: #1e3a8a; font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
                                        <i class="bi bi-camera"></i> Shoot Brief &amp; Creative Vision
                                    </div>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <c:if test="${not empty booking.outfitCount}">
                                            <span class="badge bg-primary" style="font-size: 0.75rem;">${booking.outfitCount} set đồ / outfits</span>
                                        </c:if>
                                        <c:if test="${not empty booking.participantCount}">
                                            <span class="badge bg-secondary" style="font-size: 0.75rem;">${booking.participantCount} người / participants</span>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="row g-3">
                                    <c:if test="${not empty booking.conceptDescription}">
                                        <div class="col-12">
                                            <div class="spec-item-label">Concept &amp; Creative Idea</div>
                                            <div style="font-size: 0.95rem; color: var(--text); line-height: 1.6;">
                                                <c:out value="${booking.conceptDescription}"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${not empty booking.moodboardUrl}">
                                        <div class="col-12 col-md-6">
                                            <div class="spec-item-label">Moodboard / Visual References</div>
                                            <div>
                                                <a href="<c:out value='${booking.moodboardUrl}'/>" target="_blank" rel="noopener noreferrer" class="btn btn-outline-primary btn-sm" style="font-size: 0.82rem; padding: 0.35rem 0.75rem;">
                                                    <i class="bi bi-link-45deg me-1"></i> Xem Moodboard / Reference Link &nearr;
                                                </a>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${not empty booking.backupLocation}">
                                        <div class="col-12 col-md-6">
                                            <div class="spec-item-label">Backup Location (Rain / Weather)</div>
                                            <div style="font-size: 0.95rem; color: var(--text);">
                                                <c:out value="${booking.backupLocation}"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${not empty booking.shotList}">
                                        <div class="col-12">
                                            <div class="spec-item-label">Shot List &amp; Key Poses</div>
                                            <div style="font-size: 0.92rem; color: var(--text); line-height: 1.5; background: var(--surface); padding: 0.75rem 1rem; border-radius: var(--radius-sm, 6px); border: 1px solid var(--border);">
                                                <c:out value="${booking.shotList}"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${not empty booking.propsNeeded}">
                                        <div class="col-12">
                                            <div class="spec-item-label">Props &amp; Accessories</div>
                                            <div style="font-size: 0.92rem; color: var(--muted); line-height: 1.5;">
                                                <c:out value="${booking.propsNeeded}"/>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Private Studio CRM Notes (Phase 13) -->
                    <div style="grid-column: span 2; margin-top: 1.25rem;">
                        <div style="background: rgba(15, 23, 42, 0.02); border: 1px solid var(--border); border-radius: var(--radius-sm, 8px); padding: 1.25rem 1.5rem;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                                <div style="font-weight: 700; font-size: 0.95rem; color: var(--text); display: flex; align-items: center; gap: 0.4rem;">
                                    <i class="bi bi-journal-bookmark-fill" style="color: var(--accent);"></i> Ghi chú nội bộ Studio / Private CRM Notes
                                </div>
                                <span style="font-size: 0.75rem; color: var(--muted); font-style: italic;">
                                    Chỉ bạn nhìn thấy &middot; Ẩn với khách hàng
                                </span>
                            </div>
                            <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/notes" method="post">
                                <%@ include file="fragments/csrf-input.jsp" %>
                                <div class="mb-2">
                                    <textarea name="notes" rows="3" class="form-control" style="font-size: 0.9rem;"
                                              placeholder="Ghi chú sở thích khách hàng, phong cách ánh sáng, thiết bị cần mang, thanh toán tiền mặt..."><c:out value="${booking.photographerNotes}"/></textarea>
                                </div>
                                <div style="display: flex; justify-content: flex-end;">
                                    <button type="submit" class="btn btn-secondary btn-sm" id="btn-save-crm-notes">
                                        <i class="bi bi-save me-1"></i> Lưu ghi chú / Save Notes
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- ── Photographer Action Bar ────────────────────────────── -->
                <c:if test="${booking.status == 'PENDING'}">
                    <div class="pc-actions pc-stack-mobile" style="padding-top: 1.5rem; display: flex; gap: 1rem; align-items: center;">
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/accept" method="post" style="margin: 0;">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <button type="submit" class="btn btn-primary btn-lg" id="btn-accept-booking">
                                Accept Booking
                            </button>
                        </form>
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/reject" method="post" onsubmit="return confirm('Are you sure you want to decline this request?');" style="margin: 0;">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <button type="submit" class="btn btn-danger btn-lg" id="btn-reject-booking">
                                Decline Request
                            </button>
                        </form>
                    </div>
                </c:if>

                <c:if test="${booking.status == 'ACCEPTED'}">
                    <div style="padding-top: 1.5rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                        <div style="color: var(--muted); font-size: 0.88rem;">
                            Session accepted. Once the shoot concludes, mark it completed below.
                        </div>
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/complete" method="post" onsubmit="return confirm('Mark this photoshoot session as completed?');" style="margin: 0;">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <button type="submit" class="btn btn-primary" id="btn-complete-booking">
                                Mark as Completed
                            </button>
                        </form>
                    </div>
                </c:if>

                <!-- ── Rescheduling Management ──────────────────────────────── -->
                <c:if test="${booking.status == 'ACCEPTED' || booking.status == 'DEPOSIT_PENDING' || booking.status == 'CONFIRMED'}">
                    <div style="padding-top: 1.5rem; border-top: 1px solid var(--border); margin-top: 1.5rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.25rem;">
                            <div>
                                <h3 class="editorial-heading" style="font-size: 1.15rem; margin: 0 0 0.25rem 0;">
                                    Session Rescheduling
                                </h3>
                                <div style="color: var(--muted); font-size: 0.85rem;">
                                    Propose a new date or time, or respond to client reschedule proposals.
                                </div>
                            </div>
                            <c:if test="${empty pendingReschedule}">
                                <button type="button" class="btn btn-secondary btn-sm" id="btn-toggle-photog-reschedule" onclick="var el = document.getElementById('photog-reschedule-collapse'); el.style.display = (el.style.display === 'none' ? 'block' : 'none');">
                                    Propose Reschedule
                                </button>
                            </c:if>
                        </div>

                        <!-- Active Pending Reschedule Alert -->
                        <c:if test="${not empty pendingReschedule}">
                            <div style="background: rgba(37, 99, 235, 0.05); border: 1px solid rgba(37, 99, 235, 0.25); border-radius: var(--radius-sm); padding: 1.25rem; margin-bottom: 1.25rem;">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                                    <div>
                                        <span class="status-badge ${pendingReschedule.status}" style="margin-bottom: 0.5rem; display: inline-block;">
                                            <c:out value="${pendingReschedule.statusDisplayName}"/>
                                        </span>
                                        <div style="font-weight: 600; font-size: 1rem;">
                                            Proposed: ${pendingReschedule.proposedDate} at ${pendingReschedule.proposedTime}
                                        </div>
                                        <div style="color: var(--muted); font-size: 0.85rem; margin-top: 0.35rem;">
                                            Requested by: <strong><c:out value="${pendingReschedule.requestedByName}"/></strong>
                                            <c:if test="${not empty pendingReschedule.reason}">
                                                &middot; Note: <em>&ldquo;<c:out value="${pendingReschedule.reason}"/>&rdquo;</em>
                                            </c:if>
                                        </div>
                                    </div>
                                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                                        <c:choose>
                                            <c:when test="${pendingReschedule.requestedByUserId == sessionScope.user.id}">
                                                <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/reschedule/${pendingReschedule.id}/cancel" method="post" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-secondary btn-sm" id="btn-cancel-photog-reschedule" onclick="return confirm('Withdraw this reschedule proposal?');">
                                                        Withdraw Proposal
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/reschedule/${pendingReschedule.id}/accept" method="post" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-primary btn-sm" id="btn-accept-photog-reschedule">
                                                        Accept Reschedule
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/reschedule/${pendingReschedule.id}/reject" method="post" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-danger btn-sm" id="btn-reject-photog-reschedule" onclick="return confirm('Decline this reschedule request?');">
                                                        Decline
                                                    </button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <!-- Reschedule Proposal Form -->
                        <div id="photog-reschedule-collapse" style="display: none; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 1.25rem; margin-bottom: 1.25rem;">
                            <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/reschedule" method="post">
                                <%@ include file="fragments/csrf-input.jsp" %>
                                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
                                    <div>
                                        <label style="display: block; font-size: 0.82rem; font-weight: 600; margin-bottom: 0.25rem;">New Proposed Date</label>
                                        <input type="date" name="proposedDate" class="pc-input" required style="width: 100%; padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border);" />
                                    </div>
                                    <div>
                                        <label style="display: block; font-size: 0.82rem; font-weight: 600; margin-bottom: 0.25rem;">New Proposed Time</label>
                                        <input type="time" name="proposedTime" class="pc-input" required style="width: 100%; padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border);" />
                                    </div>
                                    <div>
                                        <label style="display: block; font-size: 0.82rem; font-weight: 600; margin-bottom: 0.25rem;">Reason (Optional)</label>
                                        <input type="text" name="reason" placeholder="e.g. Schedule adjustment, emergency..." class="pc-input" maxlength="500" style="width: 100%; padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border);" />
                                    </div>
                                </div>
                                <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="document.getElementById('photog-reschedule-collapse').style.display = 'none';">Cancel</button>
                                    <button type="submit" class="btn btn-primary btn-sm" id="btn-submit-photog-reschedule">Submit Proposal</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:if>

                <c:if test="${booking.status != 'PENDING' && booking.status != 'REJECTED' && booking.status != 'CANCELLED'}">
                    <div class="mt-4 pt-3 border-top d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <div>
                            <div class="fw-semibold text-dark">Finished Photos Delivery Studio</div>
                            <div class="text-muted small">Upload, curate, and deliver finished high-resolution photos for this client.</div>
                        </div>
                        <a href="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/delivery" class="btn btn-outline-primary" id="btn-delivery-studio">
                            <i class="bi bi-images me-1"></i> Open Delivery Studio
                        </a>
                    </div>
                </c:if>

                <!-- ── Photographer Cancellation Option (PENDING, ACCEPTED, DEPOSIT_PENDING, CONFIRMED) ── -->
                <c:if test="${booking.status == 'PENDING' || booking.status == 'ACCEPTED' || booking.status == 'DEPOSIT_PENDING' || booking.status == 'CONFIRMED'}">
                    <div style="padding-top: 1.5rem; border-top: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1.5rem; margin-top: 1.5rem;">
                        <div>
                            <div style="font-weight: 600; font-size: 0.95rem; color: var(--text); margin-bottom: 0.25rem;">Cancel Session</div>
                            <div style="font-size: 0.82rem; color: var(--muted); max-width: 500px; line-height: 1.5;">
                                If you cannot fulfill this session, you may cancel it. Any deposit paid by the client will be credited for a 100% refund.
                            </div>
                        </div>
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/cancel" method="post" onsubmit="return confirm('Are you sure you want to cancel this booking? If deposit was paid, the client will receive 100% refund credit.');" style="margin: 0; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <input type="text" name="reason" placeholder="Reason (optional)" class="pc-input" style="padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border); font-size: 0.85rem; max-width: 200px;" />
                            <button type="submit" class="btn btn-danger btn-sm" id="btn-photog-cancel-booking">
                                Cancel Booking
                            </button>
                        </form>
                    </div>
                </c:if>

            </div>

        </div>
    </main>

    <!-- Editorial Footer -->
    <footer class="pc-footer">
        <div class="editorial-container">
            <div style="color: var(--muted); font-size: 0.85rem;">
                &copy; 2026 PhotoConnect. Creator Workspace.
            </div>
        </div>
    </footer>
