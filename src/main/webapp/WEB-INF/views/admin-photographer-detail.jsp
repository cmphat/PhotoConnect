<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Application Detail – Admin – PhotoConnect</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">PhotoConnect</a>
            <span class="navbar-text text-warning ms-2">Admin Panel</span>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item"><a class="nav-link" href="/admin/photographers">← All Applications</a></li>
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
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <c:choose>
            <c:when test="${not empty profile}">
                <div class="card shadow-sm">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h4 class="mb-0"><c:out value="${profile.displayName}"/></h4>
                        <c:choose>
                            <c:when test="${profile.verificationStatus == 'PENDING'}">
                                <span class="badge bg-warning text-dark fs-6">PENDING</span>
                            </c:when>
                            <c:when test="${profile.verificationStatus == 'APPROVED'}">
                                <span class="badge bg-success fs-6">APPROVED</span>
                            </c:when>
                            <c:when test="${profile.verificationStatus == 'REJECTED'}">
                                <span class="badge bg-danger fs-6">REJECTED</span>
                            </c:when>
                            <c:when test="${profile.verificationStatus == 'SUSPENDED'}">
                                <span class="badge bg-secondary fs-6">SUSPENDED</span>
                            </c:when>
                        </c:choose>
                    </div>
                    <div class="card-body">
                        <table class="table table-bordered mb-4">
                            <tbody>
                                <tr>
                                    <th scope="row" style="width:200px">Account Email</th>
                                    <td><c:out value="${profile.user.email}"/></td>
                                </tr>
                                <tr>
                                    <th scope="row">Full Name</th>
                                    <td><c:out value="${profile.user.fullName}"/></td>
                                </tr>
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
                                    <th scope="row">Submitted</th>
                                    <td><c:out value="${profile.createdAt}"/></td>
                                </tr>
                            </tbody>
                        </table>

                        <h5>Bio</h5>
                        <div class="border rounded p-3 bg-light mb-4">
                            <c:out value="${profile.bio}" escapeXml="false"/>
                        </div>

                        <c:if test="${profile.verificationStatus == 'PENDING'}">
                            <div class="d-flex gap-3">
                                <form action="/admin/photographers/${profile.id}/approve" method="post">
                                    <button type="submit" class="btn btn-success btn-lg"
                                            onclick="return confirm('Approve this application?')">
                                        &#10003; Approve
                                    </button>
                                </form>
                                <form action="/admin/photographers/${profile.id}/reject" method="post">
                                    <button type="submit" class="btn btn-danger btn-lg"
                                            onclick="return confirm('Reject this application?')">
                                        &#10007; Reject
                                    </button>
                                </form>
                            </div>
                        </c:if>

                        <c:if test="${profile.verificationStatus != 'PENDING'}">
                            <div class="alert alert-secondary mt-3">
                                This application has already been processed (status: <strong><c:out value="${profile.verificationStatus}"/></strong>).
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-warning">Application not found.</div>
            </c:otherwise>
        </c:choose>

        <a href="/admin/photographers" class="btn btn-outline-secondary mt-3">← Back to List</a>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
