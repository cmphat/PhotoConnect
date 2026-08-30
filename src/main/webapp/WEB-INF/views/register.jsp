<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />
    <div class="auth-layout" style="flex-direction: row-reverse;">
        
        <div class="auth-visual">
            <!-- Local controlled visual placeholder -->
            <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #111 0%, #1a1a1a 100%);">
                <span style="font-size: 2rem; color: var(--text-muted); opacity: 0.3; letter-spacing: 0.1em; text-transform: uppercase;">Apply to Roster</span>
            </div>
        </div>

        <div class="auth-form-container">
            <div class="auth-form" style="max-width: 480px;">
                
                <h2 class="editorial-heading" style="font-size: clamp(2rem, 4vw, 3.5rem); margin-bottom: 2.5rem;">Join PhotoConnect.</h2>

                <c:if test="${param.success != null}">
                    <div style="color: #34d399; font-size: 0.9rem; margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(52, 211, 153, 0.2);">
                        Registration successful! Welcome to PhotoConnect.
                    </div>
                </c:if>

                <form:form action="/register" method="post" modelAttribute="registerRequest">
                    
                    <c:if test="${not empty emailError}">
                        <div style="color: #dc3545; font-size: 0.9rem; margin-bottom: 1.5rem;">${emailError}</div>
                    </c:if>
                    <c:if test="${not empty passwordError}">
                        <div style="color: #dc3545; font-size: 0.9rem; margin-bottom: 1.5rem;">${passwordError}</div>
                    </c:if>

                    <div class="form-group" style="margin-bottom: 1.5rem;">
                        <label for="fullName" class="form-label">Full Name</label>
                        <form:input path="fullName" class="form-input" id="fullName" placeholder="Jane Doe" required="true" />
                        <form:errors path="fullName" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <div class="form-group" style="margin-bottom: 1.5rem;">
                        <label for="email" class="form-label">Email address</label>
                        <form:input path="email" type="email" class="form-input" id="email" placeholder="name@example.com" required="true" />
                        <form:errors path="email" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <div class="form-group" style="margin-bottom: 1.5rem;">
                        <label for="phone" class="form-label">Phone (Optional)</label>
                        <form:input path="phone" class="form-input" id="phone" placeholder="+84 123 456 789" />
                        <form:errors path="phone" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <div class="form-group" style="margin-bottom: 1.5rem;">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" id="password" name="password" class="form-input" required="true" />
                        <form:errors path="password" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <div class="form-group" style="margin-bottom: 2.5rem;">
                        <label for="confirmPassword" class="form-label">Confirm Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-input" required="true" />
                        <form:errors path="confirmPassword" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <button type="submit" class="submit-btn">Create Account</button>
                    
                </form:form>

                <div style="margin-top: 3rem; font-size: 0.9rem; color: var(--text-muted);">
                    Already have an account? <a href="/login" class="text-link">Sign in</a>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
