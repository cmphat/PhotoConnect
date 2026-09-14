# TASK-022 Development Notes: Global Exception Handling & Error Code Standardization

## Overview

TASK-022 initiates **Tuần 7 — Integration + Quality** from `docs/ROADMAP.md` and **Phase 9 — Hardening** from `docs/PLAN_BE.md` by completing **Global Exception Handling & Error Code Standardization**:
1. **Error Code Standardization**: Implements the standardized error code taxonomy (`DOMAIN_XXX_DESCRIPTION`) defined in `docs/ERROR_CODES.md` across 10 functional domains (`AUTH`, `USER`, `PHOTOGRAPHER`, `PORTFOLIO`, `PACKAGE`, `BOOKING`, `REVIEW`, `CHAT`, `ADMIN`, `SYSTEM`) as a type-safe `ErrorCode` enum with HTTP status and default message bindings.
2. **Unified API Contract Envelope**: Implements `ApiResponse<T>` according to `docs/API_CONTRACT.md` Section 1, standardizing JSON responses for both success (`success: true, data: ..., message: ...`) and error (`success: false, errorCode: ..., message: ...`, optional `errors` field map).
3. **Centralized Controller Advice**: Implements `@ControllerAdvice` `GlobalExceptionHandler` with intelligent dual-mode content negotiation:
   - REST/API invocations (`/api/**` or `Accept: application/json`) receive standardized `ApiResponse<T>` JSON envelopes.
   - Browser web requests (HTML) render the branded editorial `error.jsp` view with status codes, explanatory messages, error code badges, and safe navigation actions.
4. **Container-Level Error Interception**: Replaces Spring Boot's raw default Whitelabel Error Page and Tomcat stack traces with `AppErrorController` (`/error`), catching unmapped routes (404), unauthenticated accesses (401/403), and unexpected runtime crashes (500).
5. **Domain Exception Normalization**: Updates all application domain exceptions to extend root `PhotoConnectException` and bind to their authoritative `ErrorCode`, ensuring zero unmapped or raw exceptions leak to clients.
6. **API Delegation**: Refactored `ChatApiController` to use the `ApiResponse<T>` envelope and delegate exception handling directly to `GlobalExceptionHandler`.

---

## 1. Architecture & Component Design

