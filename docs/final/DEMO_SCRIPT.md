# PhotoConnect Demo Script (8–12 minutes)

Use prepared, non-sensitive demo accounts in separate browser profiles/windows. Prefer seeded records over creating every lifecycle state live. Demo payment transfers no real money.

## 1. Homepage and marketplace (0:00–0:45)

- Open the homepage and introduce PhotoConnect as a server-rendered photography marketplace.
- Select **Explore photographers** and note that only approved, active photographers are public.

## 2. Registration and login overview (0:45–1:15)

- Briefly show registration validation and session login.
- Explain the four experiences: guest, customer, photographer, and admin.
- Use prepared accounts rather than spending time registering live.

## 3. Marketplace search (1:15–2:00)

- Search by name/bio/city and demonstrate one price or experience filter.
- Show database-backed pagination if the full SQL seed is loaded.

## 4. Photographer profile and portfolio (2:00–2:45)

- Open an approved photographer.
- Point out portfolio images, experience, starting price, rating, and visible reviews.
- Avoid a live Cloudinary upload unless credentials and network behavior were rehearsed.

## 5. Customer booking (2:45–3:30)

- As a customer, open **Book Photographer**.
- Submit a future date/time, location, and short note.
- Explain that customer identity and the agreed price are derived/snapshotted by the server.

## 6. Photographer accepts (3:30–4:10)

- Switch to the photographer session.
- Open incoming requests and accept the prepared/new pending booking.
- Mention the protected booking state machine.

## 7. Customer sees `ACCEPTED` (4:10–4:35)

- Return to the customer session and refresh the booking detail.
- Point out status, photographer, schedule, location, agreed price, and deposit action.

## 8. Thirty-percent demo checkout (4:35–5:30)

- Open checkout.
- Read the visible **Demo Payment Environment** disclosure and the statement **No real money will be transferred.**
- Show agreed price, server-calculated 30% deposit, and remaining balance.

## 9. Demo payment and receipt (5:30–6:20)

- Select Demo QR for the most stable deterministic success path.
- Confirm the demo payment, show the `PC-*` reference, result, and printable receipt.
- State again: no gateway, bank API, merchant account, or real transfer is connected.

## 10. Realtime chat (6:20–7:10)

- Open the same booking's chat in customer and photographer sessions.
- Send a short message and show persistence/live delivery.
- If the realtime CDN/connection is unavailable, explain and demonstrate the built-in REST fallback.

## 11. Completion and review (7:10–8:00)

- Use a prepared accepted booking for the photographer to complete, or a seeded completed booking if timing is tight.
- As the owning customer, submit a 1–5 rating and comment once; show the updated public rating.

## 12. Admin dashboard (8:00–8:40)

- Switch to the admin session.
- Show user, photographer, booking, review, and clearly labeled simulated-deposit metrics.

## 13. Photographer approval (8:40–9:20)

- Filter applications to `PENDING`.
- Open a prepared application and approve or reject it.
- Explain that only `APPROVED` profiles become public.

## 14. Review moderation (9:20–10:10)

- Open review moderation and hide a visible review.
- Show that hidden reviews leave the public profile/rating aggregate; unhide to restore it if appropriate.

## Close (10:10–10:30)

- Summarize the stack: Java, Spring Boot/MVC, JSP/JSTL, plain JavaScript, JPA/Hibernate, SQL Server, STOMP/SockJS, and Cloudinary.
- Reiterate that the course constraint is respected: no React, Vue, Angular, Python, SPA, or Node frontend pipeline.
