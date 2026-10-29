<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Pay 30% Escrow Deposit — PhotoConnect</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/payment.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/demo-checkout.js" defer></script>
    <style>
        .bank-details-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 1.25rem;
            margin-top: 1.25rem;
        }
        .bank-data-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.6rem 0;
            border-bottom: 1px dashed #e2e8f0;
        }
        .bank-data-row:last-child {
            border-bottom: none;
        }
        .btn-copy {
            background: rgba(37,99,235,0.08);
            border: 1px solid rgba(37,99,235,0.2);
            color: #2563eb;
            font-size: 0.75rem;
            padding: 0.2rem 0.6rem;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.15s ease;
        }
        .btn-copy:hover {
            background: #2563eb;
            color: #ffffff;
        }
        .countdown-box {
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #dc2626;
            font-weight: 600;
            font-size: 0.88rem;
            padding: 0.6rem 1rem;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 1rem;
        }
    </style>
</head>

<main class="payment-main">
    <div class="payment-shell">
        <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="payment-back">&larr; Back to Booking Details</a>

        <header class="checkout-header">
            <div>
                <span class="payment-eyebrow">Secure Escrow Checkout / VietQR 30% Deposit</span>
                <h1>Confirm Booking Deposit</h1>
                <p>Complete your 30% deposit to officially confirm your scheduled photo session with the photographer.</p>
            </div>
            <c:choose>
                <c:when test="${paymentMode == 'REAL'}">
                    <div class="demo-notice" style="background: rgba(16,185,129,0.1); border-color: rgba(16,185,129,0.3); color: #065f46;" role="note">
                        <strong><i class="bi bi-shield-lock me-1"></i> Official VietQR</strong>
                        <span>Scan the QR code and ensure the transfer note matches exactly.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="demo-notice" role="note">
                        <strong><i class="bi bi-cpu me-1"></i> DEMO PAYMENT ENVIRONMENT — Simulation Mode</strong>
                        <span>No real money will be transferred. For academic evaluation.</span>
                    </div>
                </c:otherwise>
            </c:choose>
        </header>

        <div class="checkout-grid">
            <section class="checkout-panel" aria-labelledby="payment-method-heading">
                <div class="panel-heading">
                    <span>01</span>
                    <div>
                        <h2 id="payment-method-heading">Payment Method</h2>
                        <p>Scan VietQR using any mobile banking app or test via sandbox simulated card.</p>
                    </div>
                </div>

                <!-- Countdown Timer & Expiry Alert -->
                <div class="countdown-box" id="countdownBox">
                    <span><i class="bi bi-clock-history me-1"></i> QR Code Expiry Window:</span>
                    <span id="countdownTimer">15:00</span>
                </div>

                <form id="demo-payment-form" action="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/process" method="post" novalidate>
                    <%@ include file="fragments/csrf-input.jsp" %>
                    <div class="method-selector" role="radiogroup" aria-label="Payment method selection">
                        <label class="method-option">
                            <input type="radio" name="paymentMethod" value="DEMO_QR" checked>
                            <span class="method-marker" aria-hidden="true"></span>
                            <span>
                                <strong>${paymentMode == 'REAL' ? 'VietQR 24/7 Bank Transfer' : 'Demo QR / Bank Transfer'}</strong>
                                <small>Scan VietQR using any banking app (Vietcombank, MB, Techcombank, ACB...).</small>
                            </span>
                        </label>
                        <label class="method-option">
                            <input type="radio" name="paymentMethod" value="DEMO_CARD">
                            <span class="method-marker" aria-hidden="true"></span>
                            <span>
                                <strong>Sandbox Test Card Simulation</strong>
                                <small>Use internal simulated test cards to verify state transitions.</small>
                            </span>
                        </label>
                    </div>

                    <!-- VietQR / Demo QR Panel -->
                    <div class="payment-method-panel" data-payment-panel="DEMO_QR">
                        <div class="qr-layout">
                            <div class="qr-frame" style="text-align: center;">
                                <img src="${qrCodeUrl != null ? qrCodeUrl : demoQrDataUri}"
                                     id="mainQrImage"
                                     alt="VietQR PhotoConnect deposit for booking #${booking.id}"
                                     width="240" height="240"
                                     style="object-fit: contain; border-radius: 4px;">
                                <span id="qrBadgeMode">${paymentMode == 'REAL' ? 'VIETQR' : 'DEMO'}</span>
                            </div>
                            <div class="qr-details">
                                <span class="payment-eyebrow">
                                    ${paymentMode == 'REAL' ? 'Transfer exact amount with memo' : 'Simulated VietQR — No real funds deducted'}
                                </span>
                                <dl>
                                    <div>
                                        <dt>30% Deposit Amount</dt>
                                        <dd style="color: #2563eb; font-weight: 700; font-size: 1.15rem;">
                                            <fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND
                                        </dd>
                                    </div>
                                    <div><dt>Booking Reference</dt><dd>#<c:out value="${booking.id}" /></dd></div>
                                    <div>
                                        <dt>Transfer Memo / Note</dt>
                                        <dd style="font-family: monospace; font-weight: 600; color: #1e293b;">
                                            <c:out value="${deposit.paymentReference}" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Status</dt>
                                        <dd id="paymentStatusBadge">
                                            <span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split me-1"></i> Awaiting Transfer</span>
                                        </dd>
                                    </div>
                                </dl>

                                <!-- Bank Details Card -->
                                <c:if test="${paymentMode == 'REAL'}">
                                <div class="bank-details-card">
                                    <div class="bank-data-row">
                                        <span class="text-muted" style="font-size: 0.85rem;">Beneficiary Bank:</span>
                                        <strong style="font-size: 0.9rem;">${bankId}</strong>
                                    </div>
                                    <div class="bank-data-row">
                                        <span class="text-muted" style="font-size: 0.85rem;">Account Number:</span>
                                        <div>
                                            <strong style="font-size: 0.95rem; font-family: monospace;" id="valAccountNo">${accountNo}</strong>
                                            <button type="button" class="btn-copy ms-2" onclick="copyText('${accountNo}', 'Account number copied')">Copy</button>
                                        </div>
                                    </div>
                                    <div class="bank-data-row">
                                        <span class="text-muted" style="font-size: 0.85rem;">Account Holder:</span>
                                        <strong style="font-size: 0.9rem;">${accountName}</strong>
                                    </div>
                                    <div class="bank-data-row">
                                        <span class="text-muted" style="font-size: 0.85rem;">Deposit Amount:</span>
                                        <div>
                                            <strong style="font-size: 0.95rem; color: #2563eb;">
                                                <fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND
                                            </strong>
                                            <button type="button" class="btn-copy ms-2" onclick="copyText('${deposit.amount}', 'Deposit amount copied')">Copy</button>
                                        </div>
                                    </div>
                                    <div class="bank-data-row">
                                        <span class="text-muted" style="font-size: 0.85rem;">Transfer Note:</span>
                                        <div>
                                            <strong style="font-size: 0.95rem; font-family: monospace; color: #dc2626;" id="valRef">${deposit.paymentReference}</strong>
                                            <button type="button" class="btn-copy ms-2" onclick="copyText('${deposit.paymentReference}', 'Transfer memo copied')">Copy</button>
                                        </div>
                                    </div>
                                </div>
                                </c:if>

                                <div style="margin-top: 1rem; display: flex; gap: 0.75rem;">
                                    <button type="button" class="btn btn-outline-primary btn-sm w-100" id="btnRefreshStatus" onclick="checkStatusManually()">
                                        <i class="bi bi-arrow-clockwise me-1"></i> Check Payment Status
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Demo Card Simulation Panel -->
                    <div class="payment-method-panel" data-payment-panel="DEMO_CARD" hidden>
                        <div class="card-demo-note">
                            <strong>Simulated Card Sandbox Scenario.</strong>
                            Card credentials are verified in memory for demo evaluation and are not forwarded to any external financial institutions.
                        </div>
                        <div class="card-form-grid">
                            <label class="payment-field payment-field-wide">
                                <span>Cardholder Name</span>
                                <input type="text" name="cardholderName" maxlength="120" autocomplete="off" placeholder="Demo Customer" data-card-field>
                            </label>
                            <label class="payment-field payment-field-wide">
                                <span>Card Number</span>
                                <input type="text" name="cardNumber" maxlength="19" inputmode="numeric" autocomplete="off" placeholder="4242 4242 4242 4242" data-card-number data-card-field>
                            </label>
                            <label class="payment-field">
                                <span>Expiry (MM/YY)</span>
                                <input type="text" name="expiry" maxlength="5" inputmode="numeric" autocomplete="off" placeholder="12/30" data-card-field>
                            </label>
                            <label class="payment-field">
                                <span>Security Code (CVV)</span>
                                <input type="password" name="cvv" maxlength="4" inputmode="numeric" autocomplete="off" placeholder="123" data-card-field>
                            </label>
                        </div>
                        <div class="test-scenarios">
                            <span><strong>Success Card:</strong> 4242 4242 4242 4242</span>
                            <span><strong>Declined Card:</strong> 4000 0000 0000 0002</span>
                        </div>
                    </div>

                    <!-- Action Buttons -->
                    <c:if test="${paymentMode != 'REAL'}">
                    <div class="checkout-actions">
                        <button type="submit" class="payment-primary" data-submit-payment>
                            <span data-submit-label>Confirm Demo Payment</span>
                        </button>
                        <p>The system automatically triggers state updates upon receiving payment confirmation.</p>
                    </div>
                    </c:if>
                </form>

                <form action="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/cancel" method="post" class="cancel-payment-form mt-3">
                    <%@ include file="fragments/csrf-input.jsp" %>
                    <button type="submit" class="payment-cancel-btn btn btn-outline-secondary w-100">
                        <i class="bi bi-x-circle me-1"></i> Cancel Payment &amp; Return to Booking
                    </button>
                </form>
            </section>

            <!-- Order Summary with Detailed Price Breakdown -->
            <aside class="order-summary" aria-labelledby="order-summary-heading">
                <div class="panel-heading compact">
                    <span>02</span>
                    <div><h2 id="order-summary-heading">Session Summary</h2></div>
                </div>
                <div class="booking-summary">
                    <span class="payment-eyebrow">Booking Reference #<c:out value="${booking.id}" /></span>
                    <h3><c:out value="${booking.sessionTitle}" /></h3>
                    <p>Photographer: <strong><c:out value="${booking.photographerName}" /></strong></p>
                    <c:if test="${not empty booking.packageName}">
                        <p style="font-size: 0.85rem; color: #2563eb;">
                            <i class="bi bi-box-seam me-1"></i> Package: <c:out value="${booking.packageName}" />
                        </p>
                    </c:if>
                    <dl>
                        <div><dt>Shoot Date</dt><dd><c:out value="${booking.bookingDate}" /></dd></div>
                        <div><dt>Shoot Time</dt><dd><c:out value="${booking.bookingTime}" /></dd></div>
                        <div><dt>Location</dt><dd><c:out value="${booking.location}" /></dd></div>
                        <div><dt>Status</dt><dd><span class="status-badge ${booking.status}"><c:out value="${booking.status}" /></span></dd></div>
                    </dl>
                </div>

                <!-- Authoritative Breakdown -->
                <div class="price-summary">
                    <div>
                        <span>Base Package Price</span>
                        <strong><fmt:formatNumber value="${booking.basePrice}" pattern="#,##0" /> VND</strong>
                    </div>
                    <c:if test="${booking.nightSurcharge > 0}">
                        <div style="color: #2563eb;">
                            <span>Night Shoot Surcharge (+20%)</span>
                            <strong>+<fmt:formatNumber value="${booking.nightSurcharge}" pattern="#,##0" /> VND</strong>
                        </div>
                    </c:if>
                    <c:if test="${booking.travelSurcharge > 0}">
                        <div style="color: #2563eb;">
                            <span>Travel Surcharge (${booking.travelDistanceKm} km)</span>
                            <strong>+<fmt:formatNumber value="${booking.travelSurcharge}" pattern="#,##0" /> VND</strong>
                        </div>
                    </c:if>
                    <c:if test="${booking.extrasFee > 0}">
                        <div style="color: #2563eb;">
                            <span>Extra Deliverables (Hours/Photos/Assistant)</span>
                            <strong>+<fmt:formatNumber value="${booking.extrasFee}" pattern="#,##0" /> VND</strong>
                        </div>
                    </c:if>
                    <div style="border-top: 1px dashed #cbd5e1; padding-top: 0.5rem; margin-top: 0.5rem;">
                        <span>Total Agreed Price</span>
                        <strong style="font-size: 1.05rem;"><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND</strong>
                    </div>
                    <div><span>Mandatory Escrow Rate</span><strong>30%</strong></div>
                    <div class="price-due">
                        <span>Deposit Due Now</span>
                        <strong><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</strong>
                    </div>
                    <div>
                        <span>Remaining Balance Due at Shoot (70%)</span>
                        <strong><fmt:formatNumber value="${remainingBalance}" pattern="#,##0" /> VND</strong>
                    </div>
                </div>
                <div class="summary-assurance">
                    <i class="bi bi-shield-check me-1"></i> Funds and authenticity are verified independently on the server. The shoot transitions to CONFIRMED once escrow deposit is secured.
                </div>
            </aside>
        </div>
    </div>

    <div class="processing-overlay" data-processing-overlay hidden role="status" aria-live="polite">
        <div class="processing-dialog">
            <span class="processing-spinner" aria-hidden="true"></span>
            <h2>Processing Payment...</h2>
            <p>Please keep this window open while we synchronize with the payment engine.</p>
        </div>
    </div>
