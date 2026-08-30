# TASK-011: Photographer Portfolio with Cloudinary

## Goal
Allow approved photographers to manage a portfolio of real images:
1. Integrate Cloudinary safely (credentials via env vars, never in Git)
2. Allow APPROVED photographers to upload portfolio images
3. Store image metadata (imageUrl + publicId) in SQL Server
4. Display portfolio images on the public photographer detail page
5. Allow photographers to delete their own portfolio images
6. Prevent users from modifying another photographer's portfolio

## Context
- Builds on TASK-010 public marketplace (approved-profile listing/detail)
- Builds on TASK-009 admin approval flow
- Authentication is session-based (userId, userRole, userFullName in HttpSession)
- `open-in-view=false` — all entity/lazy access must happen inside @Transactional
- SQL Server backend; Spring Boot 3.3.0 / Java 21

## Business Rules
1. Only APPROVED photographers may upload images
2. PENDING, REJECTED, SUSPENDED photographers cannot upload
3. Only the image owner (matched via session userId → photographerProfile) may delete
4. If a photographer is later suspended, existing images remain stored; public visibility
   follows the existing approved-profile visibility rule (profile must be APPROVED to be
   accessible on public detail page)
5. Image binary is NOT stored in SQL — only Cloudinary URL + publicId are stored
6. Allowed file types: JPEG, PNG, WEBP (content-type validated, not just extension)
7. Maximum file size: 10 MB per image

## Security Rules
1. Ownership comes from session userId — NEVER from hidden form inputs or query params
2. Cloudinary credentials come from environment variables only
3. publicId is NEVER exposed on public-facing views (internal management metadata)
4. All portfolio management routes require authenticated session

## Expected Files

### New — Java
- entity/PortfolioImage.java
- repository/PortfolioImageRepository.java
- dto/PortfolioImagePublicDto.java
- service/CloudinaryStorageService.java (interface)
- service/CloudinaryStorageServiceImpl.java
- service/PortfolioService.java (interface)
- service/PortfolioServiceImpl.java
- controller/PhotographerPortfolioController.java
- config/CloudinaryConfig.java

### New — JSP
- WEB-INF/views/photographer-portfolio.jsp

### New — Test
- test/.../service/PortfolioServiceTest.java
- test/.../controller/PhotographerPortfolioControllerTest.java
- test/.../repository/PortfolioImageRepositoryIntegrationTest.java

### New — Config/Git
- .gitignore

### Modified
- pom.xml (Cloudinary dependency + multipart config)
- application.properties (multipart limits + cloudinary property references)
- controller/PhotographerController.java (inject portfolio for public detail)
- WEB-INF/views/photographer-detail.jsp (real portfolio gallery)
- docs/final/project-status.md

## Checklist
- [x] Inspect project instructions
- [x] Inspect current photographer authorization model
- [x] Create `.gitignore`
- [x] Add Cloudinary dependency
- [x] Configure Cloudinary using environment variables
- [x] Create PortfolioImage entity
- [x] Create PortfolioImageRepository
- [x] Create upload DTO/request handling (PortfolioImagePublicDto)
- [x] Create Cloudinary storage service
- [x] Create PortfolioService
- [x] Restrict portfolio management to profile owner
- [x] Restrict upload to APPROVED photographer
- [x] Implement upload route
- [x] Implement delete route
- [x] Create portfolio management JSP
- [x] Display portfolio on public photographer detail page
- [x] Write service tests
- [x] Write controller tests
- [x] Write repository integration tests
- [x] Verify upload metadata persistence
- [x] Verify ownership protection
- [x] Verify Cloudinary deletion (automated — mock)
- [x] Run regression tests
- [x] Build WAR successfully
- [x] Start application successfully
- [ ] Await human upload/browser verification
- [x] Write development documentation
- [x] Update final project status

## TDD Plan

### Vertical slice 1: PortfolioImage persistence
RED: PortfolioImageRepositoryIntegrationTest — save + query
GREEN: entity + repository

### Vertical slice 2: PortfolioService upload (happy path)
RED: PortfolioServiceTest — approved photographer upload
GREEN: PortfolioServiceImpl.addPortfolioImage()

### Vertical slice 3: PortfolioService upload (rejected paths)
RED: PENDING photographer, non-photographer, invalid file
GREEN: guard clauses in service

### Vertical slice 4: PortfolioService delete (own image)
RED: delete own image → Cloudinary called + DB deleted
GREEN: PortfolioServiceImpl.deletePortfolioImage()

### Vertical slice 5: PortfolioService delete (ownership protection)
RED: delete other's image → SecurityException, Cloudinary not called
GREEN: ownership check via session userId → profileId

### Vertical slice 6: Controller
RED: unauthenticated → redirect; upload → PRG; delete → service called
GREEN: PhotographerPortfolioController

## Verification Commands
```
mvn test
mvn clean package
# Set env vars: CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
mvn spring-boot:run
```

## Problems Encountered
1. No `.gitignore` existed in project root — created one.
2. `@WebMvcTest` for PhotographerPortfolioController requires `CloudinaryStorageService`
   to be excluded from the web context (it's not a controller). Fixed by using
   `@MockBean` for all service dependencies.
3. `fmt:formatDate` and EL method calls are not supported by standard JSTL EL —
   already known from TASK-010; used `fn:` taglib functions throughout.

## Result
Automated tests: PASS (105 tests, 0 failures, 0 errors, 0 skipped)
Portfolio persistence: PASS
Upload service behavior: PASS
APPROVED-only upload: PASS
Ownership protection: PASS
Delete flow: PASS
Public gallery integration: PASS
Marketplace regression: PASS
Maven build: PASS (`mvn clean package`)
Application startup: PASS
Runtime `/photographers`: PASS (HTTP 200)
Runtime approved photographer detail: NOT RUN (no approved profile existed in the database)
Runtime `/photographer/portfolio`: PASS authentication guard (HTTP 302 to `/login`)
Human Cloudinary upload verification: PENDING

## Status
COMPLETE (automated implementation finished; human browser/Cloudinary verification pending)
