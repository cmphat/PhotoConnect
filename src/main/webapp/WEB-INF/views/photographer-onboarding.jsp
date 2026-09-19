<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Join the Verified Roster — PhotoConnect</title>

    <!-- Google Fonts: Plus Jakarta Sans & Playfair Display -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- PhotoConnect Custom Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">

    <style>
        .auth-split-layout {
            display: flex;
            min-height: calc(100vh - 74px);
            width: 100%;
        }

        .auth-image-pane {
            flex: 0 0 48%;
            position: relative;
            overflow: hidden;
            background-color: var(--bg-dark);
        }

        .auth-image-pane img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            opacity: 0.9;
        }

        .auth-overlay-quote {
            position: absolute;
            bottom: 3.5rem;
            left: 3.5rem;
            right: 3.5rem;
            background: rgba(7, 11, 18, 0.75);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            padding: 2rem 2.25rem;
            border-radius: var(--radius-sm);
            border: 1px solid rgba(255, 255, 255, 0.12);
            color: #F8FAFC;
        }

        .auth-overlay-quote blockquote {
            margin: 0 0 0.5rem 0;
            font-family: var(--font-editorial);
            font-size: 1.35rem;
            font-weight: 400;
            font-style: italic;
            line-height: 1.4;
        }

        .auth-overlay-quote cite {
            font-family: var(--font-primary);
            font-size: 0.78rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--accent);
            font-style: normal;
        }

        .auth-form-pane {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: clamp(2.5rem, 5vw, 4.5rem);
            background-color: var(--background);
        }

        .auth-form-card {
            width: 100%;
            max-width: 480px;
        }

        @media (max-width: 991px) {
            .auth-split-layout {
                flex-direction: column;
            }
            .auth-image-pane {
                flex: none;
                height: 30vh;
            }
            .auth-overlay-quote {
                display: none;
            }
        }
    </style>
</head>
<body>

    <!-- Global Professional Navigation -->
    <jsp:include page="fragments/navbar.jsp" />

    <div class="auth-split-layout">

        <!-- Left: Editorial Studio Photography -->
        <div class="auth-image-pane">
            <img src="https://images.unsplash.com/photo-1554046920-90dc5f3ac6ed?q=80&w=2000&auto=format&fit=crop"
                 alt="Editorial Photographer in Studio"
                 loading="eager">
            <div class="auth-overlay-quote">
                <blockquote>&ldquo;Join Vietnam&rsquo;s curated network of visionary commercial and editorial photographers.&rdquo;</blockquote>
                <cite>PhotoConnect Creator Network</cite>
            </div>
        </div>

        <!-- Right: Application Form -->
        <div class="auth-form-pane">
            <div class="auth-form-card">

                <span class="section-index">Creator Application</span>
                <h1 class="editorial-title" style="font-size: clamp(2.25rem, 4vw, 3.25rem); margin: 0 0 0.5rem 0;">
                    Join the Verified Roster
                </h1>
                <p class="pc-lead" style="font-size: 0.98rem; margin-bottom: 2rem;">
                    Submit your credentials to be featured on the PhotoConnect marketplace and receive client booking inquiries.
                </p>

                <div class="pc-alert" role="note" style="margin-bottom: 2rem;">
                    Your profile will be reviewed by our curatorial team. You will receive a status of <strong>Pending Review</strong> immediately following submission.
                </div>

                <c:if test="${not empty onboardingError}">
                    <div class="pc-alert pc-alert-danger" role="alert">
                        <c:out value="${onboardingError}"/>
                    </div>
                </c:if>

                <form:form action="${pageContext.request.contextPath}/become-photographer" method="post" modelAttribute="profileRequest">
                    <div class="form-group">
                        <label for="displayName" class="form-label">Professional Display Name *</label>
                        <form:input path="displayName" id="displayName" cssClass="form-input" placeholder="e.g. Linh Nguyen Editorial" required="true" />
                        <form:errors path="displayName" cssClass="field-error"/>
                    </div>

                    <div class="form-group">
                        <label for="bio" class="form-label">Artist Statement / Bio *</label>
                        <form:textarea path="bio" id="bio" cssClass="form-input" rows="4" placeholder="Describe your style, aesthetic focus, and storytelling approach…" required="true" />
                        <form:errors path="bio" cssClass="field-error"/>
                    </div>

                    <div class="form-group">
                        <label for="city" class="form-label">Primary City *</label>
                        <form:input path="city" id="city" cssClass="form-input" placeholder="e.g. Ho Chi Minh City, Hanoi, Da Nang…" required="true" />
                        <form:errors path="city" cssClass="field-error"/>
                    </div>

                    <div class="pc-stack-mobile" style="display: flex; gap: 1.75rem;">
                        <div class="form-group" style="flex: 1;">
                            <label for="experienceYears" class="form-label">Years Experience *</label>
                            <form:input path="experienceYears" id="experienceYears" type="number" cssClass="form-input" placeholder="e.g. 5" min="0" required="true" />
                            <form:errors path="experienceYears" cssClass="field-error"/>
                        </div>
                        <div class="form-group" style="flex: 1;">
                            <label for="priceFrom" class="form-label">Starting Rate (VND) *</label>
                            <form:input path="priceFrom" id="priceFrom" type="number" cssClass="form-input" placeholder="e.g. 2000000" min="0" step="50000" required="true" />
                            <form:errors path="priceFrom" cssClass="field-error"/>
                        </div>
                    </div>

                    <button type="submit" class="submit-btn" style="margin-top: 1.5rem;" id="btn-submit-onboarding">
                        Submit Application &rarr;
                    </button>
                </form:form>
            </div>
        </div>

    </div>

</body>
</html>
