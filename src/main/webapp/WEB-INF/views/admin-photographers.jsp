<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pending Applications – Admin – PhotoConnect</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">PhotoConnect</a>
            <span class="navbar-text text-warning ms-2">Admin Panel</span>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item"><span class="nav-link">Hello, <c:out value="${sessionScope.userFullName}"/></span></li>
                    <li class="nav-item"><a class="nav-link active" href="/admin/photographers">Pending Applications</a></li>
                    <li class="nav-item">
                        <form action="/logout" method="post" class="d-inline">
                            <button type="submit" class="btn btn-link nav-link">Logout</button>
                        </form>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <h2 class="mb-4">Pending Photographer Applications</h2>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <c:choose>
            <c:when test="${empty applications}">
                <div class="alert alert-info">No pending photographer applications at this time.</div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle">
                        <thead class="table-dark">
                            <tr>
                                <th>#</th>
                                <th>Display Name</th>
                                <th>Email</th>
                                <th>City</th>
                                <th>Experience</th>
                                <th>Starting Price</th>
                                <th>Status</th>
                                <th>Submitted</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="app" items="${applications}">
                                <tr>
                                    <td><c:out value="${app.id}"/></td>
                                    <td><c:out value="${app.displayName}"/></td>
                                    <td><c:out value="${app.user.email}"/></td>
                                    <td><c:out value="${app.city}"/></td>
                                    <td><c:out value="${app.experienceYears}"/> yrs</td>
                                    <td><c:out value="${app.priceFrom}"/> VND</td>
                                    <td><span class="badge bg-warning text-dark"><c:out value="${app.verificationStatus}"/></span></td>
                                    <td>
                                        <c:if test="${not empty app.createdAt}">
                                            <c:out value="${app.createdAt}"/>
                                        </c:if>
                                    </td>
                                    <td>
                                        <a href="/admin/photographers/${app.id}" class="btn btn-sm btn-outline-primary">View</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
