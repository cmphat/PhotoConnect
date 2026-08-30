<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Become a Photographer – PhotoConnect</title>
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
                            <c:if test="${sessionScope.userRole == 'PHOTOGRAPHER'}">
                                <li class="nav-item"><a class="nav-link" href="/photographer/onboarding-status">Photographer Status</a></li>
                            </c:if>
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
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0">Apply to Become a Photographer</h4>
                    </div>
                    <div class="card-body p-4">
                        <div class="alert alert-info">
                            <strong>Please note:</strong> Your photographer profile will be reviewed by our team before it becomes publicly visible. You will receive a status of <em>Pending Review</em> after submission.
                        </div>

                        <c:if test="${not empty onboardingError}">
                            <div class="alert alert-danger">
                                <c:out value="${onboardingError}"/>
                            </div>
                        </c:if>

                        <form:form action="/become-photographer" method="post" modelAttribute="profileRequest">
                            <div class="mb-3">
                                <label for="displayName" class="form-label fw-semibold">Display Name <span class="text-danger">*</span></label>
                                <form:input path="displayName" id="displayName" cssClass="form-control" placeholder="e.g. Jane Doe Photography"/>
                                <form:errors path="displayName" cssClass="text-danger small"/>
                            </div>

                            <div class="mb-3">
                                <label for="bio" class="form-label fw-semibold">Bio <span class="text-danger">*</span></label>
                                <form:textarea path="bio" id="bio" cssClass="form-control" rows="5" placeholder="Tell clients about your photography style, experience, and specialties..."/>
                                <form:errors path="bio" cssClass="text-danger small"/>
                            </div>

                            <div class="mb-3">
                                <label for="city" class="form-label fw-semibold">City <span class="text-danger">*</span></label>
                                <form:input path="city" id="city" cssClass="form-control" placeholder="e.g. Ho Chi Minh City"/>
                                <form:errors path="city" cssClass="text-danger small"/>
                            </div>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="experienceYears" class="form-label fw-semibold">Years of Experience <span class="text-danger">*</span></label>
                                    <form:input path="experienceYears" id="experienceYears" type="number" cssClass="form-control" placeholder="e.g. 3"/>
                                    <form:errors path="experienceYears" cssClass="text-danger small"/>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="priceFrom" class="form-label fw-semibold">Starting Price (VND) <span class="text-danger">*</span></label>
                                    <form:input path="priceFrom" id="priceFrom" type="number" cssClass="form-control" placeholder="e.g. 1500000"/>
                                    <form:errors path="priceFrom" cssClass="text-danger small"/>
                                </div>
                            </div>

                            <div class="d-grid mt-3">
                                <button type="submit" class="btn btn-primary btn-lg">Submit Application</button>
                            </div>
                        </form:form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
