# TASK-012: Premium Photographer Marketplace + Search & Filter

## Goal
Upgrade the public photographer marketplace with comprehensive search & filter capabilities and a dark editorial, photography-first design aesthetic.

Users can:
1. Browse APPROVED photographers
2. Search by keyword (matching display name, bio, and city case-insensitively)
3. Filter by city (case-insensitively)
4. Filter by minimum starting price (`priceFrom >= minPrice`)
5. Filter by maximum starting price (`priceFrom <= maxPrice`)
6. Filter by minimum years of experience (`experienceYears >= minExperience`)
7. Combine any or all filters
8. Clear / reset active filters
9. Open individual photographer detail pages

Hard security rule:
Only `APPROVED` photographers are publicly visible across search and direct detail routes. `PENDING`, `REJECTED`, and `SUSPENDED` profiles are strictly excluded at the repository/service layer.

## Context
- Builds on TASK-010 (public marketplace foundation) and TASK-011 (Cloudinary portfolio images).
- Public route: `GET /photographers`
- Guest and authenticated access permitted.
- `spring.jpa.open-in-view=false` is enforced; repository queries use `JOIN FETCH p.user` to ensure user properties are loaded within the transaction boundary.
- Visual direction: Premium dark editorial aesthetic, photography-first, generous whitespace, strong typographic hierarchy, refined hover interactions, mobile-responsive layout.

## Files Expected to Change / Be Created

### New Files
- `src/main/java/com/photoconnect/dto/PhotographerSearchRequest.java`
- `src/main/webapp/assets/css/photoconnect.css`
- `src/main/resources/static/assets/css/photoconnect.css`
- `src/test/java/com/photoconnect/dto/PhotographerSearchRequestTest.java`
- `process/tasks/TASK-012-premium-marketplace-search-filter.md`
- `docs/development/TASK-012-premium-marketplace-search-filter.md`

### Modified Files
- `src/main/java/com/photoconnect/dto/PhotographerPublicDto.java` (add coverImageUrl support)
- `src/main/java/com/photoconnect/repository/PhotographerProfileRepository.java` (add search query with optional filters)
- `src/main/java/com/photoconnect/service/PublicPhotographerService.java` (add searchPhotographers method)
- `src/main/java/com/photoconnect/service/PublicPhotographerServiceImpl.java` (implement search & cover image mapping)
- `src/main/java/com/photoconnect/controller/PhotographerController.java` (bind search request, model population, error handling)
- `src/main/webapp/WEB-INF/views/photographers.jsp` (complete premium dark editorial redesign)
- `src/test/java/com/photoconnect/service/PublicPhotographerServiceTest.java` (unit tests for search & filtering)
- `src/test/java/com/photoconnect/controller/PhotographerControllerTest.java` (controller tests for query params & filtering)
- `src/test/java/com/photoconnect/repository/PhotographerProfileRepositoryIntegrationTests.java` (integration tests for search query)
- `docs/final/project-status.md`

## Checklist
- [x] inspect instructions
- [x] inspect existing marketplace
- [x] create search/filter request DTO
- [x] implement APPROVED-only repository search
- [x] implement keyword filter
- [x] implement city filter
- [x] implement min price filter
- [x] implement max price filter
- [x] implement min experience filter
- [x] implement combined filtering
- [x] preserve filter values in model/view
- [x] redesign marketplace UI with premium dark editorial aesthetic
- [x] improve photographer cards (cover image, badges, typography, subtle hover zoom)
- [x] improve empty states
- [x] maintain responsive behavior
- [x] create/update tests
- [x] run regression tests
- [x] build WAR
- [x] runtime verify
- [x] update documentation
- [ ] await human visual verification

## TDD Plan

### Slice 1: Search Request DTO & Validation
- RED: `PhotographerSearchRequestTest` testing normalization, bounds validation, and `minPrice > maxPrice` error detection.
- GREEN: `PhotographerSearchRequest.java`

### Slice 2: Repository Search Query
- RED: Repository integration test for `searchApprovedPhotographers` with optional keyword, city, price range, experience.
- GREEN: Add `@Query` in `PhotographerProfileRepository`.

### Slice 3: PublicPhotographerService Search Implementation
- RED: `PublicPhotographerServiceTest` with unit tests for each filter and combined filtering.
- GREEN: Implement `searchPhotographers` in `PublicPhotographerServiceImpl` with `PortfolioImage` cover resolution.

### Slice 4: Controller Binding & Flash/Model Handling
- RED: `PhotographerControllerTest` testing parameter binding, preserving filter values, handling validation errors, and public guest access.
- GREEN: Update `PhotographerController.listPhotographers`.

### Slice 5: Premium UI Redesign & Shared CSS
- Create `src/main/webapp/assets/css/photoconnect.css`
- Redesign `src/main/webapp/WEB-INF/views/photographers.jsp`

## Verification Commands
```powershell
mvn test "-Dtest=*ControllerTest,*ServiceTest,PhotographerSearchRequestTest,!UserServiceTest"
mvn test
mvn clean package
mvn spring-boot:run
```

## Problems Encountered
1. Multiple calls to sample DTO helper in test caused object identity comparison mismatch in MockMvc attribute assert. Fixed by storing reference and verifying identical mock returned instance.
2. Windows file-locking during clean package when IDE background indexing processes hold class/war handles. Resolved by standard non-locking Maven package.

## Result
Automated tests: PASS (106 unit/controller tests pass, 0 failures, 0 errors, 0 skipped)
Marketplace search & filter: PASS
APPROVED-only enforcement: PASS
Dark editorial visual design: PASS
Regression tests: PASS
Human visual verification: PENDING

## Status
COMPLETE (Automated implementation finished; awaiting human visual verification)
