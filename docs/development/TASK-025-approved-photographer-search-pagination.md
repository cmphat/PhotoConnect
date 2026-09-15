# TASK-025: Approved Photographer Search Pagination

## Purpose

Complete the Week 7 / Phase 9 search-pagination requirement and the `/photographers?page=...` contract in `docs/API_CONTRACT.md` without changing the existing marketplace architecture or visual system.

## Scope

- Add a zero-based, validated `page` filter to `PhotographerSearchRequest`.
- Query SQL Server through Spring Data `Pageable` rather than loading the entire approved marketplace result set.
- Preserve the mandatory `APPROVED` predicate and every existing search filter in both the page and count queries.
- Cap service page size at 24; the MVC marketplace uses 12 cards per page.
- Return total-result and page metadata to the JSP.
- Add accessible Previous/Next navigation that preserves active filters.
- Keep existing unpaged service methods for backward compatibility with completed features.

## Files Affected

### Modified

- `src/main/java/com/photoconnect/dto/PhotographerSearchRequest.java`
- `src/main/java/com/photoconnect/repository/PhotographerProfileRepository.java`
- `src/main/java/com/photoconnect/service/PublicPhotographerService.java`
- `src/main/java/com/photoconnect/service/PublicPhotographerServiceImpl.java`
- `src/main/java/com/photoconnect/controller/PhotographerController.java`
- `src/main/webapp/WEB-INF/views/photographers.jsp`
- `src/test/java/com/photoconnect/dto/PhotographerSearchRequestTest.java`
- `src/test/java/com/photoconnect/repository/PhotographerProfileRepositoryIntegrationTests.java`
- `src/test/java/com/photoconnect/service/PublicPhotographerServiceTest.java`
- `src/test/java/com/photoconnect/controller/PhotographerControllerTest.java`

### Created

- `docs/development/TASK-025-approved-photographer-search-pagination.md`

## Acceptance Criteria

- `/photographers` defaults to page `0`, size `12`.
- `page` outside `0..10000` is rejected safely and the first unfiltered page is shown with a validation message.
- Only `APPROVED` profiles can appear on any page.
- Keyword, city, price, and experience filters remain active across Previous/Next links.
- Total matching result count and page count come from the database page result.
- Requests cannot select an unbounded or excessive page size.
- `spring.jpa.open-in-view=false` remains unchanged; the paged query fetches the required `User` relationship.

## Automated Tests

- `PhotographerSearchRequestTest`: page bounds, defaulting, and page-not-a-filter semantics.
- `PublicPhotographerServiceTest`: page metadata mapping, normalized filters, approved-only query, and maximum page-size guard.
- `PhotographerControllerTest`: 12-item page request and page metadata exposed to the view.
- `PhotographerProfileRepositoryIntegrationTests`: bounded approved page and count query (skipped when live DB credentials are absent, consistent with existing integration-test policy).

Focused verification: 46 tests, 0 failures, 0 errors, 7 skipped (live SQL Server integration tests).

## Manual Verification Steps

**Human Verification: PARTIAL**

1. **PASS (manually verified by project owner):** Public `/photographers` marketplace pagination is visible using seeded demo data (14 approved profiles); Page 1 renders 12 cards, exposes the Next button, and primary pagination functions correctly across pages.
2. Filter persistence across pages and negative/out-of-bound page parameter edge cases remain verified via automated tests (`PhotographerSearchRequestTest`, `PublicPhotographerServiceTest`, `PhotographerControllerTest`); browser testing for individual complex filter edge cases remains PENDING.

## Documentation

- `docs/API_CONTRACT.md` now records the zero-based page range and the MVC page-size behavior.
- Full-batch verification: 376 tests, 0 failures, 0 errors, 23 skipped environment-gated integration tests; clean WAR packaging succeeded.
- `docs/ROADMAP.md`, `docs/PLAN_BE.md`, and `docs/final/project-status.md` were updated after verification.

## Status

Implementation, focused verification, full regression, and clean packaging complete. Human Verification: PARTIAL (primary public marketplace pagination PASS).

