<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<head>
    <title>Service Packages — PhotoConnect Studio</title>
    <meta name="description" content="Manage your photography service packages, rates, and deliverables on PhotoConnect Studio.">
    <style>
        .package-mgmt-wrap {
            max-width: var(--pc-container, 1280px);
            margin: 2.5rem auto 4rem;
            padding: 0 1.5rem;
        }
        .package-mgmt-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
            flex-wrap: wrap;
            gap: 1rem;
        }
        .package-mgmt-title {
            font-family: var(--pc-font-editorial, 'Playfair Display', Georgia, serif);
            font-size: 2.2rem;
            font-weight: 700;
            color: var(--pc-navy, #0F172A);
            margin: 0 0 0.4rem 0;
        }
        .package-mgmt-sub {
            color: var(--pc-text-secondary, #64748B);
            margin: 0;
            font-size: 0.95rem;
        }
        .package-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
            gap: 1.5rem;
        }
        .package-card {
            background: var(--pc-surface, #FFFFFF);
            border: 1px solid var(--pc-border, #CBD5E1);
            border-radius: var(--pc-radius-md, 10px);
            padding: 1.75rem;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            position: relative;
        }
        .package-card:hover {
            transform: translateY(-2px);
            box-shadow: var(--pc-shadow-card-hover, 0 8px 24px rgba(15, 23, 42, 0.08));
        }
        .package-card.is-inactive {
            opacity: 0.65;
            border-style: dashed;
        }
        .package-badge-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }
        .category-badge {
            font-size: 0.78rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.25rem 0.65rem;
            border-radius: var(--pc-radius-sm, 6px);
            background: rgba(37, 99, 235, 0.08);
            color: var(--pc-primary, #2563EB);
        }
        .status-pill {
            font-size: 0.75rem;
            font-weight: 600;
            padding: 0.2rem 0.5rem;
            border-radius: 4px;
        }
        .status-pill.active {
            background: rgba(21, 128, 61, 0.1);
            color: #15803D;
        }
        .status-pill.inactive {
            background: rgba(100, 116, 139, 0.1);
            color: #64748B;
        }
        .package-name {
            font-size: 1.3rem;
            font-weight: 700;
            color: var(--pc-navy, #0F172A);
            margin: 0 0 0.5rem 0;
        }
        .package-price {
            font-size: 1.6rem;
            font-weight: 700;
            color: var(--pc-primary, #2563EB);
            margin: 0 0 1rem 0;
        }
        .package-deliverables {
            background: var(--pc-bg, #F8FAFC);
            border-radius: var(--pc-radius-sm, 6px);
            padding: 0.75rem 1rem;
            margin-bottom: 1rem;
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 0.5rem;
            text-align: center;
        }
        .deliverable-item {
            display: flex;
            flex-direction: column;
            gap: 0.2rem;
        }
        .deliverable-label {
            font-size: 0.72rem;
            text-transform: uppercase;
            color: var(--pc-text-secondary, #64748B);
            font-weight: 600;
        }
        .deliverable-val {
            font-size: 0.88rem;
            font-weight: 700;
            color: var(--pc-navy, #0F172A);
        }
        .package-desc {
            font-size: 0.88rem;
            color: var(--pc-text-secondary, #64748B);
            line-height: 1.5;
            margin-bottom: 1.5rem;
            min-height: 40px;
        }
        .package-actions {
            display: flex;
            gap: 0.5rem;
            align-items: center;
            border-top: 1px solid var(--pc-border, #CBD5E1);
            padding-top: 1rem;
        }
        .action-form {
            display: inline;
            margin: 0;
        }
        .pc-modal-backdrop {
            display: none;
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(4px);
            z-index: 1050;
            align-items: center;
            justify-content: center;
            padding: 1rem;
        }
        .pc-modal-backdrop.is-open {
            display: flex;
        }
        .pc-modal-dialog {
            background: var(--pc-surface, #FFFFFF);
            border: 1px solid var(--pc-border, #CBD5E1);
            border-radius: var(--pc-radius-md, 10px);
            width: 100%;
            max-width: 580px;
            max-height: 90vh;
            overflow-y: auto;
            padding: 2rem;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
        }
        .modal-field {
            margin-bottom: 1.25rem;
        }
        .modal-field label {
            display: block;
            font-size: 0.85rem;
            font-weight: 600;
            color: var(--pc-navy, #0F172A);
            margin-bottom: 0.4rem;
        }
        .modal-field input, .modal-field select, .modal-field textarea {
            width: 100%;
            padding: 0.65rem 0.85rem;
            border: 1px solid var(--pc-border, #CBD5E1);
            border-radius: var(--pc-radius-sm, 6px);
            font-family: inherit;
            font-size: 0.92rem;
            background: var(--pc-surface, #FFFFFF);
            color: var(--pc-navy, #0F172A);
        }
        .modal-field textarea {
            resize: vertical;
            min-height: 80px;
        }
        .modal-grid-2 {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }
        .modal-grid-3 {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            gap: 0.75rem;
        }
        .empty-package-state {
            text-align: center;
            padding: 4rem 2rem;
            background: var(--pc-surface, #FFFFFF);
            border: 1px dashed var(--pc-border, #CBD5E1);
            border-radius: var(--pc-radius-md, 10px);
            grid-column: 1 / -1;
        }
    </style>
</head>
<div class="package-mgmt-wrap">
    <div class="package-mgmt-header">
        <div>
            <a href="${pageContext.request.contextPath}/photographer/dashboard" class="text-decoration-none text-muted mb-2 d-inline-block">← Back to Studio Dashboard</a>
            <h1 class="package-mgmt-title">Service Packages</h1>
            <p class="package-mgmt-sub">Define structured photography offerings, turnaround times, and clear deliverables for clients.</p>
            <div class="mt-2">
                <span class="badge ${activeCount >= 10 ? 'bg-danger' : 'bg-primary'} text-white px-3 py-2" style="font-size: 0.82rem; font-weight: 600; border-radius: 6px;">
                    Đang kích hoạt: <c:out value="${not empty activeCount ? activeCount : 0}"/> / tối đa 10 gói
                </span>
            </div>
        </div>
        <button type="button" class="pc-btn-primary" id="openCreateModalBtn">
            + Create New Package
        </button>
    </div>

    <!-- Flash Messages -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
            <span>✓</span> <div><c:out value="${successMessage}"/></div>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
            <span>⚠</span> <div><c:out value="${errorMessage}"/></div>
        </div>
    </c:if>

    <!-- Packages Grid -->
    <div class="package-grid">
        <c:choose>
            <c:when test="${not empty packages}">
                <c:forEach var="pkg" items="${packages}">
                    <div class="package-card ${pkg.active ? '' : 'is-inactive'}">
                        <div>
                            <div class="package-badge-row">
                                <span class="category-badge"><c:out value="${pkg.categoryDisplayName}"/></span>
                                <span class="status-pill ${pkg.active ? 'active' : 'inactive'}">
                                    <c:out value="${pkg.active ? 'Active' : 'Inactive'}"/>
                                </span>
                            </div>
                            <h2 class="package-name"><c:out value="${pkg.name}"/></h2>
                            <div class="package-price"><c:out value="${pkg.formattedPrice}"/></div>
                            
                            <div class="package-deliverables">
                                <div class="deliverable-item">
                                    <span class="deliverable-label">Duration</span>
                                    <span class="deliverable-val"><c:out value="${pkg.formattedDuration}"/></span>
                                </div>
                                <div class="deliverable-item">
                                    <span class="deliverable-label">Photos</span>
                                    <span class="deliverable-val"><c:out value="${pkg.editedPhotoCount}"/> edited</span>
                                </div>
                                <div class="deliverable-item">
                                    <span class="deliverable-label">Delivery</span>
                                    <span class="deliverable-val"><c:out value="${pkg.deliveryDays}"/> days</span>
                                </div>
                            </div>

                            <p class="package-desc">
                                <c:choose>
                                    <c:when test="${not empty pkg.description}"><c:out value="${pkg.description}"/></c:when>
                                    <c:otherwise><span class="text-muted fst-italic">No custom description provided.</span></c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div class="package-actions">
                            <button type="button" class="btn btn-sm btn-outline-secondary edit-package-btn"
                                    data-id="<c:out value='${pkg.id}'/>"
                                    data-name="<c:out value='${pkg.name}'/>"
                                    data-category="<c:out value='${pkg.category}'/>"
                                    data-price="<c:out value='${pkg.price}'/>"
                                    data-duration="<c:out value='${pkg.durationMinutes}'/>"
                                    data-photos="<c:out value='${pkg.editedPhotoCount}'/>"
                                    data-delivery="<c:out value='${pkg.deliveryDays}'/>"
                                    data-desc="<c:out value='${pkg.description}'/>">
                                Edit
                            </button>

                            <form action="${pageContext.request.contextPath}/photographer/packages/${pkg.id}/toggle" method="post" class="action-form">
                                <jsp:include page="fragments/csrf-input.jsp" />
                                <input type="hidden" name="active" value="${!pkg.active}">
                                <button type="submit" class="btn btn-sm ${pkg.active ? 'btn-outline-warning' : 'btn-outline-success'}">
                                    <c:out value="${pkg.active ? 'Deactivate' : 'Activate'}"/>
                                </button>
                            </form>

                            <form action="${pageContext.request.contextPath}/photographer/packages/${pkg.id}/delete" method="post" class="action-form" onsubmit="return confirm('Are you sure you want to delete this package?');">
                                <jsp:include page="fragments/csrf-input.jsp" />
                                <button type="submit" class="btn btn-sm btn-outline-danger">
                                    Delete
                                </button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-package-state">
                    <h3 class="mb-2">No Service Packages Yet</h3>
                    <p class="text-muted mb-4">Create your first photography package to allow customers to compare deliverables and book directly.</p>
                    <button type="button" class="pc-btn-primary" onclick="openPackageModal()">
                        Create First Package
                    </button>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Create / Edit Package Modal -->
<div class="pc-modal-backdrop" id="packageModal">
    <div class="pc-modal-dialog">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="m-0" id="modalTitle">Create Service Package</h3>
            <button type="button" class="btn-close" id="closeModalBtn" aria-label="Close"></button>
        </div>

        <form id="packageForm" method="post" action="${pageContext.request.contextPath}/photographer/packages">
            <jsp:include page="fragments/csrf-input.jsp" />

            <div class="modal-field">
                <label for="pkgName">Package Name *</label>
                <input type="text" id="pkgName" name="name" required maxlength="150" placeholder="e.g. Standard Portrait Session">
            </div>

            <div class="modal-grid-2">
                <div class="modal-field">
                    <label for="pkgCategory">Photography Category *</label>
                    <select id="pkgCategory" name="category" required>
                        <c:forEach var="cat" items="${categories}">
                            <option value="<c:out value='${cat.name()}'/>"><c:out value="${cat.displayName}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="modal-field">
                    <label for="pkgPrice">Price (VND) *</label>
                    <input type="number" id="pkgPrice" name="price" required min="1000" step="1000" placeholder="e.g. 2500000">
                </div>
            </div>

            <div class="modal-grid-3">
                <div class="modal-field">
                    <label for="pkgDuration">Duration (Mins) *</label>
                    <input type="number" id="pkgDuration" name="durationMinutes" required min="15" max="1440" step="15" value="120">
                </div>
                <div class="modal-field">
                    <label for="pkgPhotos">Edited Photos *</label>
                    <input type="number" id="pkgPhotos" name="editedPhotoCount" required min="0" max="5000" value="25">
                </div>
                <div class="modal-field">
                    <label for="pkgDelivery">Delivery (Days) *</label>
                    <input type="number" id="pkgDelivery" name="deliveryDays" required min="1" max="90" value="7">
                </div>
            </div>

            <div class="modal-field">
                <label for="pkgDesc">Description & Terms</label>
                <textarea id="pkgDesc" name="description" maxlength="1000" placeholder="Describe the shoot location scope, outfits, equipment, or any specific details included..."></textarea>
            </div>

            <div class="d-flex justify-content-end gap-2 mt-4">
                <button type="button" class="btn btn-outline-secondary" id="cancelModalBtn">Cancel</button>
                <button type="submit" class="pc-btn-primary" id="savePkgBtn">Save Package</button>
            </div>
        </form>
    </div>
</div>

<script>
    (function() {
        const modal = document.getElementById('packageModal');
        const form = document.getElementById('packageForm');
        const modalTitle = document.getElementById('modalTitle');
        const openBtn = document.getElementById('openCreateModalBtn');
        const closeBtn = document.getElementById('closeModalBtn');
        const cancelBtn = document.getElementById('cancelModalBtn');

        const nameInput = document.getElementById('pkgName');
        const catSelect = document.getElementById('pkgCategory');
        const priceInput = document.getElementById('pkgPrice');
        const durationInput = document.getElementById('pkgDuration');
        const photosInput = document.getElementById('pkgPhotos');
        const deliveryInput = document.getElementById('pkgDelivery');
        const descInput = document.getElementById('pkgDesc');

        window.openPackageModal = function() {
            modalTitle.textContent = 'Create Service Package';
            form.action = '${pageContext.request.contextPath}/photographer/packages';
            form.reset();
            modal.classList.add('is-open');
        };

        if (openBtn) openBtn.addEventListener('click', window.openPackageModal);
        if (closeBtn) closeBtn.addEventListener('click', () => modal.classList.remove('is-open'));
        if (cancelBtn) cancelBtn.addEventListener('click', () => modal.classList.remove('is-open'));

        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('is-open');
        });

        document.querySelectorAll('.edit-package-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.dataset.id;
                modalTitle.textContent = 'Edit Service Package';
                form.action = '${pageContext.request.contextPath}/photographer/packages/' + id + '/edit';

                nameInput.value = btn.dataset.name || '';
                catSelect.value = btn.dataset.category || 'PORTRAIT';
                priceInput.value = btn.dataset.price || '';
                durationInput.value = btn.dataset.duration || '120';
                photosInput.value = btn.dataset.photos || '25';
                deliveryInput.value = btn.dataset.delivery || '7';
                descInput.value = btn.dataset.desc || '';

                modal.classList.add('is-open');
            });
        });
    })();
</script>
