# Dayflow — Architecture

This document describes the implemented architecture of the Dayflow web application. It clearly distinguishes what is currently implemented from what is planned for the future.

---

## Overview

Dayflow is a single-page application (SPA) built on React 19, Vite 8, TypeScript 7, and Tailwind CSS v4. It demonstrates intelligent multi-modal input processing by connecting an interactive Demo Studio to a live Dashboard, an embedded Android Phone Simulator, and a dynamic summary grid — all synchronized through a single shared React Context store.

---

## Entry Point

```
index.html
  └── src/main.tsx
        └── <DayflowProvider> (src/store/dayflowStore.tsx)
              └── <App> (src/App.tsx)
```

1. `index.html` loads Google Fonts (*Plus Jakarta Sans*, *Newsreader*) and the root module.
2. `src/main.tsx` mounts `<DayflowProvider>` wrapping `<App />` inside React `<StrictMode>`.
3. `DayflowProvider` initializes language detection, localized seed data, sound preferences, and all dispatch methods.
4. `App.tsx` renders all page sections in sequence and handles top-level scroll behavior.

---

## Component Map

| Component | File | Responsibility |
|---|---|---|
| `App` | `src/App.tsx` | Root layout, sticky nav, language switcher, all page sections |
| `DayflowProvider` | `src/store/dayflowStore.tsx` | Global state store (events, tasks, reminders, notifications, i18n, sound) |
| `AndroidSimulator` | `src/components/AndroidSimulator.tsx` | Interactive Android phone simulator with tabbed views and notification toasts |
| `DayflowDashboard` | `src/components/DayflowDashboard.tsx` | Synchronized dashboard (timeline, tasks, reminders, insights) |
| `TodayUnderstood` | `src/components/TodayUnderstood.tsx` | Animated summary grid displaying newly recognized items |
| `OmniInputSystem` | `src/components/OmniInputSystem.tsx` | Omni-input field simulator; deep-links into demo tabs |
| `ProductConceptSections` | `src/components/ProductConceptSections.tsx` | Feature showcase cards (Share Sheet, Screen Intelligence, Privacy) |
| `DayflowLogo` | `src/components/DayflowLogo.tsx` | Brand assets: `DayflowSymbol`, `DayflowLogo`, `ProductStateBadge` |
| `MessageDemo` | `src/components/demos/MessageDemo.tsx` | Chat message analysis scenario |
| `ScreenshotDemo` | `src/components/demos/ScreenshotDemo.tsx` | Screenshot appointment receipt scenario |
| `PdfDemo` | `src/components/demos/PdfDemo.tsx` | PDF itinerary extraction scenario |
| `VoiceDemo` | `src/components/demos/VoiceDemo.tsx` | Voice note transcription and reminder scenario |

---

## State Architecture

Global state is managed by `src/store/dayflowStore.tsx` via React Context and accessed via the `useDayflowStore()` hook.

### State Slices

| Slice | Type | Description |
|---|---|---|
| `language` | `'en' \| 'de' \| 'es'` | Active UI language; persisted in `localStorage` |
| `events` | `DayflowEvent[]` | Calendar timeline events |
| `tasks` | `DayflowTask[]` | Actionable task items with completion state |
| `reminders` | `DayflowReminder[]` | Time-anchored reminders |
| `notifications` | `PhoneNotification[]` | Toast queue for the Android Simulator (capped at 4) |
| `todayUnderstood` | `UnderstoodSummaryItem[]` | Stream items in the "Today, understood." section |
| `activeInsight` | `{ title, text, visible }` | Proactive context insight banner |
| `soundMuted` | `boolean` | Suppresses Web Audio API tone synthesis |
| `activePhoneTab` | `'timeline' \| 'tasks' \| 'reminders' \| 'scanner'` | Active tab inside the Android Simulator |

### Reactive Dispatch Flow

When a user interacts with any Demo card (example: clicking "Add to Calendar"):

```
handleAddCalendar()
  ↓
addEvent({ ...newEvent })            (in dayflowStore.tsx)
  ↓
sounds.playSuccess()                 (Web Audio 2-note fifth chord + 8ms haptic)
  ↓
events ← [newEvent, ...existing]     (prepend)
  ↓
todayUnderstood ← [summaryItem, ...]
  ↓
notifications ← [toast, ...]
  ↓
React re-render
  ├── DayflowDashboard               (new event card animates in)
  ├── AndroidSimulator               (notification toast slides down)
  └── TodayUnderstood                (summary card enters with AnimatePresence)
```

