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
                            <strong>Session Cancelled</strong> &mdash; This session was cancelled by the client.
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
