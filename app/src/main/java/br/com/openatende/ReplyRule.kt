package br.com.openatende

enum class MatchMode { CONTAINS, EXACT }

data class ReplyRule(
    val trigger: String,
    val response: String,
    val mode: MatchMode,
    val enabled: Boolean = true
)

object ReplyRuleEngine {
    // Intencionalmente vazio: as regras serão cadastradas pelo usuário depois.
    private val rules: List<ReplyRule> = emptyList()

    fun findResponse(message: String): String? {
        val normalizedMessage = normalize(message)
        if (normalizedMessage.isBlank()) return null
        return rules.firstOrNull { rule ->
            if (!rule.enabled || rule.trigger.isBlank() || rule.response.isBlank()) false
            else {
                val trigger = normalize(rule.trigger)
                when (rule.mode) {
                    MatchMode.EXACT -> normalizedMessage == trigger
                    MatchMode.CONTAINS -> normalizedMessage.contains(trigger)
                }
            }
        }?.response
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")
}
