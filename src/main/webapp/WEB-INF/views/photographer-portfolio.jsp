<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Portfolio – PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .portfolio-masonry {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 1rem;
        }
        
        .portfolio-item {
            position: relative;
            background: var(--bg-dark-secondary);
            overflow: hidden;
            aspect-ratio: 4/5;
        }

        .portfolio-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .portfolio-overlay {
            position: absolute;
            inset: 0;
            background: rgba(0, 0, 0, 0.7);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 1.5rem;
            opacity: 0;
            transition: opacity 0.2s ease;
        }

        .portfolio-item:hover .portfolio-overlay {
            opacity: 1;
        }

        .portfolio-caption {
            color: #fff;
            font-size: 0.9rem;
            font-weight: 400;
        }

        .btn-delete-img {
            background: transparent;
            border: 1px solid rgba(255, 255, 255, 0.5);
            color: #fff;
            padding: 0.5rem 1rem;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            cursor: pointer;
            transition: background 0.2s, border-color 0.2s;
            align-self: flex-start;
        }

        .btn-delete-img:hover {
            background: rgba(239, 68, 68, 0.9);
            border-color: rgba(239, 68, 68, 0.9);
        }
    </style>
</head>
<body>

    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1000px;">
            
            <h1 class="editorial-title" style="margin-bottom: 2rem;">My Portfolio</h1>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(52, 211, 153, 0.2);">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(239, 68, 68, 0.2);">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- Upload Section -->
            <div style="border: 1px solid var(--border); padding: 3rem; margin-bottom: 4rem; border-radius: 0;">
                <h2 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1.5rem;">Upload New Image</h2>
                <form action="${pageContext.request.contextPath}/photographer/portfolio/upload" method="post" enctype="multipart/form-data" id="upload-form">
                    <div class="pc-upload-grid">
                        <div>
                            <label for="imageFile" style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em;">Image File *</label>
                            <input type="file" id="imageFile" name="imageFile" accept="image/jpeg,image/png,image/webp" required style="width: 100%; padding: 0.8rem; border: 1px solid var(--border-dark); background: transparent; color: var(--text-color);">
                            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem;">JPEG, PNG, or WEBP &middot; Max 10 MB</div>
                        </div>
                        <div>
                            <label for="caption" style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em;">Caption (optional)</label>
                            <input type="text" id="caption" name="caption" maxlength="500" placeholder="Describe this photo…" class="pc-input" style="border: 1px solid var(--border-dark);">
                        </div>
                        <div>
                            <button type="submit" class="btn btn-primary" id="btn-upload-submit">Upload</button>
                        </div>
                    </div>
                </form>
            </div>

            <!-- Portfolio Grid Section -->
            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <h2 class="editorial-heading" style="font-size: 1.5rem;">Your Images</h2>
                <span style="color: var(--text-muted);">${fn:length(images)} uploads</span>
            </div>

            <c:choose>
                <c:when test="${empty images}">
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 1rem;">No images yet</h3>
                        <p style="color: var(--text-muted); margin-bottom: 0;">Upload your first portfolio image above.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="portfolio-masonry">
                        <c:forEach var="img" items="${images}">
                            <div class="portfolio-item">
                                <img src="<c:out value='${img.imageUrl}'/>"
                                     alt="<c:out value='${not empty img.caption ? img.caption : \"Portfolio image\"}'/>"
                                     class="portfolio-img"
                                     loading="lazy">
                                <div class="portfolio-overlay">
                                    <form action="${pageContext.request.contextPath}/photographer/portfolio/${img.id}/delete" method="post" onsubmit="return confirm('Delete this image? This cannot be undone.');" style="margin: 0;">
                                        <button type="submit" class="btn-delete-img" id="btn-delete-${img.id}">Delete</button>
                                    </form>
                                    
                                    <div class="portfolio-caption">
                                        <c:choose>
                                            <c:when test="${not empty img.caption}">
                                                <c:out value="${img.caption}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <em style="opacity: 0.5;">No caption</em>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>

    <script>
        document.getElementById('upload-form').addEventListener('submit', function() {
            const btn = document.getElementById('btn-upload-submit');
            btn.disabled = true;
            btn.textContent = 'Uploading…';
        });
    </script>
</body>
</html>
