<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leave a Review - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 700px;">
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="text-link" style="font-size: 0.9rem;">&larr; Back to Booking</a>
            </div>

            <h1 class="editorial-title" style="margin-bottom: 0.75rem;">Leave a Review</h1>
            <p style="color: var(--text-muted); margin-bottom: 2.5rem; font-size: 1.05rem;">
                Share your experience for your completed session with <strong style="color: var(--text-color);"><c:out value="${booking.photographerName}"/></strong>.
            </p>

            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding: 1rem 1.25rem; border: 1px solid rgba(239, 68, 68, 0.3); background: rgba(239, 68, 68, 0.05);">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/bookings/${booking.id}/review" method="post" style="border: 1px solid var(--border); padding: clamp(1.5rem, 5vw, 3rem); background: var(--bg-dark-secondary);">
                
                <!-- Rating selection (Accessible 1 to 5) -->
                <div style="margin-bottom: 2.5rem;">
                    <label style="display: block; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 1rem;">
                        Rating (1 to 5 Stars) <span style="color: var(--primary);">*</span>
                    </label>
                    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <label style="display: flex; align-items: center; gap: 0.75rem; cursor: pointer; color: var(--text-color);">
                            <input type="radio" name="rating" value="5" ${reviewRequest.rating == 5 || reviewRequest.rating == null ? 'checked' : ''} style="accent-color: var(--primary); width: 1.2rem; height: 1.2rem;">
                            <span>★★★★★ &nbsp; 5 — Exceptional</span>
                        </label>
                        <label style="display: flex; align-items: center; gap: 0.75rem; cursor: pointer; color: var(--text-color);">
                            <input type="radio" name="rating" value="4" ${reviewRequest.rating == 4 ? 'checked' : ''} style="accent-color: var(--primary); width: 1.2rem; height: 1.2rem;">
                            <span>★★★★☆ &nbsp; 4 — Very Good</span>
                        </label>
                        <label style="display: flex; align-items: center; gap: 0.75rem; cursor: pointer; color: var(--text-color);">
                            <input type="radio" name="rating" value="3" ${reviewRequest.rating == 3 ? 'checked' : ''} style="accent-color: var(--primary); width: 1.2rem; height: 1.2rem;">
                            <span>★★★☆☆ &nbsp; 3 — Average</span>
                        </label>
                        <label style="display: flex; align-items: center; gap: 0.75rem; cursor: pointer; color: var(--text-color);">
                            <input type="radio" name="rating" value="2" ${reviewRequest.rating == 2 ? 'checked' : ''} style="accent-color: var(--primary); width: 1.2rem; height: 1.2rem;">
                            <span>★★☆☆☆ &nbsp; 2 — Below Expectations</span>
                        </label>
                        <label style="display: flex; align-items: center; gap: 0.75rem; cursor: pointer; color: var(--text-color);">
                            <input type="radio" name="rating" value="1" ${reviewRequest.rating == 1 ? 'checked' : ''} style="accent-color: var(--primary); width: 1.2rem; height: 1.2rem;">
                            <span>★☆☆☆☆ &nbsp; 1 — Poor</span>
                        </label>
                    </div>
                </div>

                <!-- Comment (Optional, max 1000 characters) -->
                <div style="margin-bottom: 2.5rem;">
                    <label for="review-comment" style="display: block; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                        Feedback / Comments (Optional, max 1,000 characters)
                    </label>
                    <textarea id="review-comment" name="comment" rows="5" maxlength="1000" class="form-control" style="width: 100%; background: var(--bg-dark); border: 1px solid var(--border); color: var(--text-color); padding: 1rem; font-family: inherit; font-size: 0.95rem; resize: vertical;" placeholder="Tell future clients about your photoshoot experience...">${reviewRequest.comment}</textarea>
                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem; text-align: right;">Max 1,000 characters</div>
                </div>

                <!-- Actions -->
                <div class="pc-stack-mobile" style="display: flex; gap: 1.5rem; align-items: center;">
                    <button type="submit" class="btn btn-primary" style="padding: 0.85rem 2.5rem;">
                        Submit Review
                    </button>
                    <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="btn btn-ghost">Cancel</a>
                </div>
            </form>
        </div>
    </main>
</body>
</html>
