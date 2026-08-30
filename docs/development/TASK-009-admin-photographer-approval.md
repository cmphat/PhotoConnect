# TASK-009: Admin Photographer Approval

## Concepts Explained

### Why Photographer Applications Require Admin Approval
PhotoConnect connects clients with professional photographers. To protect clients, an admin must verify each applicant's legitimacy (real identity, professional quality, appropriate pricing) before they appear publicly. Automatic approval would allow anyone to appear as a vetted photographer without scrutiny.

### Difference Between Authentication and Authorization
- **Authentication** = "Who are you?" — verifying the user's identity (login/session).
- **Authorization** = "What are you allowed to do?" — verifying that the identified user has the correct *role* to perform an action.

In this task, any authenticated session may be present, but only `ADMIN`-role sessions may access the admin endpoints.

### What ADMIN Role Authorization Means
Authorization is checked by reading `userRole` from the server-side `HttpSession`. If the role is not `ADMIN`, the request is denied before any business logic runs. This means:
- Unauthenticated users → redirect `/login`
- Non-ADMIN authenticated users → redirect `/` (denied)
- ADMIN users → allowed through

### Why Role Must Come from Session/Server State
The `userRole` used for authorization comes only from `session.getAttribute("userRole")`, which was set at login time from the database. It must never be read from a form field, URL parameter, or cookie value controlled by the client, because a malicious user could forge those values and escalate their own privileges.

### Why Approve/Reject Use POST
GET requests are idempotent — a browser, search engine bot, or link preview can trigger them. If approval/rejection were GET links, anyone who could make the admin click a link (phishing) could trigger accidental approvals. Using POST forms with confirmation prompts ensures the action is intentional and difficult to forge via simple links.

### Why PHOTOGRAPHER Role Stays After Rejection
When a photographer application is rejected, `PhotographerProfile.verificationStatus` changes to `REJECTED`, but `User.role` remains `PHOTOGRAPHER`. This is intentional:
- **Account role** (`PHOTOGRAPHER`) describes what kind of account this is.
- **Verification status** (`REJECTED`) describes whether their professional profile has been approved for public listing.

Reverting the role would destroy information (e.g., the fact that they ever applied) and create confusion if they reapply. Future logic can check `verificationStatus != APPROVED` to prevent them from appearing in public listings.

### What a Status Transition Is
A status transition is a change from one state to another. In this task:
- `PENDING → APPROVED` (allowed)
- `PENDING → REJECTED` (allowed)

Any other transitions (e.g., `APPROVED → PENDING`, `REJECTED → APPROVED`) are blocked by `InvalidStatusTransitionException`.

### Why Arbitrary Transitions Are Prevented
If any status could change to any other at any time, the system becomes unpredictable. An admin could accidentally re-approve a previously rejected applicant by clicking the wrong button. Restricting transitions ensures that each state has a clear, predictable lifecycle and prevents mistakes.

### Controller → Service → Repository Flow
1. `AdminPhotographerController` receives the request, checks admin authorization from session.
2. Calls `AdminPhotographerService.approve(id)` or `reject(id)`.
3. Service loads the profile, checks that it is in PENDING status, updates the status, and saves via `PhotographerProfileRepository`.
4. Controller redirects back to the detail page with a flash success/error message.

## Tests Performed

### AdminPhotographerServiceTest (6 unit tests)
- `listPending_shouldReturnOnlyPendingProfiles`
- `approve_pendingProfile_shouldTransitionToApproved`
- `reject_pendingProfile_shouldTransitionToRejected` (User.role stays PHOTOGRAPHER)
- `approve_alreadyApprovedProfile_shouldThrowInvalidStatusTransitionException`
- `reject_alreadyApprovedProfile_shouldThrowInvalidStatusTransitionException`
- `getApplicationById_missingProfile_shouldThrowIllegalArgumentException`

### AdminPhotographerControllerTest (12 MockMvc tests)
- Unauthenticated GET → redirect `/login`
- CUSTOMER GET → redirect `/`
- PHOTOGRAPHER GET → redirect `/`
- ADMIN GET → 200, list view
- ADMIN GET with pending items → model contains applications
- ADMIN GET detail → 200, detail view
- Non-admin GET detail → redirect `/`
- Non-admin POST approve → redirect `/`, service not called
- ADMIN POST approve → service called, redirect to detail
- ADMIN POST approve with invalid transition → redirect to detail (no crash)
- Non-admin POST reject → redirect `/`, service not called
- ADMIN POST reject → service called, redirect to detail

## Problems Encountered and Solutions
- `UserServiceTest.cleanup()` was calling `userRepository.deleteAll()` which failed with a FK constraint because `photographer_profiles.user_id` references `users.id`. Fixed by injecting `PhotographerProfileRepository` into the test and deleting all profiles before deleting users.
- Port 8080 still occupied by the previous task's devtools-restarted process; stopped it before starting the new server.

## Bug Fix: Hibernate Lazy-Loading Proxy Error (HTTP 500 on admin pages)

### Observed Error
```
GET /admin/photographers → HTTP 500
Error reading [email] on type [com.photoconnect.entity.User$HibernateProxy...]
```

### Root Cause
`PhotographerProfile` has a `@OneToOne(fetch = FetchType.LAZY)` association to `User`.

When `open-in-view=false`, the Hibernate session (and therefore the database connection) is closed once the service method returns. The controller passed the returned `PhotographerProfile` list directly to the JSP. The JSP then evaluated `${app.user.email}`, which attempted to load the `User` through a Hibernate proxy — but the session was already closed, causing `LazyInitializationException`.

### Why open-in-view=true Was NOT Used
`open-in-view=true` keeps the Hibernate session open for the entire HTTP request lifecycle, including JSP rendering. This hides the problem but creates serious side effects:
- Unpredictable session and connection lifetimes.
- Hidden N+1 query problems that are impossible to detect at compile/test time.
- Database connections held during slow view rendering.

This project uses `open-in-view=false` intentionally as a best practice.

### Why Changing to EAGER Was Avoided
`FetchType.EAGER` would force the `User` to be loaded on **every** `PhotographerProfile` query in the entire application, including the onboarding status check and repository integration tests. This is wasteful and can itself introduce N+1 problems in collections.

### Fix Applied: JPQL JOIN FETCH in Repository + @Transactional(readOnly=true) on Service
Two new `@Query` methods were added to `PhotographerProfileRepository`:

```java
@Query("""
        SELECT p FROM PhotographerProfile p
        JOIN FETCH p.user
        WHERE p.verificationStatus = :status
        ORDER BY p.createdAt ASC
        """)
List<PhotographerProfile> findByVerificationStatusWithUser(@Param("status") PhotographerVerificationStatus status);

@Query("""
        SELECT p FROM PhotographerProfile p
        JOIN FETCH p.user
        WHERE p.id = :id
        """)
Optional<PhotographerProfile> findByIdWithUser(@Param("id") Long id);
```

These issue a single SQL JOIN that fetches both `photographer_profiles` and `users` rows in one query — no proxy, no second SQL statement.

`listPendingApplications()` and `getApplicationById()` in `AdminPhotographerServiceImpl` were annotated with `@Transactional(readOnly=true)` and switched to the new methods. This ensures the `User` is fully initialized inside the transaction boundary before the Hibernate session closes.

`approve()` and `reject()` intentionally continue using plain `findById()` because they only need `verificationStatus` for their mutation logic — the `User` fields are never accessed.

### SQL for Development Admin Account
```sql
UPDATE dbo.users SET role = 'ADMIN' WHERE email = 'your-dev-email@example.com';
```

