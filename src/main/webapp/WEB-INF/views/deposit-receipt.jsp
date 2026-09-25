<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Payment Receipt - PhotoConnect</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/payment.css">
</head>
    <main class="payment-main receipt-main">
        <div class="receipt-toolbar">
            <a href="${pageContext.request.contextPath}/bookings/${booking.id}">&larr; Back to Booking</a>
            <button type="button" class="btn btn-secondary" onclick="window.print()">Print / Save as PDF</button>
        </div>
        <article class="receipt" aria-labelledby="receipt-title">
            <header class="receipt-header">
                <div>
                    <span class="receipt-brand">PhotoConnect</span>
                    <h1 id="receipt-title">Payment Receipt</h1>
                </div>
                <div class="receipt-status"><span>Payment status</span><strong class="status-badge ${deposit.status}"><c:out value="${deposit.status}" /></strong></div>
            </header>

            <div class="receipt-reference">
                <span>Transaction reference</span>
                <strong><c:out value="${deposit.paymentReference}" /></strong>
            </div>

            <section class="receipt-section">
                <h2>Transaction</h2>
                <dl class="receipt-grid">
                    <div><dt>Payment method</dt><dd><c:choose><c:when test="${not empty deposit.paymentMethod}"><c:out value="${deposit.paymentMethod.displayName}" /></c:when><c:otherwise>Demo payment (legacy)</c:otherwise></c:choose></dd></div>
                    <div><dt>Payment timestamp</dt><dd><c:choose><c:when test="${not empty deposit.paidAt}"><c:out value="${fn:substring(deposit.paidAt, 0, 16)}" /></c:when><c:otherwise>Recorded</c:otherwise></c:choose></dd></div>
                    <div><dt>Customer</dt><dd><c:out value="${booking.customerName}" /></dd></div>
                    <div><dt>Photographer</dt><dd><c:out value="${booking.photographerName}" /></dd></div>
                </dl>
            </section>

            <section class="receipt-section">
                <h2>Booking</h2>
                <dl class="receipt-grid">
                    <div><dt>Booking ID</dt><dd>#<c:out value="${booking.id}" /></dd></div>
                    <div><dt>Session title</dt><dd><c:out value="${booking.sessionTitle}" /></dd></div>
                    <div><dt>Session date</dt><dd><c:out value="${booking.bookingDate}" /></dd></div>
                    <div><dt>Session time</dt><dd><c:out value="${booking.bookingTime}" /></dd></div>
                </dl>
            </section>

            <section class="receipt-section receipt-totals">
                <h2>Payment summary</h2>
                <div><span>Agreed price</span><strong><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND</strong></div>
                <div><span>Deposit percentage</span><strong>30%</strong></div>
                <div class="receipt-paid"><span>Deposit paid</span><strong><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</strong></div>
                <div><span>Remaining balance</span><strong><fmt:formatNumber value="${remainingBalance}" pattern="#,##0" /> VND</strong></div>
            </section>

            <footer class="receipt-footer">
                <strong>Demo transaction — no real money was transferred.</strong>
                <span>This receipt documents a PhotoConnect local demonstration only.</span>
            </footer>
        </article>
        <div class="receipt-links">
            <a href="${pageContext.request.contextPath}/bookings/${booking.id}">Back to Booking</a>
            <a href="${pageContext.request.contextPath}/bookings">My Bookings</a>
        </div>
    </main>
