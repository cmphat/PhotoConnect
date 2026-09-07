<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Join the Roster – PhotoConnect</title>
    
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    
    <style>
        /* Specific page tweaks can go here if absolutely necessary, but we try to keep it in photoconnect.css */
        .onboarding-error {
            background-color: rgba(220, 53, 69, 0.1);
            color: #ff6b6b;
            padding: 1rem;
            border-left: 2px solid #dc3545;
            margin-bottom: 2rem;
            font-size: 0.9rem;
        }
        .onboarding-info {
            background-color: rgba(255, 255, 255, 0.05);
            padding: 1.5rem;
            margin-bottom: 2rem;
            border-left: 2px solid var(--text-on-dark);
            font-size: 0.9rem;
            color: var(--text-muted);
        }
    </style>
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main class="auth-layout">
        <div class="auth-visual">
            <div class="auth-brand-overlay">PhotoConnect<br><span style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--text-muted);">Creator Network</span></div>
            <img src="https://images.unsplash.com/photo-1554046920-90dc5f3ac6ed?q=80&w=2000&auto=format&fit=crop" alt="Editorial Photographer in Studio">
        </div>
        
        <div class="auth-form-container">
            <div class="auth-form">
                <h1 class="editorial-heading" style="font-size: 2.5rem; margin-bottom: 2rem;">Join the Roster</h1>
                
                <div class="onboarding-info">
                    Your photographer profile will be reviewed by our team before it becomes publicly visible. You will receive a status of <strong>Pending Review</strong> after submission.
                </div>

                <c:if test="${not empty onboardingError}">
                    <div class="onboarding-error">
                        <c:out value="${onboardingError}"/>
                    </div>
                </c:if>

                <form:form action="/become-photographer" method="post" modelAttribute="profileRequest">
                    <div class="form-group">
                        <label for="displayName" class="form-label">Display Name *</label>
                        <form:input path="displayName" id="displayName" cssClass="form-input" placeholder="e.g. Jane Doe Photography"/>
                        <form:errors path="displayName" cssClass="text-danger small" cssStyle="color: #ff6b6b; font-size: 0.85rem; margin-top: 0.5rem; display: block;"/>
                    </div>

                    <div class="form-group">
                        <label for="bio" class="form-label">Bio *</label>
                        <form:textarea path="bio" id="bio" cssClass="form-input" rows="4" placeholder="Tell clients about your photography style..."/>
                        <form:errors path="bio" cssClass="text-danger small" cssStyle="color: #ff6b6b; font-size: 0.85rem; margin-top: 0.5rem; display: block;"/>
                    </div>

                    <div class="form-group">
                        <label for="city" class="form-label">City *</label>
                        <form:input path="city" id="city" cssClass="form-input" placeholder="e.g. Ho Chi Minh City"/>
                        <form:errors path="city" cssClass="text-danger small" cssStyle="color: #ff6b6b; font-size: 0.85rem; margin-top: 0.5rem; display: block;"/>
                    </div>

                    <div style="display: flex; gap: 2rem;">
                        <div class="form-group" style="flex: 1;">
                            <label for="experienceYears" class="form-label">Years of Experience *</label>
                            <form:input path="experienceYears" id="experienceYears" type="number" cssClass="form-input" placeholder="e.g. 3"/>
                            <form:errors path="experienceYears" cssClass="text-danger small" cssStyle="color: #ff6b6b; font-size: 0.85rem; margin-top: 0.5rem; display: block;"/>
                        </div>
                        <div class="form-group" style="flex: 1;">
                            <label for="priceFrom" class="form-label">Starting Price (VND) *</label>
                            <form:input path="priceFrom" id="priceFrom" type="number" cssClass="form-input" placeholder="e.g. 1500000"/>
                            <form:errors path="priceFrom" cssClass="text-danger small" cssStyle="color: #ff6b6b; font-size: 0.85rem; margin-top: 0.5rem; display: block;"/>
                        </div>
                    </div>

                    <button type="submit" class="submit-btn" style="margin-top: 1.5rem;">Submit Application</button>
                </form:form>
            </div>
        </div>
    </main>

</body>
</html>
