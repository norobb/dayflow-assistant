package com.dayflow.app

import android.app.Application
import com.dayflow.app.ai.DayflowAnalyzer
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.data.local.AppDatabase
import com.dayflow.app.data.preferences.UserPreferences
import com.dayflow.app.data.repository.DayflowRepositoryImpl
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.accessibility.context.ScreenIntelligenceRunner
import com.dayflow.app.integrations.accessibility.context.ScreenObservationSettings
import com.dayflow.app.integrations.calendar.CalendarService
import com.dayflow.app.integrations.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DayflowApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: DayflowRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var soundAndHaptics: SoundAndHaptics
        private set

    lateinit var notificationHelper: NotificationHelper
        private set

    lateinit var calendarService: CalendarService
        private set

    lateinit var analyzer: DayflowAnalyzer
        private set

    /**
     * Process-lifetime scope for work that must outlive a screen: the screen
     * intelligence analysis pipeline and the mirrored observation setting.
     */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        repository = DayflowRepositoryImpl(database)
        preferencesRepository = UserPreferences(this)
        soundAndHaptics = SoundAndHaptics(this)
        notificationHelper = NotificationHelper(this)
        calendarService = CalendarService(this)
        analyzer = DayflowAnalyzer()
        // DayflowOverlayService is started from MainActivity (foreground) instead —
        // starting it here would crash with IllegalStateException whenever the process
        // boots in the background (e.g. bound accessibility / notification listener).

        ScreenIntelligenceRunner.init()
        applicationScope.launch {
            runCatching {
                preferencesRepository.isScreenObservationEnabled().first()
            }.getOrNull()?.let { ScreenObservationSettings.setAmbientEnabled(it) }
        }
    }

    companion object {
        @Volatile
        var instance: DayflowApplication? = null
            private set
    }
}
