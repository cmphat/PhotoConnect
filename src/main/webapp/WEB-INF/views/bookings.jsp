<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Bookings - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1000px;">
            <h1 class="editorial-title" style="margin-bottom: 2rem;">My Bookings</h1>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(52, 211, 153, 0.2);">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>

            <c:if test="${empty bookings}">
                <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border);">
                    <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem;">No bookings yet</h3>
                    <p style="color: var(--text-muted); margin-bottom: 2rem;">You haven't requested any photography sessions.</p>
                    <a href="${pageContext.request.contextPath}/photographers" class="pc-btn-primary" style="padding: 1rem 2rem;">Explore Photographers</a>
                </div>
            </c:if>

            <c:if test="${not empty bookings}">
                <div style="display: grid; gap: 2rem;">
                    <c:forEach var="booking" items="${bookings}">
                        <div class="pc-stack-mobile" style="border: 1px solid var(--border); padding: 2rem; display: flex; justify-content: space-between; align-items: center; gap: 2rem; border-radius: 0;">
                            <div>
                                <h3 class="editorial-heading" style="margin-bottom: 0.5rem; font-size: 1.25rem;">Session with <c:out value="${booking.photographerName}"/></h3>
                                <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                                <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1rem;">
                                    <fmt:formatDate value="${parsedBookingDate}" pattern="MMM d, yyyy" /> at <c:out value="${booking.bookingTime}"/> &mdash; <c:out value="${booking.location}"/>
                                </p>
                                <span style="font-size: 0.85rem; letter-spacing: 0.05em; text-transform: uppercase; color: var(--text-muted);">
                                    Status: <strong style="color: var(--text-color);">${booking.status}</strong>
                                </span>
                            </div>
                            <div style="text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 1rem;">
                                <div style="font-weight: 500; font-size: 1.2rem;">
                                    <fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND
                                </div>
                                <div style="display: flex; gap: 1.5rem; align-items: center;">
                                    <a href="${pageContext.request.contextPath}/bookings/${booking.id}/chat" class="text-link" style="font-size: 0.95rem; color: var(--accent-gold, #c9a96e);">💬 Chat</a>
                                    <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="text-link" style="font-size: 0.95rem; padding-bottom: 2px;">View Details</a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </main>
</body>
</html>
