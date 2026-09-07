<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
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

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 600px;">
            <div style="margin-bottom: 2rem;">
                <a href="/bookings/${booking.id}" class="text-link" style="font-size: 0.9rem;">&larr; Back to Booking</a>
            </div>
            
            <h1 class="editorial-title" style="margin-bottom: 1rem;">Pay Deposit</h1>

            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(239, 68, 68, 0.2);">
                    ${errorMessage}
                </div>
            </c:if>

            <div style="border: 1px solid var(--border); padding: 3rem; border-radius: 0;">
                <div style="margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border-dark);">
                    <h2 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Session with ${booking.photographerName}</h2>
                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                    <p style="color: var(--text-muted);"><fmt:formatDate value="${parsedBookingDate}" pattern="MMM d, yyyy" /> at ${booking.bookingTime}</p>
                </div>

                <div style="margin-bottom: 3rem;">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                        <span style="color: var(--text-muted);">Total Agreed Price</span>
                        <span><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 1.5rem;">
                        <span style="color: var(--text-muted);">Deposit Rate</span>
                        <span>30%</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-size: 1.5rem; font-weight: 400; padding-top: 1.5rem; border-top: 1px solid var(--border-dark);">
                        <span>Deposit Amount Due</span>
                        <span><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</span>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${deposit.status == 'PENDING'}">
                        <div style="border-left: 2px solid var(--border); padding: 1.5rem; margin-bottom: 2rem; background: var(--bg-dark-secondary);">
                            <h4 style="margin-bottom: 0.5rem; font-size: 1rem; font-weight: 500;">Development payment simulation</h4>
                            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.6; margin: 0;">
                                This is a simulated development payment flow. No real money is transferred. Clicking the button below will immediately mark this deposit as PAID.
                            </p>
                        </div>

                        <form action="/bookings/${booking.id}/deposit/simulate-payment" method="post">
                            <button type="submit" class="submit-btn">Simulate Payment</button>
                        </form>
                    </c:when>
                    <c:when test="${deposit.status == 'PAID'}">
                        <div style="text-align: center; padding: 3rem 2rem; border: 1px solid var(--border); border-radius: 0;">
                            <div style="font-size: 2rem; margin-bottom: 1.5rem;">✓</div>
                            <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Deposit Paid</h3>
                            <p style="color: var(--text-muted); margin-bottom: 2rem;">Payment Reference: ${deposit.paymentReference}</p>
                            <a href="/bookings/${booking.id}" class="pc-btn-outline" style="border: none; border-bottom: 1px solid currentColor; border-radius: 0; padding: 0.5rem 0;">Return to Booking</a>
                        </div>
                    </c:when>
                </c:choose>
            </div>
        </div>
    </main>
</body>
</html>
