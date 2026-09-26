package com.dayflow.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE dismissed = 0 ORDER BY scheduledTimeMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders SET dismissed = 1 WHERE id = :reminderId")
    suspend fun dismissReminder(reminderId: String)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: String)

    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}
