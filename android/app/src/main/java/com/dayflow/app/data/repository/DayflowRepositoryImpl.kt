package com.dayflow.app.data.repository

import com.dayflow.app.data.local.AppDatabase
import com.dayflow.app.data.local.EventEntity
import com.dayflow.app.data.local.ReminderEntity
import com.dayflow.app.data.local.TaskEntity
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.repository.DayflowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class DayflowRepositoryImpl(
    private val database: AppDatabase
) : DayflowRepository {

    private val currentInsight = MutableStateFlow(
        DayflowInsight(
            title = "",
            text = "",
            visible = false
        )
    )

    override fun observeEvents(): Flow<List<DayflowEvent>> {
        return database.eventDao().getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTasks(): Flow<List<DayflowTask>> {
        return database.taskDao().getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeReminders(): Flow<List<DayflowReminder>> {
        return database.reminderDao().getActiveReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeInsight(): Flow<DayflowInsight> {
        return currentInsight.asStateFlow()
    }

    override suspend fun insertEvent(event: DayflowEvent) {
        database.eventDao().insertEvent(EventEntity.fromDomain(event))
    }

    override suspend fun deleteEvent(eventId: String) {
        database.eventDao().deleteEvent(eventId)
    }

    override suspend fun insertTask(task: DayflowTask) {
        database.taskDao().insertTask(TaskEntity.fromDomain(task))
    }

    override suspend fun toggleTask(taskId: String) {
        database.taskDao().toggleTask(taskId)
    }

    override suspend fun deleteTask(taskId: String) {
        database.taskDao().deleteTask(taskId)
    }

    override suspend fun insertReminder(reminder: DayflowReminder) {
        database.reminderDao().insertReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun dismissReminder(reminderId: String) {
        database.reminderDao().dismissReminder(reminderId)
    }

    override suspend fun deleteReminder(reminderId: String) {
        database.reminderDao().deleteReminder(reminderId)
    }

    override suspend fun setInsight(insight: DayflowInsight) {
        currentInsight.value = insight
    }

    override suspend fun snoozeInsight() {
        currentInsight.value = currentInsight.value.copy(visible = false)
    }

    override suspend fun resetToDefaults(languageCode: String) {
        // Completely clear all user database records. No sample data populated.
        database.eventDao().clearAll()
        database.taskDao().clearAll()
        database.reminderDao().clearAll()
        currentInsight.value = DayflowInsight(title = "", text = "", visible = false)
    }
}
