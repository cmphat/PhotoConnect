<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Booking #${booking.id} — Admin Audit & Dispute — PhotoConnect</title>
</head>

<main class="pc-page pc-page-compact">
    <div class="editorial-container pc-container-admin">
        <div class="mb-3">
            <a href="${pageContext.request.contextPath}/admin/bookings" class="text-decoration-none" style="color: var(--text-muted); font-size: 0.9rem;">
                &larr; Back to Booking Monitoring
            </a>
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

        <!-- Booking Overview Header -->
        <div class="pc-page-header pc-stack-mobile">
            <div>
                <span class="section-index">Administrative Audit &amp; Moderation</span>
                <h1 class="editorial-title" style="margin-bottom: 0.25rem;">Booking #${booking.id}</h1>
                <p style="color: var(--text-muted); margin: 0;">
                    Client: <strong><c:out value="${booking.customer.fullName}"/></strong> (<c:out value="${booking.customer.email}"/>)
                    &middot; Photographer: <strong><c:out value="${booking.photographerProfile.displayName}"/></strong> (<c:out value="${booking.photographerProfile.user.email}"/>)
                </p>
            </div>
            <div>
                <c:choose>
                    <c:when test="${booking.status == 'DISPUTED'}">
                        <span class="badge" style="background-color: #ef4444; color: #fff; font-size: 0.9rem; padding: 0.4rem 0.8rem;">DISPUTED</span>
                    </c:when>
                    <c:when test="${booking.status == 'COMPLETED'}">
                        <span class="badge badge-completed" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;">COMPLETED</span>
                    </c:when>
                    <c:when test="${booking.status == 'CANCELLED'}">
                        <span class="badge badge-cancelled" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;">CANCELLED</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;"><c:out value="${booking.status}"/></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Details Grid -->
        <div class="row g-4 mb-4">
            <div class="col-12 col-md-7">
                <div class="pc-panel h-100">
                    <h3 style="font-size: 1.15rem; margin-bottom: 1rem;">Session Contract Details</h3>
                    <table class="detail-table w-100">
                        <tbody>
                            <tr>
                                <th style="width: 35%; color: var(--text-muted); padding: 0.5rem 0;">Shoot Date</th>
                                <td style="padding: 0.5rem 0; font-weight: 500;">
                                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedShootDate" type="date" />
                                    <fmt:formatDate value="${parsedShootDate}" pattern="EEEE, MMMM d, yyyy" />
                                    at <c:out value="${booking.bookingTime}"/>
                                </td>
                            </tr>
                            <tr>
                                <th style="color: var(--text-muted); padding: 0.5rem 0;">Location</th>
                                <td style="padding: 0.5rem 0;"><c:out value="${booking.location}"/></td>
                            </tr>
                            <tr>
                                <th style="color: var(--text-muted); padding: 0.5rem 0;">Agreed Price</th>
                                <td style="padding: 0.5rem 0; font-weight: 600; color: var(--text);">
                                    <fmt:formatNumber value="${booking.agreedPrice}" pattern="#,##0" /> VND
                                </td>
                            </tr>
                            <tr>
                                <th style="color: var(--text-muted); padding: 0.5rem 0;">Service Package</th>
                                <td style="padding: 0.5rem 0;">
                                    <c:choose>
                                        <c:when test="${not empty booking.packageName}">
                                            <strong><c:out value="${booking.packageName}"/></strong>
                                            <div style="font-size: 0.82rem; color: var(--text-muted);">
                                                Duration: ${booking.durationMinutes} mins &middot; Photos: ${booking.editedPhotoCount} &middot; Delivery: ${booking.deliveryDays} days
                                            </div>
                                        </c:when>
                                        <c:otherwise>Custom Session</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <c:if test="${not empty booking.cancellationReason}">
                                <tr>
                                    <th style="color: #ef4444; padding: 0.5rem 0;">Cancellation Note</th>
                                    <td style="padding: 0.5rem 0; color: #ef4444;">
                                        <c:out value="${booking.cancellationReason}"/>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="col-12 col-md-5">
                <!-- Dispute Resolution & Administrative Action Box -->
                <div class="pc-panel h-100" style="border-left: 4px solid <c:choose><c:when test='${booking.status == \"DISPUTED\"}'>#ef4444</c:when><c:otherwise>var(--accent)</c:otherwise></c:choose>;">
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">Administrative Dispute Resolution</h3>
                    <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1.25rem;">
                        As an administrator, review evidence, cancel with full refund to customer, or confirm session fulfillment in favor of the creator.
                    </p>

                    <c:choose>
                        <c:when test="${booking.status == 'DISPUTED'}">
                            <form action="${pageContext.request.contextPath}/admin/bookings/${booking.id}/resolve-dispute" method="post">
                                <%@ include file="fragments/csrf-input.jsp" %>
                                <div class="mb-3">
                                    <label class="form-label" style="font-size: 0.82rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted);">Decision / Action</label>
                                    <div class="form-check mb-2">
                                        <input class="form-check-input" type="radio" name="resolution" id="resRefund" value="REFUND_CUSTOMER" checked>
                                        <label class="form-check-label" for="resRefund">
                                            <strong>Refund Customer (Force Cancel)</strong>
                                            <div style="font-size: 0.8rem; color: var(--text-muted);">Cancels booking, refunds deposit 100%, and notifies both parties.</div>
                                        </label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="radio" name="resolution" id="resPay" value="PAY_PHOTOGRAPHER">
                                        <label class="form-check-label" for="resPay">
                                            <strong>Pay Photographer (Confirm Completed)</strong>
                                            <div style="font-size: 0.8rem; color: var(--text-muted);">Marks booking completed and settles deposit to photographer.</div>
                                        </label>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="adminNotes" class="form-label" style="font-size: 0.82rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted);">Audit Reason / Decision Notes</label>
                                    <textarea id="adminNotes" name="adminNotes" class="form-control" rows="3" placeholder="Provide detailed audit rationale for this decision..." required></textarea>
                                </div>

                                <button type="submit" class="btn btn-primary w-100" onclick="return confirm('Confirm administrative dispute resolution? This action will update booking status, escrow funds, and notify both parties.')">
                                    Execute Dispute Decision
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-secondary" style="background: rgba(255, 255, 255, 0.04); border: 1px solid var(--border); color: var(--text-muted); font-size: 0.85rem; padding: 0.85rem; border-radius: 6px;">
                                <i class="bi bi-info-circle me-1"></i>
                                Dispute resolution is disabled. This booking is in <strong><c:out value="${booking.status}"/></strong> status. Only bookings in <strong>DISPUTED</strong> status can be resolved.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Moderation Audit Trail -->
        <div class="pc-panel">
            <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Lifecycle &amp; Moderation Audit Trail</h3>
            <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 1.25rem;">
                Every state machine transition, automated deposit event, and administrative action is permanently recorded.
            </p>

            <c:choose>
                <c:when test="${not empty auditLogs}">
                    <div class="admin-table-shell" tabindex="0" aria-label="Audit log table">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Timestamp</th>
                                    <th>Action Performed By</th>
                                    <th>Transition</th>
                                    <th>Reason / Notes</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="log" items="${auditLogs}">
                                    <tr>
                                        <td style="white-space: nowrap; font-size: 0.85rem; color: var(--text-muted);">
                                            <c:out value="${log.changedAt()}"/>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500;"><c:out value="${log.changedByName()}"/></div>
                                            <div style="font-size: 0.75rem; color: var(--text-muted);"><c:out value="${log.changedByRole()}"/></div>
                                        </td>
                                        <td>
                                            <span class="badge" style="font-size: 0.75rem;"><c:out value="${log.fromStatus()}"/></span>
                                            &rarr;
                                            <span class="badge" style="font-size: 0.75rem;"><c:out value="${log.toStatus()}"/></span>
                                        </td>
                                        <td style="color: var(--text); font-size: 0.88rem;">
                                            <c:out value="${log.reason()}"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="p-4 text-center text-muted" style="background: var(--surface-subtle); border-radius: var(--radius-sm);">
                        No history records recorded yet for this booking.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>
