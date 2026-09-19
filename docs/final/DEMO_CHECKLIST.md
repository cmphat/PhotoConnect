# PhotoConnect Demo Checklist

Do not write credentials into this file, slides, terminal history, or screenshots. Use private environment values and prepared local browser sessions.

## One day before

- [ ] Confirm the checked-out revision/branch is the intended review state and the worktree contains only expected changes.
- [ ] Run `mvn test`; require zero failures and zero errors.
- [ ] Stop PhotoConnect, then run `mvn clean package`; confirm `target/photoconnect.war` exists.
- [ ] Confirm SQL Server is running and `DB_URL` points to the intended `PhotoConnect` database.
- [ ] Confirm required schema changes exist (`reviews.status`, `deposits.payment_method`, `deposits.failure_reason`).
- [ ] Confirm the optional demo dataset is available. Do not delete unrelated development data to force a row count.
- [ ] If portfolio upload is in the presentation, confirm all Cloudinary variables and a small supported test image.
- [ ] Rehearse the full 8–12 minute script and note the specific record IDs/routes to use.
- [ ] Prepare separate customer, photographer, and admin browser profiles/windows.
- [ ] Confirm the selected customer owns the selected bookings.
- [ ] Prepare one `PENDING`, one `ACCEPTED`, and one `COMPLETED` booking as needed.
- [ ] Prepare one pending photographer application and one visible review for moderation.

## Immediately before

- [ ] Verify port 8080 is available before startup.
- [ ] Start the application and wait for successful Spring Boot/Tomcat startup.
- [ ] Open `/`, `/photographers`, and one photographer detail page.
- [ ] Log in to each role session and confirm navigation/authorization.
- [ ] Confirm the customer can open the prepared `ACCEPTED` booking.
- [ ] Confirm checkout visibly says **Demo Payment Environment** and **No real money will be transferred.**
- [ ] Confirm the amount equals 30% of the agreed price and Demo QR reaches the result/receipt flow.
- [ ] Confirm chat loads for both booking participants and a message can be persisted.
- [ ] Confirm admin dashboard, pending photographer list, and review moderation load.
- [ ] Close unrelated applications, notifications, tabs, and terminals that could expose private data.

## Stable presentation choices

- Prefer Demo QR for payment success; it is local and deterministic.
- Prefer seeded portfolio URLs; do not depend on a live upload unless Cloudinary was just verified.
- Keep prepared lifecycle records so the demo does not depend on creating every state in sequence.
- Keep customer and photographer sessions in different browser profiles to avoid session replacement.
- Use context-path-aware application links rather than typing guessed deep paths.

## Recovery and fallback

- **Application will not start:** read the first root-cause exception; verify SQL Server, `DB_*`, schema, and port 8080. Do not change credentials on stage.
- **Port is occupied:** stop the known PhotoConnect process. Do not run `mvn clean` while it is active.
- **Cloudinary unavailable:** skip upload/delete and demonstrate the existing public portfolio plus the documented metadata architecture.
- **WebSocket/CDN unavailable:** the chat page automatically switches to REST send/poll fallback; explain this resilience and continue.
- **New booking data is unsuitable:** use the prepared seeded `PENDING` or `ACCEPTED` booking.
- **Payment was already completed:** use its result/receipt as an idempotency demonstration or choose another accepted booking.
- **Demo card validation fails:** use Demo QR; never enter a real card.
- **Pending application/review is missing:** describe the guarded admin screen using existing records; do not modify production-like data simply to manufacture the state.
- **Network unavailable:** all core data and demo payment remain local; omit Cloudinary upload and realtime CDN-dependent behavior, then show REST chat fallback/persisted history.

## After the demo

- [ ] Stop the application cleanly.
- [ ] Confirm no credentials, generated WAR, logs, or local configuration were added to source control.
- [ ] Record which manual scenarios passed and which were skipped; do not convert skipped checks into passes.
