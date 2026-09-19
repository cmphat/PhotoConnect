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
    <title>Payment Result - PhotoConnect</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/payment.css">
</head>
<body class="payment-page">
    <jsp:include page="fragments/navbar.jsp" />
    <main class="payment-main result-main">
        <section class="payment-result-card payment-result-${fn:toLowerCase(deposit.status)}">
            <span class="result-mark" aria-hidden="true"></span>
            <c:choose>
                <c:when test="${deposit.status == 'PAID'}">
                    <span class="payment-eyebrow">Demo payment confirmed</span>
                    <h1>Payment Successful</h1>
                    <p>Your 30% deposit has been recorded and your booking is secured for this demonstration.</p>
                </c:when>
                <c:when test="${deposit.status == 'FAILED'}">
                    <span class="payment-eyebrow">Demo payment not completed</span>
                    <h1>Payment Failed</h1>
                    <p><c:out value="${deposit.failureReason}" /></p>
                </c:when>
                <c:when test="${deposit.status == 'CANCELLED'}">
                    <span class="payment-eyebrow">Demo payment ended</span>
                    <h1>Payment Cancelled</h1>
                    <p>No deposit was recorded as paid. You may return to the booking or start a new demo attempt.</p>
                </c:when>
                <c:otherwise>
                    <span class="payment-eyebrow">Demo payment status</span>
                    <h1><c:out value="${deposit.status}" /></h1>
                    <p>Your payment has not been completed.</p>
                </c:otherwise>
            </c:choose>

            <div class="result-details">
                <div><span>Booking</span><strong>#<c:out value="${booking.id}" /></strong></div>
                <div><span>Deposit</span><strong><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</strong></div>
                <div><span>Reference</span><strong><c:out value="${deposit.paymentReference}" /></strong></div>
                <div><span>Status</span><strong class="status-badge ${deposit.status}"><c:out value="${deposit.status}" /></strong></div>
            </div>

            <div class="result-demo-note">Demo transaction — no real money was transferred.</div>
            <div class="result-actions">
                <c:choose>
                    <c:when test="${deposit.status == 'PAID'}">
                        <a class="payment-primary" href="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/receipt">View Payment Receipt</a>
                    </c:when>
                    <c:when test="${deposit.status == 'FAILED' || deposit.status == 'CANCELLED' || deposit.status == 'PENDING'}">
                        <a class="payment-primary" href="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/checkout">Try Demo Payment Again</a>
                    </c:when>
                </c:choose>
                <a class="payment-secondary" href="${pageContext.request.contextPath}/bookings/${booking.id}">Back to Booking</a>
            </div>
        </section>
    </main>
</body>
</html>
