# Dayflow — Development Guide

This document covers everything you need to set up, develop, and contribute to the Dayflow repository.

---

## Prerequisites

| Tool | Minimum version | Notes |
|---|---|---|
| Node.js | 22 LTS | 22.x or later |
| npm | 10.x | Bundled with Node 22 |
| Git | Any recent version | |

---

## Installation

```bash
git clone https://github.com/norobb/dayflow-assistant.git
cd dayflow-assistant
npm install
```

---

## Available Scripts

| Script | Command | Description |
|---|---|---|
| Dev server | `npm run dev` | Starts Vite dev server at `http://localhost:3000` |
| Type check | `npm run lint` | Runs `tsc --noEmit` — must pass before any PR |
| Build | `npm run build` | Production build → `dist/` |
| Preview | `npm run preview` | Serve the production build locally |
| Clean | `npm run clean` | Remove `dist/` |

---

## Environment Variables

Copy `.env.example` to `.env` for local development:

```bash
cp .env.example .env
```

| Variable | Required | Description |
|---|---|---|
| `GEMINI_API_KEY` | For AI features | Gemini API key from [AI Studio](https://aistudio.google.com/app/apikey) |
| `APP_URL` | Optional | Hosting URL; defaults to `http://localhost:3000` |
| `VITE_BASE_URL` | CI only | Vite build base path. Do **not** set in `.env` locally. Set automatically by the deploy workflow |
| `DISABLE_HMR` | AI Studio only | Disables Vite HMR/watch during automated agent edits |

The application runs without a `GEMINI_API_KEY` — all demos fall back to a deterministic local preset dataset.

> **Never commit** `.env` or any file containing real secrets.

---

## Branch Structure

```
main
├── website
└── android
```

### `main`

Stable/common project state. Represents the shared baseline for both product areas.

- Not used for active experimental development.
- Not automatically deployed anywhere.
- Merge into `main` when `website` or `android` reaches a stable milestone.

### `website`

Active development branch for the Dayflow web application.

Contains:
- React/Vite/TypeScript web application
- Website-specific components and demos
- Web deployment configuration

**Automatically deployed to GitHub Pages on every push.**

### `android`

Reserved for the future native Android application (Kotlin / Jetpack Compose).

- No Android code exists yet — the branch is a clean starting point.
- Android CI/CD will be configured when native development begins.
- Native Android development happens independently of the web application.

---

## Development Workflow

### Website development

```
feature work (local)
      ↓
git push origin website
      ↓
GitHub Actions (deploy.yml)
      ↓
Vite build (VITE_BASE_URL=/dayflow-assistant/)
      ↓
GitHub Pages → https://norobb.github.io/dayflow-assistant/
```

For feature branches, branch from `website`:

```bash
git checkout website
git pull origin website
git checkout -b feature/your-feature-name

# ... make changes ...

npm run lint    # must pass
npm run build   # must succeed

git push origin feature/your-feature-name
# Open PR → website
```

### Android development (future)

```
feature work (local)
      ↓
android branch
      ↓
future Android CI/CD
```

---

## CI/CD

### deploy.yml — GitHub Pages deployment

Triggered by:
- Push to `website`
- Manual dispatch (`workflow_dispatch`)

Steps:
1. Checkout
2. Setup Node 22
3. `npm ci`
4. `npm run lint` (type check)
5. `npm run build` with `VITE_BASE_URL=/dayflow-assistant/`
6. Upload `dist/` as Pages artifact
7. Deploy to GitHub Pages

### ci.yml — Pull Request checks

Triggered by PRs targeting `main`, `website`, or `android`.

Runs:
- TypeScript type check (`npm run lint`)
- Production build verification (`npm run build`)

Both checks must pass before a PR can be merged.

---

## Commit Conventions

Follow [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix | Use for |
|---|---|
| `feat:` | New features |
| `fix:` | Bug fixes |
| `chore:` | Tooling, CI, dependency updates |
| `docs:` | Documentation changes |
| `refactor:` | Code refactoring without behavior change |
| `style:` | Formatting, whitespace |

---

## Before Submitting a PR

```bash
npm run lint    # TypeScript type check — must pass with zero errors
npm run build   # Production build — must succeed
```

Also check:
- No unintended UI or functionality changes
- `AGENTS.md` updated if architecture changed
- No secrets committed

---

## Manual GitHub Repository Setup

After pushing the `website` branch, configure GitHub Pages:

1. Go to **Settings → Pages**.
2. Under **Source**, select **GitHub Actions**.
3. The `deploy.yml` workflow will handle all deployments automatically.

For CI checks on PRs, no additional GitHub configuration is required — the workflow activates automatically.

---

## AI Agent Notes

The authoritative technical context for AI coding agents is [`AGENTS.md`](../AGENTS.md) at the repository root. Read it before modifying any code.

Key constraints:
- Do not redesign or rebuild the website.
- Do not replace or move logo assets.
- Always run `npm run lint` before declaring a change complete.
- The `android` branch is for future native development — do not add placeholder Android code.
