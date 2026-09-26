# Dayflow Android — Native Application

This directory contains the production-grade native Android implementation of **Dayflow**, built with Kotlin, Jetpack Compose, Android Architecture Components, and Material 3 customized to the Dayflow editorial design system.

---

## Architecture Overview

```
android/app/src/main/java/com/dayflow/app/
├── DayflowApplication.kt       # Application class & dependency container
├── MainActivity.kt             # Primary activity hosting Compose NavHost
├── ShareActivity.kt            # Android Sharesheet target (text, image, PDF, audio)
│
├── core/
│   ├── designsystem/           # Dayflow palette (#F5EFE6, #641C24), Typography & Shapes
│   ├── sound/                  # Web Audio equivalent micro-interaction Tone & Haptics
│   └── util/                   # Date/time formatters, Android permission helpers
│
├── domain/
│   ├── model/                  # DayflowEvent, DayflowTask, DayflowReminder, DayflowInsight, etc.
│   └── repository/             # DayflowRepository, PreferencesRepository
│
├── data/
│   ├── local/                  # Room Database (AppDatabase, EventDao, TaskDao, ReminderDao)
│   ├── preferences/            # Android Jetpack DataStore preferences (language, sound, AI)
│   └── repository/             # DayflowRepositoryImpl (reactive Room Flow dispatch)
│
├── ai/
│   ├── AIProvider.kt           # Multi-engine provider-agnostic abstraction
│   ├── LocalMockProvider.kt    # Deterministic local offline engine with scenario presets
│   ├── GeminiProvider.kt       # Secure proxy architecture (no client-side hardcoded secrets)
│   └── DayflowAnalyzer.kt      # AI engine orchestrator
│
├── integrations/
│   ├── calendar/               # CalendarContract API & Intent event insertion
│   ├── notification/           # NotificationManager channels & AlarmManager exact reminders
│   ├── voice/                  # MediaRecorder real microphone recording & amplitude tracking
│   └── document/               # ContentResolver SAF document parsing
│
└── ui/
    ├── navigation/             # Screen routes & DayflowNavGraph
    ├── components/             # TopBar, BottomBar, DayflowCard, Button, VerificationCard, OmniInputSheet
    ├── today/                  # TodayScreen & TodayViewModel (Emotional center of Dayflow)
    ├── tasks/                  # TasksScreen & TasksViewModel (Interactive task management)
    ├── reminders/              # RemindersScreen & RemindersViewModel (Scheduled reminder management)
    ├── intelligence/           # IntelligenceScreen (Screen intelligence context streams)
    └── settings/               # SettingsScreen & SettingsViewModel (Language, Haptics, AI provider)
```

---

## Key Features

1. **Human-in-the-Loop Verification**:
   - Analyzed items always present an actionable `VerificationCard` allowing the user to review what was detected before creating calendar events, reminders, or tasks.
2. **Deep Android Integration**:
   - **Sharesheet Target (`ShareActivity`)**: Registered for `ACTION_SEND` on text, images/screenshots, PDFs, and audio recordings.
   - **Calendar Integration**: Direct ContentResolver or native Android Intent fallback with permission handling.
   - **Notification Channels**: Scheduled reminders using AlarmManager.
   - **Voice Recording**: Real microphone recording with amplitude metering and transcription hooks.
3. **Official Dayflow Branding**:
   - Reuses official Dayflow symbol and logo assets (`res/drawable/dayflow_symbol_official.png`, `dayflow_logo_official.png`).
4. **Localization**:
   - Fully localized into **English** (`values/strings.xml`), **German** (`values-de/strings.xml`), and **Spanish** (`values-es/strings.xml`).

---

## Build & Run

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35 (Android 15)

### Build via Gradle
```bash
cd android
./gradlew assembleDebug
```

### Run Unit Tests
```bash
./gradlew test
```
