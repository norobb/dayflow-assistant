# Revised Implementation Plan: Dayflow

## Executive Summary & Product Narrative
Dayflow is an AI-powered personal organization layer that unifies scattered information (messages, screenshots, PDFs, documents, voice notes) into structured, actionable life workflows.

**The Core Product Story:**
```
SCATTERED INFORMATION  ───►  DAYFLOW UNDERSTANDS IT  ───►  DAYFLOW ORGANIZES IT  ───►  YOUR DAY BECOMES CLEAR
```
The website is engineered as a commercial-grade consumer technology product prototype: clean, calm, warm (`#F5EFE6` ecru, `#641C24` primary burgundy, `#1E1B19` near-black), reliable out-of-the-box with zero API keys required, and featuring a live synchronized Android simulator.

---

## 1. Unified State & System Architecture (`dayflowStore`)
To ensure the demos, dashboard, phone simulator, and summary sections act as **one living product**:
- **Single Source of Truth (`src/store/dayflowStore.tsx`)**:
  - `events`: Seeded with morning schedule (`09:00 Physics`, `11:30 Dentist`, `15:00 Project deadline`, `18:30 Dinner`), dynamically expandable.
  - `tasks`: Interactive checklists (`Bring the documents`, `Submit presentation`, `Bring passport`, `Submit permission form`, etc.) with instant toggle completion.
  - `reminders`: Time-anchored reminders (`Monday: submit presentation`, `Tomorrow: bring documents`).
  - `todayUnderstood`: Live highlights tracking items Dayflow has processed for the user.
  - `notifications`: Reactive mobile notification feed on the Android simulator that pops in whenever a demo action is triggered.
  - `activeInsight`: Proactive context card (*"Your presentation deadline is tomorrow. You still have 2 unfinished preparation tasks."*) with interactive snooze/resolve buttons.
  - `soundEnabled`: Global audio setting (default subtle, easily toggled off/on).

---

## 2. Zero-Dependency Local Engine + Gemini API Abstraction
- **Analyzer Architecture (`src/services/dayflowAnalyzer.ts`)**:
  ```typescript
  export interface AnalysisResult {
    id: string;
    type: 'message' | 'screenshot' | 'pdf' | 'voice' | 'custom';
    rawPreview: string;
    events: DayflowEvent[];
    tasks: DayflowTask[];
    reminders: DayflowReminder[];
    documentDetails?: { title: string; pages: number; summary: string };
    detectedConfidence: number;
  }
  ```
- **Local Engine (NOW - 100% Reliable)**:
  - Deterministic processing for preset scenarios (Message pickup & documents, Dentist screenshot, Barcelona trip PDF, and Voice memo) with realistic multi-step processing states (Idle → Analyzing → Detected → Available Actions → Confirmed).
  - Handles custom inputs with intelligent heuristic extraction for local test exploration.
- **Gemini-Ready Abstraction**: Clean asynchronous interface matching Gemini SDK schema, ready to connect server-side via `/api/analyze` without changing UI code or exposing API keys.

---

## 3. Visual Identity & Design System
- **Original Monoline Flowing "D"**:
  - Continuous vector path conveying movement, organization, and continuity.
  - Renders as SVG app icon, browser favicon, and alongside the lowercase wordmark `dayflow`.
- **Palette**:
  - Background: `#F5EFE6` (warm ecru parchment)
  - Primary Burgundy: `#641C24` (refined deep wine)
  - Near-Black: `#1E1B19` (high-contrast typography)
  - Soft Border: `#D8CFC2`
  - Cards: `#FAF7F2` (warm white surface)
- **Typography & Encoding**: Pristine UTF-8 characters (`✦`, `—`, `“`, `”`, German umlauts `ä, ö, ü, ß`). No broken symbols.
- **Audio Feedback (`src/utils/audio.ts`)**:
  - Web Audio API synthesizer generating delicate micro-clicks (400Hz soft drop, 15ms), subtle analysis resonance, and positive resolution chimes.
  - Global mute toggle in navigation header.

---

## 4. Product Hierarchy & Key Sections

### 1. Sticky Navigation
- Flowing "D" logo + lowercase `dayflow`.
- Nav links: Overview, Demos, Today Dashboard, Features, Roadmap.
- Controls: Audio sound toggle, State reset button, and "Try Demo" CTA.

### 2. Hero Section
- Headline: *“Your day. Your phone. One intelligent flow.”*
- Supporting narrative: *“Dayflow understands the information scattered across your messages, screenshots, files and voice notes — and turns it into actions.”*
- CTAs: *“Try the Demo”* (smooth jumps to demo) & *“Explore Dayflow”*.
- **Hero Android Device Simulator**: Live interactive flagship mockup displaying Dayflow's Material-inspired UI with live notifications, timeline events, and active quick actions.

### 3. "Today, understood." (Value Proposition Highlight)
- Dedicated prominent summary block directly connecting the value proposition to live application state:
  - *"3 things Dayflow organized for you"*
  - Live synced badges: `18:30 Dinner at Vapiano [Calendar]`, `Tomorrow Bring documents [Reminder]`, `Friday Project deadline [Task]`.
  - When user runs demos, newly organized items flash and populate here in real-time.

