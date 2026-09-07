# TASK-014: Booking Management and Status Workflow

## 1. Goal
Implement complete booking management for CUSTOMER and PHOTOGRAPHER.
- CUSTOMER: view My Bookings, view detail, see status, cancel eligible bookings.
- PHOTOGRAPHER: view incoming Booking Requests, view detail, accept/reject PENDING bookings, mark ACCEPTED bookings as COMPLETED.

## 2. Existing Architecture
- Users and sessions handle authentication.
- Bookings are created in `BookingService` via `createBooking`.
- `Booking` has a `status` (PENDING, ACCEPTED, REJECTED, CANCELLED, COMPLETED).

## 3. Booking State Machine
Allowed:
- PENDING -> ACCEPTED
- PENDING -> REJECTED
- PENDING -> CANCELLED
- ACCEPTED -> CANCELLED
- ACCEPTED -> COMPLETED

## 4. Authorization Rules
- Customer actions limited to bookings where `booking.customer.id == session.userId`.
- Photographer actions limited to bookings where `booking.photographerProfile.user.id == session.userId`.

## 5. Expected Files
- `BookingViewDto.java`
- `BookingService.java` (extended)
- `BookingServiceImpl.java` (extended)
- `BookingController.java` (extended)
- `PhotographerBookingController.java` (new)
- `bookings.jsp`
- `booking-detail.jsp`
- `photographer-bookings.jsp`
- `photographer-booking-detail.jsp`

## 6. Checklist
- [x] Create BookingViewDto
- [x] Implement BookingRepository queries
- [x] Implement Service methods (Customer: get list, get single, cancel)
- [x] Implement Service methods (Photographer: get list, get single, accept, reject, complete)
- [x] Implement Controller for Customer (`/bookings`, `/bookings/{id}`, `/bookings/{id}/cancel`)
- [x] Implement Controller for Photographer (`/photographer/bookings`, etc.)
- [x] Create Customer JSPs (`bookings.jsp`, `booking-detail.jsp`)
- [x] Create Photographer JSPs (`photographer-bookings.jsp`, `photographer-booking-detail.jsp`)
- [x] Update Navbar with Booking links
- [x] Write Service and Controller Tests
- [x] Run `mvn test` and `mvn package`
- [x] Write documentation

## 7. Result & Status
- **Implementation**: Complete.
- **Manual Verification Status**:
  - `PENDING -> ACCEPTED`: PASS (Verified in browser).
  - Customer booking creation & listing: PASS.
  - Photographer booking request review: PASS.
  - `ACCEPTED -> COMPLETED`: PENDING (User to verify manually).

