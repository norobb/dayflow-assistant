package com.dayflow.app.integrations.document

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader

class DocumentParserTest {

    @Test
    fun testSanitizeFileNameStripsPathTraversalAndSeparators() {
        assertEquals("secret_file.pdf", DocumentParser.sanitizeFileName("../secret_file.pdf"))
        assertEquals("passwd", DocumentParser.sanitizeFileName("../../etc/passwd"))
        assertEquals("cmd.exe", DocumentParser.sanitizeFileName("C:\\Windows\\System32\\cmd.exe"))
    }

    @Test
    fun testSanitizeFileNameStripsControlCharacters() {
        val raw = "doc\u0000ument\u0007.pdf"
        assertEquals("document.pdf", DocumentParser.sanitizeFileName(raw))
    }

    @Test
    fun testSanitizeFileNameHandlesNullOrBlank() {
        assertEquals("Document", DocumentParser.sanitizeFileName(null))
        assertEquals("Document", DocumentParser.sanitizeFileName(""))
        assertEquals("Document", DocumentParser.sanitizeFileName("   "))
        assertEquals("Document", DocumentParser.sanitizeFileName("../.."))
    }

    @Test
    fun testExtractSnippetFromReaderBoundsLongLineWithoutOOM() {
        val longLine = "A".repeat(500000)
        val reader = StringReader(longLine)
        val snippet = DocumentParser.extractSnippetFromReader(reader, maxChars = 100)

        assertEquals(100, snippet.length)
        assertTrue(snippet.all { it == 'A' })
    }
}
