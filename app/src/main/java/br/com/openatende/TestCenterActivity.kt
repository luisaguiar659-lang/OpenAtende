package br.com.openatende

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class TestCenterActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("test_automation", MODE_PRIVATE) }
    private var selectedProvider = PROVIDER_MASTERFLIX
    private var selectedMode = MODE_STANDARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedProvider = intent.getStringExtra(EXTRA_PROVIDER)
            ?: prefs.getString(KEY_PROVIDER, PROVIDER_MASTERFLIX) ?: PROVIDER_MASTERFLIX
        prefs.edit().putString(KEY_PROVIDER, selectedProvider).apply()
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

        val providerName = if (selectedProvider == PROVIDER_STV) "STV+" else "MasterFlix"
        root.addView(TextView(this).apply {
            text = "AUTOMAÇÃO"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(31, 125, 83))
        })
        root.addView(TextView(this).apply {
            text = providerName
            textSize = 29f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(18, 59, 93))
            setPadding(0, dp(4), 0, 0)
        })
        root.addView(TextView(this).apply {
            text = "Central de configuração do módulo de testes."
            textSize = 14f
            setTextColor(Color.rgb(93, 108, 124))
            setPadding(0, dp(5), 0, dp(18))
        })

        val providerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = panelBackground()
        }
        providerCard.addView(TextView(this).apply {
            text = "MÓDULO SELECIONADO"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(111, 126, 140))
        })
        providerCard.addView(TextView(this).apply {
            text = providerName
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 75, 111))
            setPadding(0, dp(5), 0, 0)
        })
        root.addView(providerCard, panelParams())

        sectionTitle(root, "1. SERVIDOR")
        lateinit var modeGroup: RadioGroup
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

        sectionTitle(root, "2. CONFIGURAÇÃO DO TESTE")
        root.addView(bodyText("Validade definida automaticamente: 1 hora. Não é necessário escolher a duração."))

        modeGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
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

        sectionTitle(root, "3. ACESSO AO PAINEL")
        root.addView(bodyText("Abra o painel dentro do próprio OpenAtende para entrar na sua conta. Se houver CAPTCHA ou verificação de segurança, conclua manualmente na tela interna."))
        val savedCredentials = PanelCredentialStore.load(this, selectedProvider)
        val usernameField = EditText(this).apply {
            hint = "Usuário do painel"
            isSingleLine = true
            setText(savedCredentials?.username.orEmpty())
            textSize = 14f
        }
        val passwordField = EditText(this).apply {
            hint = "Senha do painel"
            isSingleLine = true
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(savedCredentials?.password.orEmpty())
            textSize = 14f
        }
        root.addView(TextView(this).apply {
            text = "Credenciais deste painel"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(35, 55, 75))
            setPadding(0, dp(8), 0, dp(4))
        })
        root.addView(usernameField, panelParams())
        root.addView(passwordField, panelParams())
        root.addView(Button(this).apply {
            text = "SALVAR ACESSO COM CRIPTOGRAFIA"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener {
                val username = usernameField.text.toString().trim()
                val password = passwordField.text.toString()
                if (username.isBlank()) {
                    usernameField.error = "Informe o usuário"
                } else if (password.isBlank()) {
                    passwordField.error = "Informe a senha"
                } else {
                    try {
                        PanelCredentialStore.save(this@TestCenterActivity, selectedProvider, username, password)
                        android.widget.Toast.makeText(this@TestCenterActivity, "Acesso salvo com proteção do Android.", android.widget.Toast.LENGTH_LONG).show()
                    } catch (_: Exception) {
                        android.widget.Toast.makeText(this@TestCenterActivity, "Não foi possível proteger e salvar o acesso neste aparelho.", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }, buttonParams())

        root.addView(Button(this).apply {
            text = "APAGAR ACESSO SALVO"
            isAllCaps = false
            setTextColor(Color.rgb(150, 55, 55))
            background = panelBackground()
            setOnClickListener {
                PanelCredentialStore.clear(this@TestCenterActivity, selectedProvider)
                usernameField.setText("")
                passwordField.setText("")
                android.widget.Toast.makeText(this@TestCenterActivity, "Acesso removido deste aparelho.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }, buttonParams())

        root.addView(bodyText("A senha é criptografada com uma chave mantida pelo Android Keystore e separada por painel. Ela não é enviada ao OpenAtende nem compartilhada entre MasterFlix e STV+."))

        root.addView(Button(this).apply {
            text = "ABRIR $providerName E FAZER LOGIN"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener {
                startActivity(android.content.Intent(this@TestCenterActivity, PanelWebViewActivity::class.java)
                    .putExtra(PanelWebViewActivity.EXTRA_PROVIDER, selectedProvider))
            }
        }, buttonParams())

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = panelBackground()
        }
        status.addView(TextView(this).apply {
            text = "STATUS DE $providerName"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(111, 126, 140))
        })
        status.addView(TextView(this).apply {
            text = "Preferências deste módulo salvas neste aparelho. Login reconhecido: ainda não conectado. Geração automática e envio das credenciais: em desenvolvimento."
            textSize = 13f
            setTextColor(Color.rgb(54, 68, 82))
            setPadding(0, dp(7), 0, 0)
        })
        root.addView(status, panelParams())

        root.addView(Button(this).apply {
            text = "VOLTAR AO OPENATENDE"
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
        const val EXTRA_PROVIDER = "selected_provider"
        private const val PROVIDER_MASTERFLIX = "masterflix"
        private const val PROVIDER_STV = "stv"
        private const val MODE_STANDARD = "standard"
        private const val MODE_ADULT = "adult"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_MODE = "mode"
    }
}
