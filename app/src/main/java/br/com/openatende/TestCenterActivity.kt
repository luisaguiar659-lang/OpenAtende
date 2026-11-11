package br.com.openatende

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView

class TestCenterActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("test_automation", MODE_PRIVATE) }
    private var selectedProvider = PROVIDER_MASTERFLIX
    private var selectedMode = MODE_STANDARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedProvider = prefs.getString(KEY_PROVIDER, PROVIDER_MASTERFLIX) ?: PROVIDER_MASTERFLIX
        selectedMode = prefs.getString(KEY_MODE, MODE_STANDARD) ?: MODE_STANDARD

        val scroll = android.widget.ScrollView(this).apply {
            setBackgroundColor(Color.rgb(244, 247, 251))
            isFillViewport = true
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "Central de Testes"
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(18, 59, 93))
        })
        root.addView(TextView(this).apply {
            text = "Configure a rotina que será usada quando o cliente pedir uma demonstração."
            textSize = 14f
            setTextColor(Color.rgb(93, 108, 124))
            setPadding(0, dp(5), 0, dp(18))
        })

        sectionTitle(root, "1. ESCOLHA O SERVIDOR")
        val providers = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val master = RadioButton(this).apply {
            id = android.view.View.generateViewId()
            text = "MasterFlix"
            textSize = 16f
            setTextColor(Color.rgb(35, 55, 75))
            isChecked = selectedProvider == PROVIDER_MASTERFLIX
        }
        val stv = RadioButton(this).apply {
            id = android.view.View.generateViewId()
            text = "STV+"
            textSize = 16f
            setTextColor(Color.rgb(35, 55, 75))
            isChecked = selectedProvider == PROVIDER_STV
        }
        providers.addView(master)
        providers.addView(stv)
        providers.setOnCheckedChangeListener { _, checkedId ->
            selectedProvider = if (checkedId == stv.id) PROVIDER_STV else PROVIDER_MASTERFLIX
            prefs.edit().putString(KEY_PROVIDER, selectedProvider).apply()
            modeGroup.visibility = if (selectedProvider == PROVIDER_STV) android.view.View.VISIBLE else android.view.View.GONE
        }
        root.addView(providers, panelParams())

        sectionTitle(root, "2. OPÇÕES DO TESTE")
        val durationLabel = bodyText("Duração desejada")
        root.addView(durationLabel)
        val durations = Spinner(this).apply {
            adapter = ArrayAdapter(this@TestCenterActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("1 dia", "2 dias", "3 dias", "7 dias"))
            val saved = prefs.getString(KEY_DURATION, "1 dia") ?: "1 dia"
            setSelection((0 until adapter.count).firstOrNull { adapter.getItem(it) == saved } ?: 0)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    prefs.edit().putString(KEY_DURATION, getItemAtPosition(position).toString()).apply()
                }
            }
        }
        root.addView(durations, panelParams())

        val modeGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val withAdults = RadioButton(this).apply {
            id = android.view.View.generateViewId()
            text = "Com conteúdo adulto"
            setTextColor(Color.rgb(35, 55, 75))
            isChecked = selectedMode == MODE_ADULT
        }
        val withoutAdults = RadioButton(this).apply {
            id = android.view.View.generateViewId()
            text = "Sem conteúdo adulto"
            setTextColor(Color.rgb(35, 55, 75))
            isChecked = selectedMode == MODE_STANDARD
        }
        modeGroup.addView(withAdults)
        modeGroup.addView(withoutAdults)
        modeGroup.visibility = if (selectedProvider == PROVIDER_STV) android.view.View.VISIBLE else android.view.View.GONE
        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedMode = if (checkedId == withAdults.id) MODE_ADULT else MODE_STANDARD
            prefs.edit().putString(KEY_MODE, selectedMode).apply()
        }
        root.addView(modeGroup, panelParams())

        sectionTitle(root, "3. PREPARAR SESSÃO")
        root.addView(bodyText("Faça o login no painel escolhido. O OpenAtende não salva sua senha: a sessão fica sob seu controle. Se aparecer uma verificação de segurança, conclua manualmente no próprio painel."))
        root.addView(Button(this).apply {
            text = "ABRIR PAINEL E FAZER LOGIN"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener {
                val url = if (selectedProvider == PROVIDER_STV) STV_URL else MASTERFLIX_URL
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }, buttonParams())

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = panelBackground()
        }
        status.addView(TextView(this).apply {
            text = "STATUS DA AUTOMAÇÃO"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(111, 126, 140))
        })
        status.addView(TextView(this).apply {
            text = "Configuração salva neste aparelho. A execução automática da geração e o envio das credenciais ainda estão em desenvolvimento."
            textSize = 13f
            setTextColor(Color.rgb(54, 68, 82))
            setPadding(0, dp(7), 0, 0)
        })
        root.addView(status, panelParams())

        root.addView(Button(this).apply {
            text = "VOLTAR AO PAINEL"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener { finish() }
        }, buttonParams())
    }

    private fun sectionTitle(root: LinearLayout, value: String) {
        root.addView(TextView(this).apply {
            text = value
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(75, 98, 119))
            setPadding(0, dp(14), 0, dp(8))
        })
    }

    private fun bodyText(value: String) = TextView(this).apply {
        text = value
        textSize = 13f
        setTextColor(Color.rgb(93, 108, 124))
        setPadding(0, dp(4), 0, dp(8))
    }

    private fun panelBackground() = android.graphics.drawable.GradientDrawable().apply {
        setColor(Color.WHITE)
        cornerRadius = dp(14).toFloat()
    }

    private fun panelButton() = android.graphics.drawable.GradientDrawable().apply {
        setColor(Color.rgb(24, 75, 111))
        cornerRadius = dp(10).toFloat()
    }

    private fun panelParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(10) }

    private fun buttonParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
    ).apply { topMargin = dp(10); bottomMargin = dp(12) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val PROVIDER_MASTERFLIX = "masterflix"
        private const val PROVIDER_STV = "stv"
        private const val MODE_STANDARD = "standard"
        private const val MODE_ADULT = "adult"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_MODE = "mode"
        private const val KEY_DURATION = "duration"
        private const val MASTERFLIX_URL = "https://masterflix.sigmab.pro/#/sign-in?redirect=/dashboard"
        private const val STV_URL = "https://stvmais.sigma.vin/#/sign-in?redirect=/dashboard"
    }
}
