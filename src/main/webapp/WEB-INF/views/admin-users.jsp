<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>User Management – Admin – PhotoConnect</title>
</head>

    <main class="pc-page pc-page-compact">
        <div class="editorial-container pc-container-admin">

            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-page-header pc-stack-mobile">
                <div>
                    <span class="section-index">Administration</span>
                    <h1 class="editorial-title">User Management</h1>
                </div>
                <div class="pc-page-meta">
                    Total Results: ${fn:length(users)}
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="pc-alert pc-alert-success" role="status">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="pc-alert pc-alert-danger" role="alert">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- Search & Filter Controls -->
            <form action="${pageContext.request.contextPath}/admin/users" method="get" class="admin-filter-panel">
                <div class="form-group" style="flex: 1; min-width: 220px;">
                    <label class="form-label" for="search">Search Name or Email</label>
                    <input type="text" id="search" name="search" class="form-input" placeholder="e.g. John Doe, user@example.com" value="<c:out value='${search}'/>" />
                </div>

                <div class="form-group" style="min-width: 150px;">
                    <label class="form-label" for="role">Role</label>
                    <select id="role" name="role" class="form-select">
                        <option value="ALL" ${selectedRole == 'ALL' ? 'selected' : ''}>All Roles</option>
                        <c:forEach var="r" items="${roles}">
                            <option value="${r.name()}" ${selectedRole == r.name() ? 'selected' : ''}>${r.name()}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group" style="min-width: 150px;">
                    <label class="form-label" for="status">Status</label>
                    <select id="status" name="status" class="form-select">
                        <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                        <c:forEach var="s" items="${statuses}">
                            <option value="${s.name()}" ${selectedStatus == s.name() ? 'selected' : ''}>${s.name()}</option>
                        </c:forEach>
                    </select>
                </div>

                <button type="submit" class="btn btn-primary btn-sm">Apply Filters</button>
                <c:if test="${not empty search || selectedRole != 'ALL' || selectedStatus != 'ALL'}">
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary btn-sm">Reset</a>
                </c:if>
            </form>

            <!-- Users Table -->
            <c:choose>
                <c:when test="${empty users}">
                    <div class="pc-empty-state">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No users found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 1.5rem;">No accounts match the specified search and filter criteria.</p>
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary btn-sm">Clear Filters</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-table-shell" tabindex="0" aria-label="Users table">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Full Name</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Status</th>
                                    <th>Registered</th>
                                    <th>Change Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="u" items="${users}">
                                    <tr>
                                        <td style="color: var(--text-muted);">#<c:out value="${u.id}"/></td>
                                        <td style="font-weight: 500;">
                                            <c:out value="${u.fullName}"/>
                                            <c:if test="${u.id == currentAdminId}">
                                                <span style="font-size: 0.75rem; color: #60a5fa; margin-left: 0.5rem;">(You)</span>
                                            </c:if>
                                        </td>
                                        <td><c:out value="${u.email}"/></td>
                                        <td>
                                            <span class="badge badge-role"><c:out value="${u.role}"/></span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${u.status == 'ACTIVE'}">
                                                    <span class="badge badge-active">ACTIVE</span>
                                                </c:when>
                                                <c:when test="${u.status == 'INACTIVE'}">
                                                    <span class="badge badge-inactive">INACTIVE</span>
                                                </c:when>
                                                <c:when test="${u.status == 'BANNED'}">
                                                    <span class="badge badge-banned">BANNED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-role"><c:out value="${u.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="color: var(--text-muted); font-size: 0.85rem;">
                                            <c:if test="${not empty u.createdAt}">
                                                <fmt:parseDate value="${fn:substring(u.createdAt, 0, 10)}" pattern="yyyy-MM-dd" var="parsedUserDate" type="date" />
                                                <fmt:formatDate value="${parsedUserDate}" pattern="MMM d, yyyy" />
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${u.id == currentAdminId}">
                                                    <span style="color: var(--text-muted); font-size: 0.8rem; font-style: italic;">Protected (Self)</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/admin/users/${u.id}/status" method="post" style="display: flex; gap: 0.5rem; align-items: center; margin: 0;">
                                                        <%@ include file="fragments/csrf-input.jsp" %>
                                                        <select name="status" class="pc-select" aria-label="Account status for user ${u.id}">
                                                            <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                                            <option value="INACTIVE" ${u.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                                            <option value="BANNED" ${u.status == 'BANNED' ? 'selected' : ''}>BANNED</option>
                                                        </select>
                                                        <button type="submit" class="btn btn-secondary btn-sm">Update</button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>
