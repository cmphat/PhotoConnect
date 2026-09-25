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

                <!-- ── Cancellation Action (Only when PENDING or ACCEPTED) ── -->
                <c:if test="${booking.status == 'PENDING' || booking.status == 'ACCEPTED'}">
                    <div style="padding-top: 2rem; border-top: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                        <div style="font-size: 0.85rem; color: var(--muted);">
                            Need to modify plans? You may cancel this session while it remains unfulfilled.
                        </div>
                        <form action="${pageContext.request.contextPath}/bookings/${booking.id}/cancel" method="post" onsubmit="return confirm('Are you sure you want to cancel this photography booking?');" style="margin: 0;">
                            <%@ include file="fragments/csrf-input.jsp" %>
                            <button type="submit" class="btn btn-danger" id="btn-cancel-booking">
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
