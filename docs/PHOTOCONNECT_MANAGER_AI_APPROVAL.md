# PhotoConnect — Manager AI application-review helper

## Scope and access

- Location: `/manager/photographers/{id}` for **PENDING** professional profiles.
- Button: `Phân tích hồ sơ`; POST to `/manager/photographers/{id}/ai-review`.
- Auth: existing Manager role/session/JWT checks plus the database-backed `PHOTOGRAPHER_APPLICATION_REVIEW` permission in `ManagerOperationsService`.
- AI never calls approve/reject methods. The Manager can still approve or reject with the existing forms; their decisions remain audited.
- Only detects completeness of provided professional information. It does **not** verify the applicant's identity, professional competence, public reputation or any protected personal attributes.

## Signals and limitations

Five checklist items: city, specialties, bio length (at least 50 characters), declared experience, equipment summary. A completed field means *declared*, not *verified*. The UI always highlights manual verification.

**Important:** `PhotographerPortfolioService` allows uploading portfolio images only for APPROVED profiles. Therefore lack of pre-approval portfolio evidence must **not** negatively affect a PENDING applicant.

No email address, real name, phone, links, bio text, photos or other raw applicant content is sent to the AI provider. Only booleans and aggregate counts are sent in the on-demand request. Avoid making automated acceptance/rejection decisions based on this output.

## Enabling real AI

At runtime, configure the server-only environment variables:

```bash
OPENAI_API_KEY=your-provider-key
OPENAI_REVIEW_MODEL=gpt-4.1-mini
```

The application calls OpenAI's Responses API over HTTPS with `store=false`, a 10-second request timeout and a limited token budget. API errors are not exposed to the browser. When the key is absent or the provider fails, the checklist still works and explicitly indicates that the response is **not** an AI judgment. Never put API keys in JSP, frontend JavaScript, Git, or application.properties.

The AI request is triggered only on explicit Manager POST; no automatic page-load calls. Repeated clicks may generate billable API requests, so rate limiting and request auditing are recommended before production rollout.

## Verification

New unit/controller tests cover Manager-only access, no-key fallback, incomplete profiles, and the non-PENDING guard. Full project test suite and live API calls must be verified in the deployment environment before release.
