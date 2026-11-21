package com.dayflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        EventEntity::class,
        TaskEntity::class,
        ReminderEntity::class,
        ContextEntryEntity::class,
        ActivityEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun contextDao(): ContextDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Keeps existing user data: v1 rows get null for the new columns. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN startMillis INTEGER")
                db.execSQL("ALTER TABLE events ADD COLUMN calendarEventId INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN dueMillis INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `context_entries` (`id` TEXT NOT NULL, `kind` TEXT NOT NULL, " +
                        "`term` TEXT NOT NULL, `value` TEXT NOT NULL, `weekdays` TEXT NOT NULL, `time` TEXT NOT NULL, " +
                        "`usageCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `activity_log` (`id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, " +
                        "`kind` TEXT NOT NULL, `message` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dayflow_database"
                ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
