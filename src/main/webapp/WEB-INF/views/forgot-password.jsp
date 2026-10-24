<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <title>Quên Mật Khẩu — PhotoConnect</title>
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
            max-width: 440px;
        }

        @media (max-width: 991px) {
            .auth-split-layout {
                flex-direction: column;
            }
            .auth-image-pane {
                flex: none;
                height: 28vh;
            }
            .auth-overlay-quote {
                display: none;
            }
        }
    </style>
</head>

    <div class="auth-split-layout">

        <!-- Left Image Pane -->
        <div class="auth-image-pane">
            <img src="https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?auto=format&fit=crop&w=1400&q=80"
                 alt="Landscape Photographer Sunset"
                 loading="eager">
            <div class="auth-overlay-quote">
                <blockquote>&ldquo;Khôi phục quyền truy cập một cách an toàn và bảo mật cho tài khoản của bạn.&rdquo;</blockquote>
                <cite>PhotoConnect Identity Security</cite>
            </div>
        </div>

        <!-- Right Form Pane -->
        <div class="auth-form-pane">
            <div class="auth-form-card">

                <span class="section-index">Khôi phục tài khoản</span>
                <h1 class="editorial-title" style="font-size: clamp(2rem, 3.5vw, 2.75rem); margin: 0 0 0.5rem 0;">
                    Quên mật khẩu?
                </h1>
                <p class="pc-lead" style="font-size: 0.95rem; margin-bottom: 2rem;">
                    Nhập địa chỉ email tài khoản của bạn. Chúng tôi sẽ gửi mã OTP 6 chữ số để đặt lại mật khẩu mới.
                </p>

                <!-- Feedback Alert -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger pc-alert pc-alert-danger mb-4" role="alert">
                        <c:out value="${errorMessage}"/>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="mb-4">
                    <%@ include file="fragments/csrf-input.jsp" %>

                    <div class="form-group mb-4">
                        <label for="email" class="form-label text-muted small fw-bold">ĐỊA CHỈ EMAIL</label>
                        <input type="email"
                               name="email"
                               id="email"
                               class="form-control form-input"
                               required
                               placeholder="your.name@example.com"
                               autocomplete="email"
                               autofocus />
                    </div>

                    <button type="submit" class="btn btn-primary submit-btn w-100 py-3" id="btn-request-reset">
                        Gửi mã xác thực OTP &rarr;
                    </button>
                </form>

                <div class="text-center pt-3 border-top">
                    <a href="${pageContext.request.contextPath}/login" class="small text-muted text-decoration-none">
                        &larr; Quay lại trang Đăng nhập
                    </a>
                </div>

            </div>
        </div>

    </div>
