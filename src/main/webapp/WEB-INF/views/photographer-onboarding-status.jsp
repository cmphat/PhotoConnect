<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Photographer Application Status – PhotoConnect</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">PhotoConnect</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav ms-auto">
                    <c:choose>
                        <c:when test="${not empty sessionScope.userId}">
                            <li class="nav-item">
                                <span class="nav-link">Hello, <c:out value="${sessionScope.userFullName}"/></span>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="/photographer/onboarding-status">Photographer Status</a>
                            </li>
                            <li class="nav-item">
                                <form action="/logout" method="post" class="d-inline">
                                    <button type="submit" class="btn btn-link nav-link">Logout</button>
                                </form>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li class="nav-item"><a class="nav-link" href="/login">Login</a></li>
                            <li class="nav-item"><a class="nav-link" href="/register">Register</a></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header bg-dark text-white">
                        <h4 class="mb-0">Photographer Application Status</h4>
                    </div>
                    <div class="card-body p-4">
                        <c:choose>
                            <c:when test="${not empty profile}">
                                <h5 class="mb-3"><c:out value="${profile.displayName}"/></h5>

                                <c:choose>
                                    <c:when test="${verificationStatus == 'PENDING'}">
                                        <div class="alert alert-warning">
                                            <strong>&#9203; Awaiting Review</strong><br>
                                            Your photographer application is awaiting review by our team. We will notify you once a decision has been made. This usually takes 1–3 business days.
                                        </div>
                                    </c:when>
                                    <c:when test="${verificationStatus == 'APPROVED'}">
                                        <div class="alert alert-success">
                                            <strong>&#10003; Approved</strong><br>
                                            Congratulations! Your photographer profile is approved and publicly visible.
                                        </div>
                                    </c:when>
                                    <c:when test="${verificationStatus == 'REJECTED'}">
                                        <div class="alert alert-danger">
                                            <strong>&#10007; Rejected</strong><br>
                                            Unfortunately your photographer application was rejected. Please contact support for more information.
                                        </div>
                                    </c:when>
                                    <c:when test="${verificationStatus == 'SUSPENDED'}">
                                        <div class="alert alert-secondary">
                                            <strong>&#9940; Suspended</strong><br>
                                            Your photographer profile has been suspended. Please contact support.
                                        </div>
                                    </c:when>
                                </c:choose>

                                <table class="table table-bordered mt-3">
                                    <tbody>
                                        <tr>
                                            <th scope="row">City</th>
                                            <td><c:out value="${profile.city}"/></td>
                                        </tr>
                                        <tr>
                                            <th scope="row">Experience</th>
                                            <td><c:out value="${profile.experienceYears}"/> years</td>
                                        </tr>
                                        <tr>
                                            <th scope="row">Starting Price</th>
                                            <td><c:out value="${profile.priceFrom}"/> VND</td>
                                        </tr>
                                        <tr>
                                            <th scope="row">Status</th>
                                            <td><c:out value="${verificationStatus}"/></td>
                                        </tr>
                                    </tbody>
                                </table>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-info">
                                    You have not yet submitted a photographer application.
                                    <a href="/become-photographer" class="alert-link">Apply now</a>.
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <a href="/" class="btn btn-outline-secondary mt-2">Back to Home</a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
