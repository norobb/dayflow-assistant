package com.dayflow.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Sparkles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.core.designsystem.DayflowBg
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.core.designsystem.DayflowTheme
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.integrations.document.DocumentParser
import com.dayflow.app.ui.components.VerificationCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DayflowApplication
        val documentParser = DocumentParser(this)

        setContent {
            DayflowTheme {
                ShareIntakeScreen(
                    intent = intent,
                    app = app,
                    documentParser = documentParser,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
fun ShareIntakeScreen(
    intent: Intent,
    app: DayflowApplication,
    documentParser: DocumentParser,
    onFinish: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<DayflowAnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(intent) {
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            val provider = app.preferencesRepository.getAiProvider().first()

            when {
                type == "text/plain" -> {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                    val result = app.analyzer.analyze(provider, SourceType.MESSAGE, sharedText)
                    analysisResult = result
                    isAnalyzing = false
                    app.soundAndHaptics.playSuccess()
                }

                type.startsWith("image/") -> {
                    val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                    }
                    val snippet = imageUri?.let { documentParser.getFileName(it) } ?: "Shared screenshot"
                    val result = app.analyzer.analyze(provider, SourceType.SCREENSHOT, snippet)
                    analysisResult = result
                    isAnalyzing = false
                    app.soundAndHaptics.playSuccess()
                }

                type == "application/pdf" || type.startsWith("application/") -> {
                    val docUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                    }
                    val snippet = docUri?.let { documentParser.extractSnippet(it) } ?: "Shared PDF"
                    val result = app.analyzer.analyze(provider, SourceType.PDF, snippet)
                    analysisResult = result
                    isAnalyzing = false
                    app.soundAndHaptics.playSuccess()
                }

                type.startsWith("audio/") -> {
                    val result = app.analyzer.analyze(provider, SourceType.VOICE, null)
                    analysisResult = result
                    isAnalyzing = false
                    app.soundAndHaptics.playSuccess()
                }

                else -> {
                    isAnalyzing = false
                    errorMessage = "Unsupported shared content"
                }
            }
        } else {
            isAnalyzing = false
            errorMessage = "No shared content received"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DayflowBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sparkles,
                        contentDescription = null,
                        tint = DayflowPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(R.string.share_received_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                }
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DayflowText
                    )
                }
            }

            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = DayflowPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = stringResource(R.string.share_analyzing),
                            fontSize = 14.sp,
                            color = DayflowTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (analysisResult != null) {
                val result = analysisResult!!
                VerificationCard(
                    result = result,
                    onAddCalendar = {
                        coroutineScope.launch {
                            result.events.firstOrNull()?.let { ev ->
                                val event = DayflowEvent(
                                    id = "ev-${System.currentTimeMillis()}",
                                    time = ev.time,
                                    title = ev.title,
                                    location = ev.location,
                                    dateLabel = ev.dateLabel,
                                    sourceType = result.sourceType
                                )
                                app.repository.insertEvent(event)
                                app.calendarService.insertEvent(event)
                                app.soundAndHaptics.playSuccess()
                            }
                        }
                    },
                    onAddTask = {
                        coroutineScope.launch {
                            result.tasks.firstOrNull()?.let { tk ->
                                val task = DayflowTask(
                                    id = "tk-${System.currentTimeMillis()}",
                                    title = tk.title,
                                    completed = false,
                                    dueDate = tk.dueDate,
                                    sourceType = result.sourceType
                                )
                                app.repository.insertTask(task)
                                app.soundAndHaptics.playSuccess()
                            }
                        }
                    },
                    onAddReminder = {
                        coroutineScope.launch {
                            result.reminders.firstOrNull()?.let { rm ->
                                val reminder = DayflowReminder(
                                    id = "rm-${System.currentTimeMillis()}",
                                    title = rm.title,
                                    timeLabel = rm.timeLabel,
                                    sourceType = result.sourceType
                                )
                                app.repository.insertReminder(reminder)
                                app.notificationHelper.scheduleReminder(reminder)
                                app.soundAndHaptics.playSuccess()
                            }
                        }
                    },
                    onAddBoth = {
                        coroutineScope.launch {
                            result.events.firstOrNull()?.let { ev ->
                                val event = DayflowEvent(
                                    id = "ev-${System.currentTimeMillis()}",
                                    time = ev.time,
                                    title = ev.title,
                                    location = ev.location,
                                    dateLabel = ev.dateLabel,
                                    sourceType = result.sourceType
                                )
                                app.repository.insertEvent(event)
                                app.calendarService.insertEvent(event)
                            }
                            result.tasks.firstOrNull()?.let { tk ->
                                val task = DayflowTask(
                                    id = "tk-${System.currentTimeMillis()}",
                                    title = tk.title,
                                    completed = false,
                                    dueDate = tk.dueDate,
                                    sourceType = result.sourceType
                                )
                                app.repository.insertTask(task)
                            }
                            result.reminders.firstOrNull()?.let { rm ->
                                val reminder = DayflowReminder(
                                    id = "rm-${System.currentTimeMillis()}",
                                    title = rm.title,
                                    timeLabel = rm.timeLabel,
                                    sourceType = result.sourceType
                                )
                                app.repository.insertReminder(reminder)
                                app.notificationHelper.scheduleReminder(reminder)
                            }
                            app.soundAndHaptics.playSuccess()
                        }
                    }
                )
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: stringResource(R.string.share_error_unsupported),
                        fontSize = 13.sp,
                        color = DayflowTextMuted
                    )
                }
            }
        }
    }
}