All three views update simultaneously. Framer Motion `AnimatePresence` handles enter/exit animations across all consumers.

---

## Services

### `src/services/dayflowAnalyzer.ts`

Provider-agnostic AI abstraction layer.

```
AIProvider (interface)
  └── GeminiProvider (implementation)
        └── @google/genai SDK → Gemini API
```

- When `GEMINI_API_KEY` is present: calls Gemini for live natural-language analysis.
- When not present (or on error): falls back to a deterministic `PRESETS` local dataset — all four demo scenarios work without an API key.
- Future providers (`OpenAIProvider`, `ClaudeProvider`, server-side proxy) can be added by implementing `AIProvider` without changing component interfaces.

---

## Utilities

| Utility | File | Description |
|---|---|---|
| `SoundSystem` | `src/utils/audio.ts` | Web Audio API synthesizer: click, analyze, success, toggle tones + `navigator.vibrate` haptics |
| `detectInitialLanguage` | `src/utils/i18n.ts` | Reads `localStorage`, then `navigator.language`, defaults to `en` |
| Translation dictionaries | `src/utils/i18n.ts` | Full `en`, `de`, `es` translations for all UI strings |
| `ScrollReveal` | `src/utils/motion.tsx` | Reduced-motion-aware wrapper; exports `cubicEase = [0.16, 1, 0.3, 1]` and animation variants |

---

## Localization

- **Supported languages:** English (`en`), German (`de`), Spanish (`es`).
- Detection: `localStorage.dayflow-language` → `navigator.language` → `en`.
- `setLanguage(lang)` re-seeds all mock events, tasks, reminders, notifications, and insights in the selected language simultaneously.

### Known untranslated strings (future work)

- AI preset texts in `src/services/dayflowAnalyzer.ts`
- Status bar clock (`09:41`), signal/battery labels (`5G`, `100%`)
- Scanner tab label (`Context Flow`) in `AndroidSimulator.tsx`

---

## Build System

| Tool | Version | Role |
|---|---|---|
| Vite | ^8.3.0 | Bundler, dev server, HMR |
| `@tailwindcss/vite` | ^4.3.3 | Tailwind CSS v4 Vite plugin |
| `@vitejs/plugin-react` | ^6.1.1 | React fast-refresh |
| TypeScript | ^7.0.2 | Type checking (`tsc --noEmit`) |

### Vite base URL

The build base path is controlled by `VITE_BASE_URL` (see `vite.config.ts`):

| Context | `VITE_BASE_URL` | Result |
|---|---|---|
| Local development | *(unset)* | `/` |
| GitHub Pages project site | `/dayflow-assistant/` | `https://norobb.github.io/dayflow-assistant/` |
| Custom domain | `/` | `https://yourdomain.com/` |

The GitHub Actions deploy workflow sets `VITE_BASE_URL=/dayflow-assistant/` automatically.

---

## Android Simulator

`src/components/AndroidSimulator.tsx` renders a 340×680 px titanium bezel device frame.

### Layout hierarchy

```
AndroidSimulator (container: 340×680px)
  └── Outer bezel (rounded-[50px], overflow-hidden)
       ├── Hardware side buttons
       └── Inner screen viewport (rounded-[38px], overflow-hidden)
            ├── Status bar (clock, signal, battery)
            ├── App header (DayflowSymbol, fullscreen toggle)
            ├── Notification toast (AnimatePresence spring slide-down)
            ├── Scrollable content (tab views)
            │    ├── Timeline tab
            │    ├── Tasks tab
            │    ├── Reminders tab
            │    └── Scanner tab (concept)
            ├── Bottom navigation bar
            └── Home gesture pill
```

---

## Future Architecture (Not Yet Implemented)

The following are planned but not implemented:

- **Native Android application** — Kotlin / Jetpack Compose. Lives on the `android` branch. No Android code exists yet.
- **Server-side API proxy** — Removes `GEMINI_API_KEY` from the client bundle.
- **Multi-provider AI** — `OpenAIProvider`, `ClaudeProvider` implementing the existing `AIProvider` interface.
- **Android CI/CD** — Gradle build + signing workflow on the `android` branch.
