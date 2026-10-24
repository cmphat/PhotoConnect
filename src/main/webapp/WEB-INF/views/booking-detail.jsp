<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Booking #${booking.id} — Photography Session Workspace — PhotoConnect</title>
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

        .deposit-workspace-box {
            background: var(--surface-raised);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: 2rem;
            margin-bottom: 2rem;
        }

        .review-workspace-box {
            background: var(--surface-raised);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: 2rem;
            margin-bottom: 2rem;
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
                <a href="${pageContext.request.contextPath}/bookings" class="text-link" style="font-size: 0.88rem;">
                    &larr; Back to My Bookings
                </a>
            </div>

            <!-- Workspace Title -->
            <div style="margin-bottom: 2rem;">
                <span class="section-index">Booking Workspace</span>
                <h1 class="editorial-title" style="font-size: clamp(2rem, 4vw, 3rem); margin: 0 0 0.5rem 0;">
                    Photography Session Details
                </h1>
                <div style="color: var(--muted); font-size: 0.9rem;">
                    Booking Reference #${booking.id}
                </div>
            </div>

            <!-- Feedback Alerts -->
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success pc-alert pc-alert-success" role="status">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger pc-alert pc-alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- ── Visual Progress Indicator derived strictly from existing data ── -->
            <c:choose>
                <c:when test="${booking.status == 'CANCELLED'}">
                    <div class="stepper-terminated">
                        <span style="font-size: 1.25rem;">✕</span>
                        <div>
                            <strong>Session Cancelled</strong> &mdash; This booking was cancelled and is no longer active.
                            <c:if test="${not empty booking.cancelledByName}">
                                <div style="font-size: 0.88rem; margin-top: 0.35rem;">
                                    Cancelled by: <strong><c:out value="${booking.cancelledByName}"/></strong>
                                    <c:if test="${not empty booking.cancellationReason}">
                                        &middot; Reason: <em>&ldquo;<c:out value="${booking.cancellationReason}"/>&rdquo;</em>
                                    </c:if>
                                </div>
                            </c:if>
                            <c:if test="${not empty deposit}">
                                <div style="font-size: 0.85rem; margin-top: 0.25rem; color: var(--muted);">
                                    Deposit Status: <strong>${deposit.status}</strong>
                                    <c:if test="${not empty deposit.failureReason}">
                                        &mdash; <c:out value="${deposit.failureReason}"/>
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
                            <strong>Request Declined</strong> &mdash; The photographer was unable to accept this booking request.
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
                                    <span class="stepper-label">Pending Review</span>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <!-- Step 3: Deposit Paid (ONLY when deposit.status == 'PAID') -->
                        <c:choose>
                            <c:when test="${deposit != null && deposit.status == 'PAID'}">
                                <div class="stepper-step completed">
                                    <div class="stepper-indicator">✓</div>
                                    <span class="stepper-label">Deposit Paid</span>
                                </div>
                            </c:when>
                            <c:when test="${booking.status == 'ACCEPTED'}">
                                <div class="stepper-step current">
                                    <div class="stepper-indicator">3</div>
                                    <span class="stepper-label">Deposit Due</span>
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
                            Photography Session
                        </div>
                        <h2 class="editorial-heading" style="font-size: 1.75rem; margin: 0 0 0.35rem 0;">
                            with <c:out value="${booking.photographerName}"/>
                        </h2>
                        <c:if test="${not empty booking.createdAt}">
                            <fmt:parseDate value="${fn:substring(booking.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedCreatedAt" type="date" />
                            <p style="color: var(--muted); font-size: 0.88rem; margin: 0;">
                                Created on <fmt:formatDate value="${parsedCreatedAt}" pattern="MMMM d, yyyy" />
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
                <div class="session-meta-spec row g-4">
                    <div class="col-12 col-md-6">
                        <div class="spec-item-label">Date &amp; Time</div>
                        <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                        <div class="spec-item-value">
                            <fmt:formatDate value="${parsedBookingDate}" pattern="EEEE, MMMM d, yyyy" /> at ${booking.bookingTime}
                        </div>
                    </div>
                    <div class="col-12 col-md-6">
                        <div class="spec-item-label">Agreed Rate</div>
                        <div class="spec-item-value" style="color: var(--text); font-weight: 600;">
                            <fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND
                        </div>
                    </div>
                    <c:if test="${not empty booking.packageName}">
                        <div class="col-12 col-md-6">
                            <div class="spec-item-label">Service Package</div>
                            <div class="spec-item-value">
                                <strong><c:out value="${booking.packageName}"/></strong>
                                <div style="font-size: 0.85rem; color: var(--muted); margin-top: 0.2rem;">
                                    ${booking.durationMinutes} mins · ${not empty booking.editedPhotoCount ? booking.editedPhotoCount : 'Standard'} photos · ${not empty booking.deliveryDays ? booking.deliveryDays : 'Standard'} days turnaround
                                </div>
                            </div>
                        </div>
                    </c:if>
                    <div class="col-12">
                        <div class="spec-item-label">Shoot Location</div>
                        <div class="spec-item-value">
                            <c:out value="${booking.location}"/>
                        </div>
                    </div>
                    <c:if test="${not empty booking.notes}">
                        <div class="col-12">
                            <div class="spec-item-label">Additional Notes</div>
                            <div style="font-size: 0.95rem; color: var(--muted); line-height: 1.6;">
                                <c:out value="${booking.notes}"/>
                            </div>
                        </div>
                    </c:if>
                </div>

                <!-- ── Deposit Information Section ────────────────────────── -->
                <c:if test="${booking.status == 'ACCEPTED' || (deposit != null && deposit.status == 'PAID')}">
                    <div class="deposit-workspace-box">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
                            <h3 class="editorial-heading" style="font-size: 1.3rem; margin: 0;">Deposit &amp; Payment Status</h3>
                            <c:if test="${deposit != null}">
                                <span class="status-badge ${deposit.status}"><c:out value="${deposit.status}"/></span>
                            </c:if>
                        </div>

                        <c:choose>
                            <c:when test="${booking.status == 'ACCEPTED' && (deposit == null || deposit.status != 'PAID')}">
                                <p style="color: var(--muted); margin-bottom: 1.5rem; line-height: 1.6;">
                                    A 30% deposit is required to secure your reserved shoot slot with this creator.
                                    <c:if test="${deposit != null}">
                                        <br><strong style="color: var(--text); font-size: 1.05rem;">Deposit Amount: <fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</strong>
                                    </c:if>
                                </p>
                                <a href="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/checkout" class="btn btn-primary" id="btn-pay-deposit">
                                    Pay 30% Deposit &rarr;
                                </a>
                            </c:when>

                            <c:when test="${deposit.status == 'PAID'}">
                                <div class="pc-detail-grid" style="gap: 1.5rem; margin-bottom: 1.5rem;">
                                    <div>
                                        <div class="spec-item-label">Deposit Amount Paid</div>
                                        <div class="spec-item-value" style="color: var(--success);">
                                            <fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND
                                        </div>
                                    </div>
                                    <div>
                                        <div class="spec-item-label">Transaction Reference</div>
                                        <div class="spec-item-value" style="font-size: 1rem; font-family: monospace;">
                                            <c:out value="${deposit.paymentReference}" />
                                        </div>
                                    </div>
                                    <div style="grid-column: span 2;">
                                        <div class="spec-item-label">Paid At</div>
                                        <c:choose>
                                            <c:when test="${not empty deposit.paidAt}">
                                                <fmt:parseDate value="${fn:substring(deposit.paidAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedPaidAt" type="date" />
                                                <div class="spec-item-value">
                                                    <fmt:formatDate value="${parsedPaidAt}" pattern="MMMM d, yyyy" />
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div style="font-size: 1.1rem;">Recorded</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <a href="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/receipt" class="primary-link">
                                    View Official Payment Receipt &rarr;
                                </a>
                            </c:when>
                        </c:choose>
                    </div>
                <!-- ── Finished Photography Delivery Section ──────────────── -->
                <c:if test="${booking.status == 'DELIVERED' || booking.status == 'COMPLETED'}">
                    <div class="review-workspace-box" style="margin-top: 1.5rem; background: rgba(37, 99, 235, 0.04); border-color: rgba(37, 99, 235, 0.2);">
                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                            <div>
                                <h3 class="editorial-heading" style="font-size: 1.3rem; margin-bottom: 0.35rem; color: var(--accent);">
                                    Finished Photographs Delivered
                                </h3>
                                <p style="color: var(--muted); margin-bottom: 0; line-height: 1.5; font-size: 0.92rem;">
                                    Your photographer has delivered your edited photo collection. Preview and download full-resolution deliverables.
                                </p>
                            </div>
                            <a href="${pageContext.request.contextPath}/bookings/${booking.id}/delivery" class="btn btn-primary" id="btn-view-delivered-photos">
                                Open Delivery Gallery &rarr;
                            </a>
                        </div>
                    </div>
                </c:if>

                <!-- ── Review & Rating Section ────────────────────────────── -->
                <c:if test="${booking.status == 'COMPLETED'}">
                    <div class="review-workspace-box">
                        <h3 class="editorial-heading" style="font-size: 1.3rem; margin-bottom: 1.25rem;">Client Review</h3>
                        <c:choose>
                            <c:when test="${review == null}">
                                <p style="color: var(--muted); margin-bottom: 1.5rem; line-height: 1.6;">
                                    Your photoshoot session has concluded! Share your experience with <c:out value="${booking.photographerName}"/> to help future clients.
                                </p>
                                <a href="${pageContext.request.contextPath}/bookings/${booking.id}/review" class="btn btn-primary" id="btn-leave-review">
                                    Leave a Review
                                </a>
                            </c:when>
                            <c:otherwise>
                                <div>
                                    <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap;">
                                        <div style="color: var(--warning); font-size: 1.25rem; letter-spacing: 0.1em;">
                                            <c:forEach begin="1" end="${review.rating}">★</c:forEach><c:forEach begin="${review.rating + 1}" end="5">☆</c:forEach>
                                        </div>
                                        <span style="font-weight: 600; font-size: 1.05rem;">${review.rating} / 5 Stars</span>
                                        <c:if test="${not empty review.createdAt}">
                                            <fmt:parseDate value="${fn:substring(review.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedReviewAt" type="date" />
                                            <span style="color: var(--muted); font-size: 0.85rem;">
                                                Reviewed on <fmt:formatDate value="${parsedReviewAt}" pattern="MMM d, yyyy" />
                                            </span>
                                        </c:if>
                                        <c:if test="${review.status == 'HIDDEN'}">
                                            <span class="status-badge status-hidden">Hidden by moderation</span>
                                        </c:if>
                                    </div>
                                    <c:choose>
                                        <c:when test="${not empty review.comment}">
                                            <div style="color: var(--text); font-size: 0.98rem; line-height: 1.65; font-style: italic; background: var(--surface); padding: 1.25rem 1.5rem; border-left: 3px solid var(--accent); border-radius: var(--radius-sm);">
                                                &ldquo;<c:out value="${review.comment}"/>&rdquo;
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="color: var(--muted); font-style: italic; font-size: 0.9rem;">No written comment provided.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- ── Rescheduling Management ──────────────────────────────── -->
                <c:if test="${booking.status == 'ACCEPTED' || booking.status == 'DEPOSIT_PENDING' || booking.status == 'CONFIRMED'}">
                    <div style="padding-top: 2rem; border-top: 1px solid var(--border); margin-top: 2rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.25rem;">
                            <div>
                                <h3 class="editorial-heading" style="font-size: 1.15rem; margin: 0 0 0.25rem 0;">
                                    Session Rescheduling
                                </h3>
                                <div style="color: var(--muted); font-size: 0.85rem;">
                                    Need to adjust date or time? Submit a reschedule proposal for creator confirmation.
                                </div>
                            </div>
                            <c:if test="${empty pendingReschedule}">
                                <button type="button" class="btn btn-secondary btn-sm" id="btn-toggle-reschedule" onclick="var el = document.getElementById('reschedule-collapse'); el.style.display = (el.style.display === 'none' ? 'block' : 'none');">
                                    Request Reschedule
                                </button>
                            </c:if>
                        </div>

                        <!-- Active Pending Reschedule Banner -->
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
                                            Proposed by: <strong><c:out value="${pendingReschedule.requestedByName}"/></strong>
                                            <c:if test="${not empty pendingReschedule.reason}">
                                                &middot; Note: <em>&ldquo;<c:out value="${pendingReschedule.reason}"/>&rdquo;</em>
                                            </c:if>
                                        </div>
                                    </div>
                                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                                        <c:choose>
                                            <c:when test="${pendingReschedule.requestedByUserId == sessionScope.user.id}">
                                                <form action="${pageContext.request.contextPath}/bookings/${booking.id}/reschedule/${pendingReschedule.id}/cancel" method="post" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-secondary btn-sm" id="btn-cancel-reschedule" onclick="return confirm('Withdraw this reschedule request?');">
                                                        Withdraw Request
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form action="${pageContext.request.contextPath}/bookings/${booking.id}/reschedule/${pendingReschedule.id}/accept" method="post" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-primary btn-sm" id="btn-accept-reschedule">
                                                        Accept Reschedule
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/bookings/${booking.id}/reschedule/${pendingReschedule.id}/reject" method="post" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-danger btn-sm" id="btn-reject-reschedule" onclick="return confirm('Decline this reschedule proposal?');">
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
                        <div id="reschedule-collapse" style="display: none; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 1.25rem; margin-bottom: 1.25rem;">
                            <form action="${pageContext.request.contextPath}/bookings/${booking.id}/reschedule" method="post">
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
                                        <input type="text" name="reason" placeholder="e.g. Schedule adjustment, weather..." class="pc-input" maxlength="500" style="width: 100%; padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border);" />
                                    </div>
                                </div>
                                <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="document.getElementById('reschedule-collapse').style.display = 'none';">Cancel</button>
                                    <button type="submit" class="btn btn-primary btn-sm" id="btn-submit-reschedule">Submit Proposal</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:if>

                <!-- ── Cancellation Action (PENDING, ACCEPTED, DEPOSIT_PENDING, CONFIRMED) ── -->
                <c:if test="${booking.status == 'PENDING' || booking.status == 'ACCEPTED' || booking.status == 'DEPOSIT_PENDING' || booking.status == 'CONFIRMED'}">
                    <div style="padding-top: 2rem; border-top: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1.5rem; margin-top: 2rem;">
                        <div>
                            <div style="font-weight: 600; font-size: 0.95rem; color: var(--text); margin-bottom: 0.25rem;">Need to Cancel This Session?</div>
                            <div style="font-size: 0.82rem; color: var(--muted); max-width: 500px; line-height: 1.5;">
                                Cancellations 48h or more prior to session start are eligible for full refund credit. Cancellations within 48h forfeit deposit per marketplace policy.
                            </div>
                        </div>
                        <form action="${pageContext.request.contextPath}/bookings/${booking.id}/cancel" method="post" onsubmit="return confirm('Are you sure you want to cancel this photography booking?');" style="margin: 0; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <input type="text" name="reason" placeholder="Reason (optional)" class="pc-input" style="padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border); font-size: 0.85rem; max-width: 200px;" />
                            <button type="submit" class="btn btn-danger btn-sm" id="btn-cancel-booking">
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
                &copy; 2026 PhotoConnect. Photography Session Workspace.
            </div>
        </div>
    </footer>
