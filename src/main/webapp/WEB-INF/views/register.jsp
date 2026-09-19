<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account — PhotoConnect</title>

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
            flex: 0 0 50%;
            position: relative;
            overflow: hidden;
            background-color: var(--bg-dark);
        }

        .auth-image-pane img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            opacity: 0.92;
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
            max-width: 460px;
        }

        @media (max-width: 991px) {
            .auth-split-layout {
                flex-direction: column-reverse;
            }
            .auth-image-pane {
                flex: none;
                height: 32vh;
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

        <!-- Right/Left Form Pane -->
        <div class="auth-form-pane">
            <div class="auth-form-card">

                <span class="section-index">Join the Community</span>
                <h1 class="editorial-title" style="font-size: clamp(2.25rem, 4vw, 3.25rem); margin: 0 0 0.5rem 0;">
                    Create your account
                </h1>
                <p class="pc-lead" style="font-size: 0.98rem; margin-bottom: 2rem;">
                    Join PhotoConnect to book verified photographers or begin your journey as a visual creator.
                </p>

                <!-- Feedback Alerts -->
                <c:if test="${param.success != null}">
                    <div class="pc-alert pc-alert-success" role="status">
                        Registration successful! Welcome to PhotoConnect.
                    </div>
                </c:if>
                <c:if test="${not empty emailError}">
                    <div class="pc-alert pc-alert-danger" role="alert">
                        <c:out value="${emailError}"/>
                    </div>
                </c:if>
                <c:if test="${not empty passwordError}">
                    <div class="pc-alert pc-alert-danger" role="alert">
                        <c:out value="${passwordError}"/>
                    </div>
                </c:if>

                <form:form action="${pageContext.request.contextPath}/register" method="post" modelAttribute="registerRequest">

                    <div class="form-group" style="margin-bottom: 1.25rem;">
                        <label for="fullName" class="form-label">Full Name *</label>
                        <form:input path="fullName" class="form-input" id="fullName" placeholder="Nguyen Van A" autocomplete="name" required="true" />
                        <form:errors path="fullName" cssClass="field-error" />
                    </div>

                    <div class="form-group" style="margin-bottom: 1.25rem;">
                        <label for="email" class="form-label">Email Address *</label>
                        <form:input path="email" type="email" class="form-input" id="email" placeholder="name@example.com" autocomplete="email" required="true" />
                        <form:errors path="email" cssClass="field-error" />
                    </div>

                    <div class="form-group" style="margin-bottom: 1.25rem;">
                        <label for="phone" class="form-label">Phone Number (Optional)</label>
                        <form:input path="phone" type="tel" class="form-input" id="phone" placeholder="+84 901 234 567" autocomplete="tel" />
                        <form:errors path="phone" cssClass="field-error" />
                    </div>

                    <div class="form-group" style="margin-bottom: 1.25rem;">
                        <label for="password" class="form-label">Password *</label>
                        <input type="password" id="password" name="password" class="form-input" autocomplete="new-password" required placeholder="8–72 characters" />
                        <div class="form-help">Must contain 8 to 72 characters.</div>
                        <form:errors path="password" cssClass="field-error" />
                    </div>

                    <div class="form-group" style="margin-bottom: 2.25rem;">
                        <label for="confirmPassword" class="form-label">Confirm Password *</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-input" autocomplete="new-password" required placeholder="Re-enter password" />
                        <form:errors path="confirmPassword" cssClass="field-error" />
                    </div>

                    <button type="submit" class="submit-btn" id="btn-register">
                        Create Account &rarr;
                    </button>

                </form:form>

                <div style="margin-top: 2.5rem; font-size: 0.9rem; color: var(--muted); text-align: center;">
                    Already have an account? <a href="${pageContext.request.contextPath}/login" class="primary-link" style="margin-left: 0.35rem;">Sign in</a>
                </div>

            </div>
        </div>

        <!-- Right: Photography Image Pane -->
        <div class="auth-image-pane">
            <img src="https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1400&q=80"
                 alt="Natural daylight fashion portrait"
                 loading="eager">
            <div class="auth-overlay-quote">
                <blockquote>&ldquo;Find the right eye for every story worth remembering.&rdquo;</blockquote>
                <cite>PhotoConnect Creator Roster</cite>
            </div>
        </div>

    </div>

</body>
</html>
