# AGENTS.md — Dayflow Developer & Agent Context Guide

> **Note for AI Coding Agents**: This document is the primary authoritative context for working on the Dayflow repository. Read this file carefully before inspecting or modifying code.

---

## 1. Project Overview

**Dayflow** is an intelligent daily flow assistant concept and interactive web application. It addresses the problem of personal information fragmentation—where crucial schedule details, appointments, travel itineraries, and to-do items are scattered across chat messages, screenshots, PDF documents, and voice notes.

Dayflow automatically ingests and understands these multi-modal inputs, converting them into structured calendar events, actionable tasks, time-anchored reminders, and proactive daily context insights.

### Repository Summary
* **Framework**: React 19 (`react` ^19.0.1, `react-dom` ^19.0.1)
* **Build System & Bundler**: Vite 8 (`vite` ^8.3.0, `@vitejs/plugin-react` ^6.1.1)
* **Type System**: TypeScript 7 (`typescript` ^7.0.2, strict bundler resolution)
* **Styling**: Tailwind CSS v4 (`@tailwindcss/vite` ^4.3.3, `@import "tailwindcss"` in `src/index.css`)
* **Animations**: Motion / Framer Motion 12 (`motion` ^12.23.24, imported as `motion/react`)
* **Icons**: Lucide React (`lucide-react` ^0.546.0)
* **Audio & Haptics**: Custom Web Audio API synthesizer + `navigator.vibrate` haptic feedback (`src/utils/audio.ts`)
* **Localization**: Custom 3-language React Context i18n system (`en`, `de`, `es`) (`src/utils/i18n.ts`)

---

## 2. Architecture

Dayflow is structured as a rich, single-page application (SPA) with a real-time reactive state loop that links an interactive Demo Studio with a live Dashboard, an embedded Android Phone Simulator, and a top-level summary grid.

```
                  ┌─────────────────────────────────────────┐
                  │          index.html / main.tsx          │
                  └────────────────────┬────────────────────┘
                                       │
                        ┌──────────────┴──────────────┐
                        │     DayflowProvider         │
                        │ (src/store/dayflowStore.tsx)│
                        └──────────────┬──────────────┘
                                       │
                        ┌──────────────┴──────────────┐
                        │          App.tsx            │
                        └──────────────┬──────────────┘
                                       │
    ┌───────────────────┬──────────────┼──────────────┬───────────────────┐
    │                   │              │              │                   │
┌───┴──────────┐ ┌──────┴───────┐ ┌────┴─────────┐ ┌──┴────────────┐ ┌────┴─────────────┐
│Sticky Nav &  │ │ Hero & Phone │ │ Today,       │ │ Problem &     │ │ Demo Studio     │
│Language      │ │ Simulator    │ │ Understood   │ │ Chaos Grid    │ │ (Message, Screen│
│Controls      │ │ Component    │ │ Grid         │ │               │ │ PDF, Voice)     │
└──────────────┘ └──────────────┘ └──────────────┘ └───────────────┘ └────────┬────────────┘
                                                                              │
                                                                    (Fires addEvent /   │
                                                                     addTask / addReminder)
                                                                              │
                                                                              ▼
                                                           ┌──────────────────┴───────────┐
                                                           │  State Update dispatches to  │
                                                           │  Dashboard, Phone Simulator, │
                                                           │  & Today, Understood         │
                                                           └──────────────────────────────┘
```

### Application Lifecycle & Entry Point
1. `index.html` loads Google Fonts (*Plus Jakarta Sans* and *Newsreader*) and boots `/src/main.tsx`.
2. `src/main.tsx` mounts `<DayflowProvider>` wrapping `<App />` in React `<StrictMode>`.
3. `DayflowProvider` (`src/store/dayflowStore.tsx`) initializes language detection, localized seed data, sound preferences, and dispatch methods.
4. `App.tsx` orchestrates section scrolling and renders all sub-components.

