<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty successMessage}"><div class="pc-alert pc-alert-success" role="status"><c:out value="${successMessage}"/></div></c:if>
<c:if test="${not empty errorMessage}"><div class="pc-alert pc-alert-danger" role="alert"><c:out value="${errorMessage}"/></div></c:if>
