# MediRemind Build Board

Last revised: 2026-05-15

## Current Truth

MediRemind now has a real offline medication loop:

- [x] Patient profile creation/switching for local shared-device use
- [x] Medication add/edit/delete
- [x] QR medication import
- [x] Demo QR payloads and PNGs for testing
- [x] Auto-create schedules from medication frequency
- [x] Schedule add/edit/delete
- [x] Dose Log with Taken, Skipped, Snoozed, and Missed
- [x] Live camera capture before confirming Taken
- [x] Medication photo matching before Taken confirmation
- [x] Stock decrement after confirmed Taken dose
- [x] Dose amount per dose support, including liquids such as 10 ml syrup
- [x] Refill reminders for low/out-of-stock medicine
- [x] Patient report QR generation
- [x] Caregiver scan/read flow
- [x] Profile-scoped data separation using `patientId`
- [x] 24-hour cleanup for verification photos
- [x] AlarmManager reminder engine with sound/vibration/lock-screen behavior
- [x] Adherence streak screen

## Important Warning For Next Reviewer

The core logic is now sendable for review. Do not do broad rewrites unless a real bug is found.

The main remaining risk is real-device behavior:

- exact alarms on Samsung/Xiaomi battery settings
- notification timing while the phone is locked or recently restarted
- camera/photo matching under bad lighting
- UI readability on smaller screens

Keep improvements cautious. Fix concrete bugs, confusing copy, overlap, or unreadable colors. Avoid redesigning the whole app before final testing.

## Current Sprint: Friend Review And Final Phone Testing

- [x] Push latest code to GitHub
- [x] Add `amountPerDose` and Room v7 migration
- [x] Update QR demo payloads with `amountPerDose`
- [x] Add syrup QR demo for non-1-unit stock decrement
- [x] Regenerate demo QR PNGs
- [x] Update schedule estimate math to use amount per dose
- [x] Apply light UI consistency to Profile and Schedule screens
- [ ] Friend code review
- [ ] Fresh install phone test
- [ ] Existing install migration test from database v6 to v7
- [ ] Real-device test alarms with screen on, screen off, and after reboot
- [ ] Real-device test QR import using all demo QR PNGs
- [ ] Real-device test stock decrement for tablets and syrup

## QR And Caregiver Flow

- [x] QR import starts schedules from the intended start date
- [x] Imported medication can auto-schedule
- [x] QR parser accepts `amountPerDose`, `doseAmount`, or `doseQuantity`
- [x] Demo QR files exist in `docs/demo_qr_codes/`
- [x] Mary Metformin QR
- [x] BP patient Amlodipine QR
- [x] Two-medicine pharmacy bundle QR
- [x] Syrup Amoxicillin QR
- [x] Caregiver report sample QR
- [x] QR import screen wording simplified for low-literacy users
- [x] Caregiver report QR uses a bounded recent-report approach
- [x] QR generation failure is guarded instead of crashing
- [ ] Test caregiver scan on two physical phones

## Profile / Multiple Patient Scope

- [x] Support multiple local profiles on one shared device
- [x] Active patient controls visible Home/report/QR context
- [x] Medications, schedules, and logs are patient-scoped
- [x] QR imports attach to active patient
- [ ] Real-device test Mary + second patient + QR imported patient
- [ ] Confirm alarm notification clearly includes patient name
- [ ] Future work: optional PIN/biometric caregiver mode

## Reminder Engine

- [x] `MedicationAlarmScheduler`
- [x] `MedicationAlarmReceiver`
- [x] `MedicationBootReceiver`
- [x] Manifest permissions:
  - [x] `POST_NOTIFICATIONS`
  - [x] `SCHEDULE_EXACT_ALARM`
  - [x] `RECEIVE_BOOT_COMPLETED`
  - [x] `VIBRATE`
  - [x] `USE_FULL_SCREEN_INTENT`
  - [x] `WAKE_LOCK`
- [x] Schedule alarms after medication/schedule/QR saves
- [x] Cancel dose notification when a dose is logged
- [x] Cancel alarms on medication delete
- [x] Reschedule alarms after boot
- [x] Sound/vibration/lock-screen reminder behavior
- [ ] Real-device test exact alarm behavior under battery optimization
- [ ] Document battery unrestricted setting for Samsung/Xiaomi if needed

## Refill / Stock

- [x] Decrement stock after confirmed Taken dose
- [x] Guard against double deduction
- [x] Support dose amount other than 1 unit
- [x] Show remaining stock in medicine and dose logging flows
- [x] Add refill reminder alarms
- [x] Immediate low-stock/out-of-stock notification behavior
- [ ] Test refill alert threshold with a small-stock medication
- [ ] Future work: recompute refill estimates from long-term actual logs

## Photo Verification

- [x] Reference photos stored in persistent app files
- [x] Verification photos cleaned after 24 hours
- [x] Photo matcher improved beyond simple average hash
- [x] 3-zone verification UI exists
- [ ] Real-device test with different backgrounds and lighting
- [ ] Tune threshold if false reject/false accept happens
- [ ] Keep copy simple: photo check should not scare patients

## UI Design To-Do

Current direction:

- warm healthcare background
- purple primary actions
- green for taken/success
- red for missed/problem
- orange for snooze/upcoming/refill warning
- blue for QR/report/info
- large readable labels and touch targets
- no color-only meaning

Screen checks still needed:

- [ ] Home: no overlap on small screens
- [ ] Dose Logging: readable stock chips and action buttons
- [ ] Medications: remaining stock and low-stock badges clear
- [ ] Schedules: matches app theme and dates are understandable
- [ ] Profile: active/use/edit wording is clear
- [ ] QR Import: elderly-friendly wording
- [ ] Patient Report: easy for caregiver to read
- [ ] Caregiver Scan: result screen is clear

## Final Demo Personas

- [x] Mary Achieng: diabetic, Metformin twice daily
- [x] Second patient: daily blood pressure medicine
- [x] Syrup patient: Amoxicillin 10 ml three times daily
- [x] QR-import patient: medication loaded without typing
- [ ] Caregiver scan flow: patient phone generates report, caregiver scans it

## Last-Minute Cuts If Time Is Bad

Cut in this order:

1. Fancy UI animations
2. Extra charts
3. Extra caregiver simulator features
4. Optional PIN/biometric caregiver mode
5. Cloud/hospital integration

Do not cut:

1. Medication CRUD
2. Profile-scoped medication/schedules/logs
3. QR import
4. Dose logging
5. Reminder notifications
6. Refill/stock tracking
7. Live camera confirmation
