# TASK-012 Development Notes: Premium Photographer Marketplace + Search & Filter

## Outcome

Upgraded the public photographer marketplace with dynamic multi-criteria search and filter capabilities and a dark editorial, photography-first design aesthetic. Users can search by keyword, filter by city, minimum/maximum starting price, and minimum experience, or combine filters seamlessly with clear filter actions. Only `APPROVED` photographers are accessible publicly, preserving strict server-side authorization and data privacy.

## Public Filtering Architecture

### Request Parameters (`PhotographerSearchRequest`)
- `keyword` (String): Matches `displayName`, `bio`, or `city` case-insensitively using `LOWER(...) LIKE LOWER('%keyword%')`.
- `city` (String): Case-insensitive match on `city`.
- `minPrice` (BigDecimal): Minimum starting price (`priceFrom >= minPrice`).
- `maxPrice` (BigDecimal): Maximum starting price (`priceFrom <= maxPrice`).
- `minExperience` (Integer): Minimum years of experience (`experienceYears >= minExperience`).

### Server-Side Filtering & Authorization
- **APPROVED-Only Enforcement**: Query parameter `verificationStatus = APPROVED` is strictly enforced in the repository JPQL query. Profiles with `PENDING`, `REJECTED`, or `SUSPENDED` status are never returned in search results or direct detail routes (`/photographers/{id}`).
- **Open-in-View Compatibility**: The repository query uses `JOIN FETCH p.user` within a `@Transactional(readOnly = true)` boundary to load user metadata safely without encountering `LazyInitializationException`.
- **Validation**:
  - `minPrice >= 0`, `maxPrice >= 0`, `minExperience >= 0`.
  - If `minPrice > maxPrice`, a clean validation error message ("Minimum price cannot exceed maximum price") is displayed on the UI rather than throwing an unhandled exception or 500 error.
  - Blank inputs are normalized to `null` to ensure clean, index-friendly JPQL query execution.

### Public DTO & Cover Image Strategy
- `PhotographerPublicDto` encapsulates public-safe data (no email, no password hash, no internal admin statuses).
- Added `coverImageUrl` support: For each photographer profile, the service queries `PortfolioImageRepository` for the first available portfolio image to feature on the marketplace card. If no portfolio image exists, an elegant dark editorial placeholder with the photographer's initial monogram is displayed.

## UI Design Decisions & Shared CSS

- **Stylesheet Location**: `src/main/webapp/assets/css/photoconnect.css` (and mirrored to `src/main/resources/static/assets/css/photoconnect.css` for embedded container compatibility).
- **Design Language**:
  - Deep obsidian dark theme (`#08080a`, `#101014`, `#16161c`).
  - Warm champagne gold accents (`#c9a96e`, `#dfc89e`) and subtle emerald verification badges.
  - Typography: Serif editorial headline (`Playfair Display`) paired with clean, modern sans-serif (`Inter`).
  - Card interactions: Subtle lift and smooth image scale (`scale(1.06)`) on hover (280ms cubic-bezier transition).
  - Responsive filter panel: Horizontal desktop layout transitioning to clean vertical inputs on mobile.
  - Empty state: Minimalist, photography-themed empty state with quick "Reset Filters" action.

## Automated Verification

- `PhotographerSearchRequestTest`: 6 unit tests verifying blank normalization, bounds validation, and price range checking.
- `PublicPhotographerServiceTest`: 18 unit tests verifying all individual and combined filters, APPROVED-only filtering, invalid price rejection, and cover image population.
- `PhotographerControllerTest`: 10 WebMvcTest controller tests verifying parameter binding, model population, error display, guest access, and direct detail protection.
- Total Unit/Controller Suite: 106 tests passing with 0 failures, 0 errors, 0 skipped.

## Human Visual Verification

`PENDING`. A human tester should verify the visual appearance and interactions in a desktop and mobile browser:
1. Navigate to `http://localhost:8080/photographers`.
2. Verify the dark editorial theme, search bar, and photographer cards.
3. Test keyword search (e.g. "wedding" or "portrait").
4. Test city filter (e.g. "Hanoi" or "Ho Chi Minh City").
5. Test price range and experience filters.
6. Verify active filter indicators and "Clear Filters" button.
