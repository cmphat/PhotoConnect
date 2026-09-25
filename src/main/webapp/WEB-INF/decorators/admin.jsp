<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <c:if test="${not empty _csrf}">
        <meta name="csrf-token" content="${_csrf.token}">
        <meta name="csrf-header" content="${_csrf.headerName}">
    </c:if>
    <title><sitemesh:write property='title'>Admin — PhotoConnect</sitemesh:write></title>

    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- Bootstrap 5.3.3 Framework -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">

    <sitemesh:write property='head'/>
</head>
<body class="admin-layout">
    <jsp:include page="/WEB-INF/views/fragments/navbar.jsp" />

    <sitemesh:write property='body'/>
</body>
</html>
