package com.dayflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dayflow.app.core.designsystem.DayflowTheme
import com.dayflow.app.ui.navigation.DayflowApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DayflowApplication

        setContent {
            DayflowTheme {
                DayflowApp(app = app)
            }
        }
    }
}
