package br.com.openatende

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object NotificationAccess {
    fun isEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ) ?: return false
        val expected = ComponentName(
            context, AutoReplyNotificationListener::class.java
        ).flattenToString()
        return enabledListeners.split(":").any { component ->
            TextUtils.equals(
                ComponentName.unflattenFromString(component)?.flattenToString(),
                expected
            )
        }
    }
}
