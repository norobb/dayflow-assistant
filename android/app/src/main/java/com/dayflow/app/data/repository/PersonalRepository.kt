package com.dayflow.app.data.repository

import com.dayflow.app.data.local.ActivityEntity
import com.dayflow.app.data.local.AppDatabase
import com.dayflow.app.data.local.ContextEntryEntity
import kotlinx.coroutines.flow.Flow

/** Personal context (vocabulary, contacts, routines) and the transparency log. */
class PersonalRepository(private val db: AppDatabase) {

    fun observeContext(): Flow<List<ContextEntryEntity>> = db.contextDao().observeAll()
    fun observeLog(): Flow<List<ActivityEntity>> = db.activityDao().observeRecent()

    suspend fun addAlias(term: String, value: String) = upsertUnique(ContextEntryEntity.KIND_ALIAS, term, value)

    suspend fun addContact(name: String) = upsertUnique(ContextEntryEntity.KIND_CONTACT, name, "")

    suspend fun addRoutine(title: String, weekdays: Set<Int>, time: String) {
        val clean = clean(title) ?: return
        db.contextDao().upsert(
            ContextEntryEntity(
                kind = ContextEntryEntity.KIND_ROUTINE,
                term = clean,
                weekdays = weekdays.filter { it in 1..7 }.sorted().joinToString(","),
                time = time.takeIf { it.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$")) }.orEmpty()
            )
        )
    }

    /** Counts a sender each time they appear; becomes a frequent contact on repeat. */
    suspend fun learnContact(name: String) {
        val clean = clean(name) ?: return
        if (clean.length > 40 || clean.any { it.isDigit() }) return
        val existing = db.contextDao().find(ContextEntryEntity.KIND_CONTACT, clean)
        db.contextDao().upsert(
            existing?.copy(usageCount = existing.usageCount + 1)
                ?: ContextEntryEntity(kind = ContextEntryEntity.KIND_CONTACT, term = clean, usageCount = 1)
        )
    }

    suspend fun delete(id: String) = db.contextDao().delete(id)

    suspend fun aliases(): Map<String, String> = db.contextDao().getAll()
        .filter { it.kind == ContextEntryEntity.KIND_ALIAS && it.value.isNotBlank() }
        .associate { it.term to it.value }

    suspend fun contacts(): List<String> = db.contextDao().getAll()
        .filter { it.kind == ContextEntryEntity.KIND_CONTACT }
        .sortedByDescending { it.usageCount }
        .map { it.term }
        .take(30)

    suspend fun routines(): List<ContextEntryEntity> =
        db.contextDao().getAll().filter { it.kind == ContextEntryEntity.KIND_ROUTINE }

    suspend fun log(kind: String, message: String) {
        db.activityDao().insert(ActivityEntity(kind = kind, message = message.take(500)))
        // Keep the log bounded to the last 30 days.
        db.activityDao().deleteBefore(System.currentTimeMillis() - 30L * 24 * 3600 * 1000)
    }

    suspend fun clearLog() = db.activityDao().clearAll()
    suspend fun clearContext() = db.contextDao().clearAll()

    private suspend fun upsertUnique(kind: String, term: String, value: String) {
        val t = clean(term) ?: return
        val existing = db.contextDao().find(kind, t)
        db.contextDao().upsert(
            existing?.copy(value = clean(value).orEmpty())
                ?: ContextEntryEntity(kind = kind, term = t, value = clean(value).orEmpty())
        )
    }

    private fun clean(v: String): String? =
        v.replace(Regex("[\\x00-\\x1F\\x7F]"), " ").trim().take(200).ifBlank { null }

    companion object {
        const val LOG_ANALYSIS_LOCAL = "analysis_local"
        const val LOG_ANALYSIS_CLOUD = "analysis_cloud"
        const val LOG_CALENDAR = "calendar"
        const val LOG_SAVED = "saved"
        const val LOG_REMINDER = "reminder"
        const val LOG_NOTIFICATION = "notification"
        const val LOG_ACTION = "action"
        const val LOG_AUTOMATION = "automation"
    }
}
