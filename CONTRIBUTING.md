# Contributing to Dayflow

Thank you for your interest in contributing to Dayflow. This guide covers the workflow, branch strategy, and standards for the repository.

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code. Auto-deploys to GitHub Pages. |
| `develop` | Integration branch for in-progress work. All feature PRs target `develop`. |
| `feature/*` | New features or enhancements (e.g. `feature/voice-input-improvements`). |
| `fix/*` | Bug fixes (e.g. `fix/simulator-corner-clip`). |
| `chore/*` | Tooling, CI, dependency updates (e.g. `chore/update-motion`). |
| `docs/*` | Documentation-only changes (e.g. `docs/update-agents-md`). |

### Release Flow

```
feature/* → develop → main (→ auto-deploys to GitHub Pages)
```

Changes always flow from feature branches into `develop` first. When `develop` is stable and ready for production, it is merged into `main` via a PR, which triggers automatic deployment.

---

## Development Setup

```bash
# Clone the repository
git clone https://github.com/norobb/dayflow-assistant.git
cd dayflow-assistant

# Install dependencies
npm install

# Start local dev server (http://localhost:3000)
npm run dev
```

---

## Making a Change

1. **Branch from `develop`** (not from `main`):
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Read `AGENTS.md`** before modifying any code — it documents the full architecture, design system, and constraints.

3. **Make your changes**, then verify:
   ```bash
   npm run lint    # TypeScript type check — must pass
   npm run build   # Production build — must succeed
   ```

4. **Commit** with a meaningful message:
   ```bash
   git commit -m "feat: describe what you did"
   ```
   Follow [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`.

5. **Open a PR** targeting `develop`. Fill out the PR template completely.

---

## Product Areas

This repository covers two distinct product areas:

### 1. Dayflow Website (`/src`)
The existing React + Vite + TypeScript + Tailwind CSS v4 web application.

- **Do not** redesign or rebuild the website
- **Do not** replace the existing branding or logo assets
- **Do not** modify the Tailwind/Motion/TypeScript stack without discussion
- **Always** run `npm run lint` before marking a PR ready

### 2. Future Android Application
The native Android app (Kotlin / Jetpack Compose) will live in a future dedicated directory or repository. Contributors interested in this area should open a Feature Request issue to discuss scope and architecture.

---

## Design System Rules

- **Color palette**: Burgundy `#641C24`, linen `#F5EFE6`, charcoal `#1E1B19` — see `AGENTS.md §7`
- **Logo**: The official logo assets in `src/assets/` and `public/assets/` are the **only** valid versions. Never replace, regenerate, or redesign them
- **Typography**: Plus Jakarta Sans (primary), Newsreader (editorial serif)
- **Animations**: Use `motion/react` (Framer Motion v12). Never add new animation libraries

---

## CI Checks

All pull requests automatically run:
- **TypeScript type check** (`npm run lint`)
- **Production build verification** (`npm run build`)

Both checks must pass before a PR can be merged.

---

## Questions

Open a GitHub Discussion or Issue — we're happy to help.
