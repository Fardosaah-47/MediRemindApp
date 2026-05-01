# MediRemind Review Handoff

## Project Overview

MediRemind is an offline-first Android medication support app built in Kotlin with Jetpack Compose and Room.

The target users are:

- elderly patients
- low-literacy patients
- caregivers who need a simple adherence summary

The project focus is:

- local medication setup
- dose scheduling
- daily logging
- refill awareness
- live photo-based dose confirmation
- offline caregiver QR sharing

The app is intentionally designed to work without requiring internet for the patient workflow.

## Current Build Status

The current project already has a real working local flow and is no longer just UI mockups.

Implemented major areas:

- medication CRUD
- schedule CRUD
- Room persistence
- QR medication import flow
- dose logging with `Taken`, `Skipped`, and `Snoozed`
- missed-dose rollover logic
- live captured proof-photo flow for `Taken`
- reference photo vs captured photo similarity check
- patient QR report generation
- caregiver QR scan and report reading
- grouped history and grouped schedule display
- patient profile save/load
- dashboard-style home screen

Not fully implemented yet:

- real Android reminder engine with `AlarmManager`
- notification channel and reminder receiver
- timed refill notifications
- 24-hour cleanup of verification photos
- release APK / final documentation screenshots

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- Coroutines
- ML Kit Code Scanner
- local file storage via `FileProvider`

## Core Data Model

### UserProfile

Stores the patient identity and caregiver information used in the app and in caregiver reports.

Current key fields:

- full name
- age
- condition
- caregiver name

### Medication

Stores the medicine setup and refill-related details.

Current key fields:

- name
- form
- dosage instructions
- stock amount
- stock unit
- refill threshold
- reference image URI

### DoseSchedule

Stores schedule timing and treatment period data.

Current key fields:

- medication ID
- frequency
- time
- start date
- end date
- active state

### DoseLog

Stores actual dose behavior and verification output.

Current key fields:

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

- gives the patient a clearer starting point
- shows summary counts
- suggests the next best step

Current behavior:

- shows patient name if saved
- shows medication count
- shows schedule count
- shows doses due today
- shows doses logged today
- shows the expected app flow in simple language
- opens the main app sections from one place

Expected user journey:

1. save patient profile
2. add medication
3. set schedule
4. log doses
5. review reports

## 2. Patient Profile

Purpose:

- saves the active local patient identity
- improves caregiver reporting quality

Current behavior:

- create or update one local patient profile
- save patient name, age, condition, and caregiver name
- loads the saved profile back into app state

Current limitation:

- profile model is still simple and should later expand with more health details if needed

## 3. Medication Setup

Purpose:

- manually add or edit a medication
- support refill tracking and verification setup

Current behavior:

- save medication details locally
- allow edit of existing medication
- allow delete with safeguards already being introduced in the app flow
- save a reference photo of the real medication pack or bottle

Why reference photo matters:

- it is used during the `Taken` flow to reduce false logging

## 4. Medication List

Purpose:

- show saved medicines in one place
- support opening a medicine for review or editing

Current behavior:

- lists added medications
- allows entering add flow
- supports edit flow

Current limitation:

- this screen can still be improved visually and navigationally so it feels more like a finished cabinet view

## 5. Dose Schedules

Purpose:

- define how often and when medication should be taken
- support treatment period planning

Current behavior:

- schedule medications with frequency and time
- group schedule entries by medication for cleaner viewing
- archive outdated schedules instead of leaving them all active
- try to sync schedules when medication details are edited or imported
- estimate treatment period and refill timing from stock and schedule data

Current limitation:

- schedule sync rules are still being hardened to avoid accidental duplication or mismatch

## 6. Dose Logging

Purpose:

- act as the daily working screen for adherence

Current behavior:

- shows what is still remaining today
- shows what has already been logged today
- supports `Taken`, `Skipped`, and `Snoozed`
- automatically rolls older overdue doses into missed history
- groups repeated history entries by medication/date so the screen is not too noisy
- uses rolling history ranges instead of awkward calendar-only grouping

