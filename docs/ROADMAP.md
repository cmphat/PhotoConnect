# ROADMAP.md — Roadmap 8 Tuần Cho 1 Người

> Mỗi tuần phải có code chạy được + commit GitHub. Không để dồn cuối kỳ.

---

## Tuần 1 — Foundation + SQL Server

### Mục tiêu
Project chạy ổn và kết nối DB.

### Việc làm
- [ ] Chốt docs
- [ ] SQL Server Developer
- [ ] SSMS
- [ ] SQL Server Authentication
- [ ] PhotoConnectDB
- [ ] Spring datasource
- [ ] Base package structure
- [ ] User entity
- [ ] PhotographerProfile entity skeleton
- [ ] GitHub

### Commit gợi ý
```text
Add project specification documents
Configure SQL Server connection
Create User entity
Create photographer profile entity
```

---

## Tuần 2 — Auth + Security

- [ ] Register
- [ ] Login
- [ ] BCrypt
- [ ] JWT
- [ ] Spring Security
- [ ] Role CUSTOMER/PHOTOGRAPHER/ADMIN
- [ ] Login/register JSP
- [ ] Authorization test

**Kết quả demo:** 3 role login và vào đúng dashboard.

---

## Tuần 3 — Photographer

- [ ] Profile CRUD
- [ ] Category
- [ ] Admin approval
- [ ] Photographer list
- [ ] Photographer detail
- [ ] Cloudinary
- [ ] Portfolio
- [ ] Service package

**Kết quả demo:** photographer được duyệt và xuất hiện công khai.

---

## Tuần 4 — Booking

- [ ] Booking schema
- [ ] Booking create
- [ ] Conflict check
- [ ] Accept/reject
- [ ] Cancel
- [ ] Start/complete
- [ ] Booking status history
- [ ] Customer + photographer booking pages

**Kết quả demo:** booking end-to-end chưa có chat.

---

## Tuần 5 — WebSocket Chat

- [ ] Message entity
- [ ] Message history
- [ ] WebSocket
- [ ] STOMP
- [ ] Chat UI
- [ ] Persist message
- [ ] Booking notifications cơ bản

**Kết quả demo:** 2 account chat realtime.

---

## Tuần 6 — Review + Admin

- [x] Review (TASK-019)
- [x] Average rating (TASK-019)
- [x] Admin dashboard (TASK-020)
- [x] User management (TASK-020)
- [x] Booking management (TASK-020)
- [x] Hide review (TASK-021)

**Kết quả demo:** hoàn chỉnh MVP feature.

---

## Tuần 7 — Integration + Quality

- [x] Validate toàn bộ form (TASK-023)
- [x] Fix authorization (TASK-024)
- [x] Error handling (TASK-022)
- [x] Pagination (TASK-025)
- [x] Responsive code audit and stabilization (TASK-026; human visual verification pending)
- [x] Seed data (TASK-028; development-only, opt-in)
- [x] Test all roles (TASK-024)
- [x] Update docs (TASK-023–028)

Không thêm feature lớn mới nếu core còn bug.

---

## Tuần 8 — Freeze + Demo

- [ ] Feature freeze
- [ ] Fix bug
- [ ] Demo data
- [ ] Screenshots
- [ ] README
- [ ] Report
- [ ] Slide
- [ ] Demo script
- [ ] Tag release

### Tag
```bash
git tag v1.0-demo
git push origin v1.0-demo
```

---

# Quy tắc cắt scope

Nếu trễ:

### Giữ bằng mọi giá
1. Auth
2. Photographer profile
3. Portfolio
4. Service package
5. Booking
6. Admin approval
7. Review
8. GitHub

### Giảm trước
1. Notification nâng cao
2. Search nâng cao
3. UI animation
4. Bonus voucher
5. Deposit
6. AI

### WebSocket
Cố giữ vì là công nghệ trong đề tài.
