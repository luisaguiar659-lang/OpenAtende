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
        val enabledRules = ReplyRuleStore.load(context).filter { it.enabled && it.trigger.isNotBlank() }

        // Test commands must win over broad greeting/reply rules, regardless of
        // the order in which the user created the rules.
        fun matches(rule: ReplyRule): Boolean {
            val trigger = normalize(rule.trigger)
            return when (rule.mode) {
                MatchMode.EXACT -> normalizedMessage == trigger
                MatchMode.CONTAINS -> normalizedMessage.contains(trigger)
            }
        }

        val testRules = enabledRules.filter { it.action != RuleAction.REPLY }
        val replyRules = enabledRules.filter { it.action == RuleAction.REPLY }

        return testRules.firstOrNull { it.mode == MatchMode.EXACT && matches(it) }
            ?: testRules.firstOrNull { matches(it) }
            ?: replyRules.firstOrNull { it.mode == MatchMode.EXACT && matches(it) }
            ?: replyRules.firstOrNull { matches(it) }
    }

    fun findResponse(context: Context, message: String): String? {
        val rule = findMatchingRule(context, message) ?: return null
        return if (rule.action == RuleAction.REPLY) rule.response.takeIf { it.isNotBlank() } else null
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")
}
