<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head><title>Finish Google registration — PhotoConnect</title></head>
<main class="container py-5" style="max-width: 720px;">
    <span class="section-index">Verified Google identity</span>
    <h1 class="editorial-title">Choose how you’ll use PhotoConnect</h1>
    <p class="pc-lead"><c:out value="${externalName}"/> · <c:out value="${externalEmail}"/></p>

    <c:if test="${not empty onboardingError}">
        <div class="alert alert-danger" role="alert"><c:out value="${onboardingError}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/google/onboarding" method="post" class="card p-4 shadow-sm">
        <%@ include file="fragments/csrf-input.jsp" %>
        <fieldset>
            <legend class="h5 mb-3">Account type</legend>
            <div class="form-check mb-3">
                <input class="form-check-input" type="radio" name="role" id="roleCustomer" value="CUSTOMER" checked>
                <label class="form-check-label" for="roleCustomer"><strong>Customer</strong><br><span class="text-muted">Discover and book photographers.</span></label>
            </div>
            <div class="form-check mb-4">
                <input class="form-check-input" type="radio" name="role" id="rolePhotographer" value="PHOTOGRAPHER">
                <label class="form-check-label" for="rolePhotographer"><strong>Photographer</strong><br><span class="text-muted">Continue to the existing creator application. Approval is still required.</span></label>
            </div>
        </fieldset>
        <button class="btn btn-primary" type="submit">Create PhotoConnect account</button>
    </form>
</main>
