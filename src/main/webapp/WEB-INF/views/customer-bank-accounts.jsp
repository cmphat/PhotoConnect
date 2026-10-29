<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Refund Bank Accounts — PhotoConnect</title>
</head>
<main class="pc-page">
    <div class="editorial-container pc-container-admin" style="max-width: 1040px; margin: 0 auto; padding: 2rem 1rem;">

        <!-- Navigation Hub for Customer Workspace -->
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-4 pb-3 border-bottom">
            <div>
                <a href="${pageContext.request.contextPath}/customer/dashboard" class="small text-muted">&larr; Back to Dashboard</a>
                <h1 class="editorial-title mb-0" style="font-size: 1.75rem;">Refund Bank Accounts</h1>
            </div>
            <div class="d-flex flex-wrap gap-2">
                <a href="${pageContext.request.contextPath}/customer/dashboard" class="btn btn-sm btn-outline-secondary">Dashboard</a>
                <a href="${pageContext.request.contextPath}/bookings" class="btn btn-sm btn-outline-secondary">Bookings</a>
                <a href="${pageContext.request.contextPath}/customer/saved-photographers" class="btn btn-sm btn-outline-secondary">Saved</a>
                <a href="${pageContext.request.contextPath}/customer/bank-accounts" class="btn btn-sm btn-primary">Bank Accounts</a>
                <a href="${pageContext.request.contextPath}/profile/me" class="btn btn-sm btn-outline-secondary">Profile</a>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <div class="row g-4">
            <!-- Left Column: List of Saved Bank Accounts -->
            <div class="col-12 col-lg-7">
                <div class="pc-card p-4 h-100" style="background: var(--surface); border-radius: 8px; border: 1px solid var(--border);">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h2 class="h5 fw-bold mb-0">Saved Accounts</h2>
                        <span class="badge bg-secondary">${accounts.size()} accounts</span>
                    </div>

                    <c:choose>
                        <c:when test="${empty accounts}">
                            <div class="text-center py-5 border rounded" style="background: var(--surface-muted); border-style: dashed !important;">
                                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🏦</div>
                                <h3 class="h6 fw-bold">No Bank Accounts Saved</h3>
                                <p class="text-muted small mb-0 px-3">Add a bank account to receive deposit refunds seamlessly when eligible bookings are cancelled.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="d-flex flex-column gap-3">
                                <c:forEach var="acc" items="${accounts}">
                                    <div class="p-3 border rounded ${acc.default ? 'border-primary' : ''}" style="background: ${acc.default ? 'rgba(37, 99, 235, 0.03)' : 'var(--surface)'};">
                                        <div class="d-flex justify-content-between align-items-start mb-2">
                                            <div>
                                                <h3 class="h6 fw-bold mb-1 text-primary"><c:out value="${acc.bankName}"/></h3>
                                                <div class="small text-muted"><c:out value="${acc.branchName != null ? acc.branchName : 'Main Branch / Default'}"/></div>
                                            </div>
                                            <c:if test="${acc.default}">
                                                <span class="badge bg-primary">DEFAULT REFUND ACCOUNT</span>
                                            </c:if>
                                        </div>

                                        <div class="p-2 rounded mb-3" style="background: var(--surface-muted); font-family: monospace; font-size: 1.1rem; letter-spacing: 0.08em;">
                                            <span class="text-muted small d-block" style="font-family: var(--font-primary); font-size: 0.75rem; letter-spacing: normal;">MASKED ACCOUNT NUMBER:</span>
                                            <strong><c:out value="${acc.maskedAccountNumber}"/></strong>
                                        </div>

                                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                                            <div class="small">
                                                <span class="text-muted">Account Holder:</span> <strong><c:out value="${acc.accountHolderName}"/></strong>
                                            </div>
                                            <div class="d-flex gap-2">
                                                <c:if test="${!acc.default}">
                                                    <form method="post" action="${pageContext.request.contextPath}/customer/bank-accounts/${acc.id}/default" style="margin: 0;">
                                                        <%@ include file="fragments/csrf-input.jsp" %>
                                                        <button type="submit" class="btn btn-sm btn-outline-primary">
                                                            Set as Default
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <form method="post" action="${pageContext.request.contextPath}/customer/bank-accounts/${acc.id}/delete" style="margin: 0;">
                                                    <%@ include file="fragments/csrf-input.jsp" %>
                                                    <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Confirm deletion of this bank account?')">
                                                        Delete
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="mt-4 pt-3 border-top small text-muted">
                        <p class="mb-1"><strong>🔒 Financial Data Security:</strong></p>
                        <p class="mb-0">PhotoConnect only retains account numbers and account holder names for refund settlement purposes. The platform <strong>never</strong> asks for or stores banking credentials, PIN codes, OTPs, or CVV numbers.</p>
                    </div>
                </div>
            </div>

            <!-- Right Column: Add Bank Account Form -->
            <div class="col-12 col-lg-5">
                <div class="pc-card p-4 h-100" style="background: var(--surface); border-radius: 8px; border: 1px solid var(--border);">
                    <h2 class="h5 fw-bold mb-3">Add Bank Account</h2>

                    <form method="post" action="${pageContext.request.contextPath}/customer/bank-accounts">
                        <%@ include file="fragments/csrf-input.jsp" %>

                        <div class="mb-3">
                            <label for="bankName" class="form-label small fw-semibold">Beneficiary Bank <span class="text-danger">*</span></label>
                            <input type="text" id="bankName" name="bankName" class="form-control" list="bankList" placeholder="e.g. Vietcombank, Techcombank, MB Bank..." required maxlength="100" value="${bankForm.bankName}">
                            <datalist id="bankList">
                                <option value="Vietcombank (Joint Stock Commercial Bank for Foreign Trade of Vietnam)">
                                <option value="Techcombank (Vietnam Technological and Commercial Joint Stock Bank)">
                                <option value="MB Bank (Military Commercial Joint Stock Bank)">
                                <option value="VietinBank (Vietnam Joint Stock Commercial Bank for Industry and Trade)">
                                <option value="BIDV (Bank for Investment and Development of Vietnam)">
                                <option value="ACB (Asia Commercial Joint Stock Bank)">
                                <option value="VPBank (Vietnam Prosperity Joint Stock Commercial Bank)">
                                <option value="TPBank (Tien Phong Commercial Joint Stock Bank)">
                                <option value="Sacombank (Saigon Thuong Tin Commercial Joint Stock Bank)">
                                <option value="VIB (Vietnam International Commercial Joint Stock Bank)">
                            </datalist>
                        </div>

                        <div class="mb-3">
                            <label for="accountNumber" class="form-label small fw-semibold">Account Number <span class="text-danger">*</span></label>
                            <input type="text" id="accountNumber" name="accountNumber" class="form-control" placeholder="Enter bank account number..." required minlength="6" maxlength="50" value="${bankForm.accountNumber}">
                            <div class="form-text" style="font-size: 0.78rem;">Account number will be masked for security once saved.</div>
                        </div>

                        <div class="mb-3">
                            <label for="accountHolderName" class="form-label small fw-semibold">Account Holder Name (Uppercase) <span class="text-danger">*</span></label>
                            <input type="text" id="accountHolderName" name="accountHolderName" class="form-control" placeholder="JOHN DOE" required maxlength="150" value="${bankForm.accountHolderName}">
                        </div>

                        <div class="mb-3">
                            <label for="branchName" class="form-label small fw-semibold">Branch (Optional)</label>
                            <input type="text" id="branchName" name="branchName" class="form-control" placeholder="e.g. Main Branch, Ho Chi Minh City" maxlength="150" value="${bankForm.branchName}">
                        </div>

                        <div class="mb-3 form-check">
                            <input type="checkbox" class="form-check-input" id="makeDefault" name="makeDefault" value="true" ${accounts.empty ? 'checked' : ''}>
                            <label class="form-check-label small" for="makeDefault">Set as default account for deposit refunds</label>
                        </div>

                        <button type="submit" class="btn btn-primary w-100">
                            Save Bank Account
                        </button>
                    </form>

                    <div class="alert alert-info p-2 mt-4 small mb-0" role="status">
                        <strong>Policy Terms:</strong> Refund transactions strictly adhere to PhotoConnect's 48-hour cancellation policy.
                    </div>
                </div>
            </div>
        </div>

    </div>
</main>
