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

## 2026-09-30 - Bounded Stream Buffer Reading for Shared Document Snippets
**Vulnerability:** Reading shared document streams line-by-line (`reader.readLine()`) without length caps allows minified files or binary streams without newlines to trigger Out-Of-Memory (OOM) Denial of Service (DoS).
**Learning:** `BufferedReader.readLine()` buffers an entire line in memory regardless of configured downstream character snippet limits. Untrusted content streams shared via Android intents must be processed using fixed-size `Reader` buffers with hard character bounds.
**Prevention:** Always read untrusted content streams into fixed byte/character buffers (`CharArray(512)`) enforcing explicit `maxChars` read limits.

## 2026-10-01 - Bounded Byte Stream Reading for Shared Media Intake
**Vulnerability:** Unbounded byte reading (`it.readBytes()`) on shared intent content streams in Android intake activity (`ShareActivity.kt`) allowed oversized images, PDFs, or audio files to trigger Out-Of-Memory (OOM) crashes.
**Learning:** `InputStream.readBytes()` reads all available stream data into a dynamically resizing byte array without hard bounds checks, making Android activities accepting shared external content vulnerable to OOM Denial of Service (DoS) attacks.
**Prevention:** Process untrusted `InputStream` binaries using bounded buffer loops (`readBytesWithLimit(stream, maxBytes)`) enforcing an explicit maximum byte threshold (e.g., 10MB) and gracefully handle size threshold violations.

## 2026-10-02 - Bounded Byte Stream Reading for In-App APK Updates
**Vulnerability:** Unbounded file stream writing when downloading update packages in `UpdateManager` allowed oversized binary payloads or infinite streams to trigger storage exhaustion Denial of Service (DoS).
**Learning:** In-app update downloaders that stream binary responses directly to local disk without byte limit bounds can fill device storage completely or crash if the remote endpoint serves an excessively large file.
**Prevention:** Always process network download streams using bounded byte thresholds (`downloadStreamWithLimit(input, output, maxBytes)`) enforcing an explicit maximum byte cap (e.g., 100MB), deleting partial files if the limit is exceeded.
