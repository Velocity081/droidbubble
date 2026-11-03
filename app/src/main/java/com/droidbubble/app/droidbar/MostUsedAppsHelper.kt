package com.droidbubble.app.droidbar

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process

data class BarAppInfo(
    val packageName: String,
    val label: String,
    val iconBitmap: Bitmap?
)

object MostUsedAppsHelper {

    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
        val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
        return try {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Throwable) {
            null
        }
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getMostUsedApps(context: Context, limit: Int = 6): List<BarAppInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        val launcherPackages = resolveInfos.map { it.activityInfo.packageName }.toSet()

        if (hasUsageStatsPermission(context)) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 1000L * 60 * 60 * 24 * 7 // past 7 days
            val stats = usageStatsManager?.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, startTime, endTime)

            if (!stats.isNullOrEmpty()) {
                val usageByPackage = mutableMapOf<String, Long>()
                for (stat in stats) {
                    val pkg = stat.packageName
                    if (pkg in launcherPackages && pkg != context.packageName) {
                        val current = usageByPackage[pkg] ?: 0L
                        usageByPackage[pkg] = current + stat.totalTimeInForeground
                    }
                }
                val sorted = usageByPackage.entries
                    .filter { it.value > 0 }
                    .sortedByDescending { it.value }
                    .map { it.key }

                if (sorted.isNotEmpty()) {
                    val result = sorted.take(limit).mapNotNull { pkg ->
                        try {
                            val appInfo = pm.getApplicationInfo(pkg, 0)
                            BarAppInfo(
                                packageName = pkg,
                                label = pm.getApplicationLabel(appInfo).toString(),
                                iconBitmap = drawableToBitmap(pm.getApplicationIcon(appInfo))
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (result.isNotEmpty()) return result
                }
            }
        }

        // Fallback: list of installed launcher apps (excluding own app)
        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .take(limit)
            .map { res ->
                BarAppInfo(
                    packageName = res.activityInfo.packageName,
                    label = res.loadLabel(pm).toString(),
                    iconBitmap = drawableToBitmap(res.loadIcon(pm))
                )
            }
    }
    fun getAllInstalledApps(context: Context): List<BarAppInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { res ->
                BarAppInfo(
                    packageName = res.activityInfo.packageName,
                    label = res.loadLabel(pm).toString(),
                    iconBitmap = drawableToBitmap(res.loadIcon(pm))
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun getAppsByPackages(context: Context, packages: List<String>): List<BarAppInfo> {
        val pm = context.packageManager
        return packages.mapNotNull { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                BarAppInfo(
                    packageName = pkg,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    iconBitmap = drawableToBitmap(pm.getApplicationIcon(appInfo))
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