### Current Taken Flow

1. user taps `Taken`
2. app launches live camera capture
3. app compares the live captured image with the saved medication reference image
4. app calculates a similarity score
5. confirmation is only allowed when the match looks likely enough
6. the saved verification image is linked to the `DoseLog`

Why this exists:

- to reduce the problem of a patient falsely claiming they took medicine

Current limitation:

- the similarity check is a practical student-project safeguard, not medical-grade recognition

## 7. Import By QR

Purpose:

- help low-literacy users load medication details without typing

Current behavior:

- scans pharmacy or hospital QR input
- parses supported medication payloads
- saves or updates medication data
- can influence schedules based on imported content
- shows import status and raw scan preview for testing

Current limitation:

- QR input testing still needs more real payload trials
- imported-field restrictions and review flow can still be refined further

## 8. Patient Report

Purpose:

- let the patient phone generate a caregiver-readable QR summary

Current behavior:

- shows patient summary stats
- shows adherence percentage
- shows grouped medication breakdown
- generates a QR bitmap for caregiver scanning

Current use:

- patient opens this screen
- caregiver scans from another device running MediRemind

## 9. Caregiver Scan

Purpose:

- let a caregiver scan the patient report QR and read the adherence summary

Current behavior:

- scans the patient report QR
- parses the caregiver summary payload
- shows patient stats and grouped medication breakdown

Current limitation:

- visual polish improved, but caregiver explanation can still be simplified further if needed

## What Has Been Refined Recently

Recent refinement work includes:

- separated patient report and caregiver scan into different flows
- grouped dose history by medication instead of repeating cards noisily
- improved schedule syncing from medication details
- improved missed-dose rollover behavior
- improved home dashboard so it is not just random buttons
- improved patient profile usage
- improved patient report, caregiver scan, and QR import presentation

## Current Strengths

- local/offline-first architecture is real
- main medication workflow works end to end
- app already stores real data in Room
- photo verification flow exists
- caregiver QR flow exists
- history and schedule views are cleaner than before

## Known Weaknesses / Gaps

### 1. Reminder Engine Not Built Yet

Still missing:

- notification channel
- runtime notification permission flow
- `AlarmManager` scheduling
- reminder receiver
- real scheduled reminder firing

### 2. Refill Logic Needs Harder Coupling To Real Logs

The app already estimates refill timing, but later it should rely even more strongly on actual taken-dose behavior instead of only static schedule math.

### 3. Some Screens Still Need UX Tightening

The app is improved, but a few screens still feel closer to tool pages than a final polished health app.

### 4. Photo Privacy Cleanup Still Missing

Proof photos are saved, but automatic 24-hour cleanup is not yet implemented.

## Recommended Next Build Order

### Before Reminder Engine

Do a short final refinement pass on:

- medication screen flow consistency
- schedule screen flow consistency
- profile/report wording consistency
- QR import review behavior

### After That

Build the real reminder system:

1. notification channel
2. reminder permission
3. reminder receiver
4. `AlarmManager` scheduling
5. refill reminder scheduling

### After Reminders

Finish:

- photo cleanup after 24 hours
- final caregiver report polish
- real-device testing pass
- release APK
- report screenshots

## What To Review In Code First

If you are reviewing the codebase quickly, start with:

- `MainActivity.kt`
- `data/repository/`
- `ui/screen/home/`
- `ui/screen/medication/`
- `ui/screen/schedule/`
- `ui/screen/reminder/`
- `ui/screen/profile/`

## Summary

MediRemind currently offers a real offline patient workflow:

- create patient profile
- add medication
- set schedule
- log doses
- verify taken doses with a live photo
- review dose history
- generate caregiver QR summary
- scan caregiver summary on another device

The biggest missing engineering block is the actual Android reminder engine.

The biggest current design goal is to keep turning the working screens into a more guided, patient-friendly app flow without breaking the stable core already built.
