# PhotoConnect V5 — Four-role demo script

This script is for an isolated demo environment only. It must not target the shared development or production database. The current repository has a known clean-install blocker at V013; complete the approved migration recovery before attempting this script.

## Prerequisites

1. A disposable SQL Server database that successfully migrates through V027 and passes Hibernate `validate`.
2. A strong, random Base64 `JWT_SECRET`; no default is accepted.
3. Payment simulation enabled. Do not describe the deposit/refund as a real bank transaction.
4. Test-only SMTP capture and Cloudinary credentials if those integrations are demonstrated.
5. Four ordinary demo accounts with unique emails. Create Customer and Photographer through normal registration/onboarding. Create the future Manager initially as an active Customer; never seed or publish a Manager password.
6. Clear browser profiles or four separate private browser contexts.

## Persona setup

| Persona | Initial role | Creation path |
|---|---|---|
| Customer | CUSTOMER | Public registration + OTP |
| Photographer | PHOTOGRAPHER | Public registration then photographer onboarding |
| Manager candidate | CUSTOMER | Public registration + OTP, later promoted by Admin |
| Admin | ADMIN | Existing authorized isolated-environment bootstrap procedure |

Before the demo, sign in as Admin, open `/admin/users`, locate the active Manager candidate and use the explicit Manager grant action. Sign out, then sign in as that account and confirm `/manager/dashboard` is available while `/admin/users` returns access denial/redirect. Do not edit roles directly in the database during the demonstration.

## Workflow A — Photographer approval

1. Photographer completes onboarding and submits an application.
2. Manager opens **Duyệt nhiếp ảnh gia**, filters `PENDING`, and opens the application.
3. Show only permitted application data. Approve, or reject with a reason and resubmit before approving.
4. Confirm reviewer identity/time and final status in the database or Admin view.
5. Confirm the photographer receives a moderation notification.
6. Confirm an operational audit entry records actor, role, action, target, outcome and safe summary.
7. Repeat the action and show duplicate decision rejection.

## Workflow B — Report moderation

1. Customer submits a report associated with an owned booking/content path.
2. Manager opens **Báo cáo vi phạm** and sees the `PENDING` item.
3. Manager chooses **Nhận xử lý**, adds investigation notes, and confirms status `INVESTIGATING` plus assignment.
4. A different Manager account, if available, attempts resolution and is denied.
5. Assigned Manager selects only `RESOLVED` or `DISMISSED`, records a reason, and submits.
6. Confirm the reporter notification, persisted reason/timestamps and audit entry.
7. Confirm the reporter cannot alter internal notes or outcome.

## Workflow C — Booking dispute

1. Prepare a booking in the valid `DISPUTED` state with a simulated paid deposit.
2. Manager opens **Tranh chấp** and reviews operational metadata. Point out that chat and private photos are absent.
3. Select the documented outcome:
   - `REFUND_CUSTOMER` → booking `CANCELLED`, simulated deposit `REFUNDED`; or
   - `PAY_PHOTOGRAPHER` → booking `COMPLETED`, simulated deposit remains/returns `PAID`.
4. Confirm booking history contains the state-machine transition and actor.
5. Confirm both parties receive notifications and audit contains one successful decision.
6. Attempt a second resolution and show it is rejected because the booking is no longer `DISPUTED`.
7. For acceptance evidence, run two concurrent decisions against the isolated database and confirm exactly one commits; this step is currently BLOCKED until DB-01 is fixed.

## Workflow D — Manager role lifecycle

1. Admin grants Manager to the active Customer candidate.
2. Candidate signs in and sees Manager navigation/dashboard.
3. Verify Manager can operate approval/report/dispute/support queues.
4. Verify Manager cannot:
   - open `/admin/users`;
   - grant a role;
   - view another user's private delivery photo;
   - subscribe to another user's notifications or unrelated booking chat.
5. Admin revokes Manager.
6. Reuse the previously issued Manager cookie and request `/manager/dashboard`; confirm authoritative role revalidation denies access.

## Workflow E — Marketplace end-to-end

1. Customer searches/filter photographers and opens an approved public profile.
2. Select a service package and create a booking. Record the package-name/price/duration snapshot.
3. Photographer accepts; confirm availability is rechecked.
4. Customer completes the simulated deposit; confirm deposit `PAID` and booking `CONFIRMED` are consistent.
5. Customer and Photographer exchange participant-only chat messages.
6. Photographer advances the shoot, uploads authenticated delivery photos and delivers the album.
7. Customer opens signed preview/download URLs, requests one revision, receives redelivery and accepts.
8. Customer submits one review; a second review is rejected.
9. Verify unrelated users and Manager cannot access delivery media or chat.

## Support workflow

1. Customer or Photographer creates a support ticket and sees it in **Trung tâm hỗ trợ**.
2. Another ordinary user tries the ticket ID and is denied.
3. Manager assigns the ticket to self, replies and resolves it.
4. Requester sees history/status and reopens with a reason.
5. Confirm the Manager queue and audit log reflect the change.

## Responsive and visual checklist

For Customer, Photographer, Manager and Admin workspaces, capture evidence at 375, 768, 1024 and 1440 px:

- Midnight Blue theme and SiteMesh chrome remain intact;
- mobile navigation is usable;
- tables scroll or reflow without clipping actions;
- filter and pagination controls work;
- empty states and success/error messages render correctly;
- no raw HTML executes from profile/report/ticket/message input.

## Acceptance record

Record database name, migration version, commit, browser/version, test account IDs (not passwords), timestamps and screenshot paths. Mark any step not actually run as **BLOCKED** or **NOT EXECUTED**. Never convert a mocked or source-reviewed result into browser evidence.
