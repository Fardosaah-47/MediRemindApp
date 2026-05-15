# MediRemind Review Handoff

Last revised: 2026-05-15

## Project Overview

MediRemind is an offline-first Android medication support app built in Kotlin with Jetpack Compose and Room.

The target users are:

- elderly patients
- low-literacy patients
- caregivers who need a simple adherence summary
- households where one phone may be shared by more than one patient

The project focus is:

- local patient profile separation
- medication setup by manual entry or QR scan
- dose scheduling
- daily logging
- refill awareness
- live photo-based dose confirmation
- reminder alarms
- offline caregiver QR sharing

The patient workflow is intentionally designed to work without internet.

## Current Build Status

The current project has a working local flow and is sendable for code review.

Implemented major areas:

- medication CRUD
- schedule CRUD
- Room persistence
- Room v7 migration for `amountPerDose`
- QR medication import flow
- demo QR payloads and PNGs in `docs/demo_qr_codes/`
- dose logging with `Taken`, `Skipped`, `Snoozed`, and `Missed`
- missed-dose rollover logic
- live captured proof-photo flow for `Taken`
- reference photo vs captured photo similarity check
- stock decrement after Taken, using the real amount per dose
- refill reminder notifications
- patient QR report generation
- caregiver QR scan and report reading
- grouped history and grouped schedule display
- multiple local patient profiles
- profile-scoped medications, schedules, and logs
- AlarmManager reminder engine
- sound/vibration/lock-screen notification behavior
- notification tap opens Dose Log
- boot receiver for rescheduling
- adherence streak screen

Remaining work before final submission:

- full real-device testing
- UI readability pass on all screens
- final screenshots and report/user manual writing

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- Coroutines
- ML Kit Code Scanner
- AlarmManager
- Notification channels
- local file storage via `FileProvider`

## Core Data Model

### UserProfile

Stores local patient identity and caregiver details.

Key fields:

- full name
- age
- condition
- caregiver name

### Medication

Stores medicine setup and refill details.

Key fields:

- patient ID
- name
- form
- dosage instructions
- amount per dose
- current stock amount
- stock unit
- refill threshold
- reference image URI
- QR-import flag

### DoseSchedule

Stores schedule timing and treatment period data.

Key fields:

- patient ID
- medication ID
- frequency
- time
- start date
- end date

### DoseLog

Stores actual dose behavior and verification output.

Key fields:

- patient ID
- medication ID
- schedule ID
- status
- scheduled time
- log date
- taken timestamp
- verification image URI

## Current App Flow

## 1. Home Dashboard

Purpose:

- gives the patient a clear starting point
- shows daily progress
- opens the major app sections

Current behavior:

- shows active patient name
- shows today progress
- separates logged, missed, and remaining doses
- opens QR, report, medications, schedule, profile, and dose logging flows

Current limitation:

- UI needs final small-screen readability testing.

## 2. Patient Profiles

Purpose:

- support shared household phone use
- keep patient data separated locally

Current behavior:

- create/update profiles
- switch active patient
- active patient controls medications, schedules, logs, reports, and QR imports

Security note:

- this MVP uses device-level privacy rather than per-profile passwords.
- optional PIN/biometric caregiver mode is future work.

## 3. Medication Setup

Purpose:

- manually add or edit medicine
- support refill tracking
- save reference photo for later verification

Current behavior:

- saves name, form, dosage, frequency, stock, unit, refill threshold, and amount per dose
- amount per dose is used when stock decrements
- QR-imported medicines protect core pharmacy fields unless unlocked

## 4. Medication List

Purpose:

- show saved medicines and remaining stock
- support opening/editing a medicine

Current behavior:

- medicine cards show stock, low-stock state, source, dosage, and form
- refill warnings are tied to stock values

## 5. Dose Schedules

Purpose:

- define dose times and treatment period
- support editing reminder times

Current behavior:

- schedule medications by frequency/time
- group schedule entries by medication
- update stale same-day missed logs when times are edited
- estimate supply/refill timing using amount per dose
- new medication schedules start the following day by default

## 6. Dose Logging

Purpose:

- daily working screen for adherence

Current behavior:

- shows remaining today
- shows logged today
- shows missed/skipped/snoozed states
- supports camera verification before confirming Taken
- cancels the active dose notification when the dose is logged
- decrements stock only once per schedule/date Taken log

### Current Taken Flow

