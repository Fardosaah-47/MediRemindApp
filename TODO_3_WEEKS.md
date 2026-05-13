# MediRemind Build Board

Last revised: 2026-05-13

## Current Truth

MediRemind now has a real offline medication loop:

- [x] Patient profile creation/switching for local shared-device use
- [x] Medication add/edit/delete
- [x] QR medication import
- [x] Auto-create schedules from medication frequency
- [x] Schedule add/edit/delete
- [x] Dose Log with Taken, Skipped, Snoozed, and Missed
- [x] Live camera capture before confirming Taken
- [x] Medication photo matching before Taken confirmation
- [x] Stock decrement after confirmed Taken dose
- [x] Patient report QR generation
- [x] Caregiver scan/read flow
- [x] Profile-scoped data separation using `patientId`
- [x] 24-hour cleanup for verification photos
- [x] Basic AlarmManager reminder engine

## Important Warning For Next Reviewer

The UI is still under design review.

Do not treat the current Home screen as final. The current design direction is:

- elderly-friendly
- high contrast
- fewer words
- large buttons
- simple colors
- no decorative clutter that reduces readability

The user is actively comparing the app against a more polished FarmConnect-style app. Keep the logic stable and improve UI one screen at a time.

## Current Sprint: Stabilize Before Charts

- [x] Fix Home progress counts so missed doses do not look like remaining doses
- [x] Fix schedule edit reflection in Dose Log by clearing stale same-day auto-missed logs
- [x] Add basic reminder scheduling with AlarmManager
- [x] Register reminder receiver and boot receiver
- [x] Request notification permission on supported Android versions
- [x] Open Dose Log from reminder notification tap
- [ ] Real-device test reminder firing
- [ ] Upgrade reminder notification with sound/vibration/lock-screen behavior
- [ ] Decide whether full-screen ringing alarm is in MVP or future work
- [ ] Finish Home screen UI approval
- [ ] Apply approved accessible design language to Dose Logging
- [ ] Apply approved accessible design language to Medications
- [ ] Apply approved accessible design language to Schedules
- [ ] Apply approved accessible design language to Profile and Reports

## QR And Caregiver Flow

- [x] QR import starts schedules from today when appropriate
- [x] Imported medication can auto-schedule
- [x] Caregiver report QR uses a bounded recent-report approach
- [x] QR generation failure is guarded instead of crashing
- [ ] Test QR import with realistic hospital/pharmacy payloads
- [ ] Create sample QR payloads for demo personas
- [ ] Simplify QR import screen wording for low-literacy users
- [ ] Simplify caregiver report wording for quick scanning

## Profile / Multiple Patient Scope

- [x] Support multiple local profiles on one shared device
- [x] Active patient controls visible Home/report/QR context
- [x] Medications and schedules are patient-scoped
- [x] QR imports attach to active patient
- [ ] Real-device test Mary + second patient + QR imported patient
- [ ] Add clearer UI copy for active patient vs edit patient
- [ ] Future work: optional PIN/biometric caregiver mode

## Reminder Engine

- [x] `MedicationAlarmScheduler`
- [x] `MedicationAlarmReceiver`
- [x] `MedicationBootReceiver`
- [x] Manifest permissions:
  - [x] `POST_NOTIFICATIONS`
  - [x] `SCHEDULE_EXACT_ALARM`
  - [x] `RECEIVE_BOOT_COMPLETED`
- [x] Schedule alarms after medication/schedule/QR saves
- [x] Cancel alarms on medication delete
- [x] Reschedule alarms after boot
- [ ] Real-device test exact alarm behavior
- [ ] Add sound and vibration
- [ ] Add notification category/visibility settings
- [ ] Consider full-screen alarm screen after normal reminder works
- [ ] Add refill reminder alarms

## Refill / Stock

- [x] Decrement stock after confirmed Taken dose
- [x] Guard against double deduction
- [ ] Support dose amount other than 1 unit
- [ ] Recompute refill estimates from actual taken logs
- [ ] Add refill reminders

## Photo Verification

- [x] Reference photos stored in persistent app files
- [x] Verification photos cleaned after 24 hours
- [x] Photo matcher improved beyond simple average hash
- [x] 3-zone verification UI exists
- [ ] Real-device test with different backgrounds and lighting
- [ ] Tune threshold if false reject/false accept happens
- [ ] Keep copy simple: photo check should not scare patients

## UI Design To-Do

Use this strict design system unless a final Figma design replaces it:

- Background: `#F7F9FB`
- Primary: `#2F80ED`
- Success: `#27AE60`
- Alert: `#EB5757`
- Text: `#222222`

Rules:

- [ ] High contrast text everywhere
- [ ] Icons plus labels, not icons alone
- [ ] Large buttons and touch targets
- [ ] No color-only status meaning
- [ ] No light gray text for important info
- [ ] No gradients/neon unless explicitly approved
- [ ] Keep Home bottom navigation stable unless user explicitly asks

Screen order:

1. Home
2. Dose Logging
3. Medications
4. Schedules
5. Profile
6. QR Import
7. Patient Report
8. Caregiver Scan

## Final Demo Personas

- [ ] Mary Achieng: diabetic, Metformin twice daily
- [ ] Second patient: daily blood pressure medicine
- [ ] QR-import patient: medication loaded without typing
- [ ] Caregiver scan flow: patient phone generates report, caregiver scans it

## Last-Minute Cuts If Time Is Bad

Cut in this order:

1. Charts
2. Streaks
3. Full-screen ringing alarm
4. Extra caregiver simulator features
5. Fancy animations

Do not cut:

1. Medication CRUD
2. Profile-scoped medication/schedules
3. QR import
4. Dose logging
5. Reminder notifications
6. Refill/stock tracking
7. Live camera confirmation
