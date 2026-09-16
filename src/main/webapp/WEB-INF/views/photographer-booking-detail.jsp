<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Booking Request Detail - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 800px;">
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/photographer/bookings" class="text-link" style="font-size: 0.9rem;">&larr; Back to Requests</a>
            </div>
            
            <h1 class="editorial-title" style="margin-bottom: 1rem;">Booking Details</h1>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(52, 211, 153, 0.2);">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(239, 68, 68, 0.2);">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <div style="border: 1px solid var(--border); padding: clamp(1.5rem, 5vw, 3rem); border-radius: 0;">
                <div class="pc-stack-mobile" style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1.5rem; margin-bottom: 3rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border-dark);">
                    <div>
                        <h2 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Request from <c:out value="${booking.customerName}"/></h2>
                        <c:if test="${not empty booking.createdAt}">
                            <fmt:parseDate value="${fn:substring(booking.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedCreatedAt" type="date" />
                            <p style="color: var(--text-muted);">Submitted on <fmt:formatDate value="${parsedCreatedAt}" pattern="MMM d, yyyy" /></p>
                        </c:if>
                    </div>
                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 0.75rem;">
                        <span style="font-size: 0.85rem; letter-spacing: 0.05em; text-transform: uppercase; color: var(--text-muted);">
                            Status: <strong style="color: var(--text-color);">${booking.status}</strong>
                        </span>
                        <a href="${pageContext.request.contextPath}/bookings/${booking.id}/chat" class="btn btn-secondary btn-sm" style="display: inline-flex; align-items: center; gap: 0.5rem;">
                            💬 Open Chat
                        </a>
                    </div>
                </div>

                <div class="pc-detail-grid" style="margin-bottom: 3rem;">
                    <div>
                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Date & Time</div>
                        <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                        <div style="font-size: 1.1rem;"><fmt:formatDate value="${parsedBookingDate}" pattern="MMM d, yyyy" /> at ${booking.bookingTime}</div>
                    </div>
                    <div>
                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Agreed Rate</div>
                        <div style="font-size: 1.1rem;"><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND</div>
                    </div>
                    <div style="grid-column: span 2;">
                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Shoot Location</div>
                        <div style="font-size: 1.1rem;"><c:out value="${booking.location}"/></div>
                    </div>
                    <c:if test="${not empty booking.notes}">
                        <div style="grid-column: span 2;">
                            <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Additional Notes</div>
                            <div style="font-size: 1rem; color: var(--text-muted); line-height: 1.6;"><c:out value="${booking.notes}"/></div>
                        </div>
                    </c:if>
                </div>

                <c:if test="${booking.status == 'PENDING'}">
                    <div class="pc-stack-mobile" style="margin-top: 3rem; padding-top: 2rem; border-top: 1px solid var(--border-dark); display: flex; gap: 1rem; align-items: center;">
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/accept" method="post" style="margin: 0;">
                            <button type="submit" class="btn btn-primary">Accept Booking</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/reject" method="post" onsubmit="return confirm('Are you sure you want to reject this request?');" style="margin: 0;">
                            <button type="submit" class="btn btn-danger">Reject Request</button>
                        </form>
                    </div>
                </c:if>
                <c:if test="${booking.status == 'ACCEPTED'}">
                    <div style="margin-top: 3rem; padding-top: 2rem; border-top: 1px solid var(--border-dark);">
                        <form action="${pageContext.request.contextPath}/photographer/bookings/${booking.id}/complete" method="post" onsubmit="return confirm('Mark this shoot as completed?');" style="margin: 0;">
                            <button type="submit" class="btn btn-primary">Mark as Completed</button>
                        </form>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</body>
</html>
