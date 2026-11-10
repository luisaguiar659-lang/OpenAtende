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
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var content: LinearLayout

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

        if (rules.isEmpty()) {
            val empty = card()
            empty.addView(TextView(this).apply {
                text = "Você ainda não cadastrou respostas.\nAdicione uma palavra ou frase que o OpenAtende deverá identificar e defina a resposta."
                textSize = 14f
                setTextColor(Color.rgb(94, 108, 122))
                setPadding(0, 0, 0, dp(12))
            })
            empty.addView(actionButton("CADASTRAR PRIMEIRA RESPOSTA") { showAddRuleDialog() }, fullWidth())
            content.addView(empty, spaced())
        } else {
            rules.forEachIndexed { index, rule ->
                val item = card()
                item.addView(TextView(this).apply {
                    text = "QUANDO RECEBER: ${rule.trigger}"
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.rgb(25, 61, 88))
                })
                item.addView(TextView(this).apply {
                    text = "RESPONDER: ${rule.response}"
                    textSize = 14f
                    setTextColor(Color.rgb(75, 88, 102))
                    setPadding(0, dp(8), 0, dp(10))
                })
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
                content.addView(item, spaced())
            }
        }

        content.addView(TextView(this).apply {
            text = "O OpenAtende só responde quando o WhatsApp Business disponibiliza uma ação de resposta na notificação. Mantenha as notificações do WhatsApp Business ativadas."
            textSize = 12f
            setTextColor(Color.rgb(112, 124, 137))
            setPadding(dp(2), dp(12), dp(2), dp(8))
        })
    }

    private fun showAddRuleDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val trigger = EditText(this).apply {
            hint = "Ex.: preço, horário ou endereço"
            singleLine = true
        }
        val response = EditText(this).apply {
            hint = "Digite a resposta automática"
            minLines = 3
            gravity = Gravity.TOP
        }
        form.addView(TextView(this).apply { text = "Palavra ou frase para identificar"; textSize = 13f })
        form.addView(trigger)
        form.addView(TextView(this).apply {
            text = "Resposta que será enviada"
            textSize = 13f
            setPadding(0, dp(12), 0, 0)
        })
        form.addView(response)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Nova resposta automática")
            .setView(form)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val keyword = trigger.text.toString().trim()
                val answer = response.text.toString().trim()
                if (keyword.isBlank()) trigger.error = "Informe uma palavra ou frase"
                else if (answer.isBlank()) response.error = "Informe a resposta"
                else {
                    ReplyRuleStore.add(this, ReplyRule(keyword, answer, MatchMode.CONTAINS))
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
