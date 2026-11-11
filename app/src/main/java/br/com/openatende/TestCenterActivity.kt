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
    private var embeddedPanel: android.webkit.WebView? = null
    private val panelHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var embeddedLoginSubmitted = false
    private var embeddedTestStarted = false
    private var embeddedCopyStarted = false
    private var embeddedClipboardListener: android.content.ClipboardManager.OnPrimaryClipChangedListener? = null


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

        lateinit var modeGroup: RadioGroup
        if (selectedProvider == PROVIDER_STV) {
            sectionTitle(root, "1. CONFIGURAÇÃO DO TESTE")
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
        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedMode = if (checkedId == withAdults.id) MODE_ADULT else MODE_STANDARD
            prefs.edit().putString(KEY_MODE, selectedMode).apply()
        }
        root.addView(modeGroup, panelParams())
        } else {
            sectionTitle(root, "1. CONFIGURAÇÃO DO TESTE")
            root.addView(bodyText("MasterFlix: teste completo com validade automática de 1 hora."))
        }

        sectionTitle(root, "2. ACESSO AO PAINEL")
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

        val embeddedPanelBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(6), dp(6), dp(6))
            background = panelBackground()
            visibility = android.view.View.GONE
        }
        val embeddedProgress = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        embeddedPanelBox.addView(embeddedProgress, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)))
        val embeddedWebView = android.webkit.WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(260))
            isVerticalScrollBarEnabled = true
            overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS
            setOnTouchListener { view, event ->
                val parent = view.parent
                when (event.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
                    android.view.MotionEvent.ACTION_MOVE -> {
                        val deltaY = event.y - event.getHistoricalY(0)
                        val canWebScroll = if (deltaY > 0) view.canScrollVertically(-1) else view.canScrollVertically(1)
                        parent?.requestDisallowInterceptTouchEvent(canWebScroll)
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onProgressChanged(view: android.webkit.WebView, newProgress: Int) {
                    embeddedProgress.progress = newProgress
                    embeddedProgress.visibility = if (newProgress >= 100) android.view.View.GONE else android.view.View.VISIBLE
                }
            }
            webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView, url: String) {
                    val expectedHost = if (selectedProvider == PROVIDER_STV) "stvmais.sigma.vin" else "masterflix.sigmab.pro"
                    if (android.net.Uri.parse(url).host != expectedHost) return
                    val credentials = runCatching { PanelCredentialStore.load(this@TestCenterActivity, selectedProvider) }.getOrNull() ?: return
                    val user = org.json.JSONObject.quote(credentials.username)
                    val pass = org.json.JSONObject.quote(credentials.password)
                    val js = "(function(){const visible=e=>!!(e.offsetWidth||e.offsetHeight||e.getClientRects().length);const p=Array.from(document.querySelectorAll('input[type=password]')).find(visible);const u=Array.from(document.querySelectorAll('input[type=email],input[autocomplete=username],input[type=text]')).find(visible);if(!p)return 'no-form';const s=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;if(u&&!u.value){s.call(u,$user);u.dispatchEvent(new Event('input',{bubbles:true}));u.dispatchEvent(new Event('change',{bubbles:true}));}if(!p.value){s.call(p,$pass);p.dispatchEvent(new Event('input',{bubbles:true}));p.dispatchEvent(new Event('change',{bubbles:true}));}const b=Array.from(document.querySelectorAll('button,[role=button],input[type=submit]')).find(e=>visible(e)&&!e.disabled&&/^(continuar|continue|entrar|login)$/i.test((e.innerText||e.value||'').trim()));if(b&&!b.dataset.oaClick){b.dataset.oaClick='1';b.click();return 'submitted';}return 'filled';})();"
                    view.evaluateJavascript(js) { result ->
                        if (result == "\"submitted\"") {
                            embeddedLoginSubmitted = true
                            panelHandler.postDelayed({ runEmbeddedTestGeneration(view) }, 3500L)
                        } else if (embeddedLoginSubmitted) {
                            panelHandler.postDelayed({ runEmbeddedTestGeneration(view) }, 3500L)
                        }
                    }
                }
            }
        }
        embeddedPanel = embeddedWebView
        embeddedPanelBox.addView(embeddedWebView)
        registerEmbeddedClipboardCapture()
        root.addView(Button(this).apply {
            text = "ABRIR $providerName NESTA TELA"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener {
                embeddedPanelBox.visibility = if (embeddedPanelBox.visibility == android.view.View.VISIBLE) android.view.View.GONE else android.view.View.VISIBLE
                if (embeddedWebView.url == null) {
                    embeddedWebView.loadUrl(if (selectedProvider == PROVIDER_STV) "https://stvmais.sigma.vin/#/sign-in?redirect=/dashboard" else "https://masterflix.sigmab.pro/#/sign-in?redirect=/dashboard")
                }
            }
        }, buttonParams())
        root.addView(embeddedPanelBox, panelParams())
        if (intent.getBooleanExtra(EXTRA_AUTO_OPEN_PANEL, false)) {
            embeddedPanelBox.visibility = android.view.View.VISIBLE
            embeddedWebView.post {
                if (embeddedWebView.url == null) {
                    embeddedWebView.loadUrl(if (selectedProvider == PROVIDER_STV) "https://stvmais.sigma.vin/#/sign-in?redirect=/dashboard" else "https://masterflix.sigmab.pro/#/sign-in?redirect=/dashboard")
                }
            }
        }

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

    private fun runEmbeddedTestGeneration(view: android.webkit.WebView) {
        if (embeddedTestStarted || isFinishing || isDestroyed) return
        val provider = selectedProvider
        val mode = prefs.getString(KEY_MODE, MODE_STANDARD) ?: MODE_STANDARD
        val label = when {
            provider == PROVIDER_MASTERFLIX -> "MASTERFLIX TESTE COMPLETO 1H"
            mode == MODE_ADULT -> "TESTE + ADULTO"
            else -> "TESTE SEM ADULTO"
        }
        val quoted = org.json.JSONObject.quote(label)
        val script = """
            (function() {
              const norm=s=>(s||'').normalize('NFD').replace(/[\\u0300-\\u036f]/g,'').toUpperCase().replace(/[^A-Z0-9]+/g,'');
              const visible=e=>!!(e.offsetWidth||e.offsetHeight||e.getClientRects().length);
              const target=norm($quoted);
              const aliases=target==='TESTEADULTO'?['TESTEADULTO','TESTEADULTOS']:target==='TESTESEMADULTO'?['TESTESEMADULTO','TESTESEMADULTOS']:[target];
              const found=Array.from(document.querySelectorAll('body *')).filter(visible).map(e=>({e,t:norm(e.innerText||e.textContent||e.value||e.getAttribute('aria-label'))})).filter(x=>x.t&&aliases.some(a=>x.t===a||x.t.startsWith(a))).sort((a,b)=>a.t.length-b.t.length)[0];
              if(!found)return 'not-found';
              found.e.scrollIntoView({block:'center'});
              found.e.click();
              return 'clicked';
            })();
        """.trimIndent()
        view.evaluateJavascript(script) { result ->
            if (result == "\"clicked\"") {
                embeddedTestStarted = true
                panelHandler.postDelayed({ copyEmbeddedTestResult(view) }, 3500L)
                android.widget.Toast.makeText(this, "Geração do teste acionada. Aguardando resultado.", android.widget.Toast.LENGTH_LONG).show()
            } else {
                android.widget.Toast.makeText(this, "Opção '$label' não encontrada no painel.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun copyEmbeddedTestResult(view: android.webkit.WebView) {
        if (embeddedCopyStarted || isFinishing || isDestroyed) return
        val script = """
            (function(){
              window.scrollTo(0,document.documentElement.scrollHeight);
              const visible=e=>!!(e.offsetWidth||e.offsetHeight||e.getClientRects().length);
              const norm=s=>(s||'').replace(/\\s+/g,' ').trim().toUpperCase();
              const b=Array.from(document.querySelectorAll('button,a,[role=button]')).find(e=>visible(e)&&!e.disabled&&norm(e.innerText||e.getAttribute('aria-label'))==='COPIAR');
              if(!b)return 'not-found';b.click();return 'clicked';
            })();
        """.trimIndent()
        view.evaluateJavascript(script) { result ->
            if (result == "\"clicked\"") embeddedCopyStarted = true
            else android.widget.Toast.makeText(this, "Botão COPIAR não encontrado.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun registerEmbeddedClipboardCapture() {
        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val listener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboard.primaryClip ?: return@OnPrimaryClipChangedListener
            val copied = (0 until clip.itemCount).joinToString("\\n") { i -> clip.getItemAt(i).coerceToText(this).toString() }.trim()
            if (copied.isNotBlank() && embeddedCopyStarted) {
                prefs.edit().putString("last_copied_test", copied).putString("last_copied_provider", selectedProvider).putLong("last_copied_at", System.currentTimeMillis()).apply()
                val sent = TestAutomationRouter.complete(this, copied)
                runOnUiThread { android.widget.Toast.makeText(this, if (sent) "Teste enviado ao WhatsApp." else "Teste copiado. Não havia resposta automática disponível nesta notificação.", android.widget.Toast.LENGTH_LONG).show() }
                embeddedCopyStarted = false
            }
        }
        embeddedClipboardListener = listener
        clipboard.addPrimaryClipChangedListener(listener)
    }

    override fun onDestroy() {
        panelHandler.removeCallbacksAndMessages(null)
        embeddedClipboardListener?.let { listener ->
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.removePrimaryClipChangedListener(listener)
        }
        embeddedPanel?.apply { stopLoading(); destroy() }
        embeddedPanel = null
        super.onDestroy()
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
        const val EXTRA_AUTO_OPEN_PANEL = "auto_open_embedded_panel"
        private const val PROVIDER_MASTERFLIX = "masterflix"
        private const val PROVIDER_STV = "stv"
        private const val MODE_STANDARD = "standard"
        private const val MODE_ADULT = "adult"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_MODE = "mode"
    }
}
