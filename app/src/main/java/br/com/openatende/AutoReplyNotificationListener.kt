package br.com.openatende

import android.content.Context
import android.util.Log

/**
 * Receives a WhatsApp trigger and routes it to the selected provider module.
 * Provider execution is intentionally not reported as successful until the
 * authenticated panel automation is implemented and verified.
 */
object TestAutomationRouter {
    private const val PREFS = "test_automation"
    private const val KEY_LAST_PROVIDER = "last_requested_provider"
    private const val KEY_LAST_TRIGGER = "last_requested_trigger"
    private const val KEY_LAST_STATUS = "last_requested_status"

    fun queue(context: Context, rule: ReplyRule, message: String) {
        val provider = when (rule.action) {
            RuleAction.TEST_MASTERFLIX -> "masterflix"
            RuleAction.TEST_STV -> "stv"
            RuleAction.REPLY -> return
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_PROVIDER, provider)
            .putString(KEY_LAST_TRIGGER, rule.trigger)
            .putString(KEY_LAST_STATUS, "waiting_for_provider_automation")
            .apply()
        Log.i("OpenAtende", "Gatilho encaminhado ao módulo $provider. A execução do painel ainda não está habilitada.")
    }
}
