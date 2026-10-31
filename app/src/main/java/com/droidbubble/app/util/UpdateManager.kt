package com.droidbubble.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppReleaseInfo(
    val tagName: String,
    val version: String,
    val name: String,
    val changelog: String,
    val isBeta: Boolean,
    val downloadUrl: String,
    val apkSize: Long
)

sealed interface UpdateCheckResult {
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

object UpdateManager {

    private const val GITHUB_RELEASES_API = "https://api.github.com/repos/Velocity081/droidbubble/releases"
    private const val GITHUB_RELEASES_ATOM = "https://github.com/Velocity081/droidbubble/releases.atom"
    private const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Droidbubble) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    fun getCurrentVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.4"
        } catch (_: Exception) {
            "1.4"
        }
    }

    suspend fun checkGithubUpdates(context: Context): UpdateCheckResult = withContext(Dispatchers.IO) {
        // Try the GitHub REST API first
        val apiResult = runCatching { fetchFromApi(context) }.getOrNull()
        if (apiResult != null && apiResult !is UpdateCheckResult.Error) {
            return@withContext apiResult
        }

        // If API returned 403 (unauthenticated rate limit), non-200, or failed, fallback to public Atom feed (no rate limits!)
        val atomResult = runCatching { fetchFromAtom(context) }.getOrNull()
        if (atomResult != null) {
            return@withContext atomResult
        }

        // Fallback error
        apiResult ?: UpdateCheckResult.Error("Unable to reach GitHub. Please check connection.")
    }

    private fun fetchFromApi(context: Context): UpdateCheckResult {
        val url = URL(GITHUB_RELEASES_API)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", DEFAULT_USER_AGENT)
            connectTimeout = 8000
            readTimeout = 8000
        }

        if (connection.responseCode !in 200..299) {
            return UpdateCheckResult.Error("HTTP Error: ${connection.responseCode}")
        }

        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        val releasesArray = JSONArray(responseText)
        if (releasesArray.length() == 0) {
            return UpdateCheckResult.UpToDate
        }

        var targetRelease: AppReleaseInfo? = null
        for (i in 0 until releasesArray.length()) {
            val relObj = releasesArray.getJSONObject(i)
            val tagName = relObj.optString("tag_name", "").trim()
            val name = relObj.optString("name", tagName)
            val body = relObj.optString("body", "No changelog provided.")
            val prerelease = relObj.optBoolean("prerelease", false)
            val isBeta = prerelease || tagName.contains("beta", ignoreCase = true) || name.contains("beta", ignoreCase = true)

            var apkUrl: String? = null
            var apkSize: Long = 0L
            val assets = relObj.optJSONArray("assets")
            if (assets != null) {
                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val aName = asset.optString("name", "").lowercase()
                    if (aName.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            val finalUrl = apkUrl
                ?: "https://github.com/Velocity081/droidbubble/releases/download/$tagName/Droidbubble.apk"

            val cleanVersion = tagName.removePrefix("v").removePrefix("V").trim()
            targetRelease = AppReleaseInfo(
                tagName = tagName,
                version = cleanVersion,
                name = name,
                changelog = body,
                isBeta = isBeta,
                downloadUrl = finalUrl,
                apkSize = apkSize
            )
            break
        }

        val release = targetRelease ?: return UpdateCheckResult.UpToDate
        val current = getCurrentVersion(context)
        return if (isNewerVersion(release.version, current)) {
            UpdateCheckResult.UpdateAvailable(release)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    private fun fetchFromAtom(context: Context): UpdateCheckResult {
        val url = URL(GITHUB_RELEASES_ATOM)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/atom+xml, text/xml, */*")
            setRequestProperty("User-Agent", DEFAULT_USER_AGENT)
            connectTimeout = 8000
            readTimeout = 8000
        }

        if (connection.responseCode !in 200..299) {
            return UpdateCheckResult.Error("HTTP Error: ${connection.responseCode}")
        }

        val atomXml = connection.inputStream.bufferedReader().use { it.readText() }
        val releases = parseAtomFeed(atomXml)
        if (releases.isEmpty()) {
            return UpdateCheckResult.UpToDate
        }

        val latest = releases.first()
        val current = getCurrentVersion(context)
        return if (isNewerVersion(latest.version, current)) {
            UpdateCheckResult.UpdateAvailable(latest)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    private fun parseAtomFeed(atomXml: String): List<AppReleaseInfo> {
        val releases = mutableListOf<AppReleaseInfo>()
        val entryRegex = Regex("<entry>([\\s\\S]*?)</entry>")
        val tagRegex = Regex("""releases/tag/([^"'\s>]+)""")
        val titleRegex = Regex("""<title>([^<]+)</title>""")
        val contentRegex = Regex("""<content type="html">([\s\S]*?)</content>""")

        for (match in entryRegex.findAll(atomXml)) {
            val entryText = match.groupValues[1]
            val tag = tagRegex.find(entryText)?.groupValues?.get(1)?.trim() ?: continue
            val title = titleRegex.find(entryText)?.groupValues?.get(1)?.trim() ?: tag
            val rawContent = contentRegex.find(entryText)?.groupValues?.get(1) ?: ""

            val changelog = rawContent
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<li>", RegexOption.IGNORE_CASE), "• ")
                .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<[^>]+>"), "")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()

            val isBeta = tag.contains("beta", ignoreCase = true) || title.contains("beta", ignoreCase = true)
            val cleanVersion = tag.removePrefix("v").removePrefix("V").trim()
            val downloadUrl = "https://github.com/Velocity081/droidbubble/releases/download/$tag/Droidbubble.apk"

            releases.add(
                AppReleaseInfo(
                    tagName = tag,
                    version = cleanVersion,
                    name = title,
                    changelog = changelog.ifBlank { "No changelog provided." },
                    isBeta = isBeta,
                    downloadUrl = downloadUrl,
                    apkSize = 0L
                )
            )
        }
        return releases
    }

    fun isNewerVersion(newVer: String, currentVer: String): Boolean {
        val newClean = newVer.trim().removePrefix("v").removePrefix("V")
        val curClean = currentVer.trim().removePrefix("v").removePrefix("V")

        if (newClean.equals(curClean, ignoreCase = true)) return false

        val newBase = newClean.substringBefore("-").substringBefore("_").substringBefore(" ").trim()
        val curBase = curClean.substringBefore("-").substringBefore("_").substringBefore(" ").trim()

        val newParts = newBase.split(".").mapNotNull { it.toIntOrNull() }
        val curParts = curBase.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(newParts.size, curParts.size)
        for (i in 0 until maxLen) {
            val n = newParts.getOrElse(i) { 0 }
            val c = curParts.getOrElse(i) { 0 }
            if (n > c) return true
            if (n < c) return false
        }

        // Base numeric versions are identical (e.g., 1.1.7 vs 1.1.7-beta.1)
        val newHasPre = newClean.contains("-") || newClean.contains("beta", ignoreCase = true) || newClean.contains("rc", ignoreCase = true)
        val curHasPre = curClean.contains("-") || curClean.contains("beta", ignoreCase = true) || curClean.contains("rc", ignoreCase = true)

        // If current installed is stable and remote is pre-release of the same version, current is newer/equal
        if (!curHasPre && newHasPre) return false

        // If current installed is pre-release and remote is stable of the same version, remote is an upgrade
        if (curHasPre && !newHasPre) return true

        // If both are pre-releases of the same base, compare build/beta numbers
        if (curHasPre && newHasPre) {
            val newPreNum = Regex("""\d+""").findAll(newClean.substringAfter("-", "")).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
            val curPreNum = Regex("""\d+""").findAll(curClean.substringAfter("-", "")).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
            return newPreNum > curPreNum
        }

        return false
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            var targetUrl = downloadUrl
            var connection: HttpURLConnection? = null
            var redirectCount = 0

            while (redirectCount < 8) {
                val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", DEFAULT_USER_AGENT)
                    connectTimeout = 15000
                    readTimeout = 30000
                }

                val code = conn.responseCode
                if (code in 301..308) {
                    val location = conn.getHeaderField("Location") ?: break
                    conn.disconnect()
                    targetUrl = location
                    redirectCount++
                } else if (code in 200..299) {
                    connection = conn
                    break
                } else {
                    conn.disconnect()
                    break
                }
            }

            val actualConn = connection ?: return@withContext null
            val totalBytes = actualConn.contentLengthLong.coerceAtLeast(1L)
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val outputFile = File(updatesDir, "Droidbubble-update.apk")
            if (outputFile.exists()) outputFile.delete()

            actualConn.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        onProgress((totalDownloaded.toFloat() / totalBytes).coerceIn(0f, 1f))
                    }
                }
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(manageIntent)
                    return
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
