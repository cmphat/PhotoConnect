# TASK-008: Photographer Onboarding

## Concepts Explained

### What Onboarding Means
Onboarding in this context is the process by which an existing PhotoConnect user declares their intention to become a professional photographer on the platform. They fill in a professional profile form and submit it for review.

### Why Onboarding Requires Login
Only an authenticated user can create a photographer profile. The system must know exactly *who* is applying. An unauthenticated request could be from anyone, so we cannot safely attribute the profile to any user.

### Why userId Must Come from the Session
The `userId` used to create the profile must be taken from the server-side `HttpSession`, never from a form field or URL parameter.

If we accepted `userId` from the form, a malicious user could submit `userId=42` in a hidden field and create a photographer profile on behalf of another user (account takeover / unauthorized profile creation). This is called a **Mass Assignment** vulnerability.

By always reading `session.getAttribute("userId")`, we guarantee that:
- The profile is always associated with the currently logged-in user.
- No other user's account can be manipulated.

### Difference Between User.role and PhotographerVerificationStatus
These are two completely separate concepts:

| Concept | Value | Meaning |
|---|---|---|
| `User.role` | `PHOTOGRAPHER` | This account has applied to be a photographer. |
| `verificationStatus` | `PENDING` | The application is awaiting admin review. |

Just because a user has `role=PHOTOGRAPHER` does **not** mean they are publicly approved. They still must pass the `verificationStatus=APPROVED` check before they appear on public listings. This two-tier system allows us to track the application state independently of the account type.

### Why Photographers Start PENDING
No photographer is automatically trusted. An admin must manually review each application to confirm identity, portfolio quality, and legitimacy before setting the status to `APPROVED`. Defaulting to `PENDING` ensures new profiles are invisible to the public until approved.

### Why @Transactional is Used
The onboarding service must perform two database writes atomically:
1. Save the new `PhotographerProfile`.
2. Update `User.role` to `PHOTOGRAPHER`.

Without `@Transactional`, if the profile saved successfully but the user role update failed (e.g., database error), we would have an inconsistent state: a profile exists but the user still appears as a CUSTOMER. `@Transactional` ensures both writes either succeed together or both roll back together.

### What Atomic Database Operation Means
An atomic operation is one that is indivisible — it either completes entirely or not at all. In database terms, this is enforced by a transaction. If any step inside the transaction fails, the entire transaction is rolled back, leaving the database in the state it was before the operation started.

### Why Duplicate Protection Exists at Both Service and DB Levels
- **Service level** (`existsByUserId` check): Provides a clean, user-friendly error message before hitting the database. Avoids exposing raw SQL constraint exceptions to the browser.
- **Database level** (unique constraint on `user_id`): Acts as the final hard safeguard. Even if the service check is somehow bypassed (e.g., a race condition), the database will reject the duplicate insert.

### Controller → Service → Repository Flow
1. `PhotographerOnboardingController` receives the HTTP request and reads `userId` from the session.
2. It calls `PhotographerProfileService.createProfile(userId, profileRequest)`.
3. The service validates business rules (user exists, is ACTIVE, has no profile).
4. The service saves the profile and updates the user role via `PhotographerProfileRepository` and `UserRepository`.
5. On success, the controller updates the session `userRole` to `PHOTOGRAPHER` and redirects to the status page.

## Tests Performed

### PhotographerProfileServiceTest (Unit Tests with Mockito)
- Successful onboarding: profile created, `verificationStatus=PENDING`, role changed to `PHOTOGRAPHER`.
- Duplicate profile: `PhotographerProfileAlreadyExistsException` thrown, no save attempted.
- Missing user: `IllegalArgumentException` thrown.
- Inactive user: `AccountDisabledException` thrown.
- Banned user: `AccountDisabledException` thrown.

### PhotographerOnboardingControllerTest (MockMvc)
- Unauthenticated GET `/become-photographer` → redirect `/login`.
- Authenticated GET, no profile → 200 + form view.
- Authenticated GET, profile exists → redirect status.
- Unauthenticated POST → redirect `/login`.
- Invalid POST (blank fields) → form view returned with errors.
- Valid POST → service called, redirect to status, session `userRole` = `PHOTOGRAPHER`.
- Duplicate → redirect to status.
- Unauthenticated status GET → redirect `/login`.
- Authenticated status GET → 200 + status view + profile model attributes.

## Problems Encountered and Solutions
- Port 8080 was already occupied by the TASK-007 application process when attempting to start a new server. Resolved by killing the running process using `Get-NetTCPConnection | Stop-Process` before restarting.
