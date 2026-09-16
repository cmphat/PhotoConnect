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
    <title>Booking Detail - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 800px;">
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/bookings" class="text-link" style="font-size: 0.9rem;">&larr; Back to My Bookings</a>
            </div>
            
            <h1 class="editorial-title" style="margin-bottom: 1rem;">Booking Details</h1>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(52, 211, 153, 0.2);">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(239, 68, 68, 0.2);">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <div style="border: 1px solid var(--border); padding: clamp(1.5rem, 5vw, 3rem); border-radius: 0;">
                <div class="pc-stack-mobile" style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1.5rem; margin-bottom: 3rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border-dark);">
                    <div>
                        <h2 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Session with <c:out value="${booking.photographerName}"/></h2>
                        <c:if test="${not empty booking.createdAt}">
                            <fmt:parseDate value="${fn:substring(booking.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedCreatedAt" type="date" />
                            <p style="color: var(--text-muted);">Created on <fmt:formatDate value="${parsedCreatedAt}" pattern="MMM d, yyyy" /></p>
                        </c:if>
                    </div>
                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 0.75rem;">
                        <span style="font-size: 0.85rem; letter-spacing: 0.05em; text-transform: uppercase; color: var(--text-muted);">
                            Status: <strong style="color: var(--text-color);">${booking.status}</strong>
                        </span>
                        <a href="${pageContext.request.contextPath}/bookings/${booking.id}/chat" class="btn btn-secondary btn-sm" style="display: inline-flex; align-items: center; gap: 0.5rem;">
                            💬 Open Chat
                        </a>
                    </div>
                </div>

                <div class="pc-detail-grid" style="margin-bottom: 3rem;">
                    <div>
                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Date & Time</div>
                        <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                        <div style="font-size: 1.1rem;"><fmt:formatDate value="${parsedBookingDate}" pattern="MMM d, yyyy" /> at ${booking.bookingTime}</div>
                    </div>
                    <div>
                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Agreed Rate</div>
                        <div style="font-size: 1.1rem;"><fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND</div>
                    </div>
                    <div style="grid-column: span 2;">
                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Shoot Location</div>
                        <div style="font-size: 1.1rem;"><c:out value="${booking.location}"/></div>
                    </div>
                    <c:if test="${not empty booking.notes}">
                        <div style="grid-column: span 2;">
                            <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Additional Notes</div>
                            <div style="font-size: 1rem; color: var(--text-muted); line-height: 1.6;"><c:out value="${booking.notes}"/></div>
                        </div>
                    </c:if>
                </div>

                <c:if test="${booking.status == 'ACCEPTED' || (deposit != null && deposit.status == 'PAID')}">
                    <div style="margin-top: 3rem; padding: 2.5rem; border: 1px solid var(--border); background: var(--bg-dark-secondary);">
                        <h3 class="editorial-heading" style="font-size: 1.25rem; margin-bottom: 1.5rem;">Deposit Information</h3>
                        <c:choose>
                            <c:when test="${booking.status == 'ACCEPTED' && (deposit == null || deposit.status != 'PAID')}">
                                <p style="color: var(--text-muted); margin-bottom: 2rem; line-height: 1.6;">
                                    A 30% deposit is required to secure this booking.
                                    <c:if test="${deposit != null}">
                                        <br><span style="color: var(--text-color); font-weight: 500; font-size: 1.1rem;">Deposit Amount: <fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</span>
                                    </c:if>
                                </p>
                                <a href="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/checkout" class="btn btn-primary">Pay Deposit</a>
                            </c:when>
                            <c:when test="${deposit.status == 'PAID'}">
                                <div class="pc-detail-grid" style="gap: 2rem;">
                                    <div>
                                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Deposit Paid</div>
                                        <div style="font-size: 1.1rem;"><fmt:formatNumber value="${deposit.amount}" pattern="#,##0" /> VND</div>
                                    </div>
                                    <div>
                                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Reference</div>
                                        <div style="font-size: 1.1rem;"><c:out value="${deposit.paymentReference}" /></div>
                                    </div>
                                    <div style="grid-column: span 2;">
                                        <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Paid At</div>
                                        <c:choose>
                                            <c:when test="${not empty deposit.paidAt}">
                                                <fmt:parseDate value="${fn:substring(deposit.paidAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedPaidAt" type="date" />
                                                <div style="font-size: 1.1rem;"><fmt:formatDate value="${parsedPaidAt}" pattern="MMM d, yyyy" /></div>
                                            </c:when>
                                            <c:otherwise>
                                                <div style="font-size: 1.1rem;">Recorded</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <a href="${pageContext.request.contextPath}/bookings/${booking.id}/deposit/receipt" class="text-link" style="display: inline-block; margin-top: 1.5rem;">View Payment Receipt</a>
                            </c:when>
                        </c:choose>
                    </div>
                </c:if>

                <c:if test="${booking.status == 'COMPLETED'}">
                    <div style="margin-top: 3rem; padding: 2.5rem; border: 1px solid var(--border); background: var(--bg-dark-secondary);">
                        <h3 class="editorial-heading" style="font-size: 1.25rem; margin-bottom: 1.5rem;">Review & Rating</h3>
                        <c:choose>
                            <c:when test="${review == null}">
                                <p style="color: var(--text-muted); margin-bottom: 2rem; line-height: 1.6;">
                                    Your shoot session is complete! Let others know about your experience with <c:out value="${booking.photographerName}"/>.
                                </p>
                                <a href="${pageContext.request.contextPath}/bookings/${booking.id}/review" class="btn btn-primary" id="btn-leave-review">Leave a Review</a>
                            </c:when>
                            <c:otherwise>
                                <div>
                                    <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem;">
                                        <div style="color: var(--primary); font-size: 1.25rem; letter-spacing: 0.1em;">
                                            <c:forEach begin="1" end="${review.rating}">★</c:forEach><c:forEach begin="${review.rating + 1}" end="5">☆</c:forEach>
                                        </div>
                                        <span style="font-weight: 500; font-size: 1.1rem;">${review.rating} / 5 Stars</span>
                                        <c:if test="${not empty review.createdAt}">
                                            <fmt:parseDate value="${fn:substring(review.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedReviewAt" type="date" />
                                            <span style="color: var(--text-muted); font-size: 0.85rem;">Reviewed on <fmt:formatDate value="${parsedReviewAt}" pattern="MMM d, yyyy" /></span>
                                        </c:if>
                                        <c:if test="${review.status == 'HIDDEN'}">
                                            <span style="color: #ef4444; font-size: 0.8rem; background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.3); padding: 0.15rem 0.5rem; border-radius: 2px;">Hidden by moderation</span>
                                        </c:if>
                                    </div>
                                    <c:choose>
                                        <c:when test="${not empty review.comment}">
                                            <div style="color: var(--text-color); font-size: 1rem; line-height: 1.6; font-style: italic; background: var(--bg-dark); padding: 1.25rem 1.5rem; border-left: 2px solid var(--primary);">
                                                &ldquo;<c:out value="${review.comment}"/>&rdquo;
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="color: var(--text-muted); font-style: italic; font-size: 0.9rem;">No written comment provided.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <c:if test="${booking.status == 'PENDING' || booking.status == 'ACCEPTED'}">
                    <div style="margin-top: 3rem; padding-top: 2rem; border-top: 1px solid var(--border-dark);">
                        <form action="${pageContext.request.contextPath}/bookings/${booking.id}/cancel" method="post" onsubmit="return confirm('Are you sure you want to cancel this booking?');">
                            <button type="submit" class="btn btn-danger">Cancel Booking</button>
                        </form>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</body>
</html>
