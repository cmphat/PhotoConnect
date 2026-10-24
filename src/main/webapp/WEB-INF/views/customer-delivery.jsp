<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
    <title>Delivery Gallery #${bookingId} — PhotoConnect</title>
</head>

<div class="container py-4">
    <!-- Header / Breadcrumbs -->
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-1">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard" class="text-decoration-none">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/bookings/${bookingId}" class="text-decoration-none">Booking #${bookingId}</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Delivery Gallery</li>
                </ol>
            </nav>
            <h1 class="h3 fw-bold mb-1">Your Finished Photographs</h1>
            <p class="text-muted mb-0">Delivered by photographer <span class="fw-semibold text-dark"><c:out value="${album.photographerName()}"/></span></p>
        </div>
        <div class="d-flex gap-2 align-items-center">
            <span class="badge ${album.statusBadgeClass()} px-3 py-2 fs-6">
                <c:out value="${album.statusDisplayName()}"/>
            </span>
            <a href="${pageContext.request.contextPath}/bookings/${bookingId}" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Back to Booking
            </a>
        </div>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>
            <c:out value="${successMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <c:out value="${errorMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Album Action & Review Banner -->
    <div class="card border-0 shadow-sm mb-4">
        <div class="card-body p-4">
            <div class="row g-4 align-items-center">
                <div class="col-12 col-md-5 border-end">
                    <span class="text-muted small text-uppercase fw-semibold">Photoshoot Details</span>
                    <h5 class="fw-bold mt-1 mb-0">
                        <c:out value="${empty album.packageName() ? 'Photography Session' : album.packageName()}"/>
                    </h5>
                    <div class="text-muted small mt-1">
                        Location: <c:out value="${album.location()}"/> &bull; Date: <c:out value="${album.bookingDate()}"/>
                    </div>
                    <div class="mt-2 text-dark small">
                        <strong><c:out value="${album.actualPhotoCount()}"/></strong> high-resolution photographs delivered
                        <c:if test="${not empty album.expectedPhotoCount()}">
                            (agreed package count: <c:out value="${album.expectedPhotoCount()}"/>)
                        </c:if>
                    </div>
                </div>

                <div class="col-12 col-md-7">
                    <c:choose>
                        <c:when test="${album.status() == 'ACCEPTED'}">
                            <div class="p-3 bg-light rounded text-success fw-semibold d-flex align-items-center gap-2">
                                <i class="bi bi-check-circle-fill fs-4"></i>
                                <div>
                                    <div>Delivery Accepted & Booking Completed</div>
                                    <small class="text-muted fw-normal">You can download your full-resolution photographs anytime below.</small>
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${album.status() == 'REVISION_REQUESTED'}">
                            <div class="p-3 bg-light rounded text-warning fw-semibold d-flex align-items-center gap-2">
                                <i class="bi bi-arrow-repeat fs-4"></i>
                                <div>
                                    <div>Revision Requested</div>
                                    <small class="text-muted fw-normal">Your photographer is reviewing your feedback and will update the album shortly.</small>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <!-- Delivered & Awaiting Review Actions -->
                            <div class="d-flex flex-wrap gap-2 justify-content-md-end">
                                <button type="button" class="btn btn-outline-warning" data-bs-toggle="modal" data-bs-target="#revisionModal">
                                    <i class="bi bi-chat-left-dots me-1"></i> Request Revision
                                </button>
                                <form action="${pageContext.request.contextPath}/bookings/${bookingId}/delivery/confirm" method="post" onsubmit="return confirm('Confirm that you have reviewed and received your finished deliverables? This will complete your booking.');" style="margin: 0;">
                                    <%@ include file="fragments/csrf-input.jsp" %>
                                    <button type="submit" class="btn btn-success" id="btn-confirm-delivery">
                                        <i class="bi bi-check2-circle me-1"></i> Confirm Receipt & Complete
                                    </button>
                                </form>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Notes or photographer instructions -->
            <c:if test="${not empty album.notes()}">
                <div class="mt-3 p-3 bg-light rounded border">
                    <span class="fw-semibold text-secondary small text-uppercase">Photographer Notes & Instructions:</span>
                    <p class="mb-0 text-dark small mt-1" style="white-space: pre-wrap;"><c:out value="${album.notes()}"/></p>
                </div>
            </c:if>
        </div>
    </div>

    <!-- Delivered Photos Grid -->
    <div class="card border-0 shadow-sm">
        <div class="card-header bg-transparent py-3 d-flex justify-content-between align-items-center border-bottom">
            <h5 class="card-title fw-bold mb-0">Delivered Photo Collection (${album.actualPhotoCount()})</h5>
            <span class="text-muted small">Click any photo to preview full resolution</span>
        </div>
        <div class="card-body p-4">
            <c:choose>
                <c:when test="${empty album.photos()}">
                    <div class="text-center py-5">
                        <i class="bi bi-images text-muted" style="font-size: 3rem;"></i>
                        <h6 class="mt-3 fw-bold text-dark">No photographs available yet</h6>
                        <p class="text-muted small mb-0">Your photographer is currently preparing your delivery album.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-4">
                        <c:forEach var="photo" items="${album.photos()}">
                            <div class="col-12 col-sm-6 col-md-4 col-lg-3">
                                <div class="card h-100 border rounded-3 overflow-hidden shadow-xs">
                                    <a href="${pageContext.request.contextPath}/bookings/${bookingId}/delivery/photos/${photo.id()}/preview" target="_blank" rel="noopener noreferrer" style="text-decoration: none;">
                                        <div style="position: relative; aspect-ratio: 1 / 1; overflow: hidden; background: #0F172A;">
                                            <img src="${pageContext.request.contextPath}/bookings/${bookingId}/delivery/photos/${photo.id()}/thumbnail" alt="Finished deliverable" style="width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease;">
                                        </div>
                                    </a>
                                    <div class="card-body p-2 d-flex justify-content-between align-items-center">
                                        <div class="text-truncate me-2 small text-muted" title="<c:out value='${photo.fileName()}'/>">
                                            <c:out value="${empty photo.fileName() ? 'Photo deliverable' : photo.fileName()}"/>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/bookings/${bookingId}/delivery/photos/${photo.id()}/download" class="btn btn-sm btn-primary" title="Download High-Res">
                                            <i class="bi bi-download"></i> Download
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<!-- Modal: Request Revision -->
<div class="modal fade" id="revisionModal" tabindex="-1" aria-labelledby="revisionModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/bookings/${bookingId}/delivery/request-revision" method="post">
                <%@ include file="fragments/csrf-input.jsp" %>
                <div class="modal-header">
                    <h5 class="modal-title fw-bold" id="revisionModalLabel">Request Delivery Revision</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <p class="text-muted small">
                        Please specify the adjustments you would like your photographer to make (e.g. crop adjustments, color grading, alternate selections).
                    </p>
                    <div class="mb-3">
                        <label for="revisionNotes" class="form-label fw-semibold">Revision Details <span class="text-danger">*</span></label>
                        <textarea class="form-control" id="revisionNotes" name="revisionNotes" rows="4" placeholder="Describe the requested changes..." required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-warning" id="btn-submit-revision">Submit Revision Request</button>
                </div>
            </form>
        </div>
    </div>
</div>
