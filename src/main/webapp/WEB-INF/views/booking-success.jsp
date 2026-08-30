<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Booking request submitted successfully on PhotoConnect.">
    <title>Booking Submitted – PhotoConnect</title>
    
    <!-- Bootstrap Grid -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main class="py-5">
        <div class="container">
            
            <div class="pc-success-card">
                
                <div class="pc-success-icon-wrap">
                    ✓
                </div>

                <div class="pc-hero-tag mb-3 border-0">
                    <span class="pc-hero-tag-dot bg-success"></span>
                    <span class="text-success">Request Submitted</span>
                </div>

                <h1 class="pc-hero-title h2 mb-3">Booking Request Sent</h1>
                
                <p class="text-muted mb-4" style="max-width: 520px; margin: 0 auto; line-height: 1.6;">
                    Your shoot request has been received and routed to <strong class="text-light"><c:out value="${booking.photographerDisplayName}"/></strong>. You will be contacted once the artist reviews your booking.
                </p>

                <!-- Booking Details Table -->
                <div class="pc-booking-details-table text-start" style="max-width: 520px; margin: 0 auto 32px auto; background: var(--surface-soft); border-radius: var(--radius-md); padding: 24px;">
                    
                    <div class="d-flex justify-content-between mb-3 border-bottom border-secondary border-opacity-25 pb-3">
                        <span class="text-muted" style="font-size: 0.9rem;">Reference ID</span>
                        <span class="font-monospace">#BK-<c:out value="${booking.id}"/></span>
                    </div>

                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted" style="font-size: 0.9rem;">Photographer</span>
                        <span><c:out value="${booking.photographerDisplayName}"/></span>
                    </div>

                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted" style="font-size: 0.9rem;">Shoot Date</span>
                        <span><c:out value="${booking.bookingDate}"/></span>
                    </div>

                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted" style="font-size: 0.9rem;">Preferred Time</span>
                        <span><c:out value="${booking.bookingTime}"/></span>
                    </div>

                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted" style="font-size: 0.9rem;">Shoot Location</span>
                        <span class="text-end" style="max-width: 250px;"><c:out value="${booking.location}"/></span>
                    </div>

                    <c:if test="${not empty booking.notes}">
                        <div class="d-flex justify-content-between mb-3">
                            <span class="text-muted" style="font-size: 0.9rem;">Notes</span>
                            <span class="text-muted fst-italic text-end" style="max-width: 250px;"><c:out value="${booking.notes}"/></span>
                        </div>
                    </c:if>

                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted" style="font-size: 0.9rem;">Agreed Rate Snapshot</span>
                        <span style="font-weight: 500;">
                            <fmt:formatNumber value="${booking.agreedPrice}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                        </span>
                    </div>

                    <div class="d-flex justify-content-between pt-3 border-top border-secondary border-opacity-25">
                        <span class="text-muted" style="font-size: 0.9rem;">Current Status</span>
                        <span class="pc-badge-pending">
                            ● <c:out value="${booking.status}"/>
                        </span>
                    </div>

                </div>

                <!-- Next Actions -->
                <div class="d-flex flex-wrap align-items-center justify-content-center gap-3 pt-2">
                    <a href="/photographers" class="pc-btn-primary" id="btn-explore-more">
                        Explore More Artists
                    </a>
                    <a href="/" class="pc-btn-outline">
                        Back to Home
                    </a>
                </div>

            </div>

        </div>
    </main>

    <footer class="pc-footer">
        <div class="container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
