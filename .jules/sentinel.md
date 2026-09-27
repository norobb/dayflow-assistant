## 2026-09-27 - Cryptographically Secure Unique ID Generation
**Vulnerability:** Weak pseudo-random number generator (`Math.random()`) used as fallback for unique ID generation in state store.
**Learning:** Standard React state generators often default to `Math.random().toString(36)` when `crypto.randomUUID()` is missing, creating predictable state identifiers and potentially sensitive token guessing risks.
**Prevention:** Always use `crypto.getRandomValues()` as the secondary fallback if `crypto.randomUUID()` is unavailable in legacy browser environments.
