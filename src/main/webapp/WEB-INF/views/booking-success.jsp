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
    <meta name="description" content="Booking request submitted successfully on PhotoConnect.">
    <title>Booking Submitted – PhotoConnect</title>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh; display: flex; align-items: center; justify-content: center;">
        <div class="editorial-container" style="max-width: 600px; text-align: center;">
            
            <div style="margin-bottom: 2rem;">
                <div style="display: inline-flex; align-items: center; justify-content: center; width: 64px; height: 64px; border-radius: 50%; border: 1px solid var(--border); font-size: 1.5rem; margin-bottom: 1.5rem;">
                    ✓
                </div>
                
                <h1 class="editorial-title" style="margin-bottom: 1rem;">Booking Request Sent</h1>
                
                <p style="color: var(--text-muted); line-height: 1.6;">
                    Your shoot request has been received and routed to <strong style="color: var(--text-color);"><c:out value="${booking.photographerDisplayName}"/></strong>. You will be contacted once the artist reviews your booking.
                </p>
            </div>

            <!-- Booking Details -->
            <div style="text-align: left; border: 1px solid var(--border); padding: 2rem; border-radius: 0; margin-bottom: 3rem;">
                
                <div style="display: flex; justify-content: space-between; margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid var(--border-dark);">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Reference ID</span>
                    <span style="font-family: monospace;">#BK-<c:out value="${booking.id}"/></span>
                </div>

                <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Photographer</span>
                    <span><c:out value="${booking.photographerDisplayName}"/></span>
                </div>

                <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Shoot Date</span>
                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                    <span><fmt:formatDate value="${parsedBookingDate}" pattern="MMM d, yyyy" /></span>
                </div>

                <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Preferred Time</span>
                    <span><c:out value="${booking.bookingTime}"/></span>
                </div>

                <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Shoot Location</span>
                    <span style="text-align: right; max-width: 250px;"><c:out value="${booking.location}"/></span>
                </div>

                <c:if test="${not empty booking.notes}">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                        <span style="color: var(--text-muted); font-size: 0.9rem;">Notes</span>
                        <span style="color: var(--text-muted); font-style: italic; text-align: right; max-width: 250px;"><c:out value="${booking.notes}"/></span>
                    </div>
                </c:if>

                <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Agreed Rate Snapshot</span>
                    <span style="font-weight: 500;">
                        <fmt:formatNumber value="${booking.agreedPrice}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                    </span>
                </div>

                <div style="display: flex; justify-content: space-between; padding-top: 1rem; border-top: 1px solid var(--border-dark);">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Current Status</span>
                    <span>
                        <c:out value="${booking.status}"/>
                    </span>
                </div>

            </div>

            <!-- Next Actions -->
            <div style="display: flex; align-items: center; justify-content: center; gap: 2rem;">
                <a href="/photographers" class="pc-btn-primary" style="padding: 1rem 2rem;">
                    Explore More Artists
                </a>
                <a href="/" class="text-link">
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
</body>
</html>
