# VISUAL-PASS-001: Legacy UI Migration

## Goal
Purge remaining Bootstrap/SaaS UI components across the application and replace them with the custom PhotoConnect editorial design system.

## Phase 1: Booking & Deposit Flow (Completed)
- `booking-form.jsp`
- `booking-success.jsp`
- `deposit.jsp`
- `booking-detail.jsp`
- `photographer-booking-detail.jsp`

## Phase 2: Photographer Dashboards & Status (Completed)
- `photographer-onboarding-status.jsp`
- `photographer-portfolio.jsp`
- `photographer-detail.jsp`
- `bookings.jsp`
- `photographer-bookings.jsp`

## Phase 3: Admin Flow (Completed)
- `admin-photographers.jsp`
- `admin-photographer-detail.jsp`

## Description
Removed all hardcoded Bootstrap classes (cards, rounded corners, pill badges, primary blue buttons) and replaced them with the custom PhotoConnect editorial design system (neutral off-white/black, masonry grids, minimal borders, custom CSS). Standardized all pages to use the `navbar.jsp` fragment and display currency in `VND`. All automated tests passed successfully after the changes.
