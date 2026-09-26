package com.dayflow.app

import android.app.Application
import com.dayflow.app.ai.DayflowAnalyzer
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.data.local.AppDatabase
import com.dayflow.app.data.preferences.UserPreferences
import com.dayflow.app.data.repository.DayflowRepositoryImpl
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.calendar.CalendarService
import com.dayflow.app.integrations.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        repository = DayflowRepositoryImpl(database)
        preferencesRepository = UserPreferences(this)
        soundAndHaptics = SoundAndHaptics(this)
        notificationHelper = NotificationHelper(this)
        calendarService = CalendarService(this)
        analyzer = DayflowAnalyzer()

        // Seed initial data if empty
        CoroutineScope(Dispatchers.IO).launch {
            val events = repository.observeEvents().first()
            if (events.isEmpty()) {
                val lang = preferencesRepository.getLanguage().first()
                repository.resetToDefaults(lang)
            }
        }
    }
}
