package com.dayflow.app.ui.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dayflow.app.DayflowApplication
import com.dayflow.app.R
import com.dayflow.app.ai.ClaudeProvider
import com.dayflow.app.ai.DayflowAnalyzer
import com.dayflow.app.ai.OpenAICompatibleProvider
import com.dayflow.app.data.local.ContextEntryEntity
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.PrefKeys
import com.dayflow.app.integrations.automation.AutomationScheduler
import com.dayflow.app.ui.components.DayflowCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun CardHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun BoolPrefRow(key: String, default: Boolean, title: String, desc: String? = null, onChanged: (Boolean) -> Unit = {}) {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    val value by app.preferencesRepository.boolPref(key, default).collectAsState(initial = default)
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Switch) {
            scope.launch { app.preferencesRepository.setBoolPref(key, !value); app.soundAndHaptics.playToggle(); onChanged(!value) }
        }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            if (desc != null) Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = value, onCheckedChange = null)
    }
}

@Composable
private fun TextPrefField(key: String, default: String, label: String, placeholder: String = "", secret: Boolean = false, numeric: Boolean = false, onChanged: () -> Unit = {}) {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    StoredTextField(
        load = { app.preferencesRepository.stringPref(key, default).first() },
        save = { v -> scope.launch { app.preferencesRepository.setStringPref(key, v.trim()); onChanged() } },
        label = label, placeholder = placeholder, secret = secret, numeric = numeric
    )
}

