# TASK-010: Public Photographer Marketplace

## What This Task Does

This task adds the first public-facing feature to PhotoConnect: a marketplace where anyone (guests and logged-in users) can browse verified photographer profiles.

Two new pages:
- `/photographers` — a card grid listing all APPROVED photographers
- `/photographers/{id}` — a detailed profile page for a single APPROVED photographer

---

## Why Only APPROVED Profiles Are Public

PhotoConnect has a multi-step verification system for photographers:

| Status | Meaning |
|---|---|
| `PENDING` | Applied, waiting for admin review |
| `APPROVED` | Admin reviewed and approved — publicly visible |
| `REJECTED` | Admin declined the application |
| `SUSPENDED` | Account suspended (future use) |

The business rule is: **only APPROVED photographers can be seen by the public.** This protects photographers who haven't been verified yet, and ensures the marketplace only shows quality-vetted professionals.

---

## Why Filtering Must Happen Server-Side

A common mistake is to fetch ALL profiles from the database and then hide non-approved ones in the JSP template. **This is unsafe.**

If you only hide cards in JSP:
- A user can still visit `/photographers/42` directly (guessing the ID).
- The server would return data for a PENDING profile.
- The user would see private information they should not.

In PhotoConnect, the filter happens in the **JPQL query** itself — at the database layer:

```java
// PhotographerProfileRepository
@Query("""
    SELECT p FROM PhotographerProfile p
    JOIN FETCH p.user
    WHERE p.id = :id
    AND p.verificationStatus = :status
    """)
Optional<PhotographerProfile> findByIdAndVerificationStatusWithUser(
    @Param("id") Long id,
    @Param("status") PhotographerVerificationStatus status);
```

When we call `findByIdAndVerificationStatusWithUser(42, APPROVED)`, the database returns **nothing** if profile 42 is PENDING. The JSP never receives the data, so there's nothing to accidentally display.

---

## Why a DTO Is Used Instead of Passing the Entity to the View

The `PhotographerProfile` entity is linked to a `User` entity that contains:
- Password hash
- Email address
- Internal account status

If we passed the entity directly to the JSP, and a developer accidentally wrote `${photographer.user.email}` or `${photographer.user.password}` in the template, sensitive data would leak.

The `PhotographerPublicDto` is a **purpose-built data container** for the public view. It contains exactly what the public should see:

```java
public class PhotographerPublicDto {
    private final Long id;
    private final String displayName;
    private final String bio;
    private final String city;
    private final Integer experienceYears;
    private final BigDecimal priceFrom;
    private final LocalDateTime createdAt;
    // NO email, NO password, NO verificationStatus (internal concept)
}
```

The DTO has a static factory method `PhotographerPublicDto.from(PhotographerProfile)` that does the mapping inside the service's `@Transactional` method — while the Hibernate session is still open and the entity data is safely available.

---

## Why Email Is Not Exposed on the Public Marketplace

There is no product requirement to show a photographer's email address on their public profile. Exposing it would:
- Enable spam/harvesting of email addresses
- Violate user privacy expectations

If a future task requires contact (e.g., inquiry forms), that will be handled through the application's messaging system, not by exposing raw email addresses.

---

## Why Lazy-Loading Issues Are Handled Before JSP

`PhotographerProfile` has a `@OneToOne(fetch = FetchType.LAZY)` relationship to `User`. This means Hibernate does NOT automatically load the User when it loads the profile.

With `open-in-view=false` (which this project uses), the Hibernate session closes before the JSP renders. If the JSP tries to access `photographer.user.email` and the User wasn't loaded, Hibernate throws a `LazyInitializationException` — a runtime crash.

**Solution**: Use `JOIN FETCH` in JPQL so the User is loaded as part of the same query, inside the transaction:

```sql
SELECT p FROM PhotographerProfile p
JOIN FETCH p.user
WHERE p.verificationStatus = :status
```

This loads both `PhotographerProfile` and `User` in a single SQL JOIN. By the time the JSP renders, the data is already fully loaded in-memory. No session needed.

**Why not change the relationship to EAGER?** EAGER loading would force User to load on every `PhotographerProfile` query, even ones that don't need User data. This causes unnecessary database load and N+1 query patterns in other parts of the application.

---

## Why JOIN FETCH Is Used for DTO Mapping

