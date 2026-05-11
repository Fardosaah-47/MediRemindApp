package com.example.mediremind.data.local

import androidx.room.TypeConverter
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.data.model.MedicationForm

class Converters {
    @TypeConverter
    fun fromMedicationForm(value: MedicationForm): String = value.name

    @TypeConverter
    fun toMedicationForm(value: String): MedicationForm =
        MedicationForm.entries.find { it.name == value } ?: MedicationForm.OTHER

    @TypeConverter
    fun fromDoseFrequency(value: DoseFrequency): String = value.name

    @TypeConverter
    fun toDoseFrequency(value: String): DoseFrequency =
        DoseFrequency.entries.find { it.name == value } ?: DoseFrequency.ONCE_DAILY

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus): String = value.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus =
        DoseStatus.entries.find { it.name == value } ?: DoseStatus.MISSED
}
