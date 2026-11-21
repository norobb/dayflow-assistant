package com.dayflow.app.core.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent

/**
 * Verified smart actions: Dayflow proposes, the user confirms, and the target
 * app performs it (browser, maps, dialer, messenger). Dayflow itself never
 * sends a message or places a call; drafts are handed to the messenger where
 * the user presses send.
 */
data class SmartAction(
    val kind: Kind,
    /** Link, place, phone number, e-mail address or recipient. */
    val target: String,
    /** Prefilled draft text for message actions. */
    val draft: String = ""
) {
    enum class Kind { OPEN_LINK, NAVIGATE, CALL, EMAIL, REPLY_DRAFT, LATE_DRAFT }
    val needsConfirmation: Boolean get() = kind == Kind.REPLY_DRAFT || kind == Kind.LATE_DRAFT
}

object SmartActionPlanner {
    private val url = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
    private val email = Regex("[\\w.+-]+@[\\w-]+\\.[\\w.]+")
    private val phone = Regex("(?<![\\d])(?:\\+\\d{1,3}[ \\-]?)?(?:\\(?\\d{2,5}\\)?[ \\-/]?)\\d{3,4}[ \\-]?\\d{3,5}(?![\\d])")

    fun plan(result: DayflowAnalysisResult, sender: String?, language: String): List<SmartAction> {
        val text = result.inputSnippet
        val actions = mutableListOf<SmartAction>()
        url.findAll(text).map { it.value.trimEnd('.', ',', ')') }.distinct().take(2).forEach { actions += SmartAction(SmartAction.Kind.OPEN_LINK, it) }
        result.events.mapNotNull { it.location }.distinct().take(2).forEach { actions += SmartAction(SmartAction.Kind.NAVIGATE, it) }
        email.findAll(text).map { it.value }.distinct().take(1).forEach { actions += SmartAction(SmartAction.Kind.EMAIL, it) }
        phone.findAll(text).map { it.value.trim() }.filter { p -> p.count(Char::isDigit) >= 7 && !Regex("\\d{1,2}[.:]\\d{2}").matches(p) }
            .distinct().take(1).forEach { actions += SmartAction(SmartAction.Kind.CALL, it) }
        val first = result.events.firstOrNull()
        if (first != null && result.sourceType.identifier == "message") {
            // "Max: Can you pick me up…" (from a notification review) names the sender.
            val who = sender ?: Regex("^([\\p{L}\\d+ .'-]{2,30}):\\s").find(text)?.groupValues?.get(1)
            // Only a message from someone can be answered.
            if (!who.isNullOrBlank()) actions += SmartAction(SmartAction.Kind.REPLY_DRAFT, who, replyDraft(first, who, language))
        }
        return actions
    }

    fun replyDraft(event: DayflowEvent, sender: String?, language: String): String {
        val name = sender?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
        val time = event.time.ifBlank { null }
        val day = event.dateLabel.ifBlank { null }?.lowercase()
        return when (language) {
            "de" -> "Hallo$name, passt! " + listOfNotNull(day, time?.let { "um $it" }).joinToString(" ").let { if (it.isBlank()) "Bis dann." else "Bis $it." }
            "es" -> "¡Hola$name, perfecto! " + listOfNotNull(day, time?.let { "a las $it" }).joinToString(" ").let { if (it.isBlank()) "Hasta luego." else "Nos vemos $it." }
            else -> "Hi$name, sounds good! " + listOfNotNull(day, time?.let { "at $it" }).joinToString(" ").let { if (it.isBlank()) "See you then." else "See you $it." }
        }
    }

    fun lateDraft(event: DayflowEvent, recipient: String?, language: String, minutes: Int = 10): String {
        val name = recipient?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
        return when (language) {
            "de" -> "Hey$name, ich verspäte mich etwa $minutes Minuten (${event.title})."
            "es" -> "Hola$name, llegaré unos $minutes minutos tarde (${event.title})."
            else -> "Hey$name, I'll be running about $minutes minutes late (${event.title})."
        }
    }

    /** The intent that performs [action] in another app. Drafts open the share sheet / SMS composer, never auto-send. */
    fun intent(action: SmartAction, draftOverride: String? = null): Intent = when (action.kind) {
        SmartAction.Kind.OPEN_LINK -> Intent(Intent.ACTION_VIEW, Uri.parse(action.target))
        SmartAction.Kind.NAVIGATE -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(action.target)))
        SmartAction.Kind.CALL -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + action.target.filter { it.isDigit() || it == '+' }))
        SmartAction.Kind.EMAIL -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + action.target))
        // A phone number as recipient: prefilled SMS to exactly that person.
        SmartAction.Kind.REPLY_DRAFT, SmartAction.Kind.LATE_DRAFT -> if (action.target.matches(Regex("^\\+?[\\d ()/-]{6,}$"))) {
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + action.target.filter { it.isDigit() || it == '+' }))
                .putExtra("sms_body", draftOverride ?: action.draft)
        } else Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, draftOverride ?: action.draft),
            null
        )
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun launch(context: Context, action: SmartAction, draftOverride: String? = null): Boolean =
        runCatching { context.startActivity(intent(action, draftOverride)) }.isSuccess
}
