<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management – Admin – PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .admin-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.95rem;
        }
        .admin-table th {
            text-align: left;
            padding: 1rem;
            border-bottom: 2px solid var(--border-dark);
            color: var(--text-muted);
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            font-size: 0.8rem;
        }
        .admin-table td {
            padding: 1.25rem 1rem;
            border-bottom: 1px solid var(--border-dark);
            vertical-align: middle;
        }
        .admin-table tbody tr:hover {
            background-color: var(--bg-dark-secondary);
        }
        .filter-panel {
            background-color: var(--bg-dark-secondary);
            border: 1px solid var(--border-dark);
            padding: 1.5rem;
            margin-bottom: 2rem;
            display: flex;
            flex-wrap: wrap;
            gap: 1rem;
            align-items: flex-end;
        }
        .form-group {
            display: flex;
            flex-direction: column;
            gap: 0.4rem;
        }
        .form-label {
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-muted);
        }
        .form-input, .form-select {
            background-color: var(--bg-dark);
            border: 1px solid var(--border-dark);
            color: var(--text-on-dark);
            padding: 0.6rem 0.8rem;
            font-size: 0.9rem;
            font-family: inherit;
        }
        .badge {
            display: inline-block;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.2rem 0.5rem;
            border-radius: 2px;
        }
        .badge-active { background-color: rgba(52, 211, 153, 0.15); color: #34d399; }
        .badge-inactive { background-color: rgba(245, 158, 11, 0.15); color: #f59e0b; }
        .badge-banned { background-color: rgba(239, 68, 68, 0.15); color: #ef4444; }
        .badge-role { background-color: rgba(255, 255, 255, 0.1); color: var(--text-on-dark); }
        .action-select {
            background-color: var(--bg-dark);
            border: 1px solid var(--border-dark);
            color: var(--text-on-dark);
            padding: 0.35rem 0.5rem;
            font-size: 0.85rem;
            cursor: pointer;
        }
        .action-btn {
            background-color: transparent;
            border: 1px solid var(--border-dark);
            color: var(--text-on-dark);
            padding: 0.35rem 0.75rem;
            font-size: 0.85rem;
            cursor: pointer;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            transition: all var(--transition-fast);
        }
        .action-btn:hover {
            background-color: var(--text-on-dark);
            color: var(--bg-dark);
        }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 4rem 0 6rem; min-height: 80vh;">
        <div class="editorial-container" style="max-width: 1200px; margin: 0 auto; padding: 0 1.5rem;">
            
            <jsp:include page="fragments/admin-navbar.jsp" />

            <div class="pc-stack-mobile" style="display: flex; justify-content: space-between; align-items: baseline; gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid var(--border-dark); padding-bottom: 1rem;">
                <div>
                    <span class="section-index">Administration</span>
                    <h1 class="editorial-title" style="font-size: 2.2rem; margin: 0.25rem 0 0;">User Management</h1>
                </div>
                <div style="color: var(--text-muted); font-size: 0.85rem;">
                    Total Results: ${fn:length(users)}
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div style="color: #34d399; margin-bottom: 2rem; padding: 1rem; border: 1px solid rgba(52, 211, 153, 0.3); background-color: rgba(52, 211, 153, 0.05);">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div style="color: #ef4444; margin-bottom: 2rem; padding: 1rem; border: 1px solid rgba(239, 68, 68, 0.3); background-color: rgba(239, 68, 68, 0.05);">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <!-- Search & Filter Controls -->
            <form action="${pageContext.request.contextPath}/admin/users" method="get" class="filter-panel">
                <div class="form-group" style="flex: 1; min-width: 220px;">
                    <label class="form-label" for="search">Search Name or Email</label>
                    <input type="text" id="search" name="search" class="form-input" placeholder="e.g. John Doe, user@example.com" value="${search}" />
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

                <button type="submit" class="action-btn" style="padding: 0.6rem 1.25rem;">Apply Filters</button>
                <c:if test="${not empty search || selectedRole != 'ALL' || selectedStatus != 'ALL'}">
                    <a href="${pageContext.request.contextPath}/admin/users" class="action-btn" style="text-align: center; line-height: 1.8; text-decoration: none;">Reset</a>
                </c:if>
            </form>

            <!-- Users Table -->
            <c:choose>
                <c:when test="${empty users}">
                    <div style="text-align: center; padding: 6rem 2rem; border: 1px solid var(--border-dark); background-color: var(--bg-dark-secondary);">
                        <h3 class="editorial-heading" style="font-size: 1.5rem; margin-bottom: 0.5rem;">No users found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 1.5rem;">No accounts match the specified search and filter criteria.</p>
                        <a href="${pageContext.request.contextPath}/admin/users" class="action-btn" style="text-decoration: none;">Clear Filters</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="overflow-x: auto; border: 1px solid var(--border-dark);">
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
                                                        <select name="status" class="action-select">
                                                            <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                                            <option value="INACTIVE" ${u.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                                            <option value="BANNED" ${u.status == 'BANNED' ? 'selected' : ''}>BANNED</option>
                                                        </select>
                                                        <button type="submit" class="action-btn" style="padding: 0.35rem 0.6rem; font-size: 0.75rem;">Update</button>
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
</body>
</html>
