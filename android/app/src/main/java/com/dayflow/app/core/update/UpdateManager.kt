package com.dayflow.app.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.dayflow.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class NoUpdateAvailable(val currentVersion: String) : UpdateStatus()
    data class UpdateAvailable(
        val versionTag: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val channel: String
    ) : UpdateStatus()
    data class Downloading(val progress: Int) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

class UpdateManager(private val context: Context) {

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    suspend fun checkForUpdates(channel: String = "releases") {
        _updateStatus.value = UpdateStatus.Checking
        withContext(Dispatchers.IO) {
            try {
                if (channel == "ci") {
                    checkCiUpdates()
                } else {
                    checkReleaseUpdates()
                }
            } catch (e: Exception) {
                _updateStatus.value = UpdateStatus.Error(e.localizedMessage ?: "Failed to check for updates")
            }
        }
    }

    private fun checkReleaseUpdates() {
        val apiUrl = "https://api.github.com/repos/norobb/dayflow-assistant/releases/latest"
        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Dayflow-Android-App")
            connectTimeout = 8000
            readTimeout = 8000
        }

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val tagName = json.optString("tag_name", "")
            val body = json.optString("body", "New official release available.")

            var apkUrl = ""
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (apkUrl.isEmpty()) {
                apkUrl = json.optString("html_url", "https://github.com/norobb/dayflow-assistant/releases")
            }

            val currentVersion = BuildConfig.VERSION_NAME
            if (isNewerVersion(tagName, currentVersion)) {
                _updateStatus.value = UpdateStatus.UpdateAvailable(
                    versionTag = tagName,
                    releaseNotes = body,
                    downloadUrl = apkUrl,
                    channel = "releases"
                )
            } else {
                _updateStatus.value = UpdateStatus.NoUpdateAvailable(currentVersion)
            }
        } else {
            // Fallback for mock/offline or missing release on github
            _updateStatus.value = UpdateStatus.NoUpdateAvailable(BuildConfig.VERSION_NAME)
        }
    }

    /**
     * CI channel: every push to main publishes a signed APK to the rolling "ci-latest"
     * prerelease (see .github/workflows/android.yml). Its notes carry the build's versionCode.
     */
    private fun checkCiUpdates() {
        val apiUrl = "https://api.github.com/repos/norobb/dayflow-assistant/releases/tags/ci-latest"
        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Dayflow-Android-App")
            connectTimeout = 8000
            readTimeout = 8000
        }
        if (connection.responseCode != 200) {
            _updateStatus.value = UpdateStatus.Error("No CI build published yet (HTTP ${connection.responseCode})")
            return
        }
        val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val body = json.optString("body", "")
        val remoteCode = Regex("versionCode: (\\d+)").find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val assets = json.optJSONArray("assets") ?: JSONArray()
        val apkUrl = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.optString("name").endsWith(".apk") }?.optString("browser_download_url").orEmpty()

        _updateStatus.value = if (apkUrl.isNotEmpty() && remoteCode > BuildConfig.VERSION_CODE) {
            UpdateStatus.UpdateAvailable(
                versionTag = json.optString("name", "CI build"),
                releaseNotes = body,
                downloadUrl = apkUrl,
                channel = "ci"
            )
        } else {
            UpdateStatus.NoUpdateAvailable(BuildConfig.VERSION_NAME)
        }
    }

    suspend fun downloadAndInstallUpdate(updateInfo: UpdateStatus.UpdateAvailable) {
        withContext(Dispatchers.IO) {
            try {
                _updateStatus.value = UpdateStatus.Downloading(0)

                // SECURITY ENHANCEMENT: Enforce HTTPS scheme to prevent MITM attacks during update download
                if (!updateInfo.downloadUrl.startsWith("https://", ignoreCase = true)) {
                    _updateStatus.value = UpdateStatus.Error("Security error: Insecure update download URL. HTTPS is required.")
                    return@withContext
                }

                // If downloadUrl is a web page or standard release APK
                if (!updateInfo.downloadUrl.endsWith(".apk")) {
                    // Open browser URL for manual apk / release download
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    _updateStatus.value = UpdateStatus.Idle
                    return@withContext
                }

                val connection = (URL(updateInfo.downloadUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val fileLength = connection.contentLength
                val apkFile = File(context.cacheDir, "dayflow_update.apk")
                if (apkFile.exists()) apkFile.delete()

                val success = connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        downloadStreamWithLimit(
                            input = input,
                            output = output,
                            maxBytes = MAX_APK_SIZE_BYTES,
                            fileLength = fileLength,
                            onProgress = { progress -> _updateStatus.value = UpdateStatus.Downloading(progress) }
                        )
                    }
                }

                if (!success) {
                    if (apkFile.exists()) apkFile.delete()
                    _updateStatus.value = UpdateStatus.Error("Security error: Update package exceeds maximum allowed size (100MB).")
                    return@withContext
                }

                _updateStatus.value = UpdateStatus.ReadyToInstall(apkFile)
                installApk(apkFile)
            } catch (e: Exception) {
                _updateStatus.value = UpdateStatus.Error("Download failed: ${e.localizedMessage}")
            }
        }
    }

    fun installApk(apkFile: File) {
        try {
            // Android asks once per app: "Allow installing from Dayflow". Send the user there, then they tap Install again.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                _updateStatus.value = UpdateStatus.ReadyToInstall(apkFile)
                return
            }
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateStatus.value = UpdateStatus.Error("Installation trigger failed: ${e.localizedMessage}")
        }
    }

    private fun isNewerVersion(remoteTag: String, localVersion: String): Boolean {
        // "v2.1" vs "2.1-ci.57": compare the numeric parts only, so a CI build is not "updated" back to its own release.
        fun parts(v: String) = v.trim().removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val remote = parts(remoteTag)
        val local = parts(localVersion)
        for (i in 0 until maxOf(remote.size, local.size)) {
            val r = remote.getOrElse(i) { 0 }
            val l = local.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    companion object {
        const val MAX_APK_SIZE_BYTES: Long = 100L * 1024L * 1024L // 100 MB limit

        /**
         * SECURITY ENHANCEMENT: Read input stream into output stream enforcing a maximum byte limit.
         * Prevents unbounded file downloads from exhausting device storage space (Storage Exhaustion DoS).
         * Returns false if total downloaded size exceeds maxBytes limit.
         */
        fun downloadStreamWithLimit(
            input: java.io.InputStream,
            output: java.io.OutputStream,
            maxBytes: Long = MAX_APK_SIZE_BYTES,
            fileLength: Int = -1,
            onProgress: ((Int) -> Unit)? = null
        ): Boolean {
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (total > maxBytes) {
                    return false
                }
                if (fileLength > 0 && onProgress != null) {
                    val progress = ((total * 100) / fileLength).toInt()
                    onProgress(progress)
                }
                output.write(data, 0, count)
            }
            return true
        }
    }
}
