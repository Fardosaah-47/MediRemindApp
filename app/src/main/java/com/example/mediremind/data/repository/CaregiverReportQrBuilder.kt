package com.example.mediremind.data.repository

import android.graphics.Bitmap
import android.graphics.Color
import com.example.mediremind.data.model.DoseLog
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.UserProfile
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CaregiverMedicationSummary(
    val name: String,
    val taken: Int,
    val skipped: Int,
    val snoozed: Int,
    val missed: Int,
    val totalLogged: Int
)

data class CaregiverReportSummary(
    val patientName: String,
    val caregiverName: String?,
    val reportDate: String,
    val adherenceRate: Int,
    val totalTaken: Int,
    val totalSkipped: Int,
    val totalSnoozed: Int,
    val totalMissed: Int,
    val totalLogged: Int,
    val medicationSummaries: List<CaregiverMedicationSummary>,
    val qrPayload: String
)

object CaregiverReportQrBuilder {
    private const val MAX_QR_PAYLOAD_CHARACTERS = 2400
    private const val MAX_QR_MEDICATION_NAME_CHARACTERS = 40

    fun buildSummary(
        userProfile: UserProfile?,
        medications: List<Medication>,
        doseLogs: List<DoseLog>
    ): CaregiverReportSummary {
        val patientName = userProfile?.fullName?.takeIf { it.isNotBlank() } ?: "Patient Not Set"
        val caregiverName = userProfile?.caregiverName?.takeIf { it.isNotBlank() }
        val reportDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val totalTaken = doseLogs.count { it.status == DoseStatus.TAKEN }
        val totalSkipped = doseLogs.count { it.status == DoseStatus.SKIPPED }
        val totalSnoozed = doseLogs.count { it.status == DoseStatus.SNOOZED }
        val totalMissed = doseLogs.count { it.status == DoseStatus.MISSED }
        val totalLogged = doseLogs.size
        val adherenceRate = if (totalLogged > 0) {
            ((totalTaken.toDouble() / totalLogged.toDouble()) * 100).toInt()
        } else {
            0
        }

        val medicationNameById = medications.associate { medication ->
            medication.id to medication.name.trim()
        }
        val medicationSummaries = doseLogs
            .groupBy { log ->
                medicationNameById[log.medicationId]
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown Medication"
            }
            .map { (name, logsForMedication) ->
                CaregiverMedicationSummary(
                    name = name,
                    taken = logsForMedication.count { it.status == DoseStatus.TAKEN },
                    skipped = logsForMedication.count { it.status == DoseStatus.SKIPPED },
                    snoozed = logsForMedication.count { it.status == DoseStatus.SNOOZED },
                    missed = logsForMedication.count { it.status == DoseStatus.MISSED },
                    totalLogged = logsForMedication.size
                )
            }
            .sortedBy { it.name.lowercase() }

        val payload = buildQrPayload(
            patientName = patientName,
            caregiverName = caregiverName,
            reportDate = reportDate,
            adherenceRate = adherenceRate,
            totalTaken = totalTaken,
            totalSkipped = totalSkipped,
            totalSnoozed = totalSnoozed,
            totalMissed = totalMissed,
            totalLogged = totalLogged,
            medicationSummaries = medicationSummaries
        )

        return CaregiverReportSummary(
            patientName = patientName,
            caregiverName = caregiverName,
            reportDate = reportDate,
            adherenceRate = adherenceRate,
            totalTaken = totalTaken,
            totalSkipped = totalSkipped,
            totalSnoozed = totalSnoozed,
            totalMissed = totalMissed,
            totalLogged = totalLogged,
            medicationSummaries = medicationSummaries,
            qrPayload = payload
        )
    }

    private fun buildQrPayload(
        patientName: String,
        caregiverName: String?,
        reportDate: String,
        adherenceRate: Int,
        totalTaken: Int,
        totalSkipped: Int,
        totalSnoozed: Int,
        totalMissed: Int,
        totalLogged: Int,
        medicationSummaries: List<CaregiverMedicationSummary>
    ): String {
        var medicationLimit = medicationSummaries.size
        var payload = buildQrPayloadJson(
            patientName = patientName,
            caregiverName = caregiverName,
            reportDate = reportDate,
            adherenceRate = adherenceRate,
            totalTaken = totalTaken,
            totalSkipped = totalSkipped,
            totalSnoozed = totalSnoozed,
            totalMissed = totalMissed,
            totalLogged = totalLogged,
            medicationSummaries = medicationSummaries,
            medicationLimit = medicationLimit
        )

        while (payload.length > MAX_QR_PAYLOAD_CHARACTERS && medicationLimit > 0) {
            medicationLimit--
            payload = buildQrPayloadJson(
                patientName = patientName,
                caregiverName = caregiverName,
                reportDate = reportDate,
                adherenceRate = adherenceRate,
                totalTaken = totalTaken,
                totalSkipped = totalSkipped,
                totalSnoozed = totalSnoozed,
                totalMissed = totalMissed,
                totalLogged = totalLogged,
                medicationSummaries = medicationSummaries,
                medicationLimit = medicationLimit
            )
        }

        return payload
    }