</main>

<!-- Copy Toast Feedback -->
<div id="copyToast" style="display: none; position: fixed; bottom: 2rem; right: 2rem; z-index: 9999; background: #0f172a; color: #ffffff; padding: 0.75rem 1.25rem; border-radius: 6px; font-size: 0.88rem; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
    <i class="bi bi-check-circle-fill text-success me-2"></i> <span id="copyToastText">Copied to clipboard</span>
</div>

<script>
function copyText(text, message) {
    if (!text) return;
    navigator.clipboard.writeText(text).then(function() {
        const toast = document.getElementById('copyToast');
        const toastText = document.getElementById('copyToastText');
        toastText.textContent = message || 'Copied to clipboard!';
        toast.style.display = 'block';
        setTimeout(() => { toast.style.display = 'none'; }, 2500);
    }).catch(function() {
        prompt("Press Ctrl+C to copy:", text);
    });
}

// 15-Minute Expiry Countdown
let timeLeftSeconds = 15 * 60;
const countdownEl = document.getElementById('countdownTimer');
const timerInterval = setInterval(function() {
    timeLeftSeconds--;
    if (timeLeftSeconds <= 0) {
        clearInterval(timerInterval);
        countdownEl.textContent = 'EXPIRED';
        document.getElementById('countdownBox').style.background = '#fef2f2';
        document.getElementById('countdownBox').style.color = '#dc2626';
        document.getElementById('paymentStatusBadge').innerHTML = '<span class="badge bg-danger">QR Code has expired</span>';
        return;
    }
    const m = Math.floor(timeLeftSeconds / 60);
    const s = timeLeftSeconds % 60;
    countdownEl.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
}, 1000);

