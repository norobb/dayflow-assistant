package com.dayflow.app.core.util

import com.dayflow.app.data.local.ContextEntryEntity
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DayInsightsTest {
    private val now = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 21, 12, 0, 0) }.timeInMillis
    private fun at(day: Int, h: Int, m: Int = 0) = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, day, h, m, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

    @Test fun nextEventAndDueTasks() {
        val events = listOf(DayflowEvent(title = "Pick up Alex", time = "17:30", startMillis = at(22, 17, 30), location = "Station"))
        val tasks = listOf(DayflowTask(title = "Presentation", dueMillis = at(22, 9)), DayflowTask(title = "Done", completed = true))
        val insight = DayInsights.contextInsight(events, tasks, "en", now)!!
        assertTrue(insight.text, insight.text.contains("Up next: Pick up Alex, tomorrow at 17:30 (Station)"))
        assertTrue(insight.text, insight.text.contains("“Presentation” is due soon — 1 unfinished task"))
    }

    @Test fun nothingToSay() {
        assertNull(DayInsights.contextInsight(emptyList(), emptyList(), "de", now))
    }

    @Test fun routinesAppearOnTheirWeekday() {
        // 2026-11-21 is a Saturday (ISO 6).
        val run = ContextEntryEntity(kind = ContextEntryEntity.KIND_ROUTINE, term = "Laufen", weekdays = "2,6", time = "18:00")
        val gym = ContextEntryEntity(kind = ContextEntryEntity.KIND_ROUTINE, term = "Gym", weekdays = "1", time = "07:00")
        val today = DayInsights.routinesToday(listOf(run, gym), now)
        assertEquals(listOf("Laufen"), today.map { it.title })
        assertEquals(at(21, 18), today.single().startMillis)
    }

    @Test fun briefingListsTodayInOrder() {
        val events = listOf(
            DayflowEvent(title = "B", time = "", startMillis = at(21, 15)),
            DayflowEvent(title = "A", time = "", startMillis = at(21, 13)),
            DayflowEvent(title = "Tomorrow", time = "", startMillis = at(22, 9))
        )
        val (title, text) = DayInsights.briefing(events, emptyList(), emptyList(), "de", now)
        assertEquals("Guten Morgen – 2 Termine heute", title)
        assertEquals("13:00  A\n15:00  B", text)
    }
}
