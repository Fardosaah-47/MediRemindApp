# MediRemind

MediRemind is an offline-first Android medication reminder app for elderly patients, low-literacy patients, and caregivers. It helps a patient load medicines, follow schedules, log doses, track remaining stock, receive refill warnings, and share a caregiver-readable adherence summary by QR code.

## Core Features

- Multiple local patient profiles for shared household phones
- Medication add, edit, delete, and QR import
- Automatic schedule creation from medication frequency
- Dose logging with Taken, Skipped, Snoozed, and Missed states
- Live camera capture before confirming a Taken dose
- Medication reference-photo matching to reduce false Taken logs
- Stock decrement using the real amount per dose, including liquids such as 10 ml syrup
- Refill reminders for low-stock and out-of-stock medicine
- AlarmManager dose reminders with sound, vibration, lock-screen support, and reboot rescheduling
- 7-day caregiver report QR generation and caregiver scan flow
- Adherence streak and recent adherence summary

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- Coroutines
- ML Kit Code Scanner
- AlarmManager and Android notifications
- Local file storage with FileProvider

## Demo QR Codes

Ready-made QR payloads and PNGs are included in:

```text
docs/demo_qr_codes/
```

Useful demos:

- `mary_metformin_twice_daily.png`
- `bp_patient_amlodipine_once_daily.png`
- `pharmacy_bundle_two_meds.png`
- `syrup_amoxicillin_three_times_daily.png`
- `caregiver_report_sample.png`

To regenerate them:

```powershell
python tools\generate_demo_qrs.py
```

## Build

Open the project in Android Studio, or build from the repo root:

```powershell
.\gradlew.bat assembleDebug --console=plain
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

## Suggested Demo Flow

1. Create a patient profile.
2. Scan a medication QR from `docs/demo_qr_codes/`.
3. Confirm the medication and schedule were created.
4. Add a reference photo for the medicine.
5. Wait for a reminder or open Dose Log.
6. Log a Taken dose with camera verification.
7. Confirm stock decreases by the correct amount per dose.
8. Open Patient Report and generate the caregiver QR.
9. Scan the caregiver QR on another phone or emulator.

## Privacy And Scope

MediRemind stores patient data locally on the device and is designed for offline use. The MVP uses device-level privacy for shared household phones. Optional PIN or biometric locking is future work.

## Presentation Status

The core app workflow is implemented and ready for final real-device testing. Remaining work before submission is mainly presentation polish, screenshots, and documentation.
