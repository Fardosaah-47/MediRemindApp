package com.example.mediremind.data.repository

import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
import org.json.JSONObject

data class QrImportPayload(
    val medications: List<QrMedicationPayload>
)

data class QrMedicationPayload(
    val medication: Medication,
    val frequency: DoseFrequency,
    val times: List<String>
)

data class QrImportResult(
    val insertedMedicationIds: List<Long>,
    val autoScheduledCount: Int
)

object QrImportParser {
    fun parse(rawValue: String): QrImportPayload {
        val normalizedValue = extractJsonPayload(rawValue.trim())

        require(!normalizedValue.startsWith("http", ignoreCase = true)) {
            "This QR contains a website link, not medication data."
        }

        val root = JSONObject(normalizedValue)
        val type = root.optString("type")
        require(type == "mediremind_rx_v1") {
            "Unsupported QR format."
        }

        val medicationsArray = root.optJSONArray("medications")
            ?: throw IllegalArgumentException("QR code has no medications.")

        val medications = buildList {
            for (index in 0 until medicationsArray.length()) {
                val item = medicationsArray.getJSONObject(index)
                val timesArray = item.optJSONArray("times")
                    ?: throw IllegalArgumentException("Each medication needs at least one reminder time.")

                val times = buildList {
                    for (timeIndex in 0 until timesArray.length()) {
                        val time = timesArray.optString(timeIndex).trim()
                        if (time.isNotBlank()) add(time)
                    }
                }

                require(times.isNotEmpty()) {
                    "Each medication needs at least one valid reminder time."
                }

                add(
                    QrMedicationPayload(
                        medication = Medication(
                            name = item.optString("name").ifBlank { "Untitled Medication" },
                            form = parseMedicationForm(item.optString("form")),
                            dosage = item.optString("dosage").ifBlank { "Not specified" },
                            currentStockAmount = item.optDouble("stockAmount", 0.0),
                            stockUnit = item.optString("stockUnit").ifBlank { "units" },
                            refillAlertAt = item.optDouble("refillAlertAt", 0.0),
                            isQrImported = true
                        ),
                        frequency = parseDoseFrequency(item.optString("frequency")),
                        times = times
                    )
                )
            }
        }

        require(medications.isNotEmpty()) {
            "QR code has no medication entries."
        }

        return QrImportPayload(medications = medications)
    }

    private fun parseMedicationForm(value: String): MedicationForm {
        return when (value.trim().uppercase()) {
            "TABLET" -> MedicationForm.TABLET
            "CAPSULE" -> MedicationForm.CAPSULE
            "LIQUID" -> MedicationForm.LIQUID
            "INJECTION" -> MedicationForm.INJECTION
            else -> MedicationForm.OTHER
        }
    }

    private fun parseDoseFrequency(value: String): DoseFrequency {
        return when (value.trim().uppercase()) {
            "ONCE_DAILY" -> DoseFrequency.ONCE_DAILY
            "TWICE_DAILY" -> DoseFrequency.TWICE_DAILY
            "THREE_TIMES_DAILY" -> DoseFrequency.THREE_TIMES_DAILY
            "WEEKLY" -> DoseFrequency.WEEKLY
            "AS_NEEDED" -> DoseFrequency.AS_NEEDED
            else -> DoseFrequency.ONCE_DAILY
        }
    }

    private fun extractJsonPayload(rawValue: String): String {
        if (rawValue.startsWith("{") && rawValue.endsWith("}")) {
            return rawValue
        }

        val firstBraceIndex = rawValue.indexOf('{')
        val lastBraceIndex = rawValue.lastIndexOf('}')

        require(firstBraceIndex >= 0 && lastBraceIndex > firstBraceIndex) {
            "QR code does not contain valid medication JSON."
        }

        return rawValue.substring(firstBraceIndex, lastBraceIndex + 1)
    }
}
