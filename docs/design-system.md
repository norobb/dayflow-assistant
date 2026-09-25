# Dayflow — Design System

This document describes the existing Dayflow visual system. It is a reference for contributors. Do not introduce changes to the design system without explicit product discussion.

---

## Brand Identity

Dayflow uses a refined, modern editorial design system characterized by warm linen tones, deep wine burgundy accents, structured card surfaces, and subtle tactile micro-interactions.

**The official branding is fixed.** Do not redesign, reinterpret, or replace:
- The `DayflowSymbol` SVG component (`src/components/DayflowLogo.tsx`)
- The `DayflowLogo` wordmark component
- Logo assets in `src/assets/` and `public/assets/`
- The favicon SVG data URI in `index.html`

---

## Colors

Defined as CSS custom properties in `src/index.css`.

| Token | Value | Usage |
|---|---|---|
| `--color-bg` | `#F5EFE6` | Warm linen canvas — page background |
| `--color-card` | `#FAF6F0` | Soft cream — card and panel surfaces |
| `--color-card-white` | `#FFFFFF` | Pure white — elevated card surfaces |
| `--color-primary` | `#641C24` | Deep burgundy — primary brand color, CTAs, marks |
| `--color-primary-light` | `#7E242F` | Burgundy hover state |
| `--color-primary-soft` | `#F0E4E6` | Light burgundy tint — insight card backgrounds |
| `--color-text` | `#1E1B19` | Dark charcoal — body text and headings |
| `--color-text-muted` | `#6B635B` | Warm taupe — secondary and muted text |
| `--color-border` | `#D8CFC2` | Soft warm border line |

### Accent Colors

| Color | Value | Usage |
|---|---|---|
| Accent Green | `#2E5C38` | Completed tasks, success states |
| Accent Amber | `#8C5E28` | State badges, "COMING SOON" indicators |

---

## Typography

### Typefaces

| Role | Typeface | Fallback | Usage |
|---|---|---|---|
| Primary | *Plus Jakarta Sans* | system-ui, sans-serif | All UI text, headings, body, labels |
| Editorial serif | *Newsreader* | Georgia, serif | Pull quotes, editorial headlines |

Both are loaded via Google Fonts in `index.html`.

### Scale (Tailwind utility classes)

| Class | Usage |
|---|---|
| `text-xs` / `text-sm` | Labels, captions, badges |
| `text-base` | Body text |
| `text-lg` / `text-xl` | Subheadings |
| `text-2xl` / `text-3xl` | Section headings |
| `text-4xl`+ | Hero headings |
| `font-medium` | Default body weight |
| `font-semibold` | Subheadings, card titles |
| `font-bold` / `font-extrabold` | Primary headings, CTA labels |

---

## Spacing

Dayflow uses Tailwind CSS v4's default spacing scale. No custom spacing tokens are defined.

Standard internal padding for cards: `p-5` or `p-6`.  
Standard gap between grid items: `gap-4` or `gap-6`.  
Section vertical padding: `py-16` to `py-24`.

---

## Cards & Surfaces

| Surface | Classes | Description |
|---|---|---|
| Standard card | `bg-[var(--color-card)] rounded-2xl` | Primary card surface |
| White card | `bg-white rounded-2xl` | Elevated, higher contrast surface |
| Insight card | `bg-[var(--color-primary-soft)] rounded-2xl` | Burgundy-tinted context card |
| Phone simulator | `bg-[#FAF6F0]` inside `rounded-[38px] overflow-hidden` | Android screen viewport |

---

## Buttons & Controls

| Type | Classes | Usage |
|---|---|---|
| Primary CTA | `bg-[var(--color-primary)] text-white rounded-xl px-5 py-2.5` | Main action buttons |
| Primary hover | `hover:bg-[var(--color-primary-light)]` | Hover state on primary buttons |
| Ghost / outline | `border border-[var(--color-border)] rounded-xl` | Secondary actions |
| Icon button | `rounded-full p-2` | Toolbar icon buttons |
| Tab button | `rounded-xl` | Demo and phone nav tabs |

---

## Border Radius

| Token | Value | Used on |
|---|---|---|
| `rounded-md` | 6px | Small badges |
| `rounded-lg` | 8px | Small controls |
| `rounded-xl` | 12px | Buttons, tags |
| `rounded-2xl` | 16px | Cards, panels |
| `rounded-3xl` | 24px | Large feature cards |
| `rounded-full` | 9999px | Pills, avatar-style badges |
| `rounded-[38px]` | 38px | Phone screen viewport |
| `rounded-[50px]` | 50px | Phone outer bezel |

---

## Shadows

Dayflow uses subtle, warm shadows — no harsh drop shadows.

| Usage | Class |
|---|---|
| Standard card elevation | `shadow-sm` |
| Slightly elevated surfaces | `shadow-md` |
| Phone simulator | `shadow-2xl` |

---

## Responsive Behavior

Dayflow is designed desktop-first as a product showcase site. The layout uses:
- Single column on mobile
- Two-column grids on `md:` and above
- Three or four-column grids on `lg:` and above

The Android Simulator is a fixed 340×680 px component — it does not resize responsively. On smaller screens the page layout reflows around it.

---

## Animation Principles

Animations are implemented with **Motion (Framer Motion 12)**, imported from `motion/react`.

Core animation utilities live in `src/utils/motion.tsx`.

### Easing

```ts
// Custom cubic bezier — fast out, slight spring feel
export const cubicEase = [0.16, 1, 0.3, 1];
```

### Principles

| Principle | Implementation |
|---|---|
| Presence transitions | `AnimatePresence` with `initial / animate / exit` |
| Entry animations | Slide up + fade in (`y: 20 → 0`, `opacity: 0 → 1`) |
| Exit animations | Fade out + collapse |
| Spring physics | Used for notification toast slides and phone interactions |
| Reduced motion | `ScrollReveal` respects `prefers-reduced-motion` — disables transform animations |

### Do not

- Add new animation libraries alongside Motion.
- Use CSS keyframe animations for interactive state transitions — use Motion variants.
- Use `useLayoutEffect`-based animation hacks.

---

## State Badges

The `ProductStateBadge` component (`src/components/DayflowLogo.tsx`) renders status labels.

**Rules:**
- Working, live features must **never** receive state badges (`"NOW"`, `"ACTIVE"`, `"LIVE"`).
- Only genuine upcoming or unreleased features may display `"COMING SOON"` or `"LATER"` badges.

---

## Sound & Haptics

Micro-interactions include synthesized Web Audio API tones and optional haptic feedback:

| Event | Sound | Haptic |
|---|---|---|
| UI click | Short click tone | None |
| Analysis start | Sweep tone | None |
| Success (add event/task) | 2-note fifth chord | 8ms vibration |
| Toggle | Brief toggle tone | None |

Sound can be muted via the `soundMuted` state slice in `dayflowStore`.  
Haptics use `navigator.vibrate()` — silently unavailable on unsupported platforms.
