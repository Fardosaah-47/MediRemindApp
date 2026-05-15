package com.example.mediremind.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE medications ADD COLUMN referenceImageUri TEXT"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE dose_logs ADD COLUMN logDate TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE dose_schedules ADD COLUMN startDate TEXT NOT NULL DEFAULT ''"
            )
            database.execSQL(
                "ALTER TABLE dose_schedules ADD COLUMN endDate TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE medications ADD COLUMN isQrImported INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE medications ADD COLUMN patientId INTEGER NOT NULL DEFAULT 1"
            )
            database.execSQL(
                "ALTER TABLE dose_schedules ADD COLUMN patientId INTEGER NOT NULL DEFAULT 1"
            )
            database.execSQL(
                "ALTER TABLE dose_logs ADD COLUMN patientId INTEGER NOT NULL DEFAULT 1"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_medications_patientId ON medications(patientId)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_dose_schedules_patientId ON dose_schedules(patientId)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_dose_logs_patientId ON dose_logs(patientId)"
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE medications ADD COLUMN amountPerDose REAL NOT NULL DEFAULT 1.0"
            )
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mediremind_database"
            )
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .build()
                .also { INSTANCE = it }
        }
    }
}
