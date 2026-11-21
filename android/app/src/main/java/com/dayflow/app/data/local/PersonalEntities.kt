package com.dayflow.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * One piece of personal context Dayflow uses while understanding input:
 * a vocabulary alias ("Büro" -> "Hauptstraße 5"), a frequent contact, or a
 * recurring routine (weekdays + time).
 */
@Entity(tableName = "context_entries")
data class ContextEntryEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    /** [KIND_ALIAS], [KIND_CONTACT] or [KIND_ROUTINE]. */
    val kind: String,
    val term: String,
    val value: String = "",
    /** ISO weekdays 1..7, comma separated (routines only). */
    val weekdays: String = "",
    /** "HH:mm" (routines only). */
    val time: String = "",
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val KIND_ALIAS = "alias"
        const val KIND_CONTACT = "contact"
        const val KIND_ROUTINE = "routine"
    }
}

/** Transparency log: everything Dayflow did on the user's behalf. */
@Entity(tableName = "activity_log")
data class ActivityEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    /** Short machine code, e.g. "analysis_local", "calendar_write". */
    val kind: String,
    val message: String
)

@Dao
interface ContextDao {
    @Query("SELECT * FROM context_entries ORDER BY kind, usageCount DESC, term COLLATE NOCASE")
    fun observeAll(): Flow<List<ContextEntryEntity>>

    @Query("SELECT * FROM context_entries")
    suspend fun getAll(): List<ContextEntryEntity>

    @Query("SELECT * FROM context_entries WHERE kind = :kind AND term = :term COLLATE NOCASE LIMIT 1")
    suspend fun find(kind: String, term: String): ContextEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ContextEntryEntity)

    @Query("DELETE FROM context_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM context_entries")
    suspend fun clearAll()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ActivityEntity)

    @Query("DELETE FROM activity_log WHERE timestamp < :before")
    suspend fun deleteBefore(before: Long)

    @Query("DELETE FROM activity_log")
    suspend fun clearAll()
}