/** Text field backed by a stored value: loaded once, then the user's edits (including clearing) win. */
@Composable
private fun StoredTextField(
    load: suspend () -> String,
    save: (String) -> Unit,
    label: String,
    placeholder: String = "",
    secret: Boolean = false,
    numeric: Boolean = false
) {
    var text by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { if (text == null) text = load() }
    OutlinedTextField(
        value = text.orEmpty(),
        onValueChange = { v -> text = v; save(v) },
        enabled = text != null,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = when { secret -> KeyboardType.Password; numeric -> KeyboardType.Number; else -> KeyboardType.Uri }),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

// ----------------------------------------------------------------------
// AI providers
// ----------------------------------------------------------------------

@Composable
fun AiProvidersSection(selected: String, geminiKey: String, geminiModel: String, onSelect: (String) -> Unit, onGeminiKey: (String) -> Unit, onGeminiModel: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    val providers = listOf(
        DayflowAnalyzer.LOCAL to (R.string.provider_local to R.string.provider_local_desc),
        DayflowAnalyzer.GEMINI to (R.string.provider_gemini to R.string.provider_gemini_desc),
        DayflowAnalyzer.CLAUDE to (R.string.provider_claude to R.string.provider_claude_desc),
        DayflowAnalyzer.OPENAI to (R.string.provider_openai to R.string.provider_openai_desc)
    )
    val sample = stringResource(R.string.provider_test_sample)
    val okText = stringResource(R.string.provider_test_ok)
    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardHeader(Icons.Outlined.AutoAwesome, stringResource(R.string.settings_ai_provider_model))
            providers.forEach { (id, res) ->
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.RadioButton) { onSelect(id); testResult = null },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == id, onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(stringResource(res.first), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(stringResource(res.second), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            when (selected) {
                DayflowAnalyzer.GEMINI -> {
                    StoredTextField(load = { app.preferencesRepository.getGeminiApiKey().first() }, save = onGeminiKey, label = stringResource(R.string.settings_ai_gemini_key), secret = true)
                    StoredTextField(load = { app.preferencesRepository.getGeminiModel().first() }, save = onGeminiModel, label = stringResource(R.string.settings_ai_model_id), placeholder = "gemini-3.5-flash")
                    TextPrefField(PrefKeys.baseUrl("gemini"), "", stringResource(R.string.provider_gateway), com.dayflow.app.ai.GeminiProvider.DEFAULT_ENDPOINT)
                }
                DayflowAnalyzer.CLAUDE -> {
                    TextPrefField(PrefKeys.baseUrl("claude"), "", stringResource(R.string.provider_base_url), "http://192.168.1.10:11434")
                    TextPrefField(PrefKeys.apiKey("claude"), "", stringResource(R.string.provider_api_key_optional), "", secret = true)
                    TextPrefField(PrefKeys.model("claude"), "", stringResource(R.string.settings_ai_model_id), ClaudeProvider.DEFAULT_MODEL)
                }
                DayflowAnalyzer.OPENAI -> {
                    TextPrefField(PrefKeys.baseUrl("openai"), "", stringResource(R.string.provider_base_url), OpenAICompatibleProvider.DEFAULT_BASE_URL)
                    TextPrefField(PrefKeys.apiKey("openai"), "", stringResource(R.string.provider_api_key_optional), "sk-…", secret = true)
                    TextPrefField(PrefKeys.model("openai"), "", stringResource(R.string.settings_ai_model_id), OpenAICompatibleProvider.DEFAULT_MODEL)
                }
            }
            if (selected != DayflowAnalyzer.LOCAL) {
                Text(stringResource(R.string.provider_privacy_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(enabled = !testing, onClick = {
                testing = true; testResult = null
                scope.launch {
                    val r = withContext(Dispatchers.IO) { app.analyzer.analyze(SourceType.MESSAGE, sample) }
                    testResult = if (r.errorMessage != null) "⚠ " + r.errorMessage
                    else okText + " " + (r.events.firstOrNull()?.let { "${it.title} · ${it.dateLabel} ${it.time}" } ?: r.summaryText)
                    testing = false
                }
            }) { Text(stringResource(if (testing) R.string.provider_testing else R.string.provider_test)) }
            testResult?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

// ----------------------------------------------------------------------
// Calendar sync
// ----------------------------------------------------------------------

@Composable
fun CalendarSection() {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    var granted by remember { mutableStateOf(app.calendarService.hasPermission()) }
    var calendars by remember { mutableStateOf(app.calendarService.writableCalendars()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        granted = map.values.all { it }
        calendars = app.calendarService.writableCalendars()
    }
    val chosen by app.preferencesRepository.stringPref(PrefKeys.CALENDAR_ID).collectAsState(initial = "")
    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardHeader(Icons.Outlined.CalendarMonth, stringResource(R.string.calendar_sync_title))
            BoolPrefRow(PrefKeys.CALENDAR_SYNC, true, stringResource(R.string.calendar_sync_toggle), stringResource(R.string.calendar_sync_desc))
            if (!granted) {
                TextButton(onClick = { launcher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)) }) {
                    Text(stringResource(R.string.calendar_grant))
                }
            } else {
                Text(stringResource(R.string.calendar_target), style = MaterialTheme.typography.labelLarge)
                if (calendars.isEmpty()) {
                    Text(stringResource(R.string.calendar_none), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val auto = chosen.isBlank() || calendars.none { it.id.toString() == chosen }
                Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton) {
                    scope.launch { app.preferencesRepository.setStringPref(PrefKeys.CALENDAR_ID, "") }
                }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = auto, onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.calendar_auto), style = MaterialTheme.typography.bodyMedium)
                }
                calendars.forEach { cal ->
                    Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton) {
                        scope.launch { app.preferencesRepository.setStringPref(PrefKeys.CALENDAR_ID, cal.id.toString()) }
                    }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !auto && chosen == cal.id.toString(), onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(cal.name, style = MaterialTheme.typography.bodyMedium)
                            Text(cal.account, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// Personal context
// ----------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalContextSection() {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    val entries by app.personalRepository.observeContext().collectAsState(initial = emptyList())
    var dialog by remember { mutableStateOf<String?>(null) }
    val dayNames = remember { java.time.DayOfWeek.entries.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) } }

    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardHeader(Icons.Outlined.Person, stringResource(R.string.context_title))
            Text(stringResource(R.string.context_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf(
                ContextEntryEntity.KIND_ALIAS to R.string.context_vocabulary,
                ContextEntryEntity.KIND_ROUTINE to R.string.context_routines,
                ContextEntryEntity.KIND_CONTACT to R.string.context_contacts
            ).forEach { (kind, titleRes) ->
                Text(stringResource(titleRes), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                val list = entries.filter { it.kind == kind }
                if (list.isEmpty()) {
                    Text(stringResource(R.string.context_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                list.take(15).forEach { e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            when (kind) {
                                ContextEntryEntity.KIND_ALIAS -> "${e.term} → ${e.value}"
                                ContextEntryEntity.KIND_ROUTINE -> "${e.term} · " +
                                    e.weekdays.split(',').mapNotNull { it.toIntOrNull() }.joinToString(", ") { dayNames[it - 1] } +
                                    (if (e.time.isNotBlank()) " · ${e.time}" else "")
                                else -> "${e.term} (${e.usageCount}×)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { scope.launch { app.personalRepository.delete(e.id) } }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { dialog = ContextEntryEntity.KIND_ALIAS }) { Text(stringResource(R.string.context_add_term)) }
                TextButton(onClick = { dialog = ContextEntryEntity.KIND_ROUTINE }) { Text(stringResource(R.string.context_add_routine)) }
                TextButton(onClick = { dialog = ContextEntryEntity.KIND_CONTACT }) { Text(stringResource(R.string.context_add_contact)) }
            }
            BoolPrefRow(PrefKeys.AUTO_LEARN_CONTACTS, true, stringResource(R.string.context_learn_contacts), stringResource(R.string.context_learn_contacts_desc))
        }
    }

    dialog?.let { kind ->
        var term by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }
        var days by remember { mutableStateOf(setOf<Int>()) }
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = {
                Text(stringResource(when (kind) {
                    ContextEntryEntity.KIND_ALIAS -> R.string.context_add_term
                    ContextEntryEntity.KIND_ROUTINE -> R.string.context_add_routine
                    else -> R.string.context_add_contact
                }))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(term, { term = it }, singleLine = true, label = {
                        Text(stringResource(when (kind) {
                            ContextEntryEntity.KIND_ALIAS -> R.string.context_field_term
                            ContextEntryEntity.KIND_ROUTINE -> R.string.verify_field_title
                            else -> R.string.context_field_name
                        }))
                    })
                    if (kind == ContextEntryEntity.KIND_ALIAS) {
                        OutlinedTextField(value, { value = it }, singleLine = true, label = { Text(stringResource(R.string.context_field_meaning)) })
                    }
                    if (kind == ContextEntryEntity.KIND_ROUTINE) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..7).forEach { d ->
                                FilterChip(selected = d in days, onClick = { days = if (d in days) days - d else days + d }, label = { Text(dayNames[d - 1]) })
                            }
                        }
                        OutlinedTextField(time, { time = it }, singleLine = true, label = { Text(stringResource(R.string.context_field_time)) }, placeholder = { Text("18:00") })
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = term.isNotBlank() && (kind != ContextEntryEntity.KIND_ALIAS || value.isNotBlank()) && (kind != ContextEntryEntity.KIND_ROUTINE || days.isNotEmpty()), onClick = {
                    scope.launch {
                        when (kind) {
                            ContextEntryEntity.KIND_ALIAS -> app.personalRepository.addAlias(term, value)
                            ContextEntryEntity.KIND_ROUTINE -> app.personalRepository.addRoutine(term, days, time.trim().let { t -> if (Regex("^\\d:\\d\\d$").matches(t)) "0$t" else t })
                            else -> app.personalRepository.addContact(term)
                        }
                        app.soundAndHaptics.playSuccess()
                    }
                    dialog = null
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
}

// ----------------------------------------------------------------------
// Automations
// ----------------------------------------------------------------------

@Composable
fun AutomationsSection() {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    val reschedule: () -> Unit = { scope.launch { AutomationScheduler.reschedule(app) } }
    val briefingOn by app.preferencesRepository.boolPref(PrefKeys.AUTO_BRIEFING, false).collectAsState(initial = false)
    val reminderOn by app.preferencesRepository.boolPref(PrefKeys.AUTO_EVENT_REMINDER, true).collectAsState(initial = true)
    var ran by remember { mutableStateOf(false) }
    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CardHeader(Icons.Outlined.AutoMode, stringResource(R.string.auto_title))
            Text(stringResource(R.string.auto_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BoolPrefRow(PrefKeys.AUTO_BRIEFING, false, stringResource(R.string.auto_briefing), stringResource(R.string.auto_briefing_desc), onChanged = { reschedule() })
            if (briefingOn) TextPrefField(PrefKeys.BRIEFING_TIME, "07:30", stringResource(R.string.auto_briefing_time), "07:30", onChanged = reschedule)
            BoolPrefRow(PrefKeys.AUTO_EVENT_REMINDER, true, stringResource(R.string.auto_event_reminder), stringResource(R.string.auto_event_reminder_desc))
            if (reminderOn) TextPrefField(PrefKeys.EVENT_REMINDER_LEAD, "30", stringResource(R.string.auto_event_reminder_lead), "30", numeric = true)
            BoolPrefRow(PrefKeys.SHADE_CHIPS, true, stringResource(R.string.shade_chips), stringResource(R.string.shade_chips_desc))
            BoolPrefRow(PrefKeys.AUTO_NOTIFICATION_SUGGESTIONS, false, stringResource(R.string.auto_suggestions), stringResource(R.string.auto_suggestions_desc))
            BoolPrefRow(PrefKeys.AUTO_CLEANUP, false, stringResource(R.string.auto_cleanup), stringResource(R.string.auto_cleanup_desc), onChanged = { reschedule() })
            TextButton(onClick = { scope.launch { AutomationScheduler.runDaily(app); ran = true } }) {
                Text(stringResource(if (ran) R.string.auto_ran else R.string.auto_run_now))
            }
        }
    }
}

// ----------------------------------------------------------------------
// Transparency log
// ----------------------------------------------------------------------

@Composable
fun ActivityLogSection() {
    val app = LocalContext.current.applicationContext as DayflowApplication
    val scope = rememberCoroutineScope()
    val log by app.personalRepository.observeLog().collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault()) }
    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CardHeader(Icons.Outlined.History, stringResource(R.string.log_title))
            Text(stringResource(R.string.log_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (log.isEmpty()) Text(stringResource(R.string.log_empty), style = MaterialTheme.typography.bodySmall)
            log.take(if (expanded) 100 else 6).forEach { e ->
                Row {
                    Text(fmt.format(Date(e.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(76.dp))
                    Text(e.message, style = MaterialTheme.typography.bodySmall, color = if (e.kind == "analysis_cloud") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface)
                }
            }
            Row {
                if (log.size > 6) TextButton(onClick = { expanded = !expanded }) { Text(stringResource(if (expanded) R.string.log_less else R.string.log_more)) }
                if (log.isNotEmpty()) TextButton(onClick = { scope.launch { app.personalRepository.clearLog() } }) { Text(stringResource(R.string.log_clear)) }
            }
        }
    }
}

/** Whether Dayflow currently holds notification access (listener). */
fun hasNotificationAccess(context: android.content.Context): Boolean =
    androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
