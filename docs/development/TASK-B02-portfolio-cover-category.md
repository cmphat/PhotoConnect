# TASK-B02: Portfolio Cover Image, Category & Portfolio Manager Enhancement

## Overview

TASK-B02 upgrades the PhotoConnect photographer portfolio management experience from a basic file upload page into a full photographer portfolio manager. It adds:
1. Controlled photography category taxonomy.
2. Portfolio cover image selection with single-cover invariant.
3. Automatic cover behavior (first upload becomes cover) and deterministic fallback on cover deletion.
4. An enhanced photographer portfolio management UI with expandable upload panel, live preview, cover highlight showcase, interactive category filter tabs, and action controls.
5. Preserved Cloudinary upload/delete semantics with delivery URL transformations.
6. Public marketplace and profile integration for cover display.

---

## 1. Schema Changes & Database Migration

Migration script: `docs/development/migrations/V011__add_portfolio_cover_and_category.sql`
- Follows sequential versioning after `V010__professional_demo_payment.sql`.
- Adds columns to `dbo.portfolio_images`:
  - `is_cover BIT NOT NULL CONSTRAINT DF_portfolio_images_is_cover DEFAULT 0`
  - `category NVARCHAR(50) NULL`
- Forward-only, non-destructive, idempotent (wrapped in transaction and protected with `IF COL_LENGTH(...) IS NULL`).
- Existing rows default to `is_cover = 0` and `category = NULL`.

---

## 2. Category Model

Implemented as a controlled enum `com.photoconnect.entity.PortfolioCategory`:
- Categories supported:
  1. `PORTRAIT` ("Portrait")
  2. `WEDDING` ("Wedding")
  3. `FASHION` ("Fashion")
  4. `LIFESTYLE` ("Lifestyle")
  5. `STREET` ("Street")
  6. `COMMERCIAL` ("Commercial")
  7. `EVENT` ("Event")
  8. `TRAVEL` ("Travel")
  9. `ARCHITECTURE` ("Architecture")
  10. `OTHER` ("Other")
- Strict backend validation: arbitrary user markup or unknown category strings are rejected with `IllegalArgumentException("Unsupported photography category: ...")`.
- Helper `fromFormValue(String value)` performs case-insensitive normalization and whitespace trimming.

---

## 3. Cover Invariant & Automatic Behavior

### Single-Cover Invariant
- For any photographer profile, at most ONE portfolio image can have `is_cover = true`.
- When setting an image as cover (`portfolioService.setCoverImage(userId, imageId)`):
  - Current cover images for the profile have `is_cover` set to `false`.
  - Target image has `is_cover` set to `true`.
  - Executed inside a `@Transactional` boundary for atomicity.
  - Ownership is verified at the database level against the authenticated session user's profile.

### Automatic Cover Assignment
- **First Upload**: When an approved photographer uploads their first portfolio photograph (`countByPhotographerProfileId == 0`), it is automatically marked as `is_cover = true`.
- **Subsequent Uploads**: When additional photographs are uploaded, they default to `is_cover = false` without displacing the active cover.

### Deterministic Fallback on Cover Deletion
- When an image with `is_cover == true` is deleted:
  - Remaining images for the profile are queried in deterministic order: `displayOrder ASC, createdAt ASC` (lowest display order tier, oldest photograph first).
  - If at least one image remains, the first item is promoted to `is_cover = true`.
  - If no images remain, the profile cleanly transitions to having no cover.
- When a non-cover image is deleted, active cover status remains unchanged.

---

## 4. Cloudinary Remote Boundary & Transformations

- **Upload Flow**: Browser -> Spring Boot -> Cloudinary SDK -> Cloudinary Media Library -> Secure URL & Public ID -> SQL Server metadata. API secrets remain strictly server-side.
- **Delete Safety**: Cloudinary deletion is performed before SQL Server metadata deletion. If Cloudinary deletion fails, an exception is thrown and the database record remains intact (maintaining single source of truth).
- **Delivery Transformations**:
  - Grid card thumbnails: `c_fill,w_600,h_750,q_auto,f_auto` via `PortfolioImage#getThumbnailUrl()`.
  - Cover showcase hero: `c_fill,w_1200,h_600,q_auto,f_auto` via `PortfolioImage#getCoverTransformedUrl()`.
  - No duplicate files uploaded; transformations are applied dynamically at delivery time.

---

## 5. Security & Ownership Protection

- Role enforcement: only authenticated users with `UserRole.PHOTOGRAPHER` can access portfolio management or mutation endpoints.
- Profile derivation: `userId` is obtained strictly from the server-side HTTP session.
- Ownership verification: database queries use `findByIdAndPhotographerProfileId(imageId, profile.getId())`. Modifying or deleting another photographer's image throws `SecurityException` and is handled with user-friendly flash errors.
- Public DTO safety: `PortfolioImagePublicDto` deliberately omits `publicId`, preventing exposure of internal Cloudinary asset identifiers.

