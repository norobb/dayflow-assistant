package com.dayflow.app.ai

import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class LocalProviderTest {
    private val provider = LocalProvider()
    private fun run(text: String, lang: String = "en", type: SourceType = SourceType.MESSAGE, aliases: Map<String, String> = emptyMap()) =
        runBlocking { provider.analyze(type, text, null, null, "", "", AnalysisContext(language = lang, aliases = aliases)) }

    private fun hourOf(millis: Long?) = Instant.ofEpochMilli(millis!!).atZone(ZoneId.systemDefault()).let { it.hour to it.minute }

    @Test fun messageScenarioEnglish() {
        val r = run("Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents.")
        assertEquals(1, r.events.size)
        val e = r.events.first()
        assertEquals(17 to 30, hourOf(e.startMillis))
        assertTrue(e.title, e.title.contains("pick me up", ignoreCase = true))
        assertTrue(e.title, !e.title.contains("17:30") && !e.title.contains("tomorrow", true))
        assertEquals("Train station", e.location)
        assertEquals(1, r.tasks.size)
        assertEquals("Bring the documents", r.tasks.first().title)
        assertNotNull("task inherits the pickup time", r.tasks.first().dueMillis)
    }

    @Test fun messageScenarioGerman() {
        val r = run("Hey, kannst du mich morgen um 17:30 am Bahnhof abholen? Und bring bitte die Unterlagen mit.", "de")
        assertEquals(1, r.events.size)
        assertEquals(17 to 30, hourOf(r.events.first().startMillis))
        assertEquals("Morgen", r.events.first().dateLabel)
        assertEquals("Bahnhof", r.events.first().location)
        assertEquals(1, r.tasks.size)
    }

    @Test fun voiceReminderScenario() {
        val r = run("Remind me next Monday to submit my presentation.", type = SourceType.VOICE)
        assertEquals(1, r.reminders.size)
        assertEquals("Submit my presentation", r.reminders.first().title)
        assertEquals(9 to 0, hourOf(r.reminders.first().scheduledTimeMillis))
        assertEquals(1, r.tasks.size)
    }

    @Test fun spanishReminder() {
        val r = run("Recuérdame mañana a las 8 llamar al dentista", "es")
        assertEquals(1, r.reminders.size)
        assertEquals(8 to 0, hourOf(r.reminders.first().scheduledTimeMillis))
    }

    @Test fun screenshotOcrLinesBecomeOneEvent() {
        val ocr = """
            Praxis Dr. Julia Stein
            Confirmed
            Dentist Appointment
            Tuesday • 14:30
            Dental Care Center • Room 4B
        """.trimIndent()
        val r = run(ocr, type = SourceType.SCREENSHOT)
        assertEquals(1, r.events.size)
        val e = r.events.first()
        assertEquals("Dentist Appointment", e.title)
        assertEquals(14 to 30, hourOf(e.startMillis))
        assertEquals("Dental Care Center, Room 4B", e.location)
    }

    @Test fun pdfItinerary() {
        val pdf = """
            School Trip — Barcelona
            June 12, 08:15 Flight departure from Airport Terminal 2
            June 13, 10:30 Sagrada Familia tour
            Please submit the parental consent form by June 5.
            Bring your passport.
        """.trimIndent()
        val r = run(pdf, type = SourceType.PDF)
        assertEquals(r.events.map { it.title }.toString(), 2, r.events.size)
        assertTrue(r.tasks.map { it.title }.toString(), r.tasks.any { it.title.contains("consent", true) })
        assertTrue(r.tasks.any { it.title.contains("passport", true) })
    }

    @Test fun personalVocabularyResolvesPlaces() {
        val r = run("Meeting tomorrow at 10:00 im Büro", "de", aliases = mapOf("Büro" to "Hauptstraße 5, Berlin"))
        assertEquals("Hauptstraße 5, Berlin", r.events.first().location)
        val r2 = run("Projektmeeting am Montag um 10 Uhr im Büro", "de", aliases = mapOf("Büro" to "Friedrichstraße 100"))
        assertEquals("Projektmeeting im Büro", r2.events.first().title)
        assertEquals("Friedrichstraße 100", r2.events.first().location)
        assertEquals(10 to 0, hourOf(r2.events.first().startMillis))
    }

    @Test fun abbreviationsDoNotSplitSentences() {
        val r = run("Zahnarzttermin am Dienstag um 14:30 in der Praxis Dr. Stein. Bitte Versichertenkarte mitbringen.", "de")
        assertTrue(r.events.first().title.startsWith("Zahnarzttermin"))
        assertEquals("Praxis Dr. Stein", r.events.first().location)
        assertEquals("Versichertenkarte mitbringen", r.tasks.first().title)
    }

    @Test fun germanOcrScreenshotWithCompoundNoun() {
        val ocr = "9:41\nMail\nPraxis Dr. Julia Stein\nTerminbestätigung\nZahnarzttermin\nDienstag, 24.11. 14:30 Uhr\n" +
            "Dental Care Center • Raum 4B\nHauptstraße 12, 10115 Berlin\nBitte bringen Sie Ihre Versichertenkarte mit."
        val r = run(ocr, "de", SourceType.SCREENSHOT)
        assertEquals(r.events.toString(), 1, r.events.size)
        assertEquals("Zahnarzttermin", r.events.first().title)
        assertEquals(14 to 30, hourOf(r.events.first().startMillis))
        assertTrue(r.tasks.any { it.title.contains("Versichertenkarte") })
    }

    @Test fun germanPdfItinerary() {
        val pdf = "Klassenfahrt Barcelona\nReiseplan 12.–15. Juni\n12. Juni 08:15 Abflug nach Barcelona, Flughafen Terminal 2\n" +
            "13. Juni 10:30 Führung Sagrada Familia\n14. Juni 18:00 Abschlussessen im Hotel Arts\nVorbereitung\n" +
            "Bitte die Einverständniserklärung bis 5. Juni im Sekretariat abgeben.\nReisepass mitbringen (mindestens 6 Monate gültig)."
        val r = run(pdf, "de", SourceType.PDF)
        assertEquals(r.events.map { it.title }.toString(), 3, r.events.size)
        assertEquals(8 to 15, hourOf(r.events[0].startMillis))
        assertEquals(6, java.time.Instant.ofEpochMilli(r.events[0].startMillis!!).atZone(ZoneId.systemDefault()).monthValue)
        assertEquals(12, java.time.Instant.ofEpochMilli(r.events[0].startMillis!!).atZone(ZoneId.systemDefault()).dayOfMonth)
        assertTrue(r.events[0].title, r.events[0].title.startsWith("Abflug"))
        assertTrue(r.tasks.map { it.title }.toString(), r.tasks.any { it.title.contains("Einverständniserklärung") })
    }

    @Test fun uiLabelsAreNotTasks() {
        val r = run("Suggested reply\nHi! Can we have lunch on Wednesday at 12:30 at Café Central?", type = SourceType.SCREENSHOT)
        assertEquals(0, r.tasks.size)
        assertEquals("Have lunch at Café Central", r.events.single().title)
    }

    @Test fun screenReaderDuplicatesCollapse() {
        val r = run("Treffen wir uns morgen um 19 Uhr im Café Einstein?\n01705552222 said  Treffen wir uns morgen um 19 Uhr im Café Einstein? 12:20 PM", "de", SourceType.SCREENSHOT)
        assertEquals(r.events.map { it.title }.toString(), 1, r.events.size)
        assertEquals("Treffen wir uns im Café Einstein", r.events.single().title)
    }

    @Test fun spanishInvertedQuestionMark() {
        val r = run("Hola, ¿cenamos el viernes a las 21:00 en el restaurante Casa Lucio? Trae el regalo para Marta.", "es")
        assertTrue(r.events.single().title, r.events.single().title.startsWith("Cenamos"))
        assertEquals(21 to 0, hourOf(r.events.single().startMillis))
        assertEquals("Trae el regalo para Marta", r.tasks.single().title)
    }

    @Test fun nothingFound() {
        val r = run("Thanks, that was a great evening!")
        assertEquals(0, r.events.size + r.tasks.size + r.reminders.size)
    }
}
