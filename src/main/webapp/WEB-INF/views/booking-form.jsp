<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Request a photography booking with ${photographer.displayName} on PhotoConnect.">
    <title>Book <c:out value="${photographer.displayName}"/> – PhotoConnect</title>
    
    <!-- Bootstrap Grid -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    
    <style>
        .pc-booking-price-box {
            background-color: var(--surface-soft);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            padding: 16px;
            margin-bottom: 24px;
        }
        .pc-booking-price-amount {
            font-size: 1.5rem;
            font-weight: 500;
            color: var(--text);
            margin-top: 4px;
        }
    </style>
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main class="py-5">
        <div class="container">
            
            <!-- Breadcrumb Navigation -->
            <div class="mb-4">
                <a href="/photographers/${photographer.id}" class="text-decoration-none text-muted" style="font-size:0.88rem;">← Back to Profile</a>
            </div>

            <div class="row g-4 g-lg-5">
                
                <!-- Left: Photographer Summary Card -->
                <div class="col-lg-5">
                    <div class="pc-booking-summary-card">
                        
                        <div class="pc-card-media" style="aspect-ratio: 16/9;">
                            <c:choose>
                                <c:when test="${not empty photographer.coverImageUrl}">
                                    <img src="<c:out value='${photographer.coverImageUrl}'/>" 
                                         alt="<c:out value='${photographer.displayName}'/> cover" 
                                         class="pc-card-img"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="pc-card-placeholder">
                                        <c:out value="${fn:toUpperCase(fn:substring(photographer.displayName, 0, 1))}"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <h2 class="h4 text-light mb-0"><c:out value="${photographer.displayName}"/></h2>
                            <span class="pc-badge-verified">✓ Verified</span>
                        </div>

                        <div class="text-muted small mb-3">
                            <c:if test="${not empty photographer.city}">
                                <span>📍 <c:out value="${photographer.city}"/></span>
                            </c:if>
                            <c:if test="${not empty photographer.experienceYears}">
                                <span class="ms-2">· <c:out value="${photographer.experienceYears}"/> yrs exp</span>
                            </c:if>
                        </div>

                        <!-- Price Snapshot Notice -->
                        <div class="pc-booking-price-box mt-4">
                            <div class="text-muted small text-uppercase" style="letter-spacing: 0.05em;">Starting Rate</div>
                            <div class="pc-booking-price-amount">
                                <c:choose>
                                    <c:when test="${not empty photographer.priceFrom}">
                                        <fmt:formatNumber value="${photographer.priceFrom}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                    </c:when>
                                    <c:otherwise>Rates upon agreement</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="text-muted" style="font-size: 0.75rem; margin-top: 8px;">
                                Locked in at booking submission.
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right: Booking Form Panel -->
                <div class="col-lg-7">
                    <div class="pc-booking-form-card">
                        
                        <div class="mb-5">
                            <span class="pc-badge-verified mb-2" style="border-color: transparent; padding-left: 0;">Direct Reservation</span>
                            <h1 class="h2 text-light mb-2">Request Shoot Booking</h1>
                            <p class="text-muted small" style="line-height: 1.6;">
                                Fill in your session preferences below to submit a formal booking request directly to <c:out value="${photographer.displayName}"/>.
                            </p>
                        </div>

                        <!-- Error alert -->
                        <c:if test="${not empty errorMessage}">
                            <div class="pc-alert-warning mb-4 text-danger border-danger">
                                <c:out value="${errorMessage}"/>
                            </div>
                        </c:if>

                        <form action="/photographers/${photographer.id}/book" method="post" id="booking-form">
                            
                            <!-- Date & Time Row -->
                            <div class="row g-3 mb-4">
                                <div class="col-sm-6">
                                    <label for="bookingDate" class="pc-label">Shoot Date *</label>
                                    <input type="date" 
                                           id="bookingDate" 
                                           name="bookingDate" 
                                           min="<c:out value='${minBookingDate}'/>"
                                           class="form-control pc-input" 
                                           value="<c:out value='${bookingRequest.bookingDate}'/>" 
                                           required>
                                    <spring:bind path="bookingRequest.bookingDate">
                                        <c:if test="${status.error}">
                                            <div class="text-danger small mt-1"><c:out value="${status.errorMessage}"/></div>
                                        </c:if>
                                    </spring:bind>
                                </div>

                                <div class="col-sm-6">
                                    <label for="bookingTime" class="pc-label">Preferred Time *</label>
                                    <input type="time" 
                                           id="bookingTime" 
                                           name="bookingTime" 
                                           class="form-control pc-input" 
                                           value="<c:out value='${bookingRequest.bookingTime}'/>" 
                                           required>
                                    <spring:bind path="bookingRequest.bookingTime">
                                        <c:if test="${status.error}">
                                            <div class="text-danger small mt-1"><c:out value="${status.errorMessage}"/></div>
                                        </c:if>
                                    </spring:bind>
                                </div>
                            </div>

                            <!-- Location -->
                            <div class="mb-4">
                                <label for="location" class="pc-label">Shoot Location / Address *</label>
                                <input type="text" 
                                       id="location" 
                                       name="location" 
                                       maxlength="255" 
                                       placeholder="e.g. Studio LightSpace, District 1..." 
                                       class="form-control pc-input" 
                                       value="<c:out value='${bookingRequest.location}'/>" 
                                       required>
                                <spring:bind path="bookingRequest.location">
                                    <c:if test="${status.error}">
                                        <div class="text-danger small mt-1"><c:out value="${status.errorMessage}"/></div>
                                    </c:if>
                                </spring:bind>
                            </div>

                            <!-- Notes / Requirements -->
                            <div class="mb-4">
                                <label for="notes" class="pc-label">Notes & Requirements (Optional)</label>
                                <textarea id="notes" 
                                          name="notes" 
                                          rows="4" 
                                          maxlength="1000" 
                                          placeholder="Share shoot concept, number of outfits, etc..." 
                                          class="form-control pc-input"><c:out value='${bookingRequest.notes}'/></textarea>
                                <spring:bind path="bookingRequest.notes">
                                    <c:if test="${status.error}">
                                        <div class="text-danger small mt-1"><c:out value="${status.errorMessage}"/></div>
                                    </c:if>
                                </spring:bind>
                            </div>

                            <div class="text-muted mb-4" style="font-size: 0.8rem;">
                                Note: Your request will be sent to the photographer for confirmation. No immediate payment is required until the photographer accepts.
                            </div>

                            <!-- Actions -->
                            <div class="d-flex align-items-center justify-content-between pt-2 border-top border-secondary border-opacity-10 mt-4 pt-4">
                                <a href="/photographers/${photographer.id}" class="pc-btn-outline px-4">
                                    Cancel
                                </a>
                                <button type="submit" class="pc-btn-primary px-5" id="btn-submit-booking">
                                    Request Booking
                                </button>
                            </div>

                        </form>
                    </div>
                </div>

            </div>

        </div>
    </main>

    <footer class="pc-footer">
        <div class="container">
            © 2026 PhotoConnect. Premium Photography Marketplace. All rights reserved.
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
