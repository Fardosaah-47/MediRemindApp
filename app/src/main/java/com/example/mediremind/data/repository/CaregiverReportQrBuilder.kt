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

data class CaregiverReportSummary(
    val patientName: String,
    val caregiverName: String?,
    val reportDate: String,
    val adherenceRate: Int,
    val totalTaken: Int,
    val totalSkipped: Int,
    val totalSnoozed: Int,
    val totalLogged: Int,
    val qrPayload: String
)

object CaregiverReportQrBuilder {
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
        val totalLogged = doseLogs.size
        val adherenceRate = if (totalLogged > 0) {
            ((totalTaken.toDouble() / totalLogged.toDouble()) * 100).toInt()
        } else {
            0
        }

        val medicationsArray = JSONArray()
        medications.forEach { medication ->
            val logsForMedication = doseLogs.filter { it.medicationId == medication.id }
            if (logsForMedication.isNotEmpty()) {
                val medicationSummary = JSONObject()
                    .put("name", medication.name)
                    .put("taken", logsForMedication.count { it.status == DoseStatus.TAKEN })
                    .put("skipped", logsForMedication.count { it.status == DoseStatus.SKIPPED })
                    .put("snoozed", logsForMedication.count { it.status == DoseStatus.SNOOZED })
                medicationsArray.put(medicationSummary)
            }
        }

        val payload = JSONObject()
            .put("type", "mediremind_report_v1")
            .put("patientName", patientName)
            .put("caregiverName", caregiverName ?: "")
            .put("reportDate", reportDate)
            .put("adherenceRate", adherenceRate)
            .put("totalTaken", totalTaken)
            .put("totalSkipped", totalSkipped)
            .put("totalSnoozed", totalSnoozed)
            .put("totalLogged", totalLogged)
            .put("medications", medicationsArray)
            .toString()

        return CaregiverReportSummary(
            patientName = patientName,
            caregiverName = caregiverName,
            reportDate = reportDate,
            adherenceRate = adherenceRate,
            totalTaken = totalTaken,
            totalSkipped = totalSkipped,
            totalSnoozed = totalSnoozed,
            totalLogged = totalLogged,
            qrPayload = payload
        )
    }

    fun generateQrBitmap(content: String, size: Int = 900): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }
}
