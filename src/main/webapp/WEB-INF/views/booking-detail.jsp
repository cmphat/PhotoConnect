<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Booking Detail - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 3rem;">
        <div style="max-width: 800px; margin: 0 auto;">
            <a href="/bookings" style="display: inline-block; margin-bottom: 2rem; font-size: 0.9rem; text-decoration: none;">&larr; Back to My Bookings</a>
            
            <h1 class="editorial-title" style="font-size: 2.5rem; margin-bottom: 1rem;">Booking Details</h1>

            <c:if test="${not empty successMessage}">
                <div class="pc-alert-warning" style="background: rgba(40, 167, 69, 0.1); border-color: rgba(40, 167, 69, 0.2); color: #28a745;">
                    ${successMessage}
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert-warning" style="color: #dc3545; border-color: rgba(220,53,69,0.2);">
                    ${errorMessage}
                </div>
            </c:if>

            <div class="pc-content-section">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border);">
                    <div>
                        <h2 style="font-size: 1.5rem; margin-bottom: 0.5rem;">Session with ${booking.photographerName}</h2>
                        <p style="color: var(--text-muted);">Created on ${booking.createdAt}</p>
                    </div>
                    <span class="pc-badge-${fn:toLowerCase(booking.status)}">${booking.status}</span>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; margin-bottom: 2rem;">
                    <div>
                        <span class="pc-label">Date & Time</span>
                        <div style="font-size: 1.1rem;">${booking.bookingDate} at ${booking.bookingTime}</div>
                    </div>
                    <div>
                        <span class="pc-label">Agreed Price</span>
                        <div style="font-size: 1.1rem;">$<fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0.00" /></div>
                    </div>
                    <div style="grid-column: span 2;">
                        <span class="pc-label">Location</span>
                        <div style="font-size: 1.1rem;">${booking.location}</div>
                    </div>
                    <c:if test="${not empty booking.notes}">
                        <div style="grid-column: span 2;">
                            <span class="pc-label">Additional Notes</span>
                            <div style="font-size: 1rem; color: var(--text-muted); line-height: 1.6;">${booking.notes}</div>
                        </div>
                    </c:if>
                </div>

                <c:if test="${booking.status == 'ACCEPTED'}">
                    <div style="margin-top: 2rem; padding: 2rem; background: rgba(255,255,255,0.02); border: 1px solid var(--border); border-radius: 8px;">
                        <h3 style="font-size: 1.2rem; margin-bottom: 1rem; color: var(--text-color);">Deposit Information</h3>
                        <c:choose>
                            <c:when test="${deposit == null || deposit.status == 'PENDING'}">
                                <p style="color: var(--text-muted); margin-bottom: 1.5rem;">
                                    A 30% deposit is required to secure this booking.
                                    <c:if test="${deposit != null}">
                                        <br><strong>Deposit Amount: $<fmt:formatNumber value="${deposit.amount}" pattern="#,##0.00" /></strong>
                                    </c:if>
                                </p>
                                <a href="/bookings/${booking.id}/deposit" class="pc-btn-primary" style="display: inline-block;">Pay Deposit</a>
                            </c:when>
                            <c:when test="${deposit.status == 'PAID'}">
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div>
                                        <span class="pc-label">Deposit Paid</span>
                                        <div style="font-size: 1.1rem; color: #28a745;">$<fmt:formatNumber value="${deposit.amount}" pattern="#,##0.00" /></div>
                                    </div>
                                    <div>
                                        <span class="pc-label">Payment Reference</span>
                                        <div style="font-size: 1.1rem;">${deposit.paymentReference}</div>
                                    </div>
                                    <div>
                                        <span class="pc-label">Paid At</span>
                                        <div style="font-size: 1.1rem;">${deposit.paidAt}</div>
                                    </div>
                                </div>
                            </c:when>
                        </c:choose>
                    </div>
                </c:if>

                <c:if test="${booking.status == 'PENDING' || booking.status == 'ACCEPTED'}">
                    <div style="margin-top: 3rem; padding-top: 2rem; border-top: 1px solid var(--border);">
                        <form action="/bookings/${booking.id}/cancel" method="post" onsubmit="return confirm('Are you sure you want to cancel this booking?');">
                            <button type="submit" class="pc-btn-outline" style="color: #dc3545 !important; border-color: rgba(220,53,69,0.5);">Cancel Booking</button>
                        </form>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</body>
</html>