```
                                  Client Request
                                        │
                                        ▼
                                DispatcherServlet
                                        │
                    ┌───────────────────┴───────────────────┐
                    ▼                                       ▼
             API Endpoints                           MVC Endpoints
             (`/api/**`, JSON)                       (Browser HTML)
                    │                                       │
                    ▼                                       ▼
            ChatApiController                      HomeController, etc.
                    │                                       │
        ┌───────────┴───────────┐               ┌───────────┴───────────┐
        ▼                       ▼               ▼                       ▼
     Success                 Throws          Success                 Throws
        │                   Exception           │                   Exception
        ▼                       │               ▼                       │
   ApiResponse<T>               │            View/JSP                   │
        │                       │               │                       │
        └────────────────┐      │               └────────────────┐      │
                         │      ▼                                │      ▼
                         │   GlobalExceptionHandler              │   GlobalExceptionHandler
                         │   (Dual-Mode Content Negotiation)     │   (Dual-Mode Content Negotiation)
                         │      │                                │      │
                         ▼      ▼                                ▼      ▼
                     ResponseEntity<ApiResponse<?>>               ModelAndView("error")
                     [JSON: success, errorCode, msg]              [error.jsp: status, badge, actions]
```

### Routes & Endpoints

| HTTP Method | Route | Component | Response Format | Description |
|---|---|---|---|---|
| `*` | `/error` | `AppErrorController` | HTML (`error.jsp`) or JSON (`ApiResponse`) | Container error dispatcher handling 404, 403, 400, 500 |
| `GET` | `/api/bookings/{id}/messages` | `ChatApiController` | `ApiResponse<List<ChatMessageDto>>` | Standardized chat history retrieval |
| `POST` | `/api/bookings/{id}/messages` | `ChatApiController` | `ApiResponse<ChatMessageDto>` | Standardized chat message creation |

---

## 2. Standardized Error Code Taxonomy

All 39 error codes from `docs/ERROR_CODES.md` are encapsulated in `ErrorCode`:

| Domain | Error Codes | HTTP Status |
|---|---|---|
| **AUTH** | `AUTH_001_INVALID_CREDENTIALS`, `AUTH_002_EMAIL_ALREADY_EXISTS`, `AUTH_003_TOKEN_INVALID`, `AUTH_004_TOKEN_EXPIRED`, `AUTH_005_ACCESS_DENIED` | 401, 409, 403 |
| **USER** | `USER_001_NOT_FOUND`, `USER_002_ACCOUNT_LOCKED`, `USER_003_INVALID_ROLE` | 404, 403, 400 |
| **PHOTOGRAPHER** | `PHOTOGRAPHER_001_NOT_FOUND`, `PHOTOGRAPHER_002_NOT_APPROVED`, `PHOTOGRAPHER_003_PROFILE_EXISTS`, `PHOTOGRAPHER_004_NOT_OWNER` | 404, 403, 409 |
| **PORTFOLIO** | `PORTFOLIO_001_FILE_REQUIRED`, `PORTFOLIO_002_INVALID_FILE_TYPE`, `PORTFOLIO_003_UPLOAD_FAILED`, `PORTFOLIO_004_NOT_FOUND` | 400, 500, 404 |
| **PACKAGE** | `PACKAGE_001_NOT_FOUND`, `PACKAGE_002_INACTIVE`, `PACKAGE_003_INVALID_PRICE` | 404, 400 |
| **BOOKING** | `BOOKING_001_NOT_FOUND`, `BOOKING_002_TIME_CONFLICT`, `BOOKING_003_INVALID_STATUS`, `BOOKING_004_PAST_TIME`, `BOOKING_005_NOT_OWNER`, `BOOKING_006_CANNOT_CANCEL`, `BOOKING_007_SELF_BOOKING` | 404, 409, 400, 403 |
| **REVIEW** | `REVIEW_001_BOOKING_NOT_COMPLETED`, `REVIEW_002_ALREADY_EXISTS`, `REVIEW_003_INVALID_RATING`, `REVIEW_004_NOT_OWNER` | 400, 409, 403 |
| **CHAT** | `CHAT_001_BOOKING_REQUIRED`, `CHAT_002_ACCESS_DENIED`, `CHAT_003_MESSAGE_EMPTY`, `CHAT_004_MESSAGE_TOO_LONG` | 400, 403 |
| **ADMIN** | `ADMIN_001_CANNOT_MODIFY_SELF`, `ADMIN_002_INVALID_APPROVAL_STATUS` | 400 |
| **SYSTEM** | `SYSTEM_001_DATABASE_ERROR`, `SYSTEM_002_INTERNAL_ERROR`, `SYSTEM_003_VALIDATION_ERROR` | 500, 400 |

---

## 3. Business & Security Rules

1. **Stack Trace Information Leak Prevention**: Unhandled system exceptions log the full stack trace server-side with SLF4J, while returning only a sanitized error title, user-friendly message, and `SYSTEM_002_INTERNAL_ERROR` badge to the client.
2. **Dual-Mode Content Negotiation**: Requests with `Accept: application/json` or starting with `/api/` receive JSON matching `API_CONTRACT.md`. Browser navigation requests receive HTML `error.jsp`.
3. **Structured Validation Error Extraction**: Bean validation (`MethodArgumentNotValidException`, `BindException`, `ConstraintViolationException`) extracts field errors into a structured `errors` map without throwing generic 500s.
4. **Preservation of Existing Controller Error Handling**: Established controller redirect patterns with flash messages (e.g. login failure, review submission validation) remain fully functional; `GlobalExceptionHandler` acts as the safety net for unhandled exceptions.
5. **Brand Continuity**: Container 404 and 500 pages match the dark editorial PhotoConnect aesthetic (`photoconnect.css`, Playfair Display, warm gold highlights), eliminating default white screens.

---

## 4. Database Changes

No database schema changes or migrations are required for TASK-022.

---

## 5. Files Created & Modified

### Created Files
- `src/main/java/com/photoconnect/exception/ErrorCode.java`: Standardized error code enum.
- `src/main/java/com/photoconnect/dto/ApiResponse.java`: Standard JSON API response envelope.
- `src/main/java/com/photoconnect/exception/PhotoConnectException.java`: Root runtime domain exception.
- `src/main/java/com/photoconnect/exception/ResourceNotFoundException.java`: Generic 404 domain exception.
- `src/main/java/com/photoconnect/exception/GlobalExceptionHandler.java`: Centralized `@ControllerAdvice` handler.
- `src/main/java/com/photoconnect/controller/AppErrorController.java`: Custom `/error` error controller.
- `src/main/webapp/WEB-INF/views/error.jsp`: Dark editorial error view.
- `src/test/java/com/photoconnect/exception/ErrorCodeTest.java`: ErrorCode taxonomy tests.
- `src/test/java/com/photoconnect/dto/ApiResponseTest.java`: ApiResponse contract serialization tests.
- `src/test/java/com/photoconnect/exception/GlobalExceptionHandlerTest.java`: Global exception handler dual-mode tests.
- `src/test/java/com/photoconnect/controller/AppErrorControllerTest.java`: Container error controller dispatch tests.
- `docs/development/TASK-022-global-exception-handling.md`: This document.

### Modified Files
- `src/main/java/com/photoconnect/controller/ChatApiController.java`: Refactored to use `ApiResponse` and delegate exceptions to `GlobalExceptionHandler`.
- `src/main/java/com/photoconnect/exception/AccountDisabledException.java`: Extends `PhotoConnectException` (`USER_002_ACCOUNT_LOCKED`).
- `src/main/java/com/photoconnect/exception/BookingNotCompletedException.java`: Extends `PhotoConnectException` (`REVIEW_001_BOOKING_NOT_COMPLETED`).
- `src/main/java/com/photoconnect/exception/ChatAccessDeniedException.java`: Extends `PhotoConnectException` (`CHAT_002_ACCESS_DENIED`).
- `src/main/java/com/photoconnect/exception/EmailAlreadyExistsException.java`: Extends `PhotoConnectException` (`AUTH_002_EMAIL_ALREADY_EXISTS`).
- `src/main/java/com/photoconnect/exception/InvalidBookingException.java`: Extends `PhotoConnectException` (`BOOKING_001_NOT_FOUND`).
- `src/main/java/com/photoconnect/exception/InvalidCredentialsException.java`: Extends `PhotoConnectException` (`AUTH_001_INVALID_CREDENTIALS`).
- `src/main/java/com/photoconnect/exception/InvalidReviewException.java`: Extends `PhotoConnectException` (`REVIEW_003_INVALID_RATING`).
- `src/main/java/com/photoconnect/exception/InvalidStatusTransitionException.java`: Extends `PhotoConnectException` (`BOOKING_003_INVALID_STATUS`).
- `src/main/java/com/photoconnect/exception/PasswordMismatchException.java`: Extends `PhotoConnectException` (`SYSTEM_003_VALIDATION_ERROR`).
- `src/main/java/com/photoconnect/exception/PhotographerProfileAlreadyExistsException.java`: Extends `PhotoConnectException` (`PHOTOGRAPHER_003_PROFILE_EXISTS`).
- `src/main/java/com/photoconnect/exception/PhotographerUnavailableException.java`: Extends `PhotoConnectException` (`BOOKING_002_TIME_CONFLICT`).
- `src/main/java/com/photoconnect/exception/ReviewAccessDeniedException.java`: Extends `PhotoConnectException` (`REVIEW_004_NOT_OWNER`).
- `src/main/java/com/photoconnect/exception/ReviewAlreadyExistsException.java`: Extends `PhotoConnectException` (`REVIEW_002_ALREADY_EXISTS`).
- `src/main/java/com/photoconnect/exception/ReviewNotFoundException.java`: Extends `ResourceNotFoundException`.
- `src/main/java/com/photoconnect/exception/SelfBookingNotAllowedException.java`: Extends `PhotoConnectException` (`BOOKING_007_SELF_BOOKING`).
- `src/main/java/com/photoconnect/exception/UnauthorizedException.java`: Extends `PhotoConnectException` (`AUTH_005_ACCESS_DENIED`).
- `docs/final/project-status.md`: Added TASK-022 summary and status.

---

## 6. Automated Verification

- **Total Test Count**: 336 tests run.
- **Test Results**: 314 passed, 22 skipped (integration tests requiring live SQL Server), 0 failures, 0 errors.
- **New Tests Added (16 tests)**:
  - `ErrorCodeTest`: 3 unit tests verifying code counts, naming conventions, and HTTP status mappings.
  - `ApiResponseTest`: 3 unit tests verifying serialization contract for success, error, and validation errors.
  - `GlobalExceptionHandlerTest`: 6 tests verifying JSON API errors, HTML view rendering, validation errors, illegal arguments, and 500 error sanitization.
  - `AppErrorControllerTest`: 4 tests verifying container error dispatch for 404, 403, and 500 in HTML and JSON modes.
- **Build Verification**: `mvn clean package` produced `photoconnect.war` successfully with 0 compilation or packaging errors.

---

## 7. Manual / Human Verification Checklist

**Human Verification Status**: `PASS` (manually verified in browser).

- [x] **1. Missing Page (404 Not Found)**:
  - Navigated to `http://localhost:8080/abcxyz` and other non-existent paths.
  - Verified that the dark editorial `error.jsp` displays with HTTP status `404`, title "Page Not Found", and error code pill `[BOOKING_001_NOT_FOUND]`.
  - Verified that clicking "Return to Home" routes to `/` and "Explore Marketplace" routes to `/photographers`.
- [x] **2. Resource Not Found Handled Gracefully**:
  - Verified that non-existent or unapproved photographer profiles/resources are handled gracefully without exposing the raw Spring Boot Whitelabel Error Page.
- [x] **3. Role-Based Access Denied & Redirection**:
  - `CUSTOMER` role attempting to access `/admin/dashboard` is blocked and cleanly redirected away.
  - `PHOTOGRAPHER` role attempting to access `/admin/dashboard` is blocked and cleanly redirected away.
  - Unauthenticated guest attempting to access `/admin/dashboard` is redirected to `/login`.
- [x] **4. REST API Missing Endpoint (404 JSON)**:
  - Sent `GET http://localhost:8080/api/unknown-endpoint` with `Accept: application/json`.
  - Verified HTTP status 404 and structured `ApiResponse` JSON body with `errorCode: "BOOKING_001_NOT_FOUND"`.
- [x] **5. Core Application Flow Regression Check**:
  - Verified `/` (Home), `/photographers` (Marketplace), `/login`, `/register`, booking, and review flows operate normally without regressions.
