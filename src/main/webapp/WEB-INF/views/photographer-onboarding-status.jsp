<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Photographer Application Status – PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh; display: flex; justify-content: center; align-items: flex-start;">
        <div class="editorial-container" style="max-width: 600px; width: 100%;">
            
            <h1 class="editorial-title" style="margin-bottom: 2rem;">Application Status</h1>

            <c:choose>
                <c:when test="${not empty profile}">
                    
                    <div style="border: 1px solid var(--border); padding: 3rem; border-radius: 0; margin-bottom: 2rem;">
                        <h2 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1.5rem;"><c:out value="${profile.displayName}"/></h2>

                        <c:choose>
                            <c:when test="${verificationStatus == 'PENDING'}">
                                <div style="background: var(--bg-dark-secondary); border-left: 2px solid #f59e0b; padding: 1.5rem; margin-bottom: 2rem;">
                                    <strong style="display: block; margin-bottom: 0.5rem; color: #f59e0b;">Awaiting Review</strong>
                                    <p style="color: var(--text-muted); font-size: 0.95rem; margin: 0; line-height: 1.6;">
                                        Your photographer application is awaiting review by our team. We will notify you once a decision has been made. This usually takes 1–3 business days.
                                    </p>
                                </div>
                            </c:when>
                            <c:when test="${verificationStatus == 'APPROVED'}">
                                <div style="background: var(--bg-dark-secondary); border-left: 2px solid #10b981; padding: 1.5rem; margin-bottom: 2rem;">
                                    <strong style="display: block; margin-bottom: 0.5rem; color: #10b981;">Approved</strong>
                                    <p style="color: var(--text-muted); font-size: 0.95rem; margin: 0; line-height: 1.6;">
                                        Congratulations! Your photographer profile is approved and publicly visible.
                                    </p>
                                </div>
                            </c:when>
                            <c:when test="${verificationStatus == 'REJECTED'}">
                                <div style="background: var(--bg-dark-secondary); border-left: 2px solid #ef4444; padding: 1.5rem; margin-bottom: 2rem;">
                                    <strong style="display: block; margin-bottom: 0.5rem; color: #ef4444;">Rejected</strong>
                                    <p style="color: var(--text-muted); font-size: 0.95rem; margin: 0; line-height: 1.6;">
                                        Unfortunately your photographer application was rejected. Please contact support for more information.
                                    </p>
                                </div>
                            </c:when>
                            <c:when test="${verificationStatus == 'SUSPENDED'}">
                                <div style="background: var(--bg-dark-secondary); border-left: 2px solid #6b7280; padding: 1.5rem; margin-bottom: 2rem;">
                                    <strong style="display: block; margin-bottom: 0.5rem; color: #9ca3af;">Suspended</strong>
                                    <p style="color: var(--text-muted); font-size: 0.95rem; margin: 0; line-height: 1.6;">
                                        Your photographer profile has been suspended. Please contact support.
                                    </p>
                                </div>
                            </c:when>
                        </c:choose>

                        <div style="display: grid; gap: 1rem; border-top: 1px solid var(--border-dark); padding-top: 2rem;">
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: var(--text-muted);">City</span>
                                <span><c:out value="${profile.city}"/></span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: var(--text-muted);">Experience</span>
                                <span><c:out value="${profile.experienceYears}"/> years</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: var(--text-muted);">Starting Price</span>
                                <span><fmt:formatNumber value="${profile.priceFrom}" pattern="#,##0" /> VND</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; padding-top: 1rem; border-top: 1px solid var(--border-dark);">
                                <span style="color: var(--text-muted);">Status</span>
                                <span style="font-weight: 500;"><c:out value="${verificationStatus}"/></span>
                            </div>
                        </div>

                    </div>
                    
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 4rem 2rem; border: 1px solid var(--border); margin-bottom: 2rem;">
                        <h2 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem;">No Application Found</h2>
                        <p style="color: var(--text-muted); margin-bottom: 2rem;">You have not yet submitted a photographer application.</p>
                        <a href="/become-photographer" class="pc-btn-primary" style="padding: 1rem 2rem;">Apply Now</a>
                    </div>
                </c:otherwise>
            </c:choose>

            <div style="text-align: center;">
                <a href="/" class="text-link">Back to Home</a>
            </div>

        </div>
    </main>
</body>
</html>
