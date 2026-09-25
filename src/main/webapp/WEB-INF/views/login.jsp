<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <title>Sign In — PhotoConnect</title>
    <style>
        .auth-split-layout {
            display: flex;
            min-height: calc(100vh - 74px);
            width: 100%;
        }

        .auth-image-pane {
            flex: 0 0 52%;
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
            padding: clamp(2.5rem, 6vw, 5rem);
            background-color: var(--background);
        }

        .auth-form-card {
            width: 100%;
            max-width: 420px;
        }

        @media (max-width: 991px) {
            .auth-split-layout {
                flex-direction: column;
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

    <div class="auth-split-layout">

        <!-- Left: Large High-Resolution Editorial Photography Pane -->
        <div class="auth-image-pane">
            <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1400&q=80"
                 alt="Editorial Studio Portraiture"
                 loading="eager">
            <div class="auth-overlay-quote">
                <blockquote>&ldquo;Every portrait is a story told in the quiet balance of light and shadow.&rdquo;</blockquote>
                <cite>PhotoConnect Curated Network</cite>
            </div>
        </div>

        <!-- Right: Clean Editorial Form Pane -->
        <div class="auth-form-pane">
            <div class="auth-form-card">

                <span class="section-index">Welcome Back</span>
                <h1 class="editorial-title" style="font-size: clamp(2.25rem, 4vw, 3.25rem); margin: 0 0 0.5rem 0;">
                    Sign in to PhotoConnect
                </h1>
                <p class="pc-lead" style="font-size: 0.98rem; margin-bottom: 2.5rem;">
                    Access your client bookings, creator schedule, or portfolio studio.
                </p>

                <!-- Feedback / Error Alert -->
                <c:if test="${not empty authError}">
                    <div class="alert alert-danger pc-alert pc-alert-danger" role="alert">
                        <c:out value="${authError}"/>
                    </div>
                </c:if>
                <c:if test="${param.oauth == 'expired'}">
                    <div class="alert alert-warning pc-alert" role="alert">Your Google sign-in expired. Please try again.</div>
                </c:if>

                <a class="btn btn-outline-secondary w-100 mb-3" href="${pageContext.request.contextPath}/auth/google">
                    Continue with Google
                </a>
                <div class="d-flex align-items-center gap-3 mb-3" aria-hidden="true">
                    <span class="border-top flex-grow-1"></span><span class="text-muted small">or use email</span><span class="border-top flex-grow-1"></span>
                </div>

                <form:form action="${pageContext.request.contextPath}/login" method="post" modelAttribute="loginRequest">
                    <%@ include file="fragments/csrf-input.jsp" %>

                    <div class="form-group mb-3">
                        <label for="email" class="form-label">Email Address</label>
                        <form:input path="email" type="email" class="form-control form-input" id="email" autocomplete="email" required="true" placeholder="your.name@example.com" />
                        <form:errors path="email" cssClass="field-error" />
                    </div>

                    <div class="form-group mb-4" style="margin-bottom: 2.5rem;">
                        <label for="password" class="form-label">Password</label>
                        <form:password path="password" class="form-control form-input" id="password" autocomplete="current-password" required="true" placeholder="••••••••" />
                        <form:errors path="password" cssClass="field-error" />
                    </div>

                    <button type="submit" class="btn btn-primary submit-btn w-100" id="btn-sign-in">
                        Sign In &rarr;
                    </button>

                </form:form>

                <div style="margin-top: 2.5rem; font-size: 0.9rem; color: var(--muted); text-align: center;">
                    New to PhotoConnect? <a href="${pageContext.request.contextPath}/register" class="primary-link" style="margin-left: 0.35rem;">Create an Account</a>
                </div>

            </div>
        </div>

    </div>
