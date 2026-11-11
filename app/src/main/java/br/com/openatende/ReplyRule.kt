package br.com.openatende

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class MatchMode { CONTAINS, EXACT }
enum class RuleAction { REPLY, TEST_MASTERFLIX, TEST_STV }

data class ReplyRule(
    val trigger: String,
    val response: String,
    val mode: MatchMode,
    val enabled: Boolean = true,
    val action: RuleAction = RuleAction.REPLY
)

object ReplyRuleStore {
    private const val KEY_RULES = "reply_rules"

    fun load(context: Context): MutableList<ReplyRule> {
        val raw = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
            .getString(KEY_RULES, "[]") ?: "[]"
        val result = mutableListOf<ReplyRule>()
        return try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(ReplyRule(
                    item.optString("trigger"),
                    item.optString("response"),
                    runCatching { MatchMode.valueOf(item.optString("mode", "CONTAINS")) }.getOrDefault(MatchMode.CONTAINS),
                    item.optBoolean("enabled", true),
                    runCatching { RuleAction.valueOf(item.optString("action", "REPLY")) }.getOrDefault(RuleAction.REPLY)
                ))
            }
            result
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun add(context: Context, rule: ReplyRule) {
        val rules = load(context)
        rules.add(rule)
        save(context, rules)
    }

    fun update(context: Context, index: Int, rule: ReplyRule) {
        val rules = load(context)
        if (index in rules.indices) {
            rules[index] = rule
            save(context, rules)
        }
    }

    fun remove(context: Context, index: Int) {
        val rules = load(context)
        if (index in rules.indices) {
            rules.removeAt(index)
            save(context, rules)
        }
    }

    private fun save(context: Context, rules: List<ReplyRule>) {
        val array = JSONArray()
        rules.forEach { rule ->
            array.put(JSONObject().apply {
                put("trigger", rule.trigger)
                put("response", rule.response)
                put("mode", rule.mode.name)
                put("enabled", rule.enabled)
                put("action", rule.action.name)
            })
        }
        context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_RULES, array.toString()).apply()
    }
}

object ReplyRuleEngine {
    fun findMatchingRule(context: Context, message: String): ReplyRule? {
        val normalizedMessage = normalize(message)
        if (normalizedMessage.isBlank()) return null
        return ReplyRuleStore.load(context).firstOrNull { rule ->
            if (!rule.enabled || rule.trigger.isBlank()) false
            else {
                val trigger = normalize(rule.trigger)
                when (rule.mode) {
                    MatchMode.EXACT -> normalizedMessage == trigger
                    MatchMode.CONTAINS -> normalizedMessage.contains(trigger)
                }
            }
        }
    }

    fun findResponse(context: Context, message: String): String? {
        val rule = findMatchingRule(context, message) ?: return null
        return if (rule.action == RuleAction.REPLY) rule.response.takeIf { it.isNotBlank() } else null
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")
}
