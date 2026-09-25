<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Booking Submitted – PhotoConnect</title>
    <meta name="description" content="Booking request submitted successfully on PhotoConnect.">
</head>

    <main class="pc-page" style="display: flex; align-items: center; justify-content: center;">
        <div class="editorial-container pc-container-copy" style="text-align: center;">

            <div style="margin-bottom: 2rem;">
                <div style="display: inline-flex; align-items: center; justify-content: center; width: 64px; height: 64px; border-radius: 50%; border: 1px solid var(--success-border); background: var(--success-bg); color: var(--success); font-size: 1.5rem; margin-bottom: 1.5rem;">
                    ✓
                </div>

                <h1 class="editorial-title" style="margin-bottom: 1rem;">Booking Request Sent</h1>

                <p style="color: var(--text-muted); line-height: 1.6;">
                    Your shoot request has been received and routed to <strong style="color: var(--text-color);"><c:out value="${booking.photographerDisplayName}"/></strong>. You will be contacted once the artist reviews your booking.
                </p>
            </div>

            <!-- Booking Details -->
            <div class="pc-panel pc-kv-list" style="text-align: left; margin-bottom: 3rem;">

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Reference ID</span>
                    <span style="font-family: monospace;">#BK-<c:out value="${booking.id}"/></span>
                </div>

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Photographer</span>
                    <span><c:out value="${booking.photographerDisplayName}"/></span>
                </div>

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Shoot Date</span>
                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                    <span><fmt:formatDate value="${parsedBookingDate}" pattern="MMM d, yyyy" /></span>
                </div>

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Preferred Time</span>
                    <span><c:out value="${booking.bookingTime}"/></span>
                </div>

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Shoot Location</span>
                    <span style="text-align: right; max-width: 250px;"><c:out value="${booking.location}"/></span>
                </div>

                <c:if test="${not empty booking.notes}">
                    <div class="pc-kv-row">
                        <span style="color: var(--text-muted); font-size: 0.9rem;">Notes</span>
                        <span style="color: var(--text-muted); font-style: italic; text-align: right; max-width: 250px;"><c:out value="${booking.notes}"/></span>
                    </div>
                </c:if>

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Agreed Rate Snapshot</span>
                    <span style="font-weight: 500;">
                        <fmt:formatNumber value="${booking.agreedPrice}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                    </span>
                </div>

                <div class="pc-kv-row">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Current Status</span>
                    <span class="status-badge ${booking.status}"><c:out value="${booking.status}"/></span>
                </div>

            </div>

            <!-- Next Actions -->
            <div class="pc-actions" style="justify-content: center;">
                <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="btn btn-primary">
                    View Booking Details
                </a>
                <a href="${pageContext.request.contextPath}/photographers" class="btn btn-secondary">
                    Explore More Artists
                </a>
                <a href="${pageContext.request.contextPath}/" class="btn btn-ghost">
                    Back to Home
                </a>
            </div>

        </div>
    </main>

    <footer class="pc-footer">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>
