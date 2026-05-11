# MediRemind Demo QR Codes

These QR codes are for local testing before a real hospital/pharmacy QR system exists.

The app imports medication QR codes with this shape:

```json
{
  "type": "mediremind_rx_v1",
  "medications": [
    {
      "name": "Metformin",
      "form": "TABLET",
      "dosage": "500mg tablet - take one tablet twice daily with meals",
      "frequency": "TWICE_DAILY",
      "times": ["08:00 AM", "08:00 PM"],
      "stockAmount": 60,
      "stockUnit": "tablets",
      "refillAlertAt": 10
    }
  ]
}
```

## Generate

From the repo root:

```powershell
python tools\generate_demo_qrs.py
```

The generated files are written to:

```text
docs/demo_qr_codes/
```

## Test Cases

- `mary_metformin_twice_daily.png`
  - Medication import QR for Mary Achieng.
  - Creates Metformin 500mg, twice daily, 08:00 AM and 08:00 PM.

- `bp_patient_amlodipine_once_daily.png`
  - Medication import QR for another patient.
  - Creates Amlodipine 5mg, once daily, 09:00 AM.

- `pharmacy_bundle_two_meds.png`
  - Medication import QR with two medicines in one scan.
  - Useful for testing bulk import.

- `caregiver_report_sample.png`
  - Caregiver report QR.
  - Use this on the Caregiver Scan screen, not the medication import screen.

## Low-Literacy Flow

For the elderly patient, the intended path is:

1. Caregiver or clinic creates/selects the patient profile once.
2. Patient taps Import by QR.
3. Patient checks the selected patient name.
4. Patient scans the pharmacy/hospital medication QR.
5. The app creates medication and schedule records automatically.
6. Patient adds a reference photo once.
7. Daily use becomes mostly tapping Dose Logging and taking the medicine photo.

Manual medication entry stays available for caregivers, not as the main elderly-patient path.