Even though the DTO mapping itself doesn't directly access the User (since the DTO excludes user fields), the entity is still loaded with `JOIN FETCH` as a clean habit:
1. The service is `@Transactional(readOnly=true)` — session is open during mapping.
2. The `from()` factory method accesses the profile's own fields only.
3. The approach is consistent with the admin service established in TASK-009.

---

## Controller → Service → Repository Flow

```
Browser (GET /photographers)
    │
    ▼
PhotographerController.listPhotographers()
    │   No session check — public route
    │
    ▼
PublicPhotographerService.listApprovedPhotographers()
    │   @Transactional(readOnly=true)
    │
    ▼
PhotographerProfileRepository.findByVerificationStatusWithUser(APPROVED)
    │   JPQL: SELECT p JOIN FETCH p.user WHERE p.verificationStatus = APPROVED
    │
    ▼
List<PhotographerProfile>  →  map to List<PhotographerPublicDto>
    │
    ▼
model.addAttribute("photographers", dtos)
    │
    ▼
photographers.jsp renders card grid
```

---

## Tests Performed

### PublicPhotographerServiceTest (9 tests, Mockito unit tests)
- `listApproved_shouldReturnOnlyApprovedProfiles` — verifies only APPROVED status returned
- `listApproved_whenNoneApproved_shouldReturnEmptyList`
- `getById_approvedProfile_shouldReturnDto`
- `getById_pendingProfile_shouldThrowIllegalArgumentException`
- `getById_rejectedProfile_shouldThrowIllegalArgumentException`
- `getById_suspendedProfile_shouldThrowIllegalArgumentException`
- `getById_nonExistentId_shouldThrowIllegalArgumentException`
- `publicDto_shouldNotHavePasswordOrEmailAccessors` — structural safety check
- `publicDto_fromApprovedProfile_shouldMapAllPublicFields`

### PhotographerControllerTest (6 tests, WebMvcTest)
- `getPhotographers_guestNoSession_shouldReturn200` — confirms no login required
- `getPhotographers_shouldReturn200AndCorrectView`
- `getPhotographers_withApprovedProfiles_shouldExposeToModel`
- `getPhotographers_loggedInUser_shouldAlsoReturn200`
- `getPhotographerDetail_guestWithApprovedId_shouldReturn200`
- `getPhotographerDetail_approvedId_shouldPopulateModel`
- `getPhotographerDetail_nonApprovedId_shouldRedirectToListing`
- `getPhotographerDetail_nonExistentId_shouldRedirectToListing`

### PhotographerProfileRepositoryIntegrationTests (additions)
- `findByIdAndVerificationStatusWithUser_approvedProfile_shouldReturnResult`
- `findByIdAndVerificationStatusWithUser_pendingProfile_queriedAsApproved_shouldReturnEmpty`

**Total test suite: 77 tests, 0 failures.**

---

## Issues Encountered

**EL method call limitations in JSTL**: Standard JSP EL (Expression Language) does not support calling Java methods like `.substring()`, `.toUpperCase()`, or `.size()` on objects. These must be replaced with JSTL functions:
- `${someString.substring(0,1)}` → `${fn:substring(someString, 0, 1)}`
- `${someString.toUpperCase()}` → `${fn:toUpperCase(someString)}`
- `${list.size()}` → `${fn:length(list)}`

The `fn` taglib must be declared: `<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>`

**LocalDateTime and fmt:formatDate**: JSTL's `<fmt:formatDate>` expects a `java.util.Date`, not `java.time.LocalDateTime`. Since the DTO uses `LocalDateTime`, dates are displayed using the `LocalDateTime` bean properties directly: `${photographer.createdAt.year}` accesses `LocalDateTime.getYear()` via EL bean introspection. This is the simplest JSTL-compatible approach consistent with how existing admin JSPs handle dates.

---

## Solutions

Both issues resolved without any changes to Java entities, services, or repositories. The fixes were purely in JSP template syntax.

---

## How to Test Manually

1. Start the app: `mvn spring-boot:run`
2. Create a photographer account (register + `/become-photographer`)
3. Log in as ADMIN and approve the photographer at `/admin/photographers`
4. Logout
5. Open `http://localhost:8080/photographers` as a guest
6. Verify the APPROVED photographer appears
7. Click "View Profile" → verify detail page
8. Create a second photographer but do NOT approve
9. Try `http://localhost:8080/photographers/{pending-id}` → should redirect to `/photographers`
