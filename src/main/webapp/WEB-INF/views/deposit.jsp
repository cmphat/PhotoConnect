<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Demo Checkout - PhotoConnect</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/payment.css">
    <script src="${pageContext.request.contextPath}/assets/js/demo-checkout.js" defer></script>
</head>
<body class="payment-page">
    <jsp:include page="fragments/navbar.jsp" />

    <main class="payment-main">
        <div class="payment-shell">
            <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="payment-back">&larr; Back to booking</a>

            <header class="checkout-header">
                <div>
                    <span class="payment-eyebrow">Secure demo checkout</span>
                    <h1>Complete your deposit</h1>
                    <p>Reserve your photography session with the required 30% deposit.</p>
                </div>
                <div class="demo-notice" role="note">
                    <strong>Demo Payment Environment</strong>
                    <span>No real money will be transferred.</span>
                </div>
            </header>

            <div class="checkout-grid">
                <section class="checkout-panel" aria-labelledby="payment-method-heading">
                    <div class="panel-heading">
                        <span>01</span>
                        <div>
                            <h2 id="payment-method-heading">Payment method</h2>
                            <p>Choose a local simulation option for this demonstration.</p>
                        </div>
                    </div>

                    <form id="demo-payment-form" action="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/process" method="post" novalidate>
                        <div class="method-selector" role="radiogroup" aria-label="Demo payment method">
                            <label class="method-option">
                                <input type="radio" name="paymentMethod" value="DEMO_QR" checked>
                                <span class="method-marker" aria-hidden="true"></span>
                                <span>
                                    <strong>Demo QR / Bank Transfer</strong>
                                    <small>Scan a harmless PhotoConnect demo payload.</small>
                                </span>
                            </label>
                            <label class="method-option">
                                <input type="radio" name="paymentMethod" value="DEMO_CARD">
                                <span class="method-marker" aria-hidden="true"></span>
                                <span>
                                    <strong>Demo Card</strong>
                                    <small>Use a documented fictional test scenario.</small>
                                </span>
                            </label>
                        </div>

                        <div class="payment-method-panel" data-payment-panel="DEMO_QR">
                            <div class="qr-layout">
                                <div class="qr-frame">
                                    <img src="${demoQrDataUri}" alt="PhotoConnect demo QR for booking ${booking.id}" width="240" height="240">
                                    <span>DEMO</span>
                                </div>
                                <div class="qr-details">
                                    <span class="payment-eyebrow">Demo QR — no real transfer</span>
                                    <dl>
                                        <div><dt>Amount</dt><dd><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</dd></div>
                                        <div><dt>Booking</dt><dd>#<c:out value="${booking.id}" /></dd></div>
                                        <div><dt>Reference</dt><dd><c:out value="${deposit.paymentReference}" /></dd></div>
                                    </dl>
                                    <details>
                                        <summary>View encoded demo payload</summary>
                                        <code><c:out value="${demoQrPayload}" /></code>
                                    </details>
                                </div>
                            </div>
                        </div>

                        <div class="payment-method-panel" data-payment-panel="DEMO_CARD" hidden>
                            <div class="card-demo-note">
                                <strong>Fictional test values only.</strong>
                                Card details are evaluated in memory and are never stored, logged, or transmitted.
                            </div>
                            <div class="card-form-grid">
                                <label class="payment-field payment-field-wide">
                                    <span>Cardholder name</span>
                                    <input type="text" name="cardholderName" maxlength="120" autocomplete="off" placeholder="Demo Customer" data-card-field>
                                </label>
                                <label class="payment-field payment-field-wide">
                                    <span>Card number</span>
                                    <input type="text" name="cardNumber" maxlength="19" inputmode="numeric" autocomplete="off" placeholder="4242 4242 4242 4242" data-card-number data-card-field>
                                </label>
                                <label class="payment-field">
                                    <span>Expiry</span>
                                    <input type="text" name="expiry" maxlength="5" inputmode="numeric" autocomplete="off" placeholder="12/30" data-card-field>
                                </label>
                                <label class="payment-field">
                                    <span>CVV</span>
                                    <input type="password" name="cvv" maxlength="4" inputmode="numeric" autocomplete="off" placeholder="123" data-card-field>
                                </label>
                            </div>
                            <div class="test-scenarios">
                                <span><strong>Success:</strong> 4242 4242 4242 4242</span>
                                <span><strong>Declined:</strong> 4000 0000 0000 0002</span>
                            </div>
                        </div>

                        <div class="checkout-actions">
                            <button type="submit" class="payment-primary" data-submit-payment>
                                <span data-submit-label>Confirm Demo Payment</span>
                            </button>
                            <p>By continuing, you confirm this is a demonstration transaction only.</p>
                        </div>
                    </form>

                    <form action="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/cancel" method="post" class="cancel-payment-form">
                        <button type="submit" class="payment-cancel-btn btn btn-secondary">Cancel payment and return</button>
                    </form>
                </section>

                <aside class="order-summary" aria-labelledby="order-summary-heading">
                    <div class="panel-heading compact">
                        <span>02</span>
                        <div><h2 id="order-summary-heading">Order summary</h2></div>
                    </div>
                    <div class="booking-summary">
                        <span class="payment-eyebrow">Booking #<c:out value="${booking.id}" /></span>
                        <h3><c:out value="${booking.sessionTitle}" /></h3>
                        <p>with <c:out value="${booking.photographerName}" /></p>
                        <dl>
                            <div><dt>Date</dt><dd><c:out value="${booking.bookingDate}" /></dd></div>
                            <div><dt>Time</dt><dd><c:out value="${booking.bookingTime}" /></dd></div>
                            <div><dt>Location</dt><dd><c:out value="${booking.location}" /></dd></div>
                            <div><dt>Status</dt><dd><span class="status-badge ${booking.status}"><c:out value="${booking.status}" /></span></dd></div>
                        </dl>
                    </div>
                    <div class="price-summary">
                        <div><span>Agreed booking price</span><strong><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND</strong></div>
                        <div><span>Deposit percentage</span><strong>30%</strong></div>
                        <div class="price-due"><span>Deposit due now</span><strong><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</strong></div>
                        <div><span>Remaining balance</span><strong><fmt:formatNumber value="${remainingBalance}" pattern="#,##0" /> VND</strong></div>
                    </div>
                    <div class="summary-assurance">
                        Amount and booking ownership are verified by PhotoConnect on the server.
                    </div>
                </aside>
            </div>
        </div>

        <div class="processing-overlay" data-processing-overlay hidden role="status" aria-live="polite">
            <div class="processing-dialog">
                <span class="processing-spinner" aria-hidden="true"></span>
                <h2>Processing your demo payment...</h2>
                <p>Please keep this window open. No real payment is taking place.</p>
            </div>
        </div>
    </main>
</body>
</html>
