<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Leave a Review - PhotoConnect</title>
</head>

    <main class="pc-page">
        <div class="editorial-container pc-container-copy">
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="text-link" style="font-size: 0.9rem;">&larr; Back to Booking</a>
            </div>

            <h1 class="editorial-title" style="margin-bottom: 0.75rem;">Leave a Review</h1>
            <p style="color: var(--text-muted); margin-bottom: 2.5rem; font-size: 1.05rem;">
                Share your experience for your completed session with <strong style="color: var(--text-color);"><c:out value="${booking.photographerName}"/></strong>.
            </p>

            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/bookings/${booking.id}/review" method="post" style="border: 1px solid var(--border); padding: clamp(1.5rem, 5vw, 3rem); background: var(--surface); border-radius: var(--radius-sm);">
                <%@ include file="fragments/csrf-input.jsp" %>

                <!-- Rating selection (Accessible 1 to 5) -->
                <fieldset style="margin: 0 0 2.5rem; padding: 0; border: 0;">
                    <legend class="form-label" style="margin-bottom: 1rem;">
                        Rating (1 to 5 Stars) <span style="color: var(--primary);">*</span>
                    </legend>
                    <div class="pc-rating-options">
                        <label class="pc-rating-option">
                            <input type="radio" name="rating" value="5" ${reviewRequest.rating == 5 || reviewRequest.rating == null ? 'checked' : ''}>
                            <span>★★★★★ &nbsp; 5 — Exceptional</span>
                        </label>
                        <label class="pc-rating-option">
                            <input type="radio" name="rating" value="4" ${reviewRequest.rating == 4 ? 'checked' : ''}>
                            <span>★★★★☆ &nbsp; 4 — Very Good</span>
                        </label>
                        <label class="pc-rating-option">
                            <input type="radio" name="rating" value="3" ${reviewRequest.rating == 3 ? 'checked' : ''}>
                            <span>★★★☆☆ &nbsp; 3 — Average</span>
                        </label>
                        <label class="pc-rating-option">
                            <input type="radio" name="rating" value="2" ${reviewRequest.rating == 2 ? 'checked' : ''}>
                            <span>★★☆☆☆ &nbsp; 2 — Below Expectations</span>
                        </label>
                        <label class="pc-rating-option">
                            <input type="radio" name="rating" value="1" ${reviewRequest.rating == 1 ? 'checked' : ''}>
                            <span>★☆☆☆☆ &nbsp; 1 — Poor</span>
                        </label>
                    </div>
                </fieldset>

                <!-- Comment (Optional, max 1000 characters) -->
                <div style="margin-bottom: 2.5rem;">
                    <label for="review-comment" style="display: block; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                        Feedback / Comments (Optional, max 1,000 characters)
                    </label>
                    <textarea id="review-comment" name="comment" rows="5" maxlength="1000" class="form-control" placeholder="Tell future clients about your photoshoot experience..."><c:out value="${reviewRequest.comment}"/></textarea>
                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem; text-align: right;">Max 1,000 characters</div>
                </div>

                <!-- Actions -->
                <div class="pc-actions pc-stack-mobile">
                    <button type="submit" class="btn btn-primary" style="padding: 0.85rem 2.5rem;">
                        Submit Review
                    </button>
                    <a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="btn btn-ghost">Cancel</a>
                </div>
            </form>
        </div>
    </main>
