# PLAN_BE.md — Kế Hoạch Backend

> Ưu tiên dependency order. Không làm module sau khi module trước chưa ổn.

## Phase 1 — Foundation

- [ ] Spring project compile
- [ ] SQL Server connection
- [ ] Base entity timestamps
- [ ] Global exception handler
- [ ] Common API response
- [ ] Enums
- [ ] Git baseline

## Phase 2 — Authentication

- [ ] User entity
- [ ] UserRepository
- [ ] Register
- [ ] BCrypt
- [ ] Login
- [ ] JWT service
- [ ] JWT filter
- [ ] Spring Security config
- [ ] Role authorization
- [ ] Current user helper

## Phase 3 — Photographer

- [ ] PhotographerProfile entity
- [ ] Category
- [ ] PhotographerCategory
- [ ] Admin approve/reject
- [ ] Public photographer list
- [ ] Search/filter query
- [ ] Photographer detail

## Phase 4 — Portfolio + Package

- [ ] Cloudinary config
- [ ] Portfolio upload
- [ ] Portfolio delete
- [ ] ServicePackage CRUD
- [ ] Ownership validation

## Phase 5 — Booking

- [ ] Booking entity
- [ ] BookingStatusHistory
- [ ] Create booking
- [ ] Calculate endTime
- [ ] Time-conflict query
- [ ] Accept
- [ ] Reject
- [ ] Cancel
- [ ] Start
- [ ] Complete
- [ ] Status transition validator

## Phase 6 — Chat

- [ ] Message entity
- [ ] Message history API
- [ ] WebSocket config
- [ ] Authentication handshake
- [ ] Send message
- [ ] Persist message
- [ ] Publish realtime
- [ ] Mark read

## Phase 7 — Review

- [ ] Review entity
- [ ] Validate completed booking
- [ ] Unique booking review
- [ ] Average rating update

## Phase 8 — Admin

- [ ] Admin dashboard stats
- [ ] User lock/unlock
- [ ] Photographer approval
- [ ] Booking list
- [ ] Hide review

## Phase 9 — Hardening

- [ ] Validation
- [ ] Error codes
- [ ] Transactions
- [ ] Authorization tests
- [ ] Search pagination
- [ ] Seed/demo data
