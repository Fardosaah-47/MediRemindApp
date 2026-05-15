import json
from pathlib import Path

import qrcode


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "docs" / "demo_qr_codes"


def compact_json(payload):
    return json.dumps(payload, separators=(",", ":"))


DEMO_PAYLOADS = {
    "mary_metformin_twice_daily": {
        "type": "mediremind_rx_v1",
        "source": "MediRemind Demo Pharmacy",
        "medications": [
            {
                "name": "Metformin",
                "form": "TABLET",
                "dosage": "500mg tablet - take one tablet twice daily with meals",
                "frequency": "TWICE_DAILY",
                "times": ["08:00 AM", "08:00 PM"],
                "amountPerDose": 1,
                "stockAmount": 60,
                "stockUnit": "tablets",
                "refillAlertAt": 10,
            }
        ],
    },
    "bp_patient_amlodipine_once_daily": {
        "type": "mediremind_rx_v1",
        "source": "MediRemind Demo Pharmacy",
        "medications": [
            {
                "name": "Amlodipine",
                "form": "TABLET",
                "dosage": "5mg tablet - take one tablet once daily in the morning",
                "frequency": "ONCE_DAILY",
                "times": ["09:00 AM"],
                "amountPerDose": 1,
                "stockAmount": 30,
                "stockUnit": "tablets",
                "refillAlertAt": 7,
            }
        ],
    },
    "pharmacy_bundle_two_meds": {
        "type": "mediremind_rx_v1",
        "source": "MediRemind Demo Pharmacy",
        "medications": [
            {
                "name": "Metformin",
                "form": "TABLET",
                "dosage": "500mg tablet - take one tablet twice daily with meals",
                "frequency": "TWICE_DAILY",
                "times": ["08:00 AM", "08:00 PM"],
                "amountPerDose": 1,
                "stockAmount": 60,
                "stockUnit": "tablets",
                "refillAlertAt": 10,
            },
            {
                "name": "Amlodipine",
                "form": "TABLET",
                "dosage": "5mg tablet - take one tablet once daily in the morning",
                "frequency": "ONCE_DAILY",
                "times": ["09:00 AM"],
                "amountPerDose": 1,
                "stockAmount": 30,
                "stockUnit": "tablets",
                "refillAlertAt": 7,
            },
        ],
    },
    "syrup_amoxicillin_three_times_daily": {
        "type": "mediremind_rx_v1",
        "source": "MediRemind Demo Pharmacy",
        "medications": [
            {
                "name": "Amoxicillin Syrup",
                "form": "LIQUID",
                "dosage": "250mg/5ml suspension - give 10ml three times daily",
                "frequency": "THREE_TIMES_DAILY",
                "times": ["07:00 AM", "02:00 PM", "09:00 PM"],
                "amountPerDose": 10,
                "stockAmount": 150,
                "stockUnit": "ml",
                "refillAlertAt": 30,
            }
        ],
    },
    "caregiver_report_sample": {
        "type": "mediremind_report_v2",
        "patient": "Mary Achieng",
        "caregiver": "Jane Achieng",
        "date": "2026-05-11",
        "adherence": 83,
        "taken": 10,
        "skipped": 1,
        "snoozed": 1,
        "missed": 0,
        "logged": 12,
        "medications": [
            {
                "name": "Metformin",
                "taken": 10,
                "skipped": 1,
                "snoozed": 1,
                "missed": 0,
                "logged": 12,
            }
        ],
        "truncated": False,
    },
}


def write_qr(name, payload):
    raw = compact_json(payload)
    image = qrcode.make(raw)
    image.save(OUTPUT_DIR / f"{name}.png")
    (OUTPUT_DIR / f"{name}.json").write_text(
        json.dumps(payload, indent=2),
        encoding="utf-8",
    )


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, payload in DEMO_PAYLOADS.items():
        write_qr(name, payload)
    print(f"Wrote {len(DEMO_PAYLOADS)} demo QR payloads to {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