---

## 6. Portfolio Manager UI Enhancements

Updated `photographer-portfolio.jsp`:
- **Header Strip**: "My Portfolio", lead description, and "+ Add Photograph" button toggling the upload drawer.
- **Expandable Upload Drawer**:
  - Preserves `.upload-expandable-panel` and `uploadDrawerPanel` contract hooks.
  - Image file selector (JPEG, PNG, WEBP, max 10 MB).
  - Category dropdown with all 10 curated categories.
  - Optional caption input (up to 500 characters).
  - **Live Image Preview (Vanilla JS)**: displays selected image thumbnail, filename, file size, and selected category tag before submission.
- **Cover Area**:
  - Highlights current cover image with large banner preview, category tag, caption, and "★ Current Cover" badge.
- **Gallery**:
  - Category filter tabs (`All`, `Portrait`, `Wedding`, etc.) providing instant client-side filtering without full-page reloads.
  - Responsive editorial image grid using transformed thumbnails.
  - Management actions on each card: "Set as Cover" (POST) and "Delete" (POST with confirmation).
  - Deliberate empty state when no portfolio images exist.

---

## 7. Public Profile & Discover Integration

- `PublicPhotographerServiceImpl#toPublicDtoWithCover` checks for an explicitly set cover image (`isCover == true`).
- If no image is explicitly marked as cover (e.g. legacy profiles before setting cover), it gracefully falls back to the first portfolio image (`images.get(0).getImageUrl()`).
- If no portfolio images exist, it falls back to `null`, triggering the existing artist initials placeholder on cards.

---

## 8. Automated Verification

Ran:
```text
mvn test
```
Result:
```text
Tests run: 442, Failures: 0, Errors: 0, Skipped: 25
BUILD SUCCESS
```

Added tests:
- `PortfolioCategoryTest`:
  - 10 enum values verified with human-friendly display names.
  - Parsing case-insensitivity and whitespace handling.
  - Validation: null, blank, and unsupported values rejected.
- `PortfolioServiceTest`:
  - First uploaded image automatically becomes cover.
  - Second uploaded image does not replace cover.
  - Upload with category correctly persists category.
  - Null category rejected before Cloudinary upload.
  - `setCoverImage` switches previous cover to false and sets target to true.
  - `setCoverImage` is idempotent when target is already cover.
  - Photographer cannot set another photographer's image as cover (`SecurityException`).
  - Deleting non-cover image does not trigger cover fallback.
  - Deleting active cover promotes deterministic fallback image.
  - Deleting final remaining image leaves profile with no cover.
- `PhotographerPortfolioControllerTest`:
  - Upload with category calls 4-arg service method and redirects.
  - Upload with invalid category redirects with flash error.
  - `POST /{id}/cover` invokes service and redirects with flash success.
  - `POST /{id}/cover` without session redirects to `/login`.
  - `POST /{id}/cover` with non-photographer role redirects to `/`.
  - `POST /{id}/cover` on unowned image redirects with error flash.
- `PublicPhotographerServiceTest`:
  - Explicit cover image is prioritized over first image.
- `PortfolioImageRepositoryIntegrationTest`:
  - Persistence and retrieval of `isCover` and `category`.
  - `findByPhotographerProfileIdAndIsCoverTrue` query test.

---

## 9. Manual Verification Checklist (Human Gate)

### Photographer Workflow
1. Sign in as an approved photographer and open `/photographer/portfolio`.
2. Click **+ Add Photograph** to expand the upload drawer.
3. Choose an image file and select category **Portrait**. Verify that the local preview immediately displays the thumbnail, filename, and category badge.
4. Click **Upload Photograph**. Verify the image appears in the gallery and is automatically displayed in the **Portfolio Cover** showcase as the current cover.
5. Upload a second image with category **Wedding**. Verify it is added to the gallery but does NOT displace the active cover.
6. Click **Set as Cover** on the second image. Verify the second image becomes the cover and the first image reverts to standard status.
7. Click the **Wedding** filter pill in the gallery. Verify only the wedding photograph is displayed.
8. Click **All** to restore the full gallery view.
9. Delete the non-cover image. Verify Cloudinary deletion and gallery update.
10. Delete the active cover image. Verify that the remaining image is deterministically assigned as the new cover.
11. Delete the last image. Verify the empty state message is shown: *"Your portfolio is empty. Upload your first photograph to start building your profile."*

### Public Marketplace Workflow
12. Visit `/photographers/{id}` for the photographer. Verify the hero banner displays the chosen cover photograph.
13. Visit `/photographers`. Verify the photographer's discovery card displays the cover photograph.

### Cloudinary Storage Check
14. Log into the Cloudinary Console Media Library. Verify newly uploaded assets exist in `photoconnect/portfolio/`.
15. Verify deleted assets are removed from Cloudinary.
