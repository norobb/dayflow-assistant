package com.dayflow.app

import com.dayflow.app.R
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.core.designsystem.DayflowTheme
import com.dayflow.app.core.util.DayflowLocaleProvider
import com.dayflow.app.integrations.document.DocumentParser
import com.dayflow.app.ui.components.VerificationCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as DayflowApplication

        setContent {
            DayflowTheme {
                ShareScreen(
                    intent = intent,
                    app = app,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
private fun ShareScreen(
    intent: Intent,
    app: DayflowApplication,
    onFinish: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val documentParser = remember { DocumentParser(app.applicationContext) }

    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<DayflowAnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedCalendarEvents by remember { mutableStateOf<List<DayflowEvent>>(emptyList()) }
    var isRetryingCalendar by remember { mutableStateOf(false) }
    var hasAcceptedResult by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("en") }

    LaunchedEffect(intent) {
        language = app.preferencesRepository.getLanguage().first()
        val type = intent.type
        if (intent.action != Intent.ACTION_SEND || type == null) {
            isAnalyzing = false
            errorMessage = app.getString(R.string.share_error_unsupported)
            return@LaunchedEffect
        }
        // SECURITY: bound shared text (10k chars) and files (10 MB) to avoid memory exhaustion.
        val sharedText = (intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.getStringExtra(Intent.EXTRA_SUBJECT))?.take(10000)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        }
        val sourceType = when {
            type.startsWith("image/") -> SourceType.SCREENSHOT
            type == "application/pdf" -> SourceType.PDF
            type.startsWith("audio/") -> SourceType.VOICE
            else -> SourceType.MESSAGE
        }
        val read = if (uri != null && sourceType != SourceType.MESSAGE) {
            runCatching { app.contentResolver.openInputStream(uri)?.use { DocumentParser.readBytesWithLimit(it) } }
        } else null
        val bytes = read?.getOrNull()
        if (read != null && bytes == null) {
            isAnalyzing = false
            errorMessage = app.getString(if (read.isFailure) R.string.input_media_read_error else R.string.share_error_too_large)
            return@LaunchedEffect
        }
        val mime = if (sourceType == SourceType.MESSAGE) null else app.contentResolver.getType(uri!!) ?: type
        analysisResult = app.analyzer.analyze(sourceType, sharedText, bytes, mime)
        isAnalyzing = false
        app.soundAndHaptics.playSuccess()
    }

    DayflowLocaleProvider(language) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(R.string.share_received_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = MaterialTheme.colorScheme.onBackground
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
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = stringResource(R.string.share_analyzing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (failedCalendarEvents.isNotEmpty()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.plurals.calendar_open_failed, failedCalendarEvents.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = !isRetryingCalendar,
                            onClick = {
                                if (!isRetryingCalendar) {
                                    coroutineScope.launch {
                                        isRetryingCalendar = true
                                        val remaining = app.itemCommitter.retryCalendar(failedCalendarEvents)
                                        remaining.forEach { app.calendarService.launchCalendarIntent(it) }
                                        failedCalendarEvents = remaining
                                        isRetryingCalendar = false
                                        onFinish()
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.calendar_retry))
                        }
                        TextButton(onClick = onFinish) {
                            Text(stringResource(R.string.calendar_dismiss))
                        }
                    }
                }
            } else if (analysisResult != null) {
                val result = analysisResult!!
                VerificationCard(
                    result = result,
                    onAccept = { confirmed ->
                        if (!hasAcceptedResult) {
                            hasAcceptedResult = true
                            coroutineScope.launch {
                                val failed = app.itemCommitter.commit(confirmed).calendarFailed
                                app.soundAndHaptics.playSuccess()
                                if (failed.isEmpty()) onFinish() else failedCalendarEvents = failed
                            }
                        }
                    },
                    onDismiss = onFinish
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }
    }
}
