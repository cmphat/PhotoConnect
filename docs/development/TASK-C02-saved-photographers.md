# TASK-C02 — Saved Photographers / Favorites

## 1. Overview
TASK-C02 enables authenticated `CUSTOMER` users on PhotoConnect to save (bookmark/favorite) approved photographers from the public directory or photographer detail pages, view their personal collection on `/customer/saved-photographers`, and remove saved favorites.

---

## 2. Database Schema & Migration (`V013`)

- **Migration File**: `docs/development/migrations/V013__create_saved_photographers.sql`
- **Table Name**: `dbo.saved_photographers`
- **Engine**: Microsoft SQL Server 2019+

```sql
CREATE TABLE dbo.saved_photographers (
    id BIGINT IDENTITY(1,1) NOT NULL,
    customer_id BIGINT NOT NULL,
    photographer_profile_id BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_saved_photographers_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_saved_photographers PRIMARY KEY CLUSTERED (id),
    CONSTRAINT FK_saved_photographers_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.users(id) ON DELETE CASCADE,
    CONSTRAINT FK_saved_photographers_profile FOREIGN KEY (photographer_profile_id)
        REFERENCES dbo.photographer_profiles(id) ON DELETE CASCADE,
    CONSTRAINT UQ_saved_photographers_customer_profile UNIQUE (customer_id, photographer_profile_id)
);

CREATE NONCLUSTERED INDEX IX_saved_photographers_customer_id
    ON dbo.saved_photographers(customer_id);

CREATE NONCLUSTERED INDEX IX_saved_photographers_photographer_profile_id
    ON dbo.saved_photographers(photographer_profile_id);
```

### Uniqueness & Referential Integrity
- **Composite Unique Constraint**: `UQ_saved_photographers_customer_profile (customer_id, photographer_profile_id)` guarantees at the database level that a customer cannot save the same photographer more than once.
- **Foreign Key Cascades**: If a user or profile is deleted in integration testing or administrative procedures, associated favorites cascade delete without violating foreign key constraints.

---

## 3. Data Model

- **Entity**: `com.photoconnect.entity.SavedPhotographer`
  - `@ManyToOne(fetch = FetchType.LAZY, optional = false)` `customer` (`User`)
  - `@ManyToOne(fetch = FetchType.LAZY, optional = false)` `photographerProfile` (`PhotographerProfile`)
  - `@CreationTimestamp` `createdAt` (`LocalDateTime`)
- **DTO**: `com.photoconnect.dto.SavedPhotographerDto`
  - Encapsulates `id`, `photographerId`, `PhotographerPublicDto photographer`, and `savedAt`.
  - Avoids exposing internal `User` or non-public fields. Compatible with `open-in-view=false`.

---

## 4. Authorization & Security

- **Authentication Source**: Uses the project's established server-side session authentication helper `SessionSecurityUtils` (`session.getAttribute("userId")` and `session.getAttribute("userRole")`).
- **Role Enforcement**:
  - Only `ROLE_CUSTOMER` can save, unsave, or view saved photographers.
  - Guests (`userId == null`) attempting to save or unsave are redirected to `/login`.
  - Authenticated non-customers (`PHOTOGRAPHER`, `ADMIN`) attempting to mutate or view saved favorites receive HTTP `403 Forbidden`.
- **IDOR Protection**: All mutations require authenticated identity (`customerUserId = SessionSecurityUtils.userId(session)`). A customer cannot delete or mutate another customer's favorites by manipulating IDs.

---

## 5. Domain Rules & Edge Cases

1. **Idempotent Saves**:
   - Calling `savePhotographer` repeatedly for an already-saved profile returns `true` without creating duplicate rows.
   - Concurrent double-clicks caught by the unique constraint (`DataIntegrityViolationException`) log a warning and return `true` gracefully without 500 errors.
2. **Idempotent Removal**:
   - Calling `removeSavedPhotographer` for a photographer that is not in the customer's favorites returns `false` gracefully without error.
3. **Unapproved Photographers**:
   - An unapproved photographer (status `PENDING`, `REJECTED`, or `SUSPENDED`) **cannot** be newly saved (`IllegalStateException: Only approved photographers can be saved to favorites`).
   - If an already-saved photographer loses `APPROVED` status, the database row is retained (preserving user intent should they be re-approved), but `getSavedPhotographers()` filters them out from the customer's active marketplace collection.

---

## 6. Route Specifications

| Method | Path | Auth Required | Description |
|---|---|---|---|
| `POST` | `/photographers/{id}/save` | Customer | Saves photographer to customer favorites. Redirects to profile or `?redirect=` target. |
| `POST` | `/photographers/{id}/unsave` | Customer | Removes photographer from favorites. Redirects to profile or `?redirect=` target. |
| `GET` | `/customer/saved-photographers` | Customer | Renders personal collection of saved approved photographers. |
| `GET` | `/photographers/{id}` | Public | Populates `isSaved` in model for authenticated customers. |
| `GET` | `/photographers` | Public | Populates `savedPhotographerIds` (`Set<Long>`) in model for authenticated customers. |

---

## 7. UI Integration

- **Navbar (`fragments/navbar.jsp`)**: Added "Saved" link (`/customer/saved-photographers`) in the primary navigation for authenticated `CUSTOMER` users.
- **Photographer Detail (`photographer-detail.jsp`)**:
  - Hero header: Displays "+ Save Photographer" or "✓ Saved" button next to "Request Booking".
  - Booking sidebar: Displays full-width Save / Remove button beneath "Request Booking".
  - Flash message alerts: Displays feedback banners for save and unsave events.
- **Explore Photographers (`photographers.jsp`)**:
  - Photographer cards display a subtle "Saved" badge when the creator is saved in the customer's favorites.
- **Saved Photographers Page (`saved-photographers.jsp`)**:
  - Deliberate empty state with "No saved photographers yet." and "Explore Photographers" CTA when collection is empty.
  - Creator cards display real data: cover image, professional display name, headline, location, experience, rating, specialties pills, starting price in VND.
  - Action buttons: "View Profile" link and "Remove" POST button.
  - Styled with PhotoConnect design system tokens and Bootstrap primitives, adhering to SiteMesh decorator structure.

---

## 8. Verification & Tests

- **Unit Tests**:
  - `SavedPhotographerServiceTest`: 13 tests verifying save, idempotent duplicate save, concurrent unique violation handling, unapproved rejection, role validation, IDOR prevention, and filtering.
- **Controller Tests**:
  - `SavedPhotographerControllerTest`: 14 MockMvc tests covering saving, unsaving, redirect parameter handling, guest redirects, and 403 authorization guards.
  - `PhotographerControllerTest`: Updated with tests verifying `isSaved` and `savedPhotographerIds` model population.
- **Repository Integration Tests**:
  - `SavedPhotographerRepositoryIntegrationTest`: Verifies unique constraint, FK cascades, and queries against SQL Server.
- **Contract Tests**:
  - `BootstrapIntegrationContractTest`, `SiteMeshIntegrationContractTest`, `Task031UiPolishContractTest`, `TransactionBoundaryTest` all passing.
- **Build Status**:
  - `mvn test`: 0 failures, 0 errors, 490 tests executed (29 DB-slice integration tests skipped without live DB).
  - `mvn clean package`: BUILD SUCCESS.
