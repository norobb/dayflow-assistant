package com.dayflow.app.domain.repository

import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import kotlinx.coroutines.flow.Flow

interface DayflowRepository {
    fun observeEvents(): Flow<List<DayflowEvent>>
    fun observeTasks(): Flow<List<DayflowTask>>
    fun observeReminders(): Flow<List<DayflowReminder>>
    fun observeInsight(): Flow<DayflowInsight>

    suspend fun insertEvent(event: DayflowEvent)
    suspend fun deleteEvent(eventId: String)

    suspend fun insertTask(task: DayflowTask)
    suspend fun toggleTask(taskId: String)
    suspend fun deleteTask(taskId: String)

    suspend fun insertReminder(reminder: DayflowReminder)
    suspend fun dismissReminder(reminderId: String)
    suspend fun deleteReminder(reminderId: String)

    suspend fun setInsight(insight: DayflowInsight)
    suspend fun snoozeInsight()

    suspend fun resetToDefaults(languageCode: String)
}
