<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Schedule - PhotoConnect</title>
    <!-- Use the shared custom CSS (no Bootstrap) -->
    <link rel="stylesheet" href="<c:url value='/assets/css/photoconnect.css'/>">
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;1,400&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
</head>
<body class="editorial-body">

    <!-- Include Standard Navbar -->
    <jsp:include page="fragments/navbar.jsp" />

    <main class="editorial-container" style="max-width: 900px; margin-top: 4rem;">
        
        <header style="margin-bottom: 3rem;">
            <h1 class="editorial-title">Availability & Schedule</h1>
            <p class="editorial-subtitle">Block out specific dates when you are unavailable for bookings.</p>
        </header>

        <c:if test="${not empty successMessage}">
            <div class="pc-alert pc-alert-success" style="margin-bottom: 2rem;">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="pc-alert pc-alert-danger" style="margin-bottom: 2rem;">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <section style="display: grid; grid-template-columns: 1fr 2fr; gap: 3rem;">
            
            <!-- Add Unavailable Date Form -->
            <div class="pc-surface" style="padding: 2rem;">
                <h2 style="font-family: 'Playfair Display', serif; font-size: 1.5rem; margin-bottom: 1.5rem; color: var(--text-light);">Add Unavailable Date</h2>
                <form action="<c:url value='/photographer/schedule/add'/>" method="post" style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <div class="form-group">
                        <label class="pc-label" for="date">Select Date</label>
                        <input type="date" id="date" name="date" class="pc-input" required min="${minDate}" style="width: 100%;">
                    </div>
                    
                    <div class="form-group">
                        <label class="pc-label" for="reason">Reason (Optional)</label>
                        <input type="text" id="reason" name="reason" class="pc-input" placeholder="e.g. Vacation, Fully Booked" maxlength="255" style="width: 100%;">
                    </div>
                    
                    <button type="submit" class="pc-btn pc-btn-primary" style="width: 100%;">Block Date</button>
                </form>
            </div>

            <!-- List of Unavailable Dates -->
            <div>
                <h2 style="font-family: 'Playfair Display', serif; font-size: 1.5rem; margin-bottom: 1.5rem; color: var(--text-light);">Blocked Dates</h2>
                
                <c:choose>
                    <c:when test="${empty unavailableDates}">
                        <div class="pc-empty-state" style="padding: 3rem; text-align: center; border: 1px dashed var(--border-dark);">
                            <p style="color: var(--text-muted);">You have no blocked dates.</p>
                            <p style="font-size: 0.9rem; color: var(--text-muted); margin-top: 0.5rem;">Your calendar is currently open for all future dates.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="pc-scroll-region">
                        <table class="pc-table" style="width: 100%; min-width: 560px;">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Reason</th>
                                    <th style="text-align: right;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dateObj" items="${unavailableDates}">
                                    <tr>
                                        <td>
                                            <fmt:parseDate value="${dateObj.date}" pattern="yyyy-MM-dd" var="parsedDate" type="date" />
                                            <span style="font-weight: 500; color: var(--text-light);">
                                                <fmt:formatDate value="${parsedDate}" pattern="MMM d, yyyy" />
                                            </span>
                                        </td>
                                        <td style="color: var(--text-muted);">
                                            <c:out value="${empty dateObj.reason ? '-' : dateObj.reason}" />
                                        </td>
                                        <td style="text-align: right;">
                                            <form action="<c:url value='/photographer/schedule/remove/${dateObj.id}'/>" method="post" style="display: inline;">
                                                <button type="submit" class="pc-btn pc-btn-outline" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="return confirm('Are you sure you want to unblock this date?');">
                                                    Remove
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            
        </section>

    </main>

</body>
</html>
