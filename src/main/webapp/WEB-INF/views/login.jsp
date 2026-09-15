<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />
    <div class="auth-layout">
        
        <div class="auth-visual">
            <!-- Local controlled visual placeholder -->
            <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #111 0%, #1a1a1a 100%);">
                <span style="font-size: 2rem; color: var(--text-muted); opacity: 0.3; letter-spacing: 0.1em; text-transform: uppercase;">Editorial Identity</span>
            </div>
        </div>

        <div class="auth-form-container">
            <div class="auth-form">
                
                <h2 class="editorial-heading" style="margin-bottom: 3rem;">Welcome back.</h2>

                <c:if test="${not empty authError}">
                    <div style="color: #dc3545; font-size: 0.9rem; margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(220,53,69,0.2);">
                        ${authError}
                    </div>
                </c:if>

                <form:form action="${pageContext.request.contextPath}/login" method="post" modelAttribute="loginRequest">
                    
                    <div class="form-group">
                        <label for="email" class="form-label">Email address</label>
                        <form:input path="email" type="email" class="form-input" id="email" required="true" />
                        <form:errors path="email" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <div class="form-group" style="margin-bottom: 3rem;">
                        <label for="password" class="form-label">Password</label>
                        <form:password path="password" class="form-input" id="password" required="true" />
                        <form:errors path="password" cssStyle="color: #dc3545; font-size: 0.8rem; margin-top: 0.5rem; display: block;" />
                    </div>

                    <button type="submit" class="submit-btn">Sign In</button>
                    
                </form:form>

                <div style="margin-top: 3rem; font-size: 0.9rem; color: var(--text-muted);">
                    Don't have an account? <a href="${pageContext.request.contextPath}/register" class="text-link">Create one</a>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