---

## 3. Important Files & Directory Map

| Path | Purpose & Responsibility |
| :--- | :--- |
| [`package.json`](file:///home/norobb/dayflow-assistant/package.json) | Package dependencies, scripts (`dev`, `build`, `preview`, `clean`, `lint`), and module definition. |
| [`vite.config.ts`](file:///home/norobb/dayflow-assistant/vite.config.ts) | Vite configuration with `@tailwindcss/vite`, React plugin, `@/` path alias, and `DISABLE_HMR` file-watch check. |
| [`tsconfig.json`](file:///home/norobb/dayflow-assistant/tsconfig.json) | TypeScript compiler configuration (ES2022 target, bundler resolution, `@/*` path mapping). |
| [`index.html`](file:///home/norobb/dayflow-assistant/index.html) | Root HTML template, Google Fonts preconnect links, meta tags, and inline SVG favicon. |
| [`.env.example`](file:///home/norobb/dayflow-assistant/.env.example) | Environment variable documentation (`GEMINI_API_KEY`, `APP_URL`). |
| [`src/index.css`](file:///home/norobb/dayflow-assistant/src/index.css) | Global design system CSS tokens (`--color-bg`, `--color-primary`, etc.), custom scrollbar, and `@media (prefers-reduced-motion)`. |
| [`src/main.tsx`](file:///home/norobb/dayflow-assistant/src/main.tsx) | Application entry point that mounts `DayflowProvider` and `App`. |
| [`src/App.tsx`](file:///home/norobb/dayflow-assistant/src/App.tsx) | Main page layout, sticky navigation bar, language switcher, Hero section, problem grid, demo tabs, architecture overview, roadmap, and footer. |
| [`src/store/dayflowStore.tsx`](file:///home/norobb/dayflow-assistant/src/store/dayflowStore.tsx) | Global React Context store. Manages state for language, events, tasks, reminders, phone notifications, todayUnderstood summary items, proactive insights, sound muting, and phone tabs. |
| [`src/components/AndroidSimulator.tsx`](file:///home/norobb/dayflow-assistant/src/components/AndroidSimulator.tsx) | Android Phone Simulator component. Renders titanium outer bezel, hardware side buttons, notch/camera cutout, notification toasts, tabbed phone views, and full-screen modal mode. |
| [`src/components/DayflowDashboard.tsx`](file:///home/norobb/dayflow-assistant/src/components/DayflowDashboard.tsx) | Synchronized Dayflow Dashboard showing timeline events, interactive task checkboxes, time-anchored reminders, and proactive context insight cards. Includes reset state button. |
| [`src/components/TodayUnderstood.tsx`](file:///home/norobb/dayflow-assistant/src/components/TodayUnderstood.tsx) | High-visibility dynamic summary grid ("Today, understood.") displaying newly recognized items with animated entry. |
| [`src/components/OmniInputSystem.tsx`](file:///home/norobb/dayflow-assistant/src/components/OmniInputSystem.tsx) | Interactive input field simulator allowing users to test preset scenarios (message, screenshot, PDF, voice) and deep-link into the Demo Studio. |
| [`src/components/ProductConceptSections.tsx`](file:///home/norobb/dayflow-assistant/src/components/ProductConceptSections.tsx) | Feature showcase cards for Android Share Sheet integration, Screen Intelligence, Smart Verified Actions (with interactive human verification toggle), and Privacy & Trust. |
| [`src/components/DayflowLogo.tsx`](file:///home/norobb/dayflow-assistant/src/components/DayflowLogo.tsx) | Brand visual assets: `DayflowSymbol` SVG icon mark, `DayflowLogo` full logo wordmark, and `ProductStateBadge` status component. |
| [`src/components/demos/MessageDemo.tsx`](file:///home/norobb/dayflow-assistant/src/components/demos/MessageDemo.tsx) | Interactive scenario card for chat message analysis (Alex train station pickup & documents task). |
| [`src/components/demos/ScreenshotDemo.tsx`](file:///home/norobb/dayflow-assistant/src/components/demos/ScreenshotDemo.tsx) | Interactive scenario card for dentist appointment receipt screenshot analysis. |
| [`src/components/demos/PdfDemo.tsx`](file:///home/norobb/dayflow-assistant/src/components/demos/PdfDemo.tsx) | Interactive scenario card for multi-page PDF itinerary extraction (Barcelona trip dates & parental consent task). |
| [`src/components/demos/VoiceDemo.tsx`](file:///home/norobb/dayflow-assistant/src/components/demos/VoiceDemo.tsx) | Interactive scenario card for voice note recording, transcription, and reminder extraction. |
| [`src/services/dayflowAnalyzer.ts`](file:///home/norobb/dayflow-assistant/src/services/dayflowAnalyzer.ts) | Provider-agnostic AI abstraction layer (`AIProvider` interface, `GeminiProvider` implementation, deterministic local fallback dataset `PRESETS`). |
| [`src/utils/audio.ts`](file:///home/norobb/dayflow-assistant/src/utils/audio.ts) | Client-side Web Audio API SoundSystem class. Synthesizes click, analyze, success, and toggle tones, and triggers optional haptic vibration (`navigator.vibrate`). |
| [`src/utils/i18n.ts`](file:///home/norobb/dayflow-assistant/src/utils/i18n.ts) | Complete translation dictionaries for English (`en`), German (`de`), and Spanish (`es`), plus browser locale detection (`detectInitialLanguage`). |
| [`src/utils/motion.tsx`](file:///home/norobb/dayflow-assistant/src/utils/motion.tsx) | Central Motion system export, custom easing curves (`cubicEase = [0.16, 1, 0.3, 1]`), variants, and `ScrollReveal` wrapper with reduced-motion support. |

---

## 4. State Architecture & Reactive Data Flow

Global state is managed by `src/store/dayflowStore.tsx` via `DayflowProvider` and accessed using the `useDayflowStore()` hook.

### Core State Slices
1. **`language`** (`'en' | 'de' | 'es'`): Persisted in `localStorage` (`dayflow-language` or `dayflow_lang`). When changed via `setLanguage(lang)`, all default seed arrays (`events`, `tasks`, `reminders`, `notifications`, `todayUnderstood`, `activeInsight`) automatically re-populate with translated mock data.
2. **`events`** (`DayflowEvent[]`): Timeline events containing `id`, `time`, `title`, `location`, `sourceType`, `createdAt`.
3. **`tasks`** (`DayflowTask[]`): Action items containing `id`, `title`, `completed`, `dueDate`, `sourceType`.
4. **`reminders`** (`DayflowReminder[]`): Reminders containing `id`, `title`, `timeLabel`, `sourceType`.
5. **`notifications`** (`PhoneNotification[]`): Toast notification stack displayed inside the Android Simulator screen (capped at 4 items).
6. **`todayUnderstood`** (`UnderstoodSummaryItem[]`): Stream items displayed in the "Today, understood." dynamic section.
7. **`activeInsight`**: `{ title: string, text: string, visible: boolean }` context banner card.
8. **`soundMuted`** (`boolean`): Controls whether `SoundSystem` synthesizes Web Audio tones.
9. **`activePhoneTab`** (`'timeline' | 'tasks' | 'reminders' | 'scanner'`): Controls the active bottom tab inside the Android Phone Simulator.

### Reactive Dispatch Flow
When a user interacts with any Demo card (e.g., clicking "Add to Calendar" in `MessageDemo.tsx`):
1. `handleAddCalendar()` calls `addEvent({...})` on `useDayflowStore()`.
2. `addEvent()` triggers `sounds.playSuccess()`, synthesizes a Web Audio 2-note fifth chord, and triggers an 8ms haptic tap.
3. `addEvent()` creates a unique ID (via `crypto.randomUUID()` or timestamp fallback) and prepends the new event to `events`.
4. `addEvent()` dispatches a synced record to `todayUnderstood`.
5. `addEvent()` dispatches an incoming toast to `notifications`.
6. React re-renders `DayflowDashboard`, `AndroidSimulator`, and `TodayUnderstood` simultaneously. Framer Motion `AnimatePresence` animates the new cards entering smoothly across all views in real time.

---

## 5. Phone Simulator Architecture & Corner Issue Analysis

The Android simulator is rendered by [`src/components/AndroidSimulator.tsx`](file:///home/norobb/dayflow-assistant/src/components/AndroidSimulator.tsx).

### Component & CSS Layout Hierarchy

```
<AndroidSimulator> (Container: 340px x 680px)
 └── Outer Titanium Device Bezel (div.absolute.inset-0.rounded-[50px].bg-[#1E1B19].p-3.border-4.border-[#3D3734].overflow-hidden)
      ├── Hardware Side Buttons (div.absolute.-left-1..., div.absolute.-right-1...)
      └── Inner Screen Viewport (div.relative.w-full.h-full.rounded-[38px].overflow-hidden.border.bg-[#FAF6F0])
           └── renderScreenContent(isFull: false)
                ├── Status Bar (div.h-9.px-6.pt-2..., center hole-punch camera cutout: div.w-3.5.h-3.5.rounded-full.bg-black)
                ├── App Header (div.px-5.py-2.5..., DayflowSymbol, "dayflow" wordmark, Fullscreen toggle button)
                ├── Notification Toast Banner (AnimatePresence spring slide-down toast)
                ├── Internal Scroll Viewport (div.flex-1.overflow-y-auto.px-4.py-3.5.scrollbar-none)
                │    └── Tab Content: Timeline / Tasks / Reminders / Screen Intelligence Scanner
                ├── Bottom Navigation Bar (div.h-14.border-t.bg-white/95..., 4 interactive tab buttons)
                └── Home Gesture Pill Bar (div.h-3.5.w-full.bg-white..., child gesture pill: div.w-24.h-1.bg-[#1E1B19]/30.rounded-full)
```

### Known Square/Angular Corner Issue (Root Cause Analysis)

**Symptom**: Square white corners bleed through at the bottom corners of the Android phone simulator screen.

**Structural Root Causes**:
1. **Unclipped Solid Bottom Element**: The bottom-most element in `renderScreenContent()` is the Home Gesture Pill Bar container:
   `<div className="h-3.5 w-full bg-white flex items-center justify-center shrink-0">`
   This element has a solid `bg-white` background with square corners (`w-full`) extending to the absolute bottom edges of the content stream.
2. **Missing Border Radius on Content Container**: The root `div` returned by `renderScreenContent()` (`div.relative.w-full.h-full.bg-[#FAF6F0].flex.flex-col.overflow-hidden`) does NOT have a `rounded-*` class of its own. It relies entirely on its parent element (`div.rounded-[38px].overflow-hidden`) for corner clipping.
3. **Browser GPU Anti-aliasing / Sub-pixel Bleed**: In Chromium and WebKit rendering engines, parent containers using `border-radius` with `overflow: hidden` frequently suffer from sub-pixel anti-aliasing leaks when child elements have opaque background colors (`bg-white` or `bg-[#FAF6F0]`) pushed flush into extreme rounded corners unless GPU layer promotion (`isolation: isolate;` or `transform: translateZ(0);`) is applied.
4. **Hardware Button Clipping**: The hardware volume and power buttons (`div.absolute.-left-1`, `div.absolute.-right-1`) are nested INSIDE the outer bezel `div` which has `overflow-hidden`. Consequently, the hardware buttons are partially clipped by the outer `rounded-[50px]` mask.

**Target Files for Future Correction**:
* [`src/components/AndroidSimulator.tsx`](file:///home/norobb/dayflow-assistant/src/components/AndroidSimulator.tsx) (lines 62, 389–393, 411–420).

---

## 6. Branding & Logo Guidelines

### Logo Implementations in Codebase
1. **Inline Data URI Favicon** ([`index.html:15`](file:///home/norobb/dayflow-assistant/index.html#L15)):
   `<link rel="icon" type="image/svg+xml" href="data:image/svg+xml,...">`
   An SVG data URI rendering a burgundy squircle (`#641C24`) with an off-white monogram 'D' path (`#F5EFE6`).
2. **`DayflowSymbol` Component** ([`src/components/DayflowLogo.tsx:15`](file:///home/norobb/dayflow-assistant/src/components/DayflowLogo.tsx#L15)):
   An SVG component (`viewBox="0 0 100 100"`) rendering an abstract ribbon loop mark with a dark outline (`#1E1B19`), primary burgundy inner curve (`#641C24`), and a center anchor node (`<circle cx="40" cy="52" r="5" fill="#641C24" />`). Used in the Android Simulator header and notification toasts.
3. **`DayflowLogo` Component** ([`src/components/DayflowLogo.tsx:67`](file:///home/norobb/dayflow-assistant/src/components/DayflowLogo.tsx#L67)):
   Combines `DayflowSymbol` with the geometric wordmark `"dayflow"` in lowercase `font-extrabold text-[#1E1B19]`. Used in the main sticky header navigation and page footer.

> [!IMPORTANT]
> **CRITICAL BRAND RULE**: The official Dayflow logo supplied by the project owner is the source of truth. Never redesign, reinterpret, redraw, or replace it.

---

## 7. Design System

Dayflow uses a refined, modern editorial design system characterized by warm linen tones, deep wine burgundy accents, structured card surfaces, and subtle tactile micro-interactions.

### Color Palette Tokens ([`src/index.css`](file:///home/norobb/dayflow-assistant/src/index.css))
* `--color-bg`: `#F5EFE6` (Warm linen canvas)
* `--color-card`: `#FAF6F0` (Soft cream surface)
* `--color-card-white`: `#FFFFFF` (Pure white card surface)
* `--color-primary`: `#641C24` (Deep burgundy / wine mark & buttons)
* `--color-primary-light`: `#7E242F` (Primary hover state)
* `--color-primary-soft`: `#F0E4E6` (Light burgundy tint for insight cards)
* `--color-text`: `#1E1B19` (Dark charcoal body & headings)
* `--color-text-muted`: `#6B635B` (Warm taupe secondary text)
* `--color-border`: `#D8CFC2` (Soft warm border line)
* `Accent Green`: `#2E5C38` (Forest green for completed tasks & success indicators)
* `Accent Amber`: `#8C5E28` (Warm amber for state badges & "COMING SOON" badges)

### Typography
* **Primary Sans-serif**: `'Plus Jakarta Sans'`, system-ui, sans-serif
* **Secondary Editorial Serif**: `'Newsreader'`, Georgia, serif (imported in `index.html`)

### Border Radii & Surfaces
* Cards & Panels: `rounded-2xl` (16px) or `rounded-3xl` (24px)
* Buttons & Controls: `rounded-xl` (12px) or `rounded-2xl` (16px)
* Badges & Tags: `rounded-full` or `rounded-md`
* Device Bezel: `rounded-[50px]`, Screen Clipping Viewport: `rounded-[38px]`

---

## 8. Localization System

Localization is handled by [`src/utils/i18n.ts`](file:///home/norobb/dayflow-assistant/src/utils/i18n.ts) and [`src/store/dayflowStore.tsx`](file:///home/norobb/dayflow-assistant/src/store/dayflowStore.tsx).

* **Supported Languages**: English (`en`), German (`de`), Spanish (`es`).
* **Detection Pipeline**:
  1. Reads `localStorage.getItem('dayflow-language')` or `localStorage.getItem('dayflow_lang')`.
  2. Inspects `navigator.language` (e.g. `de-DE` -> `de`, `es-ES` -> `es`).
  3. Defaults to `en`.
* **State Updates**: Calling `setLanguage(lang)` updates state, saves to `localStorage`, and immediately re-seeds mock events, tasks, reminders, notifications, todayUnderstood summary items, and proactive insights into the new language.

### Localization Audit Notes
* Most UI strings are localized via `t.<section>.<key>`.
* *Untranslated Strings to be aware of*:
  * Hardcoded default fallback preset texts inside `src/services/dayflowAnalyzer.ts` (`PRESETS.message`, `PRESETS.screenshot`, `PRESETS.pdf`, `PRESETS.voice`).
  * Simulator status bar clock (`'09:41'`) and cell header (`'5G'`, `'100%'`).
  * Scanner concept tab label (`'Context Flow'`) in `AndroidSimulator.tsx:334`.

---

## 9. Development & Verification Commands

Use the actual npm scripts defined in [`package.json`](file:///home/norobb/dayflow-assistant/package.json):

```bash
# Start local development server (port 3000, host 0.0.0.0)
npm run dev

# Run TypeScript type checker (no emit)
npm run lint

# Production build (outputs to dist/)
npm run build

# Preview production build locally
npm run preview

# Clean build artifacts
npm run clean
```

---

## 10. Environment Variables

Documented in [`.env.example`](file:///home/norobb/dayflow-assistant/.env.example):

* `GEMINI_API_KEY`: API key for Gemini AI calls (injected at runtime in Cloud/AI Studio environments).
* `APP_URL`: App host URL (used for self-referential links, OAuth callbacks, and API endpoints).
* `DISABLE_HMR`: Set to `'true'` during automated AI editing sessions to disable Vite file watching and save system CPU (`vite.config.ts`).

> [!CAUTION]
> Never commit actual API keys or secret credentials to the repository.

---

## 11. Important Constraints & Guidelines for Future Agents

1. **Do NOT modify product features or UI unless explicitly requested**: This task was analysis and documentation only.
2. **Preserve existing design & identity**: Do NOT replace Tailwind CSS v4 or Motion with other libraries. Maintain the burgundy (`#641C24`) and linen (`#F5EFE6`) brand identity.
3. **Respect Product State Badging Rules**:
   * Working, live features MUST NEVER receive state badges such as "NOW", "ACTIVE", or "LIVE".
   * Only genuine upcoming or unreleased features should display `"COMING SOON"` or `"LATER"` badges (enforced in `src/components/DayflowLogo.tsx`).
4. **Always verify code with `npm run lint`**: Before declaring any change complete, run TypeScript verification (`npm run lint` or `tsc --noEmit`).

---

## 12. Known Issues & Future Development Guidance

1. **Android Simulator Bottom Corner Clipping**:
   * *Issue*: `bg-white` Home Gesture Pill Bar at the bottom of `renderScreenContent` spills square pixels past the 38px corner radius.
   * *Fix Strategy*: Apply explicit `rounded-[38px] overflow-hidden` directly to `renderScreenContent` root `div`, move hardware buttons outside the `overflow-hidden` bezel container, and add `isolation: isolate` / `transform: translateZ(0)` for GPU layer promotion.
2. **Express Server Dependency Cleanup**:
   * *Issue*: `package.json` includes `express` and `@types/express`, and `npm run clean` refers to `server.js`, but no `server.js` exists in root.
3. **Multi-Provider AI Abstraction**:
   * *Architecture Ready*: `src/services/dayflowAnalyzer.ts` defines `AIProvider` and `GeminiProvider`. Future tasks can implement `OpenAIProvider`, `ClaudeProvider`, or server-side proxies without altering client component interfaces.
