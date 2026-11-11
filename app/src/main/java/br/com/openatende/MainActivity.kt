package br.com.openatende

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var ruleListContainer: LinearLayout
    private var ruleSearchQuery = ""
    private var ruleFilter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(244, 247, 251))
            isFillViewport = true
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }
        scroll.addView(content)
        setContentView(scroll)
        renderDashboard()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) renderDashboard()
    }

    private fun renderDashboard() {
        content.removeAllViews()
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val access = NotificationAccess.isEnabled(this)
        val automation = prefs.getBoolean(KEY_ENABLED, false)
        val rules = ReplyRuleStore.load(this)
        val sentCount = prefs.getInt(KEY_SENT_COUNT, 0)

        content.addView(TextView(this).apply {
            text = "OpenAtende"
            textSize = 29f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(18, 59, 93))
        })
        content.addView(TextView(this).apply {
            text = "Painel de atendimento"
            textSize = 15f
            setTextColor(Color.rgb(93, 108, 124))
            setPadding(0, dp(4), 0, dp(20))
        })

        val status = card()
        status.addView(sectionLabel("STATUS DO APLICATIVO"))
        status.addView(statusLine("Notificações", if (access) "AUTORIZADO" else "PENDENTE", access))
        status.addView(statusLine("Automação", if (automation) "ATIVADA" else "DESATIVADA", automation))
        status.addView(actionButton(if (access) "GERENCIAR PERMISSÃO" else "AUTORIZAR NOTIFICAÇÕES") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }, fullWidth())
        content.addView(status, spaced())

        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        stats.addView(statCard("REGRAS", rules.size.toString(), "respostas cadastradas"),
            LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginEnd = dp(6) })
        stats.addView(statCard("RESPOSTAS ENVIADAS", sentCount.toString(), "desde a instalação"),
            LinearLayout.LayoutParams(0, dp(112), 1f).apply { marginStart = dp(6) })
        content.addView(stats, spaced())

        val control = card()
        control.addView(sectionLabel("CONTROLE DA AUTOMAÇÃO"))
        control.addView(TextView(this).apply {
            text = if (automation) "O atendimento automático está ligado." else "O atendimento automático está pausado."
            textSize = 15f
            setTextColor(Color.rgb(54, 68, 82))
            setPadding(0, dp(7), 0, dp(12))
        })
        control.addView(actionButton(if (automation) "DESATIVAR AUTOMAÇÃO" else "ATIVAR AUTOMAÇÃO") {
            prefs.edit().putBoolean(KEY_ENABLED, !automation).apply()
            renderDashboard()
        }, fullWidth())
        content.addView(control, spaced())

        val testCenter = card()
        testCenter.addView(sectionLabel("AUTOMAÇÃO COMERCIAL"))
        testCenter.addView(TextView(this).apply {
            text = "Central de Testes"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 61, 88))
            setPadding(0, dp(6), 0, dp(4))
        })
        testCenter.addView(TextView(this).apply {
            text = "Prepare a integração de geração de testes MasterFlix e STV+."
            textSize = 13f
            setTextColor(Color.rgb(75, 88, 102))
        })
        testCenter.addView(actionButton("ABRIR CENTRAL DE TESTES") {
            startActivity(Intent(this, TestCenterActivity::class.java))
        }, fullWidth())
        content.addView(testCenter, spaced())

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "Respostas automáticas"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(32, 48, 64))
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(actionButton("+ ADICIONAR") { showAddRuleDialog() })
        content.addView(header, spaced())

        val search = EditText(this).apply {
            hint = "Pesquisar por gatilho ou resposta..."
            isSingleLine = true
            setText(ruleSearchQuery)
            textSize = 14f
            setPadding(dp(14), dp(4), dp(14), dp(4))
            background = rounded(Color.WHITE, dp(10))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    ruleSearchQuery = s?.toString().orEmpty()
                    renderRuleCards(rules)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        content.addView(search, spaced())

        val filters = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            setPadding(0, 0, 0, dp(8))
        }
        listOf("Todas", "Ativas", "Pausadas").forEachIndexed { index, label ->
            filters.addView(RadioButton(this).apply {
                id = index + 1
                text = label
                textSize = 12f
                setPadding(dp(4), 0, dp(4), 0)
            }, RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        filters.check(ruleFilter + 1)
        filters.setOnCheckedChangeListener { _, checkedId ->
            ruleFilter = (checkedId - 1).coerceIn(0, 2)
            renderRuleCards(rules)
        }
        content.addView(filters, spaced())

        ruleListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(ruleListContainer, spaced())
        renderRuleCards(rules)

        content.addView(TextView(this).apply {
            text = "O OpenAtende só responde quando o WhatsApp Business disponibiliza uma ação de resposta na notificação. Mantenha as notificações do WhatsApp Business ativadas."
            textSize = 12f
            setTextColor(Color.rgb(112, 124, 137))
            setPadding(dp(2), dp(12), dp(2), dp(8))
        })
    }

    private fun renderRuleCards(rules: List<ReplyRule>) {
        if (!::ruleListContainer.isInitialized) return
        ruleListContainer.removeAllViews()
        val query = ruleSearchQuery.trim()
        val filtered = rules.withIndex().filter { (_, rule) ->
            val matchesQuery = query.isBlank() ||
                rule.trigger.contains(query, ignoreCase = true) ||
                rule.response.contains(query, ignoreCase = true)
            val matchesStatus = when (ruleFilter) {
                1 -> rule.enabled
                2 -> !rule.enabled
                else -> true
            }
            matchesQuery && matchesStatus
        }
        if (rules.isEmpty()) {
            val empty = card()
            empty.addView(TextView(this).apply {
                text = "Você ainda não cadastrou respostas. Toque em + ADICIONAR para criar a primeira regra."
                textSize = 14f
                setTextColor(Color.rgb(94, 108, 122))
            })
            empty.addView(actionButton("CADASTRAR PRIMEIRA RESPOSTA") { showAddRuleDialog() }, fullWidth())
            ruleListContainer.addView(empty, spaced())
            return
        }
        if (filtered.isEmpty()) {
            ruleListContainer.addView(TextView(this).apply {
                text = "Nenhuma resposta encontrada com essa pesquisa ou filtro."
                textSize = 14f
                setTextColor(Color.rgb(94, 108, 122))
                setPadding(dp(8), dp(14), dp(8), dp(14))
            })
            return
        }
        ruleListContainer.addView(TextView(this).apply {
            text = "${filtered.size} de ${rules.size} respostas"
            textSize = 12f
            setTextColor(Color.rgb(112, 124, 137))
            setPadding(dp(2), 0, 0, dp(8))
        })
        filtered.forEach { entry ->
            val index = entry.index
            val rule = entry.value
            val item = card()
            item.addView(TextView(this).apply {
                text = "QUANDO RECEBER: ${rule.trigger}"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.rgb(25, 61, 88))
            })
            item.addView(TextView(this).apply {
                text = "MODO: " + if (rule.mode == MatchMode.CONTAINS) "CONTÉM A PALAVRA/ FRASE" else "MENSAGEM EXATA"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.rgb(31, 125, 83))
                setPadding(0, dp(6), 0, 0)
            })
            item.addView(TextView(this).apply {
                text = when (rule.action) {
                    RuleAction.REPLY -> "RESPOSTA: ${rule.response}"
                    RuleAction.TEST_MASTERFLIX -> "AÇÃO: GERAR TESTE NO MASTERFLIX (1 HORA)"
                    RuleAction.TEST_STV -> "AÇÃO: GERAR TESTE NO STV+ (1 HORA)"
                }
                textSize = 14f
                setTextColor(Color.rgb(75, 88, 102))
                setPadding(0, dp(8), 0, dp(10))
            })
            item.addView(actionButton("EDITAR RESPOSTA") { showRuleDialog(index) }, fullWidth())
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            actions.addView(actionButton(if (rule.enabled) "ATIVA" else "PAUSADA") {
                ReplyRuleStore.update(this@MainActivity, index, rule.copy(enabled = !rule.enabled))
                renderDashboard()
            }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginEnd = dp(6) })
            actions.addView(actionButton("EXCLUIR") {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Excluir resposta?")
                    .setMessage("A regra para '${rule.trigger}' será removida.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir") { _, _ ->
                        ReplyRuleStore.remove(this@MainActivity, index)
                        renderDashboard()
                    }.show()
            }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginStart = dp(6) })
            item.addView(actions)
            ruleListContainer.addView(item, spaced())
        }
    }

    private fun showAddRuleDialog() = showRuleDialog(null)

    private fun showRuleDialog(editIndex: Int?) {
        val existing = editIndex?.let { ReplyRuleStore.load(this).getOrNull(it) }
        if (editIndex != null && existing == null) return

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val trigger = EditText(this).apply {
            hint = "Ex.: preço, horário ou endereço"
            isSingleLine = true
            setText(existing?.trigger.orEmpty())
        }
        val actionGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val replyAction = RadioButton(this).apply { id = 10; text = "Enviar resposta automática" }
        val masterAction = RadioButton(this).apply { id = 11; text = "Gerar teste no MasterFlix (1 hora)" }
        val stvAction = RadioButton(this).apply { id = 12; text = "Gerar teste no STV+ (1 hora)" }
        actionGroup.addView(replyAction)
        actionGroup.addView(masterAction)
        actionGroup.addView(stvAction)
        actionGroup.check(when (existing?.action ?: RuleAction.REPLY) {
            RuleAction.REPLY -> replyAction.id
            RuleAction.TEST_MASTERFLIX -> masterAction.id
            RuleAction.TEST_STV -> stvAction.id
        })

        val modeGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }
        val containsOption = RadioButton(this).apply {
            id = 1
            text = "Contém a palavra ou frase"
            textSize = 14f
        }
        val exactOption = RadioButton(this).apply {
            id = 2
            text = "Mensagem exatamente igual"
            textSize = 14f
        }
        modeGroup.addView(containsOption)
        modeGroup.addView(exactOption)
        modeGroup.check(if (existing?.mode == MatchMode.EXACT) exactOption.id else containsOption.id)

        val response = EditText(this).apply {
            hint = "Digite a resposta automática"
            minLines = 3
            gravity = Gravity.TOP
            setText(existing?.response.orEmpty())
        }
        form.addView(TextView(this).apply { text = "Palavra ou frase para identificar"; textSize = 13f })
        form.addView(trigger)
        form.addView(TextView(this).apply {
            text = "Como identificar a mensagem?"
            textSize = 13f
            setPadding(0, dp(12), 0, 0)
        })
        form.addView(modeGroup)
        form.addView(TextView(this).apply {
            text = "Resposta que será enviada"
            textSize = 13f
            setPadding(0, dp(12), 0, 0)
        })
        form.addView(response)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Nova resposta automática" else "Editar resposta automática")
            .setView(form)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton(if (existing == null) "Salvar" else "Atualizar", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val keyword = trigger.text.toString().trim()
                val answer = response.text.toString().trim()
                if (keyword.isBlank()) trigger.error = "Informe uma palavra ou frase"
                else {
                    val selectedMode = if (modeGroup.checkedRadioButtonId == exactOption.id) MatchMode.EXACT else MatchMode.CONTAINS
                    val selectedAction = when (actionGroup.checkedRadioButtonId) {
                        masterAction.id -> RuleAction.TEST_MASTERFLIX
                        stvAction.id -> RuleAction.TEST_STV
                        else -> RuleAction.REPLY
                    }
                    if (selectedAction == RuleAction.REPLY && answer.isBlank()) {
                        response.error = "Informe a resposta automática"
                        return@setOnClickListener
                    }
                    val updatedRule = ReplyRule(
                        keyword,
                        answer,
                        selectedMode,
                        existing?.enabled ?: true,
                        selectedAction
                    )
                    if (editIndex == null) {
                        ReplyRuleStore.add(this, updatedRule)
                    } else {
                        ReplyRuleStore.update(this, editIndex, updatedRule)
                    }
                    dialog.dismiss()
                    renderDashboard()
                }
            }
        }
        dialog.show()
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(Color.WHITE, dp(16))
        elevation = dp(2).toFloat()
    }

    private fun statCard(title: String, value: String, subtitle: String) = card().apply {
        addView(sectionLabel(title))
        addView(TextView(this@MainActivity).apply {
            text = value
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(18, 59, 93))
            setPadding(0, dp(5), 0, 0)
        })
        addView(TextView(this@MainActivity).apply {
            text = subtitle
            textSize = 11f
            setTextColor(Color.rgb(105, 119, 132))
        })
    }

    private fun statusLine(label: String, value: String, ok: Boolean) = TextView(this).apply {
        text = "$label     •     $value"
        textSize = 14f
        setTextColor(if (ok) Color.rgb(31, 125, 83) else Color.rgb(180, 105, 35))
        setPadding(0, dp(9), 0, dp(9))
    }

    private fun sectionLabel(value: String) = TextView(this).apply {
        text = value
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.rgb(111, 126, 140))
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 12f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = rounded(Color.rgb(24, 75, 111), dp(10))
        setPadding(dp(8), 0, dp(8), 0)
        setOnClickListener { action() }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
    ).apply { topMargin = dp(10) }

    private fun spaced() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(14) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val PREFS = "openatende_settings"
        const val KEY_ENABLED = "automation_enabled"
        const val KEY_SENT_COUNT = "sent_reply_count"
    }
}
