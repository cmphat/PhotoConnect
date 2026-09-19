# PhotoConnect Documentation

The professional project overview is the repository-root `README.md`.

## Final handoff documents

- `final/ARCHITECTURE.md` — implemented system boundaries and integrations
- `final/FEATURE_MATRIX.md` — factual implementation/automation/manual status
- `final/SETUP_GUIDE.md` — clean Windows setup and run procedure
- `final/DEMO_SCRIPT.md` — 8–12 minute presentation flow
- `final/DEMO_CHECKLIST.md` — preparation and fallback checklist
- `final/project-status.md` — current implementation and verification state
- `ERD.md` — actual JPA/SQL Server model
- `API_CONTRACT.md` — actual MVC, REST fallback, and WebSocket mappings
- `SPEC.md` — frozen final functional scope

## Supporting records

- `development/TASK-001...TASK-033` — chronological implementation/audit notes
- `development/migrations/` — guarded forward-only SQL scripts
- `development/seed/` — optional idempotent/non-destructive demo seed
- `ERROR_CODES.md` and `REALTIME_EVENTS.md` — error/realtime references
- `PLAN_BE.md`, `PLAN_FE.md`, and `ROADMAP.md` — final plan/completion status

Source code wins if an older historical task note describes the state that existed at that task's completion. Final documents reflect the current application.

The allowed course stack is Java/Spring Boot/MVC/Data JPA/SQL Server with JSP/JSTL, HTML, CSS, and plain JavaScript. React, Vue, Angular, Python, SPA migration, and a Node frontend build pipeline are not used.
