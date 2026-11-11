package br.com.openatende

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Routes a WhatsApp test command to the in-app provider panel and, when the
 * notification exposes a reply action, sends the captured result to that chat.
 */
object TestAutomationRouter {
    private const val PREFS = "test_automation"
    private const val KEY_LAST_PROVIDER = "last_requested_provider"
    private const val KEY_LAST_TRIGGER = "last_requested_trigger"
    private const val KEY_LAST_STATUS = "last_requested_status"
    private const val KEY_LAST_MESSAGE = "last_requested_message"

    @Volatile private var replyPendingIntent: PendingIntent? = null
    @Volatile private var replyRemoteInputs: Array<RemoteInput>? = null

    @Synchronized
    fun queue(context: Context, rule: ReplyRule, message: String, replyAction: Notification.Action?) {
        val provider = when (rule.action) {
            RuleAction.TEST_MASTERFLIX -> "masterflix"
            RuleAction.TEST_STV -> "stv"
            RuleAction.REPLY -> return
        }

        val action = replyAction
        replyPendingIntent = action?.actionIntent
        replyRemoteInputs = action?.remoteInputs

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_PROVIDER, provider)
            .putString(KEY_LAST_TRIGGER, rule.trigger)
            .putString(KEY_LAST_MESSAGE, message)
            .putString(KEY_LAST_STATUS, "opening_provider_panel")
            .apply()

        try {
            val launch = Intent(context, TestCenterActivity::class.java).apply {
                putExtra(TestCenterActivity.EXTRA_PROVIDER, provider)
                putExtra(TestCenterActivity.EXTRA_AUTO_OPEN_PANEL, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(launch)
            Log.i("OpenAtende", "Painel interno $provider solicitado pelo comando do WhatsApp.")
        } catch (error: Exception) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_STATUS, "panel_launch_blocked")
                .apply()
            Log.e("OpenAtende", "O Android bloqueou a abertura automática do painel.", error)
        }
    }

    @Synchronized
    fun complete(context: Context, copiedText: String): Boolean {
        if (copiedText.isBlank()) return false
        val pendingIntent = replyPendingIntent
        val remoteInputs = replyRemoteInputs
        if (pendingIntent == null || remoteInputs.isNullOrEmpty()) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_STATUS, "test_captured_reply_action_unavailable")
                .putString("last_copied_test", copiedText)
                .apply()
            return false
        }

        return try {
            val fillInIntent = Intent()
            val results = Bundle()
            remoteInputs.forEach { input -> results.putCharSequence(input.resultKey, copiedText) }
            RemoteInput.addResultsToIntent(remoteInputs, fillInIntent, results)
            pendingIntent.send(context, 0, fillInIntent)
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_STATUS, "test_result_sent")
                .putString("last_copied_test", copiedText)
                .apply()
            replyPendingIntent = null
            replyRemoteInputs = null
            Log.i("OpenAtende", "Resultado do teste enviado à conversa que solicitou.")
            true
        } catch (error: Exception) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_STATUS, "test_captured_send_failed")
                .putString("last_copied_test", copiedText)
                .apply()
            Log.e("OpenAtende", "Falha ao enviar o resultado do teste ao WhatsApp.", error)
            false
        }
    }
}