1. user taps Taken/log dose
2. app launches live camera capture
3. app compares captured image with saved reference image
4. app shows match status
5. confirmed Taken creates the log
6. stock decreases by `amountPerDose`
7. low-stock/refill logic is evaluated

## 7. Import By QR

Purpose:

- help low-literacy users load medication details without typing

Current behavior:

- scans supported pharmacy/hospital medication QR payloads
- saves or updates medication data
- creates schedules automatically
- supports `amountPerDose`, `doseAmount`, or `doseQuantity`
- uses simpler wording for elderly users

Demo files:

- `mary_metformin_twice_daily.png`
- `bp_patient_amlodipine_once_daily.png`
- `pharmacy_bundle_two_meds.png`
- `syrup_amoxicillin_three_times_daily.png`
- `caregiver_report_sample.png`

## 8. Patient Report

Purpose:

- let the patient phone generate a caregiver-readable QR summary

Current behavior:

- shows patient summary stats
- shows adherence percentage
- shows medication breakdown
- generates bounded QR content so very large reports do not crash QR generation

## 9. Caregiver Scan

Purpose:

- let a caregiver scan the patient report QR and read the adherence summary

Current behavior:

- scans patient report QR
- parses caregiver summary payload
- shows patient stats and medication breakdown

## 10. Reminder And Refill Notifications

Purpose:

- remind patient at the scheduled dose time
- warn patient/caregiver when stock is low

Current behavior:

- schedules dose reminders with AlarmManager
- notification has sound/vibration/lock-screen behavior
- notification opens Dose Log
- boot receiver reschedules alarms after restart
- low-stock and out-of-stock refill notifications exist

Real-device note:

- Samsung/Xiaomi battery optimization may delay exact alarms unless the app is set to unrestricted battery use.

## What Has Been Refined Recently

Recent work includes:

- added ringing reminder behavior
- added refill reminder scheduler
- added adherence streak screen
- added `amountPerDose` with Room v7 migration
- updated stock decrement and schedule estimates to use amount per dose
- added syrup QR demo for liquid medicine
- regenerated QR demo PNGs
- simplified QR import wording
- lightly aligned Profile and Schedule screens with the warmer app theme
- pushed all changes to GitHub

## Current Strengths

- offline-first architecture is real
- profile-scoped data separation is implemented
- QR import helps low-literacy users avoid typing
- main medication workflow works end to end
- stock/refill logic now handles non-tablet amounts
- reminder engine exists
- caregiver QR flow exists
- photo verification flow exists

## Known Weaknesses / Gaps

### 1. Real-Device Testing Still Matters

Needs testing on physical devices:

- alarms while screen is off
- alarms after reboot
- QR scanning under normal phone camera conditions
- photo matching under different backgrounds/lighting
- stock decrement and refill notification thresholds
- multi-profile switching with scheduled alarms

### 2. UI Is Still The Main Polish Risk

The UI is functional but still needs a final readability pass.

Review for:

- overlapping text
- low contrast
- crowded chips
- bottom navigation spacing
- confusing labels for elderly users

### 3. Documentation Is Not Final

Still needed:

- final screenshots
- user manual steps
- report chapters explaining QR, profiles, reminders, and privacy-by-device-lock
- future work section

## Recommended Next Build Order

### Immediate

- let friend review GitHub repo
- run full phone test using demo QR codes
- fix only concrete bugs or obvious readability problems

### Next

- final UI consistency pass
- final demo script
- screenshots for report

### Later

- release APK
- optional PIN/biometric caregiver mode
- richer charts
- cloud/hospital QR integration

## What To Review In Code First

If reviewing quickly, start with:

- `MainActivity.kt`
- `data/model/Medication.kt`
- `data/local/AppDatabaseProvider.kt`
- `data/repository/QrImportParser.kt`
- `domain/MedicationAlarmScheduler.kt`
- `domain/RefillAlarmScheduler.kt`
- `ui/screen/reminder/`
- `ui/screen/medication/`
- `ui/screen/schedule/`
- `ui/screen/profile/`

## Summary

MediRemind currently offers a real offline patient workflow:

- create/switch patient profile
- add or scan medication
- auto-create schedules
- receive reminders
- log doses
- verify taken doses with live photo
- decrement stock correctly
- receive refill warnings
- review adherence/streaks
- generate caregiver QR summary
- scan caregiver summary on another device

The biggest risk now is not missing core code. It is final real-device testing and UI readability.
