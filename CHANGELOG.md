# Changelog

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
