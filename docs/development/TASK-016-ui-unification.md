# TASK-016: UI Unification Pass

## Overview
This task unifies the visual styling across PhotoConnect, enforcing an "Editorial photography platform" aesthetic. It removes outdated Bootstrap styles, giant SaaS cards, and inconsistent blue elements in favor of a neutral black/off-white photography-first design.

## Key Changes
1. **Shared Design System**:
   - Consolidated CSS rules into `photoconnect.css`.
   - Removed inline styles from `photographers.jsp`.
2. **Photographer Marketplace (`/photographers`)**:
   - Replaced "Curated Visual Artists" with a modern "Discover Photographers" editorial hero section.
   - Refined filter panel to feel like a seamless editorial filter rather than a SaaS panel.
   - Removed the bottom border of the filter panel.
3. **Onboarding (`/become-photographer`)**:
   - Replaced default Bootstrap structure with the custom `auth-layout` (split 60/40 screen layout).
   - Removed the default Bootstrap navbar and blue primary header.
   - Integrated the global `navbar.jsp`.
4. **Global Navbar (`navbar.jsp`)**:
   - Ensured uniform usage across all main entry points including the onboarding flow.

## Verification
- Validated styling and UI across desktop and mobile.
- Confirmed no business logic or backend workflows (Booking/Deposit) were modified.
- All 175 Maven tests pass.
