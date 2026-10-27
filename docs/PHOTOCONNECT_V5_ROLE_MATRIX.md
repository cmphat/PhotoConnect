# PhotoConnect V5 — Role and permission matrix

Source of truth: `PlatformPermission`, `RolePermissionPolicy`, route enforcement in `JwtAuthenticationFilter`, and resource ownership checks in services. Navigation visibility is not treated as authorization.

Legend: **Allow** = authorized subject to resource/state validation; **Own** = only owned/participating resources; **Deny** = no permission.

| Action/resource | Guest | Customer | Photographer | Manager | Admin |
|---|---:|---:|---:|---:|---:|
| Browse marketplace/public profiles | Allow | Allow | Allow | Allow | Allow |
| Register/login/OTP/OAuth | Allow | Allow | Allow | Allow | Allow |
| Create/manage booking | Deny | Own | Participating | Operational view only | Operational view |
| Manage packages/portfolio/calendar | Deny | Deny | Own | Deny | Administrative visibility only |
| Participant chat/private STOMP booking topic | Deny | Own | Own | Deny unless participant | Allow by existing Admin policy |
| Per-user notification topic | Deny | Own | Own | Own only | Allow by existing Admin policy |
| Private delivery gallery | Deny | Own | Assigned booking | **Deny** | Allow by existing delivery policy |
| Submit/view support ticket | Deny | Own | Own | Deny as requester | Deny as requester |
| Manage support tickets | Deny | Deny | Deny | Assigned/open queue | Operational override via permission policy |
| Review photographer applications | Deny | Deny | Deny | Allow | Allow |
| Assign/investigate user report | Deny | Deny | Deny | Allow | Allow through Admin moderation path |
| Resolve/dismiss report | Deny | Deny | Deny | Assigned `INVESTIGATING` only | `PENDING` or `INVESTIGATING` |
| View booking operational metadata | Deny | Own | Own | Allow | Allow |
| Resolve dispute | Deny | Deny | Deny | `DISPUTED -> COMPLETED|CANCELLED` only | Same domain operation |
| View operational analytics | Deny | Deny | Deny | Allow | Allow |
| View operational audit log | Deny | Deny | Deny | Allow/read-only | Allow/read-only |
| Grant/revoke MANAGER | Deny | Deny | Deny | **Deny** | Allow for eligible accounts |
| Change arbitrary roles | Deny | Deny | Deny | **Deny** | Not exposed beyond explicit Manager grant/revoke |
| Change secrets/security configuration | Deny | Deny | Deny | **Deny** | No web endpoint |
| Delete/edit audit records | Deny | Deny | Deny | **Deny** | **Deny** through ordinary application endpoints |

## Permission constants

| Permission | Manager | Admin | Enforcement |
|---|---:|---:|---|
| `PHOTOGRAPHER_APPLICATION_REVIEW` | Yes | Yes | `ManagerOperationsServiceImpl` |
| `USER_REPORT_REVIEW` | Yes | Yes | Manager report methods; legacy Admin resolver separately requires active Admin |
| `BOOKING_OPERATIONAL_VIEW` | Yes | Yes | Manager booking list/detail |
| `BOOKING_DISPUTE_RESOLVE` | Yes | Yes | Admin booking service + booking state machine |
| `SUPPORT_TICKET_MANAGE` | Yes | Yes | `SupportTicketServiceImpl` |
| `OPERATIONAL_ANALYTICS_VIEW` | Yes | Yes | Manager dashboard |
| `AUDIT_LOG_VIEW` | Yes | Yes | Manager/Admin audit controllers |
| `ACCOUNT_ROLE_MANAGE` | **No** | Yes | `AdminUserServiceImpl` requires authoritative active Admin |
| `SYSTEM_SETTINGS_MANAGE` | **No** | Yes | Matrix only; no settings mutation endpoint exists |

## Security invariants

- `/manager/**` requires exactly the current authoritative `MANAGER` role; `/admin/**` requires `ADMIN`.
- JWT role/status is rechecked against the database on each protected request. Session identity is refreshed only from a valid matching JWT.
- Public registration DTO contains no role property; Google onboarding accepts only Customer/Photographer choices.
- Manager grant accepts only an active Customer. Revocation returns Manager to Customer. Photographer identity is not overwritten.
- A Manager cannot appoint another Manager because the service reloads and validates the acting Admin.
- Manager booking access intentionally excludes chat content and private delivery images.
- Support requester reads are ownership scoped; Manager replies/resolution require current assignment.
- Sensitive decisions validate current domain state to prevent duplicate approval, report resolution and dispute resolution.
