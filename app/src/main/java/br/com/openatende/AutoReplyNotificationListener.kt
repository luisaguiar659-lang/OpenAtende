package br.com.openatende

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class AutoReplyNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (sbn.packageName != WHATSAPP_BUSINESS_PACKAGE) return

        val prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        if (!prefs.getBoolean(MainActivity.KEY_ENABLED, false)) return

        val notification = sbn.notification ?: return
        val message = extractMessage(notification) ?: return
        val response = ReplyRuleEngine.findResponse(this, message) ?: return
        if (!NotificationAccess.isEnabled(this)) return

        val sentKey = "${sbn.key}:$message"
        if (prefs.getString("last_reply_key", null) == sentKey) return

        val replyAction = notification.actions?.firstNotNullOfOrNull { action ->
            val inputs = action.remoteInputs ?: return@firstNotNullOfOrNull null
            if (inputs.isEmpty()) null else action to inputs
        } ?: return

        val (action, remoteInputs) = replyAction
        val fillInIntent = Intent()
        val results = Bundle()
        remoteInputs.forEach { input -> results.putCharSequence(input.resultKey, response) }
        RemoteInput.addResultsToIntent(remoteInputs, fillInIntent, results)

        try {
            action.actionIntent.send(this, 0, fillInIntent)
            prefs.edit().putString("last_reply_key", sentKey)\n                .putInt(MainActivity.KEY_SENT_COUNT, prefs.getInt(MainActivity.KEY_SENT_COUNT, 0) + 1)\n                .apply()
            Log.i(TAG, "Resposta automática enviada.")
        } catch (error: Exception) {
            Log.e(TAG, "Falha ao responder pela notificação.", error)
        }
    }

    private fun extractMessage(notification: Notification): String? {
        val extras = notification.extras ?: return null
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: return null
        return text.toString().takeIf { it.isNotBlank() }
    }

    companion object {
        private const val TAG = "OpenAtende"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }
}
