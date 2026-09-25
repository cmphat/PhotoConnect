# TASK-011 Development Notes: Photographer Portfolio with Cloudinary

## Outcome

Implemented portfolio image management for photographers and a public portfolio gallery.
Image binaries are stored by Cloudinary; SQL Server stores only the secure image URL,
Cloudinary public ID, caption, display order, ownership, and creation timestamp.

## Implementation

- Added environment-only Cloudinary configuration through
  `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`.
- Added multipart limits of 10 MB per file and request.
- Added `PortfolioImage` persistence and ownership-scoped repository queries.
- Added a Cloudinary storage abstraction and SDK-backed upload/delete implementation.
- Added transactional portfolio business logic:
  - only `APPROVED` photographers can upload;
  - JPEG, PNG, and WEBP content types are accepted;
  - files larger than 10 MB are rejected;
  - ownership is derived from the session user ID;
  - another photographer's image cannot be deleted;
  - failed database persistence triggers a best-effort Cloudinary rollback;
  - failed Cloudinary deletion leaves the database record intact.
- Added authenticated portfolio management routes and JSP at
  `/photographer/portfolio`.
- Added public-safe portfolio DTOs and gallery rendering on approved photographer
  detail pages. Cloudinary public IDs are not exposed in public DTOs or views.

## Automated Verification

On 2026-08-30:

- `mvn test`: PASS — 105 tests, 0 failures, 0 errors, 0 skipped.
- `mvn clean package`: PASS — executable WAR created successfully.
- Portfolio repository integration tests ran against the configured SQL Server.
- Cloudinary upload/delete behavior was verified with mocked storage; no real
  Cloudinary request was made.

## Configuration

No database or Cloudinary credentials are stored in source control. Before running
the application or database-backed tests, set:

```powershell
$env:DB_USERNAME="..."
$env:DB_PASSWORD="..."
$env:CLOUDINARY_CLOUD_NAME="..."
$env:CLOUDINARY_API_KEY="..."
$env:CLOUDINARY_API_SECRET="..."
```

Set `DB_URL` as well when the default local SQL Server URL is not appropriate.

## Human Verification

- **Cloudinary Live Upload (TASK-B01):** **HUMAN VERIFIED**. The real flow (PhotoConnect photographer portfolio -> Spring Boot -> Cloudinary -> real asset appears in Cloudinary Media Library) has been verified by the user uploading real images from PhotoConnect to the active Cloudinary account.
- **Cloudinary Live Deletion:** **PENDING**. Live deletion of an asset from Cloudinary via browser flow remains pending explicit human confirmation.

## Runtime Verification

The packaged application started successfully on 2026-08-30. Direct HTTP checks
confirmed `/photographers` returned 200 with rendered content and unauthenticated
`/photographer/portfolio` returned 302 to `/login`. The database contained no
approved photographer, so an approved detail URL could not be checked without
creating or altering data. Browser automation was unavailable because the
`agent-browser` executable was not installed.
