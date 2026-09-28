## 2026-09-27 - Cryptographically Secure Unique ID Generation
**Vulnerability:** Weak pseudo-random number generator (`Math.random()`) used as fallback for unique ID generation in state store.
**Learning:** Standard React state generators often default to `Math.random().toString(36)` when `crypto.randomUUID()` is missing, creating predictable state identifiers and potentially sensitive token guessing risks.
**Prevention:** Always use `crypto.getRandomValues()` as the secondary fallback if `crypto.randomUUID()` is unavailable in legacy browser environments.

## 2026-09-28 - HTTPS Enforcement for APK In-App Updates
**Vulnerability:** In-app update download handler accepted arbitrary network schemes without enforcing secure transport.
**Learning:** Auto-update mechanisms that fetch binary URLs from remote API responses must validate protocol schemes to prevent cleartext HTTP downgrade and MITM executable package substitution attacks.
**Prevention:** Always check `downloadUrl.startsWith("https://", ignoreCase = true)` before opening network streams or passing URLs to system package download intents.
