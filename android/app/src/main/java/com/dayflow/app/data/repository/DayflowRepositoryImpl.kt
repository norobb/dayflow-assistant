package com.dayflow.app.data.repository

import com.dayflow.app.data.local.AppDatabase
import com.dayflow.app.data.local.EventEntity
import com.dayflow.app.data.local.ReminderEntity
import com.dayflow.app.data.local.TaskEntity
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
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
            title = "Context Insight",
            text = "Your afternoon is tightly scheduled. Project milestone review begins at 15:00.",
            visible = true
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
        database.eventDao().clearAll()
        database.taskDao().clearAll()
        database.reminderDao().clearAll()

        when (languageCode) {
            "de" -> {
                database.eventDao().insertAll(
                    listOf(
                        EventEntity("ev-1", "09:00", "Physik Vorlesung", "Hörsaal B", "Heute", "default", System.currentTimeMillis()),
                        EventEntity("ev-2", "11:30", "Zahnarzt Kontrolltermin", "Praxis Dr. Stein", "Heute", "default", System.currentTimeMillis()),
                        EventEntity("ev-3", "15:00", "Projekt Meilenstein Review", "Design Studio", "Heute", "default", System.currentTimeMillis()),
                        EventEntity("ev-4", "18:30", "Abendessen im Vapiano", "Hauptbahnhof", "Heute", "default", System.currentTimeMillis())
                    )
                )
                database.taskDao().insertAll(
                    listOf(
                        TaskEntity("tk-1", "Seminarfolien durchsehen", true, "Heute 08:30", "default", System.currentTimeMillis()),
                        TaskEntity("tk-2", "Projektunterlagen ausdrucken", false, "Heute 14:00", "default", System.currentTimeMillis()),
                        TaskEntity("tk-3", "Dokumente mitbringen", false, "Morgen 17:30", "default", System.currentTimeMillis())
                    )
                )
                database.reminderDao().insertAll(
                    listOf(
                        ReminderEntity("rm-1", "Dokumente zum Bahnhof mitbringen", "Morgen 17:00", System.currentTimeMillis() + 86400000L, "default", false, System.currentTimeMillis())
                    )
                )
                currentInsight.value = DayflowInsight(
                    title = "Kontexthinweis",
                    text = "Deine Präsentationsabgabe ist morgen. Es stehen noch 2 Vorbereitungsaufgaben aus.",
                    visible = true
                )
            }
            "es" -> {
                database.eventDao().insertAll(
                    listOf(
                        EventEntity("ev-1", "09:00", "Clase de Física", "Aula Magna", "Hoy", "default", System.currentTimeMillis()),
                        EventEntity("ev-2", "11:30", "Revisión dental", "Clínica Dra. Stein", "Hoy", "default", System.currentTimeMillis()),
                        EventEntity("ev-3", "15:00", "Revisión del proyecto", "Estudio de Diseño", "Hoy", "default", System.currentTimeMillis()),
                        EventEntity("ev-4", "18:30", "Cena en Vapiano", "Estación Central", "Hoy", "default", System.currentTimeMillis())
                    )
                )
                database.taskDao().insertAll(
                    listOf(
                        TaskEntity("tk-1", "Revisar diapositivas del seminario", true, "Hoy 08:30", "default", System.currentTimeMillis()),
                        TaskEntity("tk-2", "Imprimir resumen del proyecto", false, "Hoy 14:00", "default", System.currentTimeMillis()),
                        TaskEntity("tk-3", "Traer los documentos", false, "Mañana 17:30", "default", System.currentTimeMillis())
                    )
                )
                database.reminderDao().insertAll(
                    listOf(
                        ReminderEntity("rm-1", "Traer los documentos a la estación", "Mañana 17:00", System.currentTimeMillis() + 86400000L, "default", false, System.currentTimeMillis())
                    )
                )
                currentInsight.value = DayflowInsight(
                    title = "Información de contexto",
                    text = "La entrega de la presentación es mañana. Tienes 2 tareas pendientes.",
                    visible = true
                )
            }
            else -> {
                database.eventDao().insertAll(
                    listOf(
                        EventEntity("ev-1", "09:00", "Physics Lecture", "Hall B", "Today", "default", System.currentTimeMillis()),
                        EventEntity("ev-2", "11:30", "Dentist Checkup", "Dr. Stein Praxis", "Today", "default", System.currentTimeMillis()),
                        EventEntity("ev-3", "15:00", "Project Milestone Review", "Design Studio", "Today", "default", System.currentTimeMillis()),
                        EventEntity("ev-4", "18:30", "Dinner at Vapiano", "Central Station", "Today", "default", System.currentTimeMillis())
                    )
                )
                database.taskDao().insertAll(
                    listOf(
                        TaskEntity("tk-1", "Review physics seminar slides", true, "Today 08:30", "default", System.currentTimeMillis()),
                        TaskEntity("tk-2", "Print milestone summary handout", false, "Today 14:00", "default", System.currentTimeMillis()),
                        TaskEntity("tk-3", "Bring documents to station", false, "Tomorrow 17:30", "default", System.currentTimeMillis())
                    )
                )
                database.reminderDao().insertAll(
                    listOf(
                        ReminderEntity("rm-1", "Bring documents to station", "Tomorrow 17:00", System.currentTimeMillis() + 86400000L, "default", false, System.currentTimeMillis())
                    )
                )
                currentInsight.value = DayflowInsight(
                    title = "Context Insight",
                    text = "Your afternoon is tightly scheduled. Project milestone review begins at 15:00.",
                    visible = true
                )
            }
        }
    }
}