// Live Payment Polling & WebSocket
const bookingId = '${booking.id}';
function checkStatusManually() {
    const btn = document.getElementById('btnRefreshStatus');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Checking status...';
    fetch('${pageContext.request.contextPath}/bookings/' + bookingId + '/deposit/status')
        .then(res => res.json())
        .then(data => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-arrow-clockwise me-1"></i> Check Payment Status';
            if (data.isPaid || data.status === 'PAID') {
                document.getElementById('paymentStatusBadge').innerHTML = '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i> Paid Successfully</span>';
                window.location.href = '${pageContext.request.contextPath}/bookings/' + bookingId + '/deposit/receipt';
            } else {
                document.getElementById('paymentStatusBadge').innerHTML = '<span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split me-1"></i> ' + data.status + '</span>';
            }
        })
        .catch(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-arrow-clockwise me-1"></i> Check Payment Status';
        });
}

// Polling every 5 seconds
const pollInterval = setInterval(function() {
    fetch('${pageContext.request.contextPath}/bookings/' + bookingId + '/deposit/status')
        .then(res => res.json())
        .then(data => {
            if (data.isPaid || data.status === 'PAID') {
                clearInterval(pollInterval);
                window.location.href = '${pageContext.request.contextPath}/bookings/' + bookingId + '/deposit/receipt';
            }
        })
        .catch(() => {});
}, 5000);

// STOMP WebSocket Live Notification Listener
try {
    const socket = new SockJS('${pageContext.request.contextPath}/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null; // disable noisy console logs
    stompClient.connect({}, function() {
        stompClient.subscribe('/topic/bookings/' + bookingId + '/payment', function(message) {
            try {
                const payload = JSON.parse(message.body);
                if (payload.status === 'CONFIRMED' || payload.depositStatus === 'PAID') {
                    clearInterval(pollInterval);
                    window.location.href = '${pageContext.request.contextPath}/bookings/' + bookingId + '/deposit/receipt';
                }
            } catch(e) {}
        });
    });
} catch(err) {
    // Fallback gracefully to polling
}
</script>
