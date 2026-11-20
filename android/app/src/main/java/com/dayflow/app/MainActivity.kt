package com.dayflow.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dayflow.app.core.designsystem.DayflowTheme
import com.dayflow.app.integrations.overlay.DayflowOverlayService
import com.dayflow.app.integrations.overlay.ScreenIntelligenceBubbleService
import com.dayflow.app.ui.navigation.DayflowApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DayflowApplication

        // Start the ambient message-overlay service while the app is in the
        // foreground; starting it from Application.onCreate crashes when the
        // process boots in the background.
        try {
            startService(Intent(this, DayflowOverlayService::class.java))
        } catch (_: Exception) {
            // Background start not permitted right now; overlay resumes next launch.
        }

        // Same rule for the screen-intelligence bubble: it is only started
        // from a foreground context, and only if the user switched it on.
        app.applicationScope.launch {
            val enabled = runCatching {
                app.preferencesRepository.isScreenBubbleEnabled().first()
            }.getOrDefault(false)
            if (enabled) {
                runCatching {
                    startService(Intent(this@MainActivity, ScreenIntelligenceBubbleService::class.java))
                }
            }
        }

        setContent {
            DayflowTheme {
                DayflowApp(app = app)
            }
        }
    }
}
