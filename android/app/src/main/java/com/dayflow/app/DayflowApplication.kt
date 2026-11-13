package com.dayflow.app

import android.app.Application
import android.content.Intent
import com.dayflow.app.ai.DayflowAnalyzer
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.data.local.AppDatabase
import com.dayflow.app.data.preferences.UserPreferences
import com.dayflow.app.data.repository.DayflowRepositoryImpl
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.calendar.CalendarService
import com.dayflow.app.integrations.notification.NotificationHelper

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
    }

    companion object {
        @Volatile
        var instance: DayflowApplication? = null
            private set
    }
}
