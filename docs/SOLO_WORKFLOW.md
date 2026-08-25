# SOLO_WORKFLOW.md — Quy Trình Làm Đồ Án Một Mình

## 1. Mục tiêu

Project này không có chia task cho nhiều thành viên. Mục tiêu là tránh tự làm rối bằng cách chỉ xử lý **một vertical slice tại một thời điểm**.

---

## 2. Mỗi buổi code

### Trước khi code

```bash
git pull
git status
```

Đọc:
- `ROADMAP.md`
- task đang làm
- contract liên quan

Ví dụ làm booking thì xem:
- `SPEC.md`
- `ERD.md`
- `API_CONTRACT.md`
- `ERROR_CODES.md`

### Trong lúc code

Một task nhỏ:

```text
Entity
→ Repository
→ Service
→ Controller
→ JSP/API
→ Test
```

Không nhảy 5 module một lúc.

### Sau khi code

```bash
git status
git add .
git commit -m "Implement booking creation"
git push
```

Update checkbox docs nếu task xong.

---

## 3. Daily target

Một ngày tốt không cần 10 commit.

Ví dụ:

```text
Commit 1: Create Booking entity
Commit 2: Add booking conflict query
Commit 3: Implement booking creation
```

3 commit thật tốt hơn 20 commit giả.

---

## 4. Weekly checkpoint

Cuối mỗi tuần:

1. Run full project.
2. Test role.
3. Test feature tuần đó.
4. Fix error đỏ IDE.
5. Update README/ROADMAP.
6. Push.
7. Chụp screenshot progress nếu cần báo cáo.

---

## 5. Không làm cùng lúc

Tránh:

```text
Auth chưa xong
+ Cloudinary
+ Booking
+ WebSocket
+ AI
```

Đúng:

```text
Auth xong
→ Photographer
→ Portfolio
→ Booking
→ Chat
→ Review
```

---

## 6. Khi gặp bug

Thứ tự:

1. Đọc error.
2. Xác định layer.
3. Reproduce nhỏ nhất.
4. Fix.
5. Test lại.
6. Commit riêng bugfix.

Commit:

```text
Fix booking status validation
```

---

## 7. Khi thay đổi database

Nếu entity/schema thay đổi:
- update `ERD.md`
- update migration nếu dùng migration
- test dữ liệu cũ
- commit cùng thay đổi liên quan

---

## 8. Khi thay đổi API/status

Phải update:
- `SPEC.md` nếu thay đổi nghiệp vụ
- `API_CONTRACT.md`
- `ERROR_CODES.md`
- `REALTIME_EVENTS.md` nếu realtime

---

## 9. Rule 70/20/10

Trong thời gian còn lại:

- 70% core feature
- 20% test + fix
- 10% UI/bonus

Không đảo thành 70% giao diện.

---

## 10. Definition of Done cá nhân

Không đánh dấu task xong chỉ vì code đã viết.

Done khi:
- compile
- chạy được
- test được
- đúng role
- không lộ secret
- commit/push
- docs khớp
