<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Bookings — PhotoConnect</title>

    <!-- Google Fonts: Plus Jakarta Sans & Playfair Display -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>

    <!-- Global Professional Navigation -->
    <jsp:include page="fragments/navbar.jsp" />

    <main class="pc-page">
        <div class="editorial-container pc-container-wide">

            <div style="margin-bottom: 2.5rem; border-bottom: 1px solid var(--border); padding-bottom: 1.5rem;">
                <span class="section-index">Client Dashboard</span>
                <h1 class="editorial-title" style="margin: 0 0 0.4rem 0;">My Bookings</h1>
                <p class="pc-lead" style="font-size: 1.05rem;">
                    Manage your upcoming, active, and past photography sessions with verified creators.
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
                        <h2 class="editorial-heading" style="font-size: 1.7rem; margin-bottom: 0.75rem;">No Bookings Yet</h2>
                        <p style="font-size: 1.05rem; margin-bottom: 2rem;">
                            You haven&rsquo;t requested any photography sessions yet. Browse our verified roster to find the ideal artist for your vision.
                        </p>
                        <a href="${pageContext.request.contextPath}/photographers" class="btn btn-primary btn-lg">
                            Explore Photographers &rarr;
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
                                        Session with <c:out value="${booking.photographerName}"/>
                                    </h2>
                                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                                    <p style="color: var(--muted); font-size: 0.92rem; margin-bottom: 0.5rem;">
                                        <fmt:formatDate value="${parsedBookingDate}" pattern="EEEE, MMMM d, yyyy" /> at <c:out value="${booking.bookingTime}"/>
                                    </p>
                                    <div style="color: var(--muted); font-size: 0.88rem;">
                                        Location: <strong style="color: var(--text);"><c:out value="${booking.location}"/></strong>
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
                                        <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="btn btn-secondary btn-sm">
                                            View Details &rarr;
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
                &copy; 2026 PhotoConnect. Client Session Management.
            </div>
        </div>
    </footer>

</body>
</html>
