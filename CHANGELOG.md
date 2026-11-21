# Changelog

## 2026-11-21 — Android 2.0: all website features implemented

- Real on-device understanding (replaces the mock engine): date/time resolver (EN/DE/ES), OCR for screenshots, PDF reading, voice transcription.
- Google Calendar sync via the calendar provider (write, live read, delete), local "Dayflow" calendar fallback.
- Notification suggestions with Add/Review actions and overlay popup; reminders with Done/Snooze, restored after reboot.
- Smart verified actions with draft confirmation, editable verification card, personal context, automations, activity log.
- Multi-provider settings: on-device, Gemini, Anthropic-compatible, OpenAI-compatible (incl. Ollama) with connection test.
- Screen agent works across apps (open_app action, selection state, step history, real Stop) with any configured provider.
- Fixed: missing bubble service (build), Intelligence screen wired to real features, missing strings, dialogs ignoring the app language, typography colors breaking buttons/dark mode.

All notable changes merged into `main` on 2026-09-25.

## 2026-09-25 — Integration merge

- Merged branches: `website`, `android`, `develop` into `main` via integration branch `merge/all-into-main`.
- Updated and added developer documentation under `docs/` (architecture, development, design-system, README).
- Added a localized GitHub footer link (localized `en`/`de`/`es`) and small UI polish in the footer.
- Fixed dependency resolution by adding a compatible `esbuild@^0.28` devDependency to satisfy `vite@8.3.1`.
- Verified `npm ci` and `npm run build` locally; production `dist/` builds successfully.

Notes:
- The integration was performed non-destructively and pushed to `origin/main`.
- The temporary integration branch `merge/all-into-main` was removed after the merge.
