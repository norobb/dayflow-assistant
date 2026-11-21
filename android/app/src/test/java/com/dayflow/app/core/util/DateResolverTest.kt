package com.dayflow.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DateResolverTest {
    // Friday 2026-11-20
    private val today = LocalDate.of(2026, 11, 20)

    private fun d(text: String) = DateResolver.findDate(text, today)?.date

    @Test fun relativeWords() {
        assertEquals(today.plusDays(1), d("pick me up tomorrow at 17:30"))
        assertEquals(today.plusDays(1), d("Kannst du mich morgen abholen?"))
        assertEquals(today.plusDays(2), d("übermorgen um 9"))
        assertEquals(today.plusDays(1), d("¿Me recoges mañana a las 5?"))
        assertEquals(today, d("Heute Abend Kino"))
    }

    @Test fun morningIsNotTomorrow() {
        assertNull(d("Guten Morgen zusammen"))
        assertNull(d("por la mañana"))
    }

    @Test fun weekdays() {
        assertEquals(LocalDate.of(2026, 11, 23), d("Remind me next Monday to submit my presentation"))
        assertEquals(LocalDate.of(2026, 11, 23), d("nächsten Montag"))
        assertEquals(LocalDate.of(2026, 11, 24), d("Tuesday • 14:30"))
        assertEquals(LocalDate.of(2026, 11, 20), d("am Freitag"))
        assertEquals(LocalDate.of(2026, 11, 25), d("el miércoles"))
    }

    @Test fun absoluteDates() {
        assertEquals(LocalDate.of(2027, 6, 12), d("June 12–15"))
        assertEquals(LocalDate.of(2027, 6, 12), d("12. Juni"))
        assertEquals(LocalDate.of(2027, 6, 12), d("12 de junio"))
        assertEquals(LocalDate.of(2026, 12, 3), d("am 03.12. um 10 Uhr"))
        assertEquals(LocalDate.of(2027, 1, 5), d("Termin 05.01.2027"))
        assertEquals(LocalDate.of(2026, 12, 24), d("2026-12-24"))
        assertEquals(today.plusDays(3), d("in 3 Tagen"))
    }

    @Test fun times() {
        assertEquals(LocalTime.of(17, 30), DateResolver.findTime("tomorrow at 17:30")?.time)
        assertEquals(LocalTime.of(17, 0), DateResolver.findTime("at 5 pm")?.time)
        assertEquals(LocalTime.of(9, 0), DateResolver.findTime("um 9 Uhr")?.time)
        assertEquals(LocalTime.of(17, 0), DateResolver.findTime("a las 17")?.time)
        assertEquals(LocalTime.of(8, 15), DateResolver.findTime("Abflug 08.15")?.time)
        // A date must not be read as a time.
        val text = "am 03.12. um 10 Uhr"
        val date = DateResolver.findDate(text, today)
        assertEquals(LocalTime.of(10, 0), DateResolver.findTime(text, date?.range)?.time)
    }
}
