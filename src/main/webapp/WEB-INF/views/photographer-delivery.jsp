<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
    <title>Delivery Studio #${bookingId} — PhotoConnect</title>
</head>

<div class="container py-4">
    <!-- Header / Breadcrumbs -->
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-1">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/photographer/dashboard" class="text-decoration-none">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/photographer/bookings/${bookingId}" class="text-decoration-none">Booking #${bookingId}</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Delivery Studio</li>
                </ol>
            </nav>
            <h1 class="h3 fw-bold mb-1">Delivery Studio & Gallery</h1>
            <p class="text-muted mb-0">Upload and curate finished deliverables for client <span class="fw-semibold text-dark"><c:out value="${album.customerName()}"/></span></p>
        </div>
        <div class="d-flex gap-2 align-items-center">
            <span class="badge ${album.statusBadgeClass()} px-3 py-2 fs-6">
                <c:out value="${album.statusDisplayName()}"/>
            </span>
            <a href="${pageContext.request.contextPath}/photographer/bookings/${bookingId}" class="btn btn-outline-secondary">
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

    <!-- Deliverables KPI & Progress Summary -->
    <div class="card border-0 shadow-sm mb-4">
        <div class="card-body p-4">
            <div class="row g-4 align-items-center">
                <div class="col-12 col-md-4 border-end">
                    <span class="text-muted small text-uppercase fw-semibold">Package Deliverable</span>
                    <h5 class="fw-bold mt-1 mb-0">
                        <c:out value="${empty album.packageName() ? 'Standard Photography Session' : album.packageName()}"/>
                    </h5>
                    <div class="text-muted small mt-1">
                        Location: <c:out value="${album.location()}"/> &bull; Date: <c:out value="${album.bookingDate()}"/>
                    </div>
                </div>
                <div class="col-12 col-md-4 border-end">
                    <span class="text-muted small text-uppercase fw-semibold">Photo Count Progress</span>
                    <div class="d-flex align-items-baseline gap-2 mt-1">
                        <span class="h3 fw-bold mb-0 text-primary"><c:out value="${album.actualPhotoCount()}"/></span>
                        <c:if test="${not empty album.expectedPhotoCount()}">
                            <span class="text-muted">/ <c:out value="${album.expectedPhotoCount()}"/> expected photos</span>
                        </c:if>
                    </div>
                    <c:if test="${not empty album.expectedPhotoCount() && album.expectedPhotoCount() > 0}">
                        <div class="progress mt-2" style="height: 6px;">
                            <c:set var="pct" value="${(album.actualPhotoCount() * 100) / album.expectedPhotoCount()}"/>
                            <div class="progress-bar bg-primary" role="progressbar" style="width: ${pct > 100 ? 100 : pct}%;" aria-valuenow="${pct}" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>
                    </c:if>
                </div>
                <div class="col-12 col-md-4 d-flex flex-column justify-content-center">
                    <c:choose>
                        <c:when test="${album.status() == 'ACCEPTED'}">
                            <div class="text-success fw-semibold">
                                <i class="bi bi-check2-all fs-4 me-1"></i> Album Accepted by Client
                            </div>
                            <small class="text-muted">Completed on <c:out value="${album.acceptedAt()}"/></small>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/photographer/bookings/${bookingId}/delivery/deliver" method="post" onsubmit="return confirm('Ready to deliver this album to your client for review?');">
                                <%@ include file="fragments/csrf-input.jsp" %>
                                <button type="submit" class="btn btn-primary w-100" id="btn-deliver-album" ${album.actualPhotoCount() == 0 ? 'disabled' : ''}>
                                    <i class="bi bi-send-check me-1"></i> Deliver Album to Client
                                </button>
                            </form>
                            <small class="text-muted mt-1 text-center d-block">Submits gallery for client review & verification</small>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Notes Banner if Revision Requested -->
            <c:if test="${not empty album.notes()}">
                <div class="mt-3 p-3 bg-light rounded border">
                    <span class="fw-semibold text-secondary small text-uppercase">Album Notes & Client Feedback:</span>
                    <p class="mb-0 text-dark small mt-1" style="white-space: pre-wrap;"><c:out value="${album.notes()}"/></p>
                </div>
            </c:if>
        </div>
    </div>

    <!-- Upload Box (if not accepted) -->
    <c:if test="${album.status() != 'ACCEPTED'}">
        <div class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-transparent py-3 border-bottom">
                <h5 class="card-title fw-bold mb-0">Upload Finished Photographs</h5>
            </div>
            <div class="card-body p-4">
                <form action="${pageContext.request.contextPath}/photographer/bookings/${bookingId}/delivery/upload" method="post" enctype="multipart/form-data" class="row g-3 align-items-center">
                    <%@ include file="fragments/csrf-input.jsp" %>
                    <div class="col-12 col-md-8">
                        <label for="photoFile" class="form-label text-muted small fw-semibold">Select image file (JPEG, PNG, WEBP, up to 15MB)</label>
                        <input type="file" class="form-control" id="photoFile" name="photo" accept="image/jpeg,image/png,image/webp" required>
                    </div>
                    <div class="col-12 col-md-4 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100" id="btn-upload-photo" style="margin-top: 1.8rem;">
                            <i class="bi bi-cloud-arrow-up me-1"></i> Upload Photograph
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <!-- Photo Gallery Grid -->
    <div class="card border-0 shadow-sm">
        <div class="card-header bg-transparent py-3 d-flex justify-content-between align-items-center border-bottom">
            <h5 class="card-title fw-bold mb-0">Delivered Photographs (${album.actualPhotoCount()})</h5>
            <span class="text-muted small">Server-side authenticated delivery</span>
        </div>
        <div class="card-body p-4">
            <c:choose>
                <c:when test="${empty album.photos()}">
                    <div class="text-center py-5">
                        <i class="bi bi-images text-muted" style="font-size: 3rem;"></i>
                        <h6 class="mt-3 fw-bold text-dark">No photographs uploaded yet</h6>
                        <p class="text-muted small mb-0">Upload your edited, full-resolution photos above to prepare this delivery album.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-4">
                        <c:forEach var="photo" items="${album.photos()}">
                            <div class="col-12 col-sm-6 col-md-4 col-lg-3">
                                <div class="card h-100 border rounded-3 overflow-hidden shadow-xs">
                                    <a href="${pageContext.request.contextPath}/photographer/bookings/${bookingId}/delivery/photos/${photo.id()}/preview" target="_blank" rel="noopener noreferrer" style="text-decoration: none;">
                                        <div style="position: relative; aspect-ratio: 1 / 1; overflow: hidden; background: #0F172A;">
                                            <img src="${pageContext.request.contextPath}/photographer/bookings/${bookingId}/delivery/photos/${photo.id()}/thumbnail" alt="Delivered photo" style="width: 100%; height: 100%; object-fit: cover;">
                                        </div>
                                    </a>
                                    <div class="card-body p-2 d-flex justify-content-between align-items-center">
                                        <div class="text-truncate me-2 small text-muted" title="<c:out value='${photo.fileName()}'/>">
                                            <c:out value="${empty photo.fileName() ? 'Photo deliverable' : photo.fileName()}"/>
                                        </div>
                                        <div class="d-flex gap-1">
                                            <a href="${pageContext.request.contextPath}/photographer/bookings/${bookingId}/delivery/photos/${photo.id()}/download" class="btn btn-sm btn-outline-primary" title="Download Full Resolution">
                                                <i class="bi bi-download"></i>
                                            </a>
                                            <c:if test="${album.status() != 'ACCEPTED'}">
                                                <form action="${pageContext.request.contextPath}/photographer/bookings/${bookingId}/delivery/photos/${photo.id()}/delete" method="post" onsubmit="return confirm('Remove this photo from the delivery album?');" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-sm btn-outline-danger" title="Delete Photo">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
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