### 4. The Problem: "Your life is everywhere."
- Visual contrast: Scattered floating fragments (messy iMessage bubble, unorganized screenshot, buried PDF, voice note, fragmented calendar) transitioning into Dayflow's unified flowing stream.
- Headline: *“Dayflow connects the dots.”*

### 5. The Interactive Demo Studio
- Tabs: **Message**, **Screenshot**, **PDF**, **Voice**.
- Step-by-step lifelike interaction:
  - **Message**: *"Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents."* → Click `✦ Analyze with Dayflow` → Live animated progress → Event & Task cards detected → *Add to Calendar*, *Create Reminder*, or *Add both*.
  - **Screenshot**: Realistic appointment confirmation receipt → `Analyze Screenshot` → Dentist Tuesday 14:30 detected → Add to calendar.
  - **PDF**: *"School Trip — Barcelona"* multi-item agenda → `Analyze PDF` → Expands into 3 dates, 2 tasks, 1 file reference with individual or bulk action controls.
  - **Voice**: Animated soundwave recording simulation → Transcribing *"Remind me next Monday to submit my presentation."* → Detects Task & Reminder with instant scheduling.
- Every action triggers simulated notification popups on the Android phone and updates the dashboard immediately.

### 6. Interactive Dayflow Dashboard
- Complete consumer app experience:
  - Current greeting (*"Good morning"*) and live clock.
  - Calendar timeline with time markers.
  - Interactive Task checklist (click to mark completed with sound feedback).
  - Active Reminders stream.
  - Proactive AI Insight card (*"Your presentation deadline is tomorrow..."*) with *View tasks* and *Snooze* options.

### 7. Unified Input System ("Drop anything here")
- Prominent Dayflow omni-input bar with preset scenario switchers:
  - Allows selecting preset message, image receipt, trip itinerary PDF, or voice memo to immediately observe the extraction pipeline.

### 8. The Three-Layer Architecture
- **UNDERSTAND**: Messages, Screenshots, Images, PDFs, Files, Text, Voice, Notifications.
- **ORGANIZE**: Calendar, Tasks, Reminders, Notes, Personal context.
- **ACT**: Android integrations, App actions, Deep links, Messaging, Automation, Screen understanding.

### 9. Future Android Experience & Screen Intelligence (Strictly Marked `COMING SOON`)
- **Android Share & Notifications**: Mockups showing system-level notification quick-actions and share-sheet routing.
- **Screen Intelligence (Conceptual Prototype)**: Simulated smartphone display with bounding box recognition over app elements, clearly labeled *“Screen Intelligence — Interactive Concept — Coming soon”*.
- **Smart Actions ("Understand. Act. Verify.")**: Safety demonstration showing:
  - User: *"Tell Anna I'll be 10 minutes late."*
  - Dayflow: 1. Understand request → 2. Identify Anna → 3. Prepare draft → 4. Show preview → 5. Ask for confirmation before sending. (Sending is never automated).

### 10. Personal AI & Multilingual Support (`COMING SOON`)
- Personal vocabulary, contact recognition, recurring routines, and dual German (`de-DE`) / English (`en-US`) awareness.

### 11. Safety & Privacy: "Helpful by default. Careful by design."
- Realistic, trustworthy principles:
  - Sensitive actions require explicit user confirmation.
  - No irreversible background actions.
  - Zero client-side API key exposure.
  - Granular, opt-in device permissions.
  - Complete visibility into what Dayflow reads and creates.

### 12. Transparent Product Roadmap
Structured into consistent product states:
- **NOW (Interactive Web Prototype)**: Message/Screenshot/PDF/Voice demos, real-time shared state, Android phone simulator, dynamic timeline & task manager, "Today, understood." dashboard.
- **COMING SOON**: Native Android APK, Google Calendar 2-way sync, Android notification actions, Gemini API multi-modal parsing, Screen Intelligence engine, Safety confirmation dialogs.
- **LATER**: Cross-platform desktop apps, deeper system automation, multi-calendar support, autonomous complex workflows.

### 13. Final CTA & Comprehensive Footer
- Headline: *“Your day is already full. Let Dayflow handle the small things.”*
- Quick action to relaunch demo or inspect the prototype.

---

## 5. Implementation Steps
1. **Metadata & HTML**: Update `metadata.json` and `index.html` with Dayflow title, meta tags, and font definitions.
2. **Design Tokens & Audio Utility**: Define custom styles in `src/index.css` and create Web Audio synthesizer in `src/utils/audio.ts`.
3. **Core Store & Analysis Engine**: Build `src/store/dayflowStore.tsx` and `src/services/dayflowAnalyzer.ts`.
4. **Android Simulator Component**: Create realistic interactive Android phone simulator `src/components/AndroidSimulator.tsx`.
5. **Interactive Demo Modules**: Build `src/components/demos/` for Message, Screenshot, PDF, and Voice.
6. **Dashboard & Summary Components**: Build `src/components/TodayUnderstood.tsx` and `src/components/DayflowDashboard.tsx`.
7. **Product Story, Architecture, Concept & Privacy Sections**: Build feature, problem, screen intelligence, and roadmap components.
8. **App Integration & Verification**: Assemble in `src/App.tsx`, test responsive views, verify build with `compile_applet`.
