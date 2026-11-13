package com.dayflow.app.integrations.accessibility.control

import android.view.accessibility.AccessibilityNodeInfo
import com.dayflow.app.integrations.accessibility.DayflowAccessibilityService
import com.dayflow.app.DayflowApplication
import com.dayflow.app.ai.GeminiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Screen Agent — turns a natural-language goal into a sequence of on-screen
 * actions, mirroring AURA's agent loop: perceive → decide → act → re-perceive.
 *
 * The model is asked for ONE action per step as strict JSON:
 *   {"action":"tap|type|swipe_up|swipe_down|back|done","id":N,"text":"...","reason":"..."}
 * Execution is gated by [DayflowSafetyGate] on every step.
 */
class ScreenAgent(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val gestures = DayflowAccessibilityService.instance?.let { DayflowGestureController(it) }
    private val safetyGate = DayflowSafetyGate()
    private val gemini = GeminiProvider()

    fun run(goal: String) {
        ScreenAgentBus.start(goal)
        scope.launch {
            try {
                execute(goal)
            } catch (e: Exception) {
                ScreenAgentBus.publish(
                    ScreenAgentBus.Step(0, "error", e.message ?: "unknown", done = true)
                )
            } finally {
                ScreenAgentBus.finish()
            }
        }
    }

    private suspend fun execute(goal: String) {
        val service = DayflowAccessibilityService.instance ?: run {
            ScreenAgentBus.publish(ScreenAgentBus.Step(0, "error", "accessibility service not active", done = true))
            return
        }
        val extractor = ScreenElementExtractor(service) { service.packageName }
        val controller = DayflowGestureController(service)

        val prefs = DayflowApplication.instance?.preferencesRepository
        val apiKey = prefs?.getGeminiApiKey()?.let { it.first() } ?: ""
        val model = prefs?.getGeminiModel()?.let { it.first() } ?: "gemini-2.0-flash"

        val maxSteps = 10
        for (step in 1..maxSteps) {
            // Safety: never drive restricted apps.
            val root = service.rootInActiveWindow
            val foreground = root?.packageName?.toString().orEmpty()
            val gate = safetyGate.checkPackage(foreground)
            if (!gate.allowed) {
                ScreenAgentBus.publish(ScreenAgentBus.Step(step, "blocked", gate.reason, done = true))
                return
            }

            extractor.resetIds()
            val elements = extractor.extract()
            val screenText = buildString {
                elements.forEach { appendLine("${it.id}. ${it.label}") }
            }

            val decision = decide(goal, screenText, apiKey, model)
            val action = decision.optString("action", "done")
            val reason = decision.optString("reason", "")
            val id = decision.optInt("id", -1)
            val text = decision.optString("text", "")

            ScreenAgentBus.publish(ScreenAgentBus.Step(step, action, reason))

            when (action) {
                "done" -> return
                "tap" -> {
                    if (id > 0) controller.clickElementById(id) || controller.tap(
                        elements.firstOrNull { it.id == id }?.centerX?.toFloat() ?: 0f,
                        elements.firstOrNull { it.id == id }?.centerY?.toFloat() ?: 0f
                    )
                }
                "type" -> {
                    if (id > 0 && text.isNotBlank()) {
                        val nodeTarget = elements.firstOrNull { it.id == id }
                        if (nodeTarget != null) {
                            val n = findNodeAt(service.rootInActiveWindow, nodeTarget.centerX, nodeTarget.centerY)
                            if (n != null) {
                                val tgate = safetyGate.checkTypeTarget(n, text)
                                if (tgate.allowed) {
                                    val args = android.os.Bundle().apply {
                                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                                    }
                                    n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                                } else {
                                    ScreenAgentBus.publish(ScreenAgentBus.Step(step, "blocked", tgate.reason))
                                }
                                try { n.recycle() } catch (_: Exception) {}
                            }
                        }
                    }
                }
                "swipe_up" -> {
                    val m = service.resources.displayMetrics
                    controller.swipe(m.widthPixels / 2f, m.heightPixels * 0.75f, m.widthPixels / 2f, m.heightPixels * 0.30f)
                }
                "swipe_down" -> {
                    val m = service.resources.displayMetrics
                    controller.swipe(m.widthPixels / 2f, m.heightPixels * 0.30f, m.widthPixels / 2f, m.heightPixels * 0.75f)
                }
                "back" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            }

            delay(900L)
        }

        ScreenAgentBus.publish(ScreenAgentBus.Step(maxSteps + 1, "done", "step limit reached", done = true))
    }

    private suspend fun decide(goal: String, screenText: String, apiKey: String, model: String): JSONObject {
        val prompt = """
            You are a phone-control agent. Goal: "$goal"

            Current screen elements (id. label):
            $screenText

            Decide the SINGLE next action to make progress toward the goal.
            Reply ONLY with a JSON object:
            {"action":"tap|type|swipe_up|swipe_down|back|done","id":<element id or -1>,"text":"<text to type, if type>","reason":"<short reason>"}
            Use "done" when the goal is achieved or no further action helps.
        """.trimIndent()

        val raw = if (apiKey.isNotBlank()) {
            gemini.complete(prompt, apiKey, model)
        } else {
            localDecide(goal, screenText)
        }

        return parseJson(raw)
    }

    private fun localDecide(goal: String, screenText: String): String {
        val g = goal.lowercase()
        val lines = screenText.lines()
        // Scroll intents
        if (g.contains("runter") || g.contains("down") || g.contains("abajo")) {
            return """{"action":"swipe_down","id":-1,"text":"","reason":"scroll down"}"""
        }
        if (g.contains("hoch") || g.contains("up") || g.contains("arriba")) {
            return """{"action":"swipe_up","id":-1,"text":"","reason":"scroll up"}"""
        }
        if (g.contains("zurück") || g.contains("back") || g.contains("atrás") || g.contains("atras")) {
            return """{"action":"back","id":-1,"text":"","reason":"go back"}"""
        }
        // "tippe/tap/toca X" → element whose label contains X
        val target = g.replace(Regex(".*(tippe|tap|toca|click)\\s*"), "").trim()
        if (target.isNotBlank()) {
            for (line in lines) {
                val parts = line.split(".", limit = 2)
                if (parts.size == 2 && parts[1].trim().contains(target, ignoreCase = true)) {
                    val id = parts[0].trim().toIntOrNull() ?: continue
                    return """{"action":"tap","id":$id,"text":"","reason":"tap $target"}"""
                }
            }
        }
        return """{"action":"done","id":-1,"text":"","reason":"no API key and goal not understood locally"}"""
    }

    private fun parseJson(raw: String): JSONObject {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return try { JSONObject(cleaned) } catch (_: Exception) {
            // Try to extract first {...}
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            if (start >= 0 && end > start) {
                try { JSONObject(cleaned.substring(start, end + 1)) } catch (_: Exception) { JSONObject("""{"action":"done","id":-1,"text":"","reason":"parse error"}""") }
            } else JSONObject("""{"action":"done","id":-1,"text":"","reason":"empty"}""")
        }
    }

    private fun findNodeAt(node: AccessibilityNodeInfo?, x: Int, y: Int): AccessibilityNodeInfo? {
        if (node == null) return null
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        if (!rect.contains(x, y)) return null
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (_: Exception) { null } ?: continue
            val found = findNodeAt(child, x, y)
            if (found != null) return found
        }
        return node
    }
}
