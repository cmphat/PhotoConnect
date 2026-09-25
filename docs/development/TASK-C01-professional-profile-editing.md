# TASK-C01: Professional Photographer Profile Editing

## 1. Overview
TASK-C01 elevates the photographer profile in PhotoConnect V2 from a minimal registration record into a comprehensive, professional creator profile. It introduces controlled photography specialties, server-side URL protocol whitelisting, live profile completeness metrics with actionable recommendations, a dedicated creator studio profile editing workspace, and enriched public-facing profile metadata.

---

## 2. Reused vs. New Data Fields

### Reused Fields (from `photographer_profiles`)
- `displayName`: Reused as Professional / Studio Name (2–150 characters).
- `bio`: Reused as Biography and Artist Statement (20–2,000 characters).
- `city`: Reused as Primary City of operation (up to 100 characters).
- `experienceYears`: Reused as Years of Experience (0–80 years).
- `priceFrom`: Reused as Starting Session Rate (VND currency; `>= 0`).
- `verificationStatus`: Kept intact; editing public profile never mutates or overrides approval.
- `averageRating`, `reviewCount`: Kept intact; immutable via profile editing.

### New Fields Added (Migration `V012`)
- `headline` (`NVARCHAR(255) NULL`): Concise one-sentence creative introduction.
- `country` (`NVARCHAR(100) NULL`): Country/region of operation (e.g., Vietnam).
- `specialties` (`NVARCHAR(255) NULL`): Comma-delimited list of controlled specialty tokens.
- `websiteUrl` (`NVARCHAR(255) NULL`): External portfolio website link (`http://` or `https://`).
- `instagramUrl` (`NVARCHAR(255) NULL`): External Instagram profile link (`http://` or `https://`).
- `facebookUrl` (`NVARCHAR(255) NULL`): External Facebook page link (`http://` or `https://`).
- `equipmentSummary` (`NVARCHAR(500) NULL`): Camera bodies, lenses, and lighting equipment summary.
- `languages` (`NVARCHAR(150) NULL`): Working languages spoken by the artist.
- `travelAvailable` (`BIT NOT NULL DEFAULT 0`): Boolean flag indicating destination and travel availability.

---

## 3. Database Migration
- **Script**: `docs/development/migrations/V012__add_professional_profile_fields.sql`
- **Safety Properties**:
  - Forward-only, non-destructive, and idempotent via `COL_LENGTH('dbo.photographer_profiles', '<column>') IS NULL` checks.
  - Wrapped in a transaction with `SET XACT_ABORT ON`.
  - All new columns are nullable (or have safe default `0` for bit), guaranteeing that existing approved and pending photographer records remain completely valid without data loss.

---

## 4. Controlled Specialty Taxonomy
- **Enum**: `com.photoconnect.entity.PhotographerSpecialty`
- **Allowed Categories (10)**:
  1. `PORTRAIT` ("Portrait")
  2. `WEDDING` ("Wedding")
  3. `FASHION` ("Fashion")
  4. `LIFESTYLE` ("Lifestyle")
  5. `EVENT` ("Event")
  6. `COMMERCIAL` ("Commercial")
  7. `PRODUCT` ("Product")
  8. `TRAVEL` ("Travel")
  9. `ARCHITECTURE` ("Architecture")
  10. `DOCUMENTARY` ("Documentary")
- Stored as comma-separated valid tokens in `photographer_profiles.specialties`. Arbitrary user-supplied HTML or unknown tags are strictly rejected by server-side validation.

---

## 5. Profile Completeness Formula
The profile completeness score is calculated dynamically on the server from the actual profile and portfolio state (not a client-editable number):

| Factor | Criterion | Weight |
|---|---|:---:|
| **Professional Name** | `displayName` non-blank | 10% |
| **Headline** | `headline` non-blank | 15% |
| **Biography** | `bio` non-blank and `>= 20` chars | 15% |
| **Location** | `city` non-blank or `country` non-blank | 10% |
| **Specialties** | `specialties` contains `>= 1` controlled genre | 15% |
| **Starting Price** | `priceFrom > 0` | 10% |
| **Portfolio Images** | Portfolio image count `>= 1` | 15% |
| **Dedicated Cover** | Profile has designated cover image (`isCover == true`) | 10% |
| **Total** | | **100%** |

Actionable recommendations are provided to guide creators to 100% completeness.

---

## 6. Security and Ownership Architecture
1. **Authentication Guard**: Only authenticated users with `ROLE_PHOTOGRAPHER` can access the edit routes (`/photographer/profile/edit`). Customers are redirected to `/`; unauthenticated users are redirected to `/login`.
2. **Server-Side Ownership**: The profile to edit is looked up exclusively via `session.getAttribute("userId")`. No client-submitted profile ID or request parameter can hijack another photographer's profile.
3. **Privilege & Status Tampering Prevention**: Form submission uses `PhotographerProfileEditRequest`. The entity fields `verificationStatus`, `averageRating`, `reviewCount`, and the user's `role` are never bound or mutated during profile editing.
4. **URL Protocol Whitelist**: `websiteUrl`, `instagramUrl`, and `facebookUrl` are strictly validated on the server. Only `http://` and `https://` protocols are permitted. Unsafe schemes (`javascript:`, `data:`, `file:`) and whitespace are rejected.
5. **XSS Prevention**: All public profile renderings escape text and attribute values using `<c:out>`. External links include `rel="noopener noreferrer" target="_blank"`.

---

## 7. Routes and UI Workspaces
- `GET /photographer/profile/edit`: Creator profile edit workspace with completeness tracker and categorized sections.
- `POST /photographer/profile/edit`: Validates input and updates profile, redirecting with a flash success message on success or returning form errors with preserved input.
- `GET /photographers/{id}`: Public photographer detail page displays headline, country, specialties pills, studio equipment, languages, destination travel availability, and safe social channels without requiring a visual redesign.
- `GET /photographers`: Marketplace directory cards display artist headline, country, and primary specialties badges.

---

## 8. Verification and Quality Assurance
- Automated test suites cover taxonomy parsing, profile update operations, security boundaries, URL validation, and completeness calculations.
- Contract tests enforce button styling rules across all 28 full-page JSPs.
