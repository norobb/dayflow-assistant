## 2026-09-27 - Cryptographically Secure Unique ID Generation
**Vulnerability:** Weak pseudo-random number generator (`Math.random()`) used as fallback for unique ID generation in state store.
**Learning:** Standard React state generators often default to `Math.random().toString(36)` when `crypto.randomUUID()` is missing, creating predictable state identifiers and potentially sensitive token guessing risks.
**Prevention:** Always use `crypto.getRandomValues()` as the secondary fallback if `crypto.randomUUID()` is unavailable in legacy browser environments.

## 2026-09-28 - HTTPS Enforcement for APK In-App Updates
**Vulnerability:** In-app update download handler accepted arbitrary network schemes without enforcing secure transport.
**Learning:** Auto-update mechanisms that fetch binary URLs from remote API responses must validate protocol schemes to prevent cleartext HTTP downgrade and MITM executable package substitution attacks.
**Prevention:** Always check `downloadUrl.startsWith("https://", ignoreCase = true)` before opening network streams or passing URLs to system package download intents.

## 2026-09-29 - API Key Redaction in Exception Strings & Model Parameter Validation
**Vulnerability:** Exception messages and network error streams in HTTP clients retain sensitive URL query parameters (e.g. `?key=AIzaSy...`), leaking credentials to UI error states and logs.
**Learning:** Java `HttpURLConnection` includes full requested URL query strings in exception messages when connections fail or return HTTP errors. Error messages must explicitly sanitize raw keys and URL query parameters before propagating them to user-facing models or loggers.
**Prevention:** Filter input model overrides with strict regex (`^[a-zA-Z0-9._-]+$`) and pass all error text through a credential redactor that replaces matching secrets and `key=...` params with `[REDACTED]`.
