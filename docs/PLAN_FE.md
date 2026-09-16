# PLAN_FE.md — Kế Hoạch Frontend JSP/JSTL

## 1. Layout chung

Sitemesh / fragments:

```text
Header
Navbar
Main content
Footer
Toast/alert
```

Bootstrap responsive.

---

## 2. Public pages

### Home
- Hero
- Photographer nổi bật
- Category
- CTA

### Photographer list
- Search
- Location filter
- Category filter
- Price filter
- Pagination

### Photographer detail
- Avatar
- Bio
- Rating
- Category
- Portfolio gallery
- Service packages
- Booking CTA
- Review list

---

## 3. Auth

### Login
- Email
- Password
- Error display

### Register
- Full name
- Email
- Password
- Role CUSTOMER / PHOTOGRAPHER

---

## 4. Customer area

### Dashboard
- Upcoming bookings
- Recent bookings

### Booking create
- Package
- Date/time
- Location
- Note
- Confirmation

### Booking detail
- Status timeline
- Photographer info
- Chat
- Cancel button theo rule
- Review form khi completed

---

## 5. Photographer area

### Profile management
- Bio
- Location
- Experience
- Price from
- Category

### Portfolio management
- Upload
- Preview
- Delete

### Package management
- CRUD package

### Booking management
- Pending list
- Accept/Reject
- Accepted
- In progress
- Completed

### Chat
- Booking conversation

---

## 6. Admin

### Dashboard
Cards:
- Users
- Photographers
- Pending approvals
- Bookings
- Completed bookings

### Photographer approval
- Profile detail
- Approve
- Reject

### User management
- Search
- Lock/unlock

### Booking management
- Filter status

---

## 7. JS modules

```text
assets/js/
├── auth.js
├── photographer-list.js
├── photographer-profile.js
├── portfolio.js
├── booking.js
├── chat.js
└── admin.js
```

---

## 8. UI priority

1. Luồng chạy đúng.
2. Form rõ ràng.
3. Responsive.
4. Error/loading states.
5. Sau cùng mới animation/đẹp nâng cao.

Không dành quá nhiều thời gian CSS trong 4 tuần đầu.

### Implementation status

- [x] Responsive code audit and stabilization for the authoritative JSP surface (TASK-026).
- [x] Professional responsive demo deposit checkout, result states, and print-friendly receipt (TASK-029; core flow manually verified, edge cases automated).
- [ ] Human visual verification across representative desktop and mobile browsers.
