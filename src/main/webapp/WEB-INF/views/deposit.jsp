<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pay Deposit - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 3rem;">
        <div style="max-width: 600px; margin: 0 auto;">
            <a href="/bookings/${booking.id}" style="display: inline-block; margin-bottom: 2rem; font-size: 0.9rem; text-decoration: none;">&larr; Back to Booking</a>
            
            <h1 class="editorial-title" style="font-size: 2.5rem; margin-bottom: 1rem;">Pay Deposit</h1>

            <c:if test="${not empty errorMessage}">
                <div class="pc-alert-warning" style="color: #dc3545; border-color: rgba(220,53,69,0.2);">
                    ${errorMessage}
                </div>
            </c:if>

            <div class="pc-content-section">
                <div style="margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border);">
                    <h2 style="font-size: 1.5rem; margin-bottom: 0.5rem;">Session with ${booking.photographerName}</h2>
                    <p style="color: var(--text-muted);">${booking.bookingDate} at ${booking.bookingTime}</p>
                </div>

                <div style="margin-bottom: 2rem;">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                        <span style="color: var(--text-muted);">Total Agreed Price</span>
                        <span>$<fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0.00" /></span>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                        <span style="color: var(--text-muted);">Deposit Rate</span>
                        <span>30%</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-size: 1.25rem; font-weight: 600; padding-top: 1rem; border-top: 1px solid var(--border);">
                        <span>Deposit Amount Due</span>
                        <span>$<fmt:formatNumber value="${deposit.amount}" pattern="#,##0.00" /></span>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${deposit.status == 'PENDING'}">
                        <div style="background: rgba(255, 193, 7, 0.05); border: 1px solid rgba(255, 193, 7, 0.2); padding: 1.5rem; border-radius: 8px; margin-bottom: 2rem;">
                            <h4 style="color: #ffc107; margin-bottom: 0.5rem; font-size: 1rem;">Development payment simulation</h4>
                            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5; margin: 0;">
                                This is a simulated development payment flow. No real money is transferred. Clicking the button below will immediately mark this deposit as PAID.
                            </p>
                        </div>

                        <form action="/bookings/${booking.id}/deposit/simulate-payment" method="post">
                            <button type="submit" class="pc-btn-primary" style="width: 100%;">Simulate Payment</button>
                        </form>
                    </c:when>
                    <c:when test="${deposit.status == 'PAID'}">
                        <div style="text-align: center; padding: 2rem; background: rgba(40, 167, 69, 0.05); border: 1px solid rgba(40, 167, 69, 0.2); border-radius: 8px;">
                            <div style="color: #28a745; font-size: 3rem; margin-bottom: 1rem;">&#10003;</div>
                            <h3 style="color: #28a745; margin-bottom: 0.5rem;">Deposit Paid</h3>
                            <p style="color: var(--text-muted); margin-bottom: 1.5rem;">Payment Reference: ${deposit.paymentReference}</p>
                            <a href="/bookings/${booking.id}" class="pc-btn-outline">Return to Booking</a>
                        </div>
                    </c:when>
                </c:choose>
            </div>
        </div>
    </main>
</body>
</html>
