<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Booking Requests - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 3rem;">
        <div style="max-width: 1000px; margin: 0 auto;">
            <h1 class="editorial-title" style="font-size: 3rem; margin-bottom: 2rem;">Booking Requests</h1>

            <c:if test="${not empty successMessage}">
                <div class="pc-alert-warning" style="background: rgba(40, 167, 69, 0.1); border-color: rgba(40, 167, 69, 0.2); color: #28a745;">
                    ${successMessage}
                </div>
            </c:if>

            <c:if test="${empty bookings}">
                <div class="pc-empty-state">
                    <h3 class="pc-empty-title">No requests yet</h3>
                    <p class="pc-empty-desc">You don't have any booking requests at the moment.</p>
                </div>
            </c:if>

            <c:if test="${not empty bookings}">
                <div style="display: grid; gap: 1.5rem;">
                    <c:forEach var="booking" items="${bookings}">
                        <div class="pc-content-section" style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <h3 style="margin-bottom: 0.5rem; font-size: 1.25rem;">Request from ${booking.customerName}</h3>
                                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 0.5rem;">
                                    ${booking.bookingDate} at ${booking.bookingTime} • ${booking.location}
                                </p>
                                <span class="pc-badge-pending" style="
                                    <c:if test="${booking.status == 'PENDING'}">color: #ffc107; border-color: rgba(255,193,7,0.3);</c:if>
                                    <c:if test="${booking.status == 'ACCEPTED'}">color: #28a745; border-color: rgba(40,167,69,0.3);</c:if>
                                    <c:if test="${booking.status == 'REJECTED' || booking.status == 'CANCELLED'}">color: #dc3545; border-color: rgba(220,53,69,0.3);</c:if>
                                    <c:if test="${booking.status == 'COMPLETED'}">color: #17a2b8; border-color: rgba(23,162,184,0.3);</c:if>
                                ">${booking.status}</span>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 500; font-size: 1.2rem; margin-bottom: 1rem;">
                                    $<fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0.00" />
                                </div>
                                <a href="/photographer/bookings/${booking.id}" class="pc-btn-outline" style="padding: 8px 16px;">View Details</a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </main>
</body>
</html>
