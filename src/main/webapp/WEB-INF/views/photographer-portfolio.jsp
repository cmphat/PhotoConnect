<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Portfolio – PhotoConnect</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background: #f8f9fa; color: #1a1a2e; }

        /* ── Navbar ──────────────────────────────────────────────── */
        .navbar {
            background: linear-gradient(135deg, #0f0c29, #302b63, #24243e) !important;
            box-shadow: 0 2px 20px rgba(0,0,0,0.3);
        }
        .navbar-brand { font-weight: 700; font-size: 1.4rem; color: #fff !important; }
        .navbar-brand span { color: #a78bfa; }

        /* ── Page header ─────────────────────────────────────────── */
        .page-header {
            background: linear-gradient(135deg, #0f0c29, #302b63);
            color: #fff;
            padding: 40px 0 32px;
        }
        .page-header h1 { font-size: 1.9rem; font-weight: 700; margin-bottom: 4px; }
        .page-header p { color: #c4b5fd; font-size: 0.95rem; margin: 0; }

        /* ── Upload card ─────────────────────────────────────────── */
        .upload-card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 2px 16px rgba(0,0,0,0.07);
            padding: 28px;
            margin-bottom: 32px;
        }
        .upload-card h5 {
            font-weight: 700;
            color: #7c3aed;
            margin-bottom: 18px;
            font-size: 1rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .form-label { font-weight: 500; font-size: 0.9rem; }
        .btn-upload {
            background: linear-gradient(135deg, #7c3aed, #6d28d9);
            color: #fff;
            border: none;
            border-radius: 10px;
            padding: 10px 28px;
            font-weight: 600;
            font-size: 0.9rem;
            transition: opacity 0.18s;
        }
        .btn-upload:hover { opacity: 0.88; color: #fff; }
        .file-hint { font-size: 0.8rem; color: #9ca3af; margin-top: 4px; }

        /* ── Portfolio grid ──────────────────────────────────────── */
        .portfolio-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            overflow: hidden;
            transition: transform 0.2s, box-shadow 0.2s;
            height: 100%;
        }
        .portfolio-card:hover { transform: translateY(-4px); box-shadow: 0 8px 28px rgba(0,0,0,0.12); }
        .portfolio-img {
            width: 100%;
            height: 200px;
            object-fit: cover;
            display: block;
        }
        .portfolio-card-body {
            padding: 14px 16px 16px;
        }
        .portfolio-caption {
            font-size: 0.88rem;
            color: #374151;
            margin-bottom: 12px;
            min-height: 20px;
        }
        .btn-delete {
            background: none;
            border: 1px solid #ef4444;
            color: #ef4444;
            border-radius: 8px;
            padding: 5px 14px;
            font-size: 0.8rem;
            font-weight: 600;
            transition: all 0.18s;
            cursor: pointer;
        }
        .btn-delete:hover { background: #ef4444; color: #fff; }

        /* ── Empty state ─────────────────────────────────────────── */
        .empty-portfolio {
            text-align: center;
            padding: 64px 24px;
            color: #9ca3af;
        }
        .empty-portfolio .empty-icon { font-size: 3.5rem; margin-bottom: 14px; display: block; }
        .empty-portfolio h4 { font-weight: 600; color: #374151; margin-bottom: 6px; }

        /* ── Alerts ──────────────────────────────────────────────── */
        .alert { border-radius: 12px; font-size: 0.9rem; }
    </style>
</head>
<body>

    <%-- ── Navbar ─────────────────────────────────────────────────── --%>
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="/"><span>Photo</span>Connect</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item"><a class="nav-link" href="/">Home</a></li>
                    <li class="nav-item"><a class="nav-link" href="/photographers">Photographers</a></li>
                    <li class="nav-item"><a class="nav-link active" href="/photographer/portfolio">My Portfolio</a></li>
                </ul>
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <span class="nav-link text-light">Hello, <c:out value="${sessionScope.userFullName}"/></span>
                    </li>
                    <li class="nav-item"><a class="nav-link" href="/photographer/onboarding-status">My Status</a></li>
                    <li class="nav-item">
                        <form action="/logout" method="post" class="d-inline">
                            <button type="submit" class="btn btn-link nav-link">Logout</button>
                        </form>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <%-- ── Page header ─────────────────────────────────────────────── --%>
    <section class="page-header">
        <div class="container">
            <h1>📷 My Portfolio</h1>
            <p>Upload and manage your portfolio images. Only JPEG, PNG, and WEBP supported (max 10 MB each).</p>
        </div>
    </section>

    <%-- ── Main content ─────────────────────────────────────────────── --%>
    <div class="container py-4">

        <%-- Flash messages --%>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success mb-4"><c:out value="${successMessage}"/></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger mb-4"><c:out value="${errorMessage}"/></div>
        </c:if>

        <%-- ── Upload form ─────────────────────────────────────────── --%>
        <div class="upload-card">
            <h5>Upload New Image</h5>
            <%--
                enctype="multipart/form-data" is required for file uploads.
                Without it, the file bytes are never sent — only the filename.
            --%>
            <form action="/photographer/portfolio/upload" method="post"
                  enctype="multipart/form-data" id="upload-form">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label for="imageFile" class="form-label">Image File *</label>
                        <input type="file" class="form-control" id="imageFile" name="imageFile"
                               accept="image/jpeg,image/png,image/webp" required>
                        <div class="file-hint">JPEG, PNG, or WEBP · Maximum 10 MB</div>
                    </div>
                    <div class="col-md-5">
                        <label for="caption" class="form-label">Caption (optional)</label>
                        <input type="text" class="form-control" id="caption" name="caption"
                               maxlength="500" placeholder="Describe this photo…">
                    </div>
                    <div class="col-md-1 d-flex align-items-end">
                        <button type="submit" class="btn-upload w-100" id="btn-upload-submit">Upload</button>
                    </div>
                </div>
            </form>
        </div>

        <%-- ── Portfolio grid ──────────────────────────────────────── --%>
        <h5 class="fw-700 mb-3" style="font-weight:700; color:#1a1a2e;">
            Your Images (<c:out value="${fn:length(images)}"/>)
        </h5>

        <c:choose>
            <c:when test="${empty images}">
                <div class="empty-portfolio">
                    <span class="empty-icon">🖼️</span>
                    <h4>No images yet</h4>
                    <p>Upload your first portfolio image above.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-3">
                    <c:forEach var="img" items="${images}">
                        <div class="col-sm-6 col-md-4 col-lg-3">
                            <div class="portfolio-card">
                                <img src="<c:out value='${img.imageUrl}'/>"
                                     alt="<c:out value='${not empty img.caption ? img.caption : \"Portfolio image\"}'/>"
                                     class="portfolio-img"
                                     loading="lazy">
                                <div class="portfolio-card-body">
                                    <p class="portfolio-caption">
                                        <c:choose>
                                            <c:when test="${not empty img.caption}">
                                                <c:out value="${img.caption}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted fst-italic">No caption</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <form action="/photographer/portfolio/${img.id}/delete"
                                          method="post"
                                          onsubmit="return confirm('Delete this image? This cannot be undone.');">
                                        <button type="submit" class="btn-delete"
                                                id="btn-delete-${img.id}">
                                            🗑 Delete
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Prevent double-submit on upload
        document.getElementById('upload-form').addEventListener('submit', function() {
            const btn = document.getElementById('btn-upload-submit');
            btn.disabled = true;
            btn.textContent = 'Uploading…';
        });
    </script>
</body>
</html>
