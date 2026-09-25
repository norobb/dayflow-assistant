# Dayflow

**Your day. Your phone. One intelligent flow.**

Dayflow is an intelligent daily flow assistant that solves personal information fragmentation — the problem of crucial schedule details scattered across chat messages, screenshots, PDFs, and voice notes. It automatically ingests and understands these multi-modal inputs and converts them into structured calendar events, actionable tasks, time-anchored reminders, and proactive daily context insights, all surfaced through a synchronized web dashboard and Android phone simulator.

---

## Overview

Modern life produces information in every format: a friend texts you a pickup time, a confirmation PDF lands in your inbox, a dentist receipt sits in your camera roll, and a voice note carries the rest. Dayflow reads all of it, understands it, and brings it together into one coherent daily view — without you lifting a finger.

---

## How Dayflow Works

```
Multi-modal input
(message · screenshot · PDF · voice note)
          ↓
  Gemini AI analysis
          ↓
Structured extraction
(events · tasks · reminders · insights)
          ↓
  Synchronized output
(Dashboard · Android Simulator · Summary grid)
```

---

## Core Capabilities

| Capability | Description |
|---|---|
| **Message Analysis** | Extracts calendar events and tasks from chat messages |
| **Screenshot Intelligence** | Reads appointment receipts and confirmation screenshots |
| **PDF Parsing** | Processes multi-page itineraries and documents |
| **Voice Transcription** | Converts voice notes to structured reminders |
| **Live Dashboard** | Synchronized timeline, task list, and reminders |
| **Android Simulator** | Interactive phone preview with real-time notifications |
| **Proactive Insights** | Context-aware daily briefing cards |
| **Localization** | English, German, and Spanish interface |

---

## Current Status

Dayflow is an **interactive concept demonstration**. The web application is fully functional and deployable. The native Android application is a future development track.

---

## Roadmap

| Area | Status |
|---|---|
| Web application | ✅ Live |
| Gemini AI integration | ✅ Implemented |
| Multi-language support | ✅ Implemented (en / de / es) |
| Interactive demo studio | ✅ Implemented |
| Native Android application | 🔜 Future |
| Multi-provider AI abstraction | 🔜 Future |
| Server-side API proxy | 🔜 Future |

---

## Technology

| Layer | Technology |
|---|---|
| Framework | React 19 |
| Build | Vite 8 |
| Language | TypeScript 7 (strict) |
| Styling | Tailwind CSS v4 |
| Animation | Motion / Framer Motion 12 |
| Icons | Lucide React |
| AI | Google Gemini (`@google/genai`) |
| Audio | Web Audio API (custom synthesizer) |
| Localization | Custom React Context i18n |

---

## Repository Structure

```
dayflow-assistant/
├── src/
│   ├── App.tsx                        # Root layout and page sections
│   ├── main.tsx                       # Application entry point
│   ├── index.css                      # Design system tokens and global styles
│   ├── components/
│   │   ├── AndroidSimulator.tsx       # Interactive Android phone simulator
│   │   ├── DayflowDashboard.tsx       # Synchronized events/tasks/reminders dashboard
│   │   ├── DayflowLogo.tsx            # Brand assets (symbol, wordmark, badge)
│   │   ├── OmniInputSystem.tsx        # Omni-input field simulator
│   │   ├── ProductConceptSections.tsx # Feature showcase sections
│   │   ├── TodayUnderstood.tsx        # Dynamic summary grid
│   │   └── demos/
│   │       ├── MessageDemo.tsx        # Chat message analysis demo
│   │       ├── ScreenshotDemo.tsx     # Screenshot analysis demo
│   │       ├── PdfDemo.tsx            # PDF itinerary demo
│   │       └── VoiceDemo.tsx          # Voice note demo
│   ├── services/
│   │   └── dayflowAnalyzer.ts         # AI abstraction layer (Gemini + local fallback)
│   ├── store/
│   │   └── dayflowStore.tsx           # Global React Context state
│   └── utils/
│       ├── audio.ts                   # Web Audio API sound synthesizer
│       ├── i18n.ts                    # Translation dictionaries + locale detection
│       └── motion.tsx                 # Motion system exports and variants
├── public/
│   └── assets/                        # Static logo assets served at root
├── docs/                              # Developer documentation
│   ├── README.md
│   ├── architecture.md
│   ├── development.md
│   └── design-system.md
├── .github/
│   ├── workflows/
│   │   ├── deploy.yml                 # GitHub Pages deployment (website branch only)
│   │   └── ci.yml                    # Type check + build on pull requests
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
├── AGENTS.md                          # Authoritative AI agent context document
├── CONTRIBUTING.md                    # Contribution guide
├── vite.config.ts
├── tsconfig.json
├── package.json
└── .env.example
```

---

## Getting Started

**Prerequisites:** Node.js 22 LTS or later, npm 10 or later.

```bash
# Clone the repository
git clone https://github.com/norobb/dayflow-assistant.git
cd dayflow-assistant

# Install dependencies
npm install

# Start local development server
npm run dev
# → http://localhost:3000
```

---

## Development

```bash
npm run dev       # Start local dev server (port 3000)
npm run build     # Production build → dist/
npm run preview   # Preview production build locally
npm run lint      # TypeScript type check (must pass before PR)
npm run clean     # Remove dist/
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in the values:

```bash
cp .env.example .env
```

| Variable | Required | Description |
|---|---|---|
| `GEMINI_API_KEY` | For AI features | Gemini API key from [AI Studio](https://aistudio.google.com/app/apikey) |
| `APP_URL` | Optional | Hosting URL (defaults to `http://localhost:3000`) |

The application runs without `GEMINI_API_KEY` — it falls back to a deterministic local preset dataset for all demo scenarios.

---

## Build

```bash
npm run build
```

Output is written to `dist/`. The build is a standard static site — no server required.

---

## Documentation

Full developer documentation lives in [`docs/`](docs/):

- [`docs/architecture.md`](docs/architecture.md) — Component architecture, state management, data flow
- [`docs/development.md`](docs/development.md) — Branch strategy, development workflow, contribution guide
- [`docs/design-system.md`](docs/design-system.md) — Colors, typography, spacing, animation principles

The [`AGENTS.md`](AGENTS.md) file is the authoritative context document for AI coding agents and provides deeper technical detail on every component.

---

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the full contribution guide including branch strategy, commit conventions, and PR checklist.

---

## License

MIT License
