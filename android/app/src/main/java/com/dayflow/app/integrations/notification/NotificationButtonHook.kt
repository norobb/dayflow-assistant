package com.dayflow.app.integrations.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Binder
import android.service.notification.StatusBarNotification
import com.dayflow.app.BuildConfig
import com.dayflow.app.R
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Root only (LSPosed/Vector, scope "android"): runs inside system_server and adds an
 * "Analyze with Dayflow" button to every notification before it reaches the shade.
 * Nothing is read or sent anywhere until the user taps it; the tap opens Dayflow's
 * review screen with the notification text (same path as the old "Review" action).
 */
class NotificationButtonHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android" || android.os.Build.VERSION.SDK_INT < 29) return
        val record = XposedHelpers.findClass("com.android.server.notification.NotificationRecord", lpparam.classLoader)
        XposedBridge.hookAllConstructors(record, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val context = param.args.firstOrNull { it is Context } as? Context ?: return
                    val sbn = param.args.firstOrNull { it is StatusBarNotification } as? StatusBarNotification ?: return
                    addButton(context, sbn)
                } catch (t: Throwable) {
                    XposedBridge.log("Dayflow: $t") // never break notification posting
                }
            }
        })
        XposedBridge.log("Dayflow: notification button hook active")
    }

    private fun addButton(context: Context, sbn: StatusBarNotification) {
        val n = sbn.notification ?: return
        if (sbn.packageName.startsWith("com.dayflow.app")) return
        if (n.flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE or Notification.FLAG_GROUP_SUMMARY) != 0) return
        val actions: Array<Notification.Action> = n.actions ?: emptyArray()
        if (actions.any { it.extras.getBoolean(MARKER) }) return
        val extras = n.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT))
            ?.toString()?.trim().orEmpty()
        if (text.isBlank()) return

        val intent = Intent().apply {
            component = ComponentName(BuildConfig.APPLICATION_ID, "com.dayflow.app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationHelper.EXTRA_REVIEW_TEXT, (if (title.isBlank()) text else "$title: $text").take(4000))
        }
        // We run on the posting app's binder thread; the PendingIntent must belong to "android".
        val token = Binder.clearCallingIdentity()
        val pi = try {
            PendingIntent.getActivity(context, sbn.key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } finally {
            Binder.restoreCallingIdentity(token)
        }
        val visible = actions.count { !it.isContextual }
        val action = Notification.Action.Builder(Icon.createWithResource(BuildConfig.APPLICATION_ID, R.drawable.dayflow_symbol_mono), label(context), pi)
            .addExtras(android.os.Bundle().apply { putBoolean(MARKER, true) })
            // The shade shows at most 3 buttons; with 3 already (e.g. YouTube) a contextual chip still shows up below them.
            .setContextual(visible >= 3)
            .build()
        n.actions = actions + action
    }

    private fun label(context: Context): String = when (context.resources.configuration.locales[0].language) {
        "de" -> "Mit Dayflow analysieren"
        "es" -> "Analizar con Dayflow"
        else -> "Analyze with Dayflow"
    }

    private companion object {
        const val MARKER = "dayflow_analyze"
    }
}
