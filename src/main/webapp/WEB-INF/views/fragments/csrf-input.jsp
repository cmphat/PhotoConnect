<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty _csrf}">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
</c:if>
