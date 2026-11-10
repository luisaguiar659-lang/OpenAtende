package br.com.openatende

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var automationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 48, 28, 28)
            setBackgroundColor(0xFFF5F7FA.toInt())
        }
        root.addView(TextView(this).apply {
            text = "OpenAtende"
            textSize = 30f
            setTextColor(0xFF123B5D.toInt())
            gravity = Gravity.CENTER
        }, matchWidth())

        root.addView(TextView(this).apply {
            text = "Seu atendimento automático começa aqui"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 32)
            setTextColor(0xFF526273.toInt())
        }, matchWidth())

        statusText = TextView(this).apply {
            textSize = 17f
            setTextColor(0xFF263746.toInt())
            setPadding(18, 20, 18, 20)
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        root.addView(statusText, matchWidth())

        root.addView(Button(this).apply {
            text = "Autorizar acesso às notificações"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }, matchWidth())

        automationText = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFF263746.toInt())
            setPadding(0, 24, 0, 12)
        }
        root.addView(automationText, matchWidth())

        root.addView(Button(this).apply {
            text = "Ativar / desativar automação"
            setOnClickListener {
                val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
                val enabled = prefs.getBoolean(KEY_ENABLED, false)
                prefs.edit().putBoolean(KEY_ENABLED, !enabled).apply()
                refreshStatus()
            }
        }, matchWidth())

        root.addView(TextView(this).apply {
            text = "Os gatilhos e as respostas serão cadastrados em uma próxima etapa.\n\nO envio só será possível quando o WhatsApp Business disponibilizar uma ação de resposta na notificação."
            textSize = 13f
            setTextColor(0xFF647383.toInt())
            setPadding(0, 28, 0, 0)
        }, matchWidth())

        setContentView(root)
    }

    private fun refreshStatus() {
        val accessGranted = NotificationAccess.isEnabled(this)
        val enabled = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
        statusText.text = if (accessGranted) {
            "Acesso às notificações: AUTORIZADO"
        } else {
            "Acesso às notificações: NÃO AUTORIZADO"
        }
        automationText.text = if (enabled) "Automação: ATIVADA" else "Automação: DESATIVADA"
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    companion object {
        const val PREFS = "openatende_settings"
        const val KEY_ENABLED = "automation_enabled"
    }
}
