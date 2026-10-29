<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <title>System Settings — PhotoConnect Admin</title>
</head>

<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">

        <div class="pc-page-header pc-stack-mobile">
            <div>
                <span class="section-index">ADMINISTRATION &amp; CONFIGURATION</span>
                <h1 class="editorial-title">PhotoConnect System Settings</h1>
            </div>
            <div class="pc-page-meta">
                Environment: <strong>Local Development / Demo</strong>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success pc-alert pc-alert-success" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger pc-alert pc-alert-danger" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <div class="row g-4">
            <!-- Box 1: Commission Policies & Escrow -->
            <div class="col-12 col-md-6">
                <div class="admin-card-container h-100">
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                        <span style="color: var(--admin-primary);">％</span> Commission &amp; Escrow Deposit Policies
                    </h3>
                    <p style="font-size: 0.85rem; color: var(--admin-text-muted); margin-bottom: 20px;">
                        Core platform operational policies governing customer escrow and platform fees.
                    </p>

                    <form action="${pageContext.request.contextPath}/admin/platform-fees/rate" method="post" class="mb-4">
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        </c:if>
                        <div class="form-group mb-3">
                            <label class="form-label" style="font-weight: 600; font-size: 0.85rem;">Photographer Platform Fee Rate (% on completed bookings)</label>
                            <div class="input-group">
                                <input type="number" step="0.5" min="0" max="30" name="ratePercent" class="form-control" value="${platformFeeRatePercent}" required>
                                <span class="input-group-text">%</span>
                                <button type="submit" class="btn btn-primary btn-sm">Update Rate</button>
                            </div>
                            <small class="text-muted">Applies to bookings completed after this update is saved.</small>
                        </div>
                    </form>

                    <div class="border-top pt-3">
                        <div class="d-flex justify-content-between py-2 border-bottom">
                            <span style="font-size: 0.88rem; color: var(--admin-text-muted);">Mandatory deposit rate:</span>
                            <strong style="font-size: 0.88rem;">${depositRatePercent}% of contract value</strong>
                        </div>
                        <div class="d-flex justify-content-between py-2 border-bottom">
                            <span style="font-size: 0.88rem; color: var(--admin-text-muted);">Safe cancellation window:</span>
                            <strong style="font-size: 0.88rem;">${cancellationWindowHours} hours prior to shoot</strong>
                        </div>
                        <div class="d-flex justify-content-between py-2">
                            <span style="font-size: 0.88rem; color: var(--admin-text-muted);">Dispute handling:</span>
                            <strong style="font-size: 0.88rem;">Exclusive to Manager / Admin roles</strong>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Box 2: Payment Gateway & VietQR -->
            <div class="col-12 col-md-6">
                <div class="admin-card-container h-100">
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                        <span style="color: #10b981;">₫</span> Payment Gateway &amp; VietQR Integration
                    </h3>
                    <p style="font-size: 0.85rem; color: var(--admin-text-muted); margin-bottom: 20px;">
                        Status of the 30% escrow deposit engine and fee collection gateways.
                    </p>

                    <div class="py-2 border-bottom d-flex justify-content-between align-items-center">
                        <div>
                            <strong style="font-size: 0.88rem;">Payment Engine Mode:</strong>
                            <div style="font-size: 0.78rem; color: var(--admin-text-muted);">${paymentMode} Mode</div>
                        </div>
                        <span class="status-pill success">ACTIVE</span>
                    </div>

                    <div class="py-2 border-bottom d-flex justify-content-between align-items-center">
                        <div>
                            <strong style="font-size: 0.88rem;">VietQR Simulator (ZXing):</strong>
                            <div style="font-size: 0.78rem; color: var(--admin-text-muted);">Synthesizes demo QR codes for local demonstration</div>
                        </div>
                        <span class="status-pill ${paymentSimulationEnabled ? 'success' : 'secondary'}">
                            ${paymentSimulationEnabled ? 'ENABLED (DEMO)' : 'DISABLED'}
                        </span>
                    </div>

                    <div class="py-2 d-flex justify-content-between align-items-center">
                        <div>
                            <strong style="font-size: 0.88rem;">Webhook Signature Verification:</strong>
                            <div style="font-size: 0.78rem; color: var(--admin-text-muted);">Idempotent processing preventing duplicate captures</div>
                        </div>
                        <span class="status-pill success">PROTECTED</span>
                    </div>
                </div>
            </div>

            <!-- Box 3: External Integrations & Infrastructure -->
            <div class="col-12">
                <div class="admin-card-container">
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                        <span style="color: #f59e0b;">⚙</span> External Integrations &amp; Infrastructure
                    </h3>
                    <div class="row g-3">
                        <div class="col-12 col-md-4">
                            <div class="p-3 border rounded bg-light">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong style="font-size: 0.88rem;">Gmail SMTP Mailer</strong>
                                    <span class="status-pill ${mailCaptureMode ? 'warning' : 'success'}">
                                        ${mailCaptureMode ? 'CAPTURE' : 'LIVE'}
                                    </span>
                                </div>
                                <small class="text-muted">Sends OTP verification codes and shoot notifications.</small>
                            </div>
                        </div>
                        <div class="col-12 col-md-4">
                            <div class="p-3 border rounded bg-light">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong style="font-size: 0.88rem;">Cloudinary Image CDN</strong>
                                    <span class="status-pill ${cloudinaryConfigured ? 'success' : 'secondary'}">
                                        ${cloudinaryConfigured ? 'CONFIGURED' : 'NOT CONFIGURED'}
                                    </span>
                                </div>
                                <small class="text-muted">Cloud storage for portfolios and client album delivery.</small>
                            </div>
                        </div>
                        <div class="col-12 col-md-4">
                            <div class="p-3 border rounded bg-light">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong style="font-size: 0.88rem;">Google OAuth 2.0</strong>
                                    <span class="status-pill ${googleOAuthConfigured ? 'success' : 'secondary'}">
                                        ${googleOAuthConfigured ? 'READY' : 'NOT CONFIGURED'}
                                    </span>
                                </div>
                                <small class="text-muted">Single sign-on authentication via Google accounts.</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</main>
