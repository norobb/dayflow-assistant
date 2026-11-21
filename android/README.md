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

Every feature announced on the Dayflow website is implemented and was tested on the API 37 emulator:

| Website feature | Android implementation |
| :--- | :--- |
| Message understanding | `LocalProvider` + `ScreenContextAnalyzer` + `DateResolver`: on-device extraction of events, tasks and reminders (EN/DE/ES, relative and absolute dates, German compounds, abbreviations) |
| Screenshot recognition | `ContentExtractor.imageText` (ML Kit OCR, bundled model, offline) → same parser, multi-line appointment blocks |
| PDF dossier extraction | `ContentExtractor.pdfText` (`PdfRenderer` + OCR, up to 8 pages) |
| Voice note transcription | `ContentExtractor.audioText`: decodes any audio to PCM and transcribes with the system recognizer (on-device; triggers the language-pack download when missing) |
| Synchronized daily dashboard | Today: proactive context hint (View tasks / Snooze), "Today, understood.", timeline (Dayflow + device calendar + routines), tasks, reminders, reset |
| System share integration | `ShareActivity` for text, images, PDFs and audio |
| Notification actions | `DayflowNotificationListenerService` analyzes incoming messages on device and posts a suggestion with **Add** / **Review**; optional overlay popup |
| Google Calendar sync | `CalendarService`: writes to the chosen calendar (Google primary or a local "Dayflow" calendar), reads device calendars live, deletes in sync |
| Screen intelligence | Accessibility capture, floating bubble (`ScreenIntelligenceBubbleService`), screen map, cross-app screen agent (open app → act → done; uses the selected AI provider, simple goals work on device), safety gate |
| Smart verified actions | `SmartActionPlanner`: open link, route, dial, e-mail, reply / running-late drafts (SMS or share sheet) — always confirmed, never sent by Dayflow |
| Human confirmation | `VerificationCard`: every item can be unchecked or edited before saving |
| Personal context | Vocabulary (term → meaning), routines (weekdays + time), learned frequent contacts (`PersonalRepository`) |
| Multi-provider model settings | On-device, Gemini, Anthropic-compatible (Messages API: Claude gateways, Ollama, LiteLLM), OpenAI-compatible (OpenAI, OpenRouter, Ollama); optional gateway URL, retry on 429/503, cloud failures fall back to on-device |
| System automation workflows | Morning briefing, reminder before each event, suggestions from messages, task clean-up (`AutomationScheduler`, `ItemCommitter`) |
| Privacy & transparency | Explicit permissions, notifications always analyzed on device, activity log of everything Dayflow did and what went to a cloud model |
| Sound & haptics | Synthesized UI tones (click, analyze, success fifth, toggle) and haptics, each switchable |
| Languages | English, German, Spanish; applied app-wide via the per-app locale (Android 13+) |

Reminders survive reboots (`BootReceiver`); the cross-platform desktop app from the website roadmap is out of scope for Android.

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

### Run On-Device Tests (emulator)
```bash
# Speaks voice notes with the system TTS and transcribes them on the device
./gradlew connectedDebugAndroidTest
```
Use JDK 17 (`JAVA_HOME`); newer JDKs break the Android `jlink` step.
