<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PhotoConnect | <c:out value="${errorTitle != null ? errorTitle : 'Notice'}"/></title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <!-- Bootstrap 5.3.3 Framework -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
</head>
<body>

    <%@ include file="fragments/navbar.jsp" %>

    <main class="error-viewport">
        <div class="error-card">
            <div class="error-status-badge">
                <c:out value="${statusCode != null ? statusCode : 'Notice'}"/>
            </div>

            <c:if test="${not empty errorCode}">
                <div>
                    <span class="error-code-pill">
                        <c:out value="${errorCode}"/>
                    </span>
                </div>
            </c:if>

            <h1 class="error-title">
                <c:out value="${errorTitle != null ? errorTitle : 'Unexpected Request'}"/>
            </h1>

            <p class="error-description">
                <c:out value="${errorMessage != null ? errorMessage : 'The requested operation could not be completed.'}"/>
            </p>

            <div class="error-actions">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Home</a>
                <a href="${pageContext.request.contextPath}/photographers" class="btn btn-secondary">Explore Marketplace</a>
                <button type="button" onclick="window.history.back()" class="btn btn-ghost">Previous Page</button>
            </div>
        </div>
    </main>

</body>
</html>
