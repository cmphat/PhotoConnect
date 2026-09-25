<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Booking Requests — Creator Studio — PhotoConnect</title>
</head>

    <main class="pc-page">
        <div class="editorial-container pc-container-wide">

            <div style="margin-bottom: 2.5rem; border-bottom: 1px solid var(--border); padding-bottom: 1.5rem;">
                <span class="section-index">Creator Studio</span>
                <h1 class="editorial-title" style="margin: 0 0 0.4rem 0;">Booking Requests</h1>
                <p class="pc-lead" style="font-size: 1.05rem;">
                    Review, accept, and coordinate photoshoot inquiries submitted by prospective clients.
                </p>
            </div>

            <!-- Feedback Alerts -->
            <c:if test="${not empty successMessage}">
                <div class="pc-alert pc-alert-success" role="status">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${empty bookings}">
                    <!-- Intentional Empty State -->
                    <div class="pc-empty-state" style="max-width: 600px; margin: 3rem auto;">
                        <div style="font-size: 2.5rem; margin-bottom: 1rem; color: var(--accent);">✦</div>
                        <h2 class="editorial-heading" style="font-size: 1.7rem; margin-bottom: 0.75rem;">No Inquiries Yet</h2>
                        <p style="font-size: 1.05rem; margin-bottom: 2rem;">
                            You don&rsquo;t have any active booking requests at the moment. Keep your portfolio refreshed to attract new clients.
                        </p>
                        <a href="${pageContext.request.contextPath}/photographer/portfolio" class="btn btn-primary btn-lg">
                            Manage Portfolio &rarr;
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="pc-card-list">
                        <c:forEach var="booking" items="${bookings}">
                            <article class="pc-list-card pc-stack-mobile">
                                <div>
                                    <div style="margin-bottom: 0.35rem;">
                                        <span class="status-badge ${booking.status}"><c:out value="${booking.status}"/></span>
                                    </div>
                                    <h2 class="editorial-heading" style="margin-bottom: 0.35rem; font-size: 1.35rem;">
                                        Request from <c:out value="${booking.customerName}"/>
                                    </h2>
                                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                                    <p style="color: var(--muted); font-size: 0.92rem; margin-bottom: 0.5rem;">
                                        <fmt:formatDate value="${parsedBookingDate}" pattern="EEEE, MMMM d, yyyy" /> at <c:out value="${booking.bookingTime}"/>
                                    </p>
                                    <div style="color: var(--muted); font-size: 0.88rem;">
                                        Shoot Location: <strong style="color: var(--text);"><c:out value="${booking.location}"/></strong>
                                    </div>
                                </div>
                                <div style="text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 1rem;">
                                    <div style="font-family: var(--font-primary); font-weight: 600; font-size: 1.25rem; color: var(--text);">
                                        <fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND
                                    </div>
                                    <div style="display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;">
                                        <a href="${pageContext.request.contextPath}/bookings/${booking.id}/chat" class="btn btn-ghost btn-sm">
                                            Open Chat
                                        </a>
                                        <a href="${pageContext.request.contextPath}/photographer/bookings/${booking.id}" class="btn btn-secondary btn-sm">
                                            Review Request &rarr;
                                        </a>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>

    <!-- Editorial Footer -->
    <footer class="pc-footer">
        <div class="editorial-container">
            <div style="color: var(--muted); font-size: 0.85rem;">
                &copy; 2026 PhotoConnect. Creator Studio Management.
            </div>
        </div>
    </footer>
