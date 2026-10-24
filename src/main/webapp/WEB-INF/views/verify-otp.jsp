<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <title>Xác thực Email — PhotoConnect</title>
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

        .otp-input-box {
            font-family: monospace;
            font-size: 1.75rem;
            letter-spacing: 0.5rem;
            text-align: center;
            font-weight: 700;
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
            <img src="https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=1400&q=80"
                 alt="Camera Lens Editorial"
                 loading="eager">
            <div class="auth-overlay-quote">
                <blockquote>&ldquo;Bảo vệ tài khoản và tính xác thực của cộng đồng nhiếp ảnh gia PhotoConnect.&rdquo;</blockquote>
                <cite>PhotoConnect Security Protocol</cite>
            </div>
        </div>

        <!-- Right Form Pane -->
        <div class="auth-form-pane">
            <div class="auth-form-card">

                <span class="section-index">Bước 2: Xác thực tài khoản</span>
                <h1 class="editorial-title" style="font-size: clamp(2rem, 3.5vw, 2.75rem); margin: 0 0 0.5rem 0;">
                    Nhập mã xác thực OTP
                </h1>
                <p class="pc-lead" style="font-size: 0.95rem; margin-bottom: 2rem;">
                    Mã xác thực gồm 6 chữ số đã được gửi tới email:
                    <strong class="text-dark"><c:out value="${email}"/></strong>
                </p>

                <!-- Alerts -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger pc-alert pc-alert-danger mb-4" role="alert">
                        <c:out value="${errorMessage}"/>
                    </div>
                </c:if>
                <c:if test="${not empty infoMessage}">
                    <div class="alert alert-success pc-alert pc-alert-success mb-4" role="alert">
                        <c:out value="${infoMessage}"/>
                    </div>
                </c:if>

                <!-- OTP Form -->
                <form action="${pageContext.request.contextPath}/verify-otp" method="post" class="mb-4">
                    <%@ include file="fragments/csrf-input.jsp" %>
                    <input type="hidden" name="email" value="<c:out value='${email}'/>" />
                    <input type="hidden" name="purpose" value="<c:out value='${purpose}'/>" />

                    <div class="form-group mb-4">
                        <label for="otpCode" class="form-label text-muted small fw-bold">MÃ XÁC THỰC (6 CHỮ SỐ)</label>
                        <input type="text"
                               name="otpCode"
                               id="otpCode"
                               class="form-control form-input otp-input-box"
                               maxlength="6"
                               pattern="[0-9]{6}"
                               inputmode="numeric"
                               autocomplete="one-time-code"
                               placeholder="••••••"
                               required
                               autofocus />
                        <div class="form-text mt-2 small text-muted">
                            Mã có hiệu lực trong 5 phút. Tối đa 5 lần thử.
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary submit-btn w-100 py-3" id="btn-verify-otp">
                        Xác nhận và kích hoạt &rarr;
                    </button>
                </form>

                <!-- Resend Form -->
                <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                    <span class="small text-muted">Chưa nhận được mã?</span>
                    <form action="${pageContext.request.contextPath}/resend-otp" method="post" class="d-inline m-0">
                        <%@ include file="fragments/csrf-input.jsp" %>
                        <input type="hidden" name="email" value="<c:out value='${email}'/>" />
                        <input type="hidden" name="purpose" value="<c:out value='${purpose}'/>" />
                        <button type="submit" class="btn btn-link p-0 small text-primary text-decoration-none fw-semibold">
                            Gửi lại mã OTP
                        </button>
                    </form>
                </div>

                <div class="mt-4 text-center">
                    <a href="${pageContext.request.contextPath}/login" class="small text-muted text-decoration-none">
                        &larr; Quay lại trang Đăng nhập
                    </a>
                </div>

            </div>
        </div>

    </div>
