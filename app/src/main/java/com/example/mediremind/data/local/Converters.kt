package com.example.mediremind.data.local

import androidx.room.TypeConverter
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.data.model.MedicationForm

class Converters {
    @TypeConverter
    fun fromMedicationForm(value: MedicationForm): String = value.name

    @TypeConverter
    fun toMedicationForm(value: String): MedicationForm = MedicationForm.valueOf(value)

    @TypeConverter
    fun fromDoseFrequency(value: DoseFrequency): String = value.name

    @TypeConverter
    fun toDoseFrequency(value: String): DoseFrequency = DoseFrequency.valueOf(value)

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus): String = value.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus = DoseStatus.valueOf(value)
}
