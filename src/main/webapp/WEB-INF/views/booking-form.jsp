<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Request a photography booking on PhotoConnect.">
    <title>Book <c:out value="${photographer.displayName}"/> – PhotoConnect</title>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 0;">
        <div class="editorial-container">
            
            <!-- Breadcrumb Navigation -->
            <div style="margin-bottom: 2rem;">
                <a href="${pageContext.request.contextPath}/photographers/${photographer.id}" class="text-link" style="font-size:0.88rem;">← Back to Profile</a>
            </div>

            <div class="pc-booking-layout">
                
                <!-- Left: Photographer Summary -->
                <div>
                    <div style="position: sticky; top: 120px;">
                        <div class="photo-frame" style="aspect-ratio: 4/5; margin-bottom: 2rem;">
                            <c:choose>
                                <c:when test="${not empty photographer.coverImageUrl}">
                                    <img src="<c:out value='${photographer.coverImageUrl}'/>" 
                                         alt="<c:out value='${photographer.displayName}'/> cover" 
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: var(--bg-dark); color: var(--border-dark); font-size: 3rem; font-weight: 300;">
                                        <c:out value="${fn:toUpperCase(fn:substring(photographer.displayName, 0, 1))}"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
                            <h2 class="editorial-heading" style="margin: 0; font-size: 1.5rem;"><c:out value="${photographer.displayName}"/></h2>
                        </div>

                        <div style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 2rem;">
                            <c:if test="${not empty photographer.city}">
                                <span>📍 <c:out value="${photographer.city}"/></span>
                            </c:if>
                            <c:if test="${not empty photographer.experienceYears}">
                                <span style="margin-left: 0.5rem;">· <c:out value="${photographer.experienceYears}"/> yrs exp</span>
                            </c:if>
                        </div>

                        <!-- Price Snapshot Notice -->
                        <div style="border-top: 1px solid var(--border); padding-top: 1.5rem;">
                            <div style="color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">Starting Rate</div>
                            <div style="font-size: 1.5rem; font-weight: 400;">
                                <c:choose>
                                    <c:when test="${not empty photographer.priceFrom}">
                                        <fmt:formatNumber value="${photographer.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                    </c:when>
                                    <c:otherwise>Rates upon agreement</c:otherwise>
                                </c:choose>
                            </div>
                            <div style="color: var(--text-muted); font-size: 0.8rem; margin-top: 0.5rem;">
                                Locked in at booking submission.
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right: Booking Form Panel -->
                <div>
                    <div style="margin-bottom: 3rem;">
                        <h1 class="editorial-title" style="margin-bottom: 1rem;">Request Shoot Booking</h1>
                        <p style="color: var(--text-muted); line-height: 1.6;">
                            Fill in your session preferences below to submit a formal booking request directly to <c:out value="${photographer.displayName}"/>.
                        </p>
                    </div>

                    <!-- Error alert -->
                    <c:if test="${not empty errorMessage}">
                        <div style="color: #ef4444; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(239, 68, 68, 0.2);">
                            <c:out value="${errorMessage}"/>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/photographers/${photographer.id}/book" method="post" id="booking-form">
                        
                        <!-- Date & Time Row -->
                        <div class="pc-booking-datetime-grid">
                            <div class="form-group">
                                <label for="bookingDate" class="form-label">Shoot Date *</label>
                                <input type="date" 
                                       id="bookingDate" 
                                       name="bookingDate" 
                                       min="<c:out value='${minBookingDate}'/>"
                                       class="form-input" 
                                       value="<c:out value='${bookingRequest.bookingDate}'/>" 
                                       required>
                                <spring:bind path="bookingRequest.bookingDate">
                                    <c:if test="${status.error}">
                                        <div style="color: #ef4444; font-size: 0.8rem; margin-top: 0.5rem;"><c:out value="${status.errorMessage}"/></div>
                                    </c:if>
                                </spring:bind>
                            </div>

                            <div class="form-group">
                                <label for="bookingTime" class="form-label">Preferred Time *</label>
                                <input type="time" 
                                       id="bookingTime" 
                                       name="bookingTime" 
                                       class="form-input" 
                                       value="<c:out value='${bookingRequest.bookingTime}'/>" 
                                       required>
                                <spring:bind path="bookingRequest.bookingTime">
                                    <c:if test="${status.error}">
                                        <div style="color: #ef4444; font-size: 0.8rem; margin-top: 0.5rem;"><c:out value="${status.errorMessage}"/></div>
                                    </c:if>
                                </spring:bind>
                            </div>
                        </div>

                        <!-- Location -->
                        <div class="form-group" style="margin-bottom: 2rem;">
                            <label for="location" class="form-label">Shoot Location / Address *</label>
                            <input type="text" 
                                   id="location" 
                                   name="location" 
                                   maxlength="255" 
                                   placeholder="e.g. Studio LightSpace, District 1..." 
                                   class="form-input" 
                                   value="<c:out value='${bookingRequest.location}'/>" 
                                   required>
                            <spring:bind path="bookingRequest.location">
                                <c:if test="${status.error}">
                                    <div style="color: #ef4444; font-size: 0.8rem; margin-top: 0.5rem;"><c:out value="${status.errorMessage}"/></div>
                                </c:if>
                            </spring:bind>
                        </div>

                        <!-- Notes / Requirements -->
                        <div class="form-group" style="margin-bottom: 2rem;">
                            <label for="notes" class="form-label">Notes & Requirements (Optional)</label>
                            <textarea id="notes" 
                                      name="notes" 
                                      rows="4" 
                                      maxlength="1000" 
                                      placeholder="Share shoot concept, number of outfits, etc..." 
                                      class="form-input"><c:out value='${bookingRequest.notes}'/></textarea>
                            <spring:bind path="bookingRequest.notes">
                                <c:if test="${status.error}">
                                    <div style="color: #ef4444; font-size: 0.8rem; margin-top: 0.5rem;"><c:out value="${status.errorMessage}"/></div>
                                </c:if>
                            </spring:bind>
                        </div>

                        <div style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 3rem;">
                            Note: Your request will be sent to the photographer for confirmation. No immediate payment is required until the photographer accepts.
                        </div>

                        <!-- Actions -->
                        <div class="pc-booking-actions">
                            <a href="${pageContext.request.contextPath}/photographers/${photographer.id}" class="pc-btn-outline" style="border: none; padding-left: 0;">
                                Cancel
                            </a>
                            <button type="submit" class="submit-btn" id="btn-submit-booking" style="width: auto; padding: 1rem 3rem;">
                                Request Booking
                            </button>
                        </div>

                    </form>
                </div>

            </div>

        </div>
    </main>

    <footer class="pc-footer">
        <div class="editorial-container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>
</body>
</html>
