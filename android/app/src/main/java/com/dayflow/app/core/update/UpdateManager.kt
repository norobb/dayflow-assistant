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

    private fun checkCiUpdates() {
        val apiUrl = "https://api.github.com/repos/norobb/dayflow-assistant/actions/runs?status=success&per_page=1"
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
            val runs = json.optJSONArray("workflow_runs")
            if (runs != null && runs.length() > 0) {
                val latestRun = runs.getJSONObject(0)
                val runNumber = latestRun.optInt("run_number", 0)
                val commitMsg = latestRun.optJSONObject("head_commit")?.optString("message") ?: "Latest CI build from main branch"
                val htmlUrl = latestRun.optString("html_url", "")
                val versionTag = "CI Build #$runNumber"

                // Compare run number or flag update available
                _updateStatus.value = UpdateStatus.UpdateAvailable(
                    versionTag = versionTag,
                    releaseNotes = commitMsg,
                    downloadUrl = htmlUrl,
                    channel = "ci"
                )
                return
            }
        }
        _updateStatus.value = UpdateStatus.NoUpdateAvailable(BuildConfig.VERSION_NAME)
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

                connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val data = ByteArray(4096)
                        var total: Long = 0
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            total += count
                            if (fileLength > 0) {
                                val progress = (total * 100 / fileLength).toInt()
                                _updateStatus.value = UpdateStatus.Downloading(progress)
                            }
                            output.write(data, 0, count)
                        }
                    }
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
        val cleanRemote = remoteTag.replace("v", "").trim()
        val cleanLocal = localVersion.replace("v", "").trim()
        return cleanRemote != cleanLocal && cleanRemote.isNotEmpty()
    }
}