    private fun buildQrPayloadJson(
        patientName: String,
        caregiverName: String?,
        reportDate: String,
        adherenceRate: Int,
        totalTaken: Int,
        totalSkipped: Int,
        totalSnoozed: Int,
        totalMissed: Int,
        totalLogged: Int,
        medicationSummaries: List<CaregiverMedicationSummary>,
        medicationLimit: Int
    ): String {
        val visibleMedicationSummaries = medicationSummaries.take(medicationLimit)
        val medicationsArray = JSONArray().apply {
            visibleMedicationSummaries.forEach { medicationSummary ->
                put(
                    JSONObject()
                        .put("name", medicationSummary.name.take(MAX_QR_MEDICATION_NAME_CHARACTERS))
                        .put("taken", medicationSummary.taken)
                        .put("skipped", medicationSummary.skipped)
                        .put("snoozed", medicationSummary.snoozed)
                        .put("missed", medicationSummary.missed)
                        .put("logged", medicationSummary.totalLogged)
                )
            }
        }

        return JSONObject()
            .put("type", "mediremind_report_v2")
            .put("patient", patientName)
            .put("caregiver", caregiverName ?: "")
            .put("date", reportDate)
            .put("adherence", adherenceRate)
            .put("taken", totalTaken)
            .put("skipped", totalSkipped)
            .put("snoozed", totalSnoozed)
            .put("missed", totalMissed)
            .put("logged", totalLogged)
            .put("medications", medicationsArray)
            .put("truncated", visibleMedicationSummaries.size < medicationSummaries.size)
            .toString()
    }

    fun parseQrPayload(rawValue: String): CaregiverReportSummary {
        val payload = JSONObject(rawValue.trim())
        val payloadType = payload.optString("type")
        require(payloadType == "mediremind_report_v1" || payloadType == "mediremind_report_v2") {
            "This QR is not a MediRemind caregiver report."
        }

        val medicationsArray = payload.optJSONArray("medications") ?: JSONArray()
        val medicationSummaries = buildList {
            for (index in 0 until medicationsArray.length()) {
                val medication = medicationsArray.optJSONObject(index) ?: continue
                add(
                    CaregiverMedicationSummary(
                        name = medication.optString("name", "Unknown Medication"),
                        taken = medication.optInt("taken", 0),
                        skipped = medication.optInt("skipped", 0),
                        snoozed = medication.optInt("snoozed", 0),
                        missed = medication.optInt("missed", 0),
                        totalLogged = medication.optInt(
                            "logged",
                            medication.optInt("taken", 0) +
                                medication.optInt("skipped", 0) +
                                medication.optInt("snoozed", 0) +
                                medication.optInt("missed", 0)
                        )
                    )
                )
            }
        }

        return CaregiverReportSummary(
            patientName = payload.optString("patient", payload.optString("patientName", "Patient Not Set")),
            caregiverName = payload.optString("caregiver", payload.optString("caregiverName", "")).takeIf { it.isNotBlank() },
            reportDate = payload.optString("date", payload.optString("reportDate", "")),
            adherenceRate = payload.optInt("adherence", payload.optInt("adherenceRate", 0)),
            totalTaken = payload.optInt("taken", payload.optInt("totalTaken", 0)),
            totalSkipped = payload.optInt("skipped", payload.optInt("totalSkipped", 0)),
            totalSnoozed = payload.optInt("snoozed", payload.optInt("totalSnoozed", 0)),
            totalMissed = payload.optInt("missed", 0),
            totalLogged = payload.optInt("logged", payload.optInt("totalLogged", 0)),
            medicationSummaries = medicationSummaries,
            qrPayload = rawValue.trim()
        )
    }

    fun generateQrBitmap(content: String, size: Int = 900): Bitmap? {
        val bitMatrix = runCatching {
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        }.getOrNull() ?: return null
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }
}
