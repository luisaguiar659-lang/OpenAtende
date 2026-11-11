package br.com.openatende

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.net.http.SslError
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class PanelWebViewActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private val automationHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var loginSubmitted = false
    private var generationStarted = false
    private var copyStarted = false
    private var clipboardListener: android.content.ClipboardManager.OnPrimaryClipChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val provider = intent.getStringExtra(EXTRA_PROVIDER) ?: "masterflix"
        val target = when (provider) {
            "masterflix" -> MASTERFLIX_URL
            "stv" -> STV_URL
            else -> {
                finish()
                return
            }
        }
        val allowedHost = if (provider == "stv") "stvmais.sigma.vin" else "masterflix.sigmab.pro"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(244, 247, 251))
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setBackgroundColor(Color.WHITE)
        }
        toolbar.addView(Button(this).apply {
            text = "‹ Voltar"
            isAllCaps = false
            setTextColor(Color.rgb(24, 75, 111))
            setOnClickListener { finish() }
        })
        toolbar.addView(TextView(this).apply {
            text = if (provider == "stv") "STV+ • Painel interno" else "MasterFlix • Painel interno"
            textSize = 16f
            setTextColor(Color.rgb(18, 59, 93))
            setPadding(dp(8), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(toolbar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = ProgressBar.GONE
        }
        root.addView(progress, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(3)
        ))

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        webView.settings.setSupportMultipleWindows(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.settings.safeBrowsingEnabled = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return uri.scheme != "https" || uri.host != allowedHost
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (android.net.Uri.parse(url).host != allowedHost) return
                if (loginSubmitted) scheduleTestGeneration(view, provider) else fillSavedCredentials(view, provider)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.cancel()
                Toast.makeText(this@PanelWebViewActivity, "Conexão segura não validada. A navegação foi bloqueada.", Toast.LENGTH_LONG).show()
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress >= 100) ProgressBar.GONE else ProgressBar.VISIBLE
            }
        }
        root.addView(webView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
        registerClipboardCapture()
        webView.loadUrl(target)
    }

    private fun fillSavedCredentials(view: WebView, provider: String) {
        val credentials = try {
            PanelCredentialStore.load(this, provider)
        } catch (_: Exception) {
            null
        } ?: return

        val username = org.json.JSONObject.quote(credentials.username)
        val password = org.json.JSONObject.quote(credentials.password)
        val script = """
            (function() {
              const visible = (el) => !!(el.offsetWidth || el.offsetHeight || el.getClientRects().length);
              const passwordField = Array.from(document.querySelectorAll('input[type="password"]')).find(visible);
              if (!passwordField) return 'no-login-form';
              const usernameField = Array.from(document.querySelectorAll(
                'input[type="email"], input[autocomplete="username"], input[type="text"]'
              )).find(visible);
              const setValue = (el, value) => {
                if (!el) return;
                const setter = Object.getOwnPropertyDescriptor(
                  window.HTMLInputElement.prototype, 'value'
                ).set;
                setter.call(el, value);
                el.dispatchEvent(new Event('input', { bubbles: true }));
                el.dispatchEvent(new Event('change', { bubbles: true }));
              };
              if (usernameField && !usernameField.value) setValue(usernameField, $username);
              if (!passwordField.value) setValue(passwordField, $password);

              // Never attempt to solve or bypass CAPTCHA / anti-bot challenges.
              const pageText = (document.body?.innerText || '').toLowerCase();
              const challengePresent =
                !!document.querySelector('iframe[src*="captcha"], iframe[src*="recaptcha"], iframe[src*="hcaptcha"], [class*="captcha"], [id*="captcha"], [class*="challenge"], [id*="challenge"]') ||
                /captcha|recaptcha|hcaptcha|cloudflare/.test(pageText);
              if (challengePresent) return 'security-check-required';

              const continueButton = Array.from(document.querySelectorAll(
                'button, input[type="submit"], [role="button"]'
              )).find((el) => {
                if (!visible(el) || el.disabled) return false;
                const label = (el.innerText || el.value || el.getAttribute('aria-label') || '').trim().toLowerCase();
                return label === 'continuar' || label === 'continue' || label === 'entrar' || label === 'login';
              });
              if (!continueButton) return 'credentials-filled';
              if (continueButton.dataset.openAtendeAutoClicked === '1') return 'already-clicked';
              continueButton.dataset.openAtendeAutoClicked = '1';
              continueButton.click();
              return 'continue-clicked';
            })();
        """.trimIndent()
        view.evaluateJavascript(script) { result ->
            when (result) {
                "\"continue-clicked\"" -> {
                    loginSubmitted = true
                    Toast.makeText(this, "Login enviado. Aguardando 3,5 segundos.", Toast.LENGTH_LONG).show()
                    scheduleTestGeneration(view, provider)
                }
                "\"security-check-required\"" ->
                    Toast.makeText(this, "Credenciais preenchidas. O painel solicitou verificação de segurança; conclua manualmente.", Toast.LENGTH_LONG).show()
                "\"credentials-filled\"" ->
                    Toast.makeText(this, "Credenciais preenchidas. Não foi possível identificar o botão Continuar nesta tela.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scheduleTestGeneration(view: WebView, provider: String) {
        if (generationStarted || isFinishing || isDestroyed) return
        automationHandler.postDelayed({
            if (!isFinishing && !isDestroyed && !generationStarted) clickGenerationButton(view, provider)
        }, 3500L)
    }

    private fun clickGenerationButton(view: WebView, provider: String) {
        val mode = getSharedPreferences("test_automation", MODE_PRIVATE).getString("mode", "standard") ?: "standard"
        val label = when {
            provider == "masterflix" -> "MASTERFLIX TESTE COMPLETO 1H"
            mode == "adult" -> "TESTE + ADULTO"
            else -> "TESTE SEM ADULTO"
        }
        val quotedLabel = org.json.JSONObject.quote(label)
        val script = """
            (function() {
              const norm = s => (s || '').replace(/\\s+/g, ' ').trim().toUpperCase();
              const visible = e => !!(e.offsetWidth || e.offsetHeight || e.getClientRects().length);
              const target = $quotedLabel;
              const items = Array.from(document.querySelectorAll('button,a,[role=button],input[type=button],input[type=submit]'));
              const button = items.find(e => visible(e) && !e.disabled && norm(e.innerText || e.value || e.getAttribute('aria-label')) === target);
              if (!button) return 'not-found';
              button.click();
              return 'clicked';
            })();
        """.trimIndent()
        view.evaluateJavascript(script) { result ->
            if (result == "\"clicked\"") {
                generationStarted = true
                automationHandler.postDelayed({ scrollAndClickCopy(view) }, 3500L)
                Toast.makeText(this, "Geração acionada. Aguardando o resultado.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Botão '$label' não encontrado. A automação foi pausada.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scrollAndClickCopy(view: WebView) {
        if (copyStarted || isFinishing || isDestroyed) return
        val script = """
            (function() {
              window.scrollTo(0, document.documentElement.scrollHeight);
              const norm = s => (s || '').replace(/\\s+/g, ' ').trim().toUpperCase();
              const visible = e => !!(e.offsetWidth || e.offsetHeight || e.getClientRects().length);
              const items = Array.from(document.querySelectorAll('button,a,[role=button]'));
              const button = items.find(e => visible(e) && !e.disabled && norm(e.innerText || e.getAttribute('aria-label')) === 'COPIAR');
              if (!button) return 'not-found';
              button.click();
              return 'clicked';
            })();
        """.trimIndent()
        view.evaluateJavascript(script) { result ->
            if (result == "\"clicked\"") {
                copyStarted = true
                Toast.makeText(this, "COPIAR acionado. Capturando a área de transferência.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Botão COPIAR não encontrado. Nenhuma mensagem foi enviada.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerClipboardCapture() {
        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val listener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboard.primaryClip ?: return@OnPrimaryClipChangedListener
            val copiedText = (0 until clip.itemCount).joinToString("\\n") { index ->
                clip.getItemAt(index).coerceToText(this).toString()
            }.trim()
            if (copiedText.isNotBlank()) {
                getSharedPreferences("test_automation", MODE_PRIVATE).edit()
                    .putString("last_copied_test", copiedText)
                    .putString("last_copied_provider", intent.getStringExtra(EXTRA_PROVIDER))
                    .putLong("last_copied_at", System.currentTimeMillis())
                    .apply()
                val sent = TestAutomationRouter.complete(this, copiedText)
                runOnUiThread {
                    Toast.makeText(
                        this,
                        if (sent) "Teste capturado e enviado ao cliente no WhatsApp."
                        else "Teste capturado, mas não foi possível responder automaticamente nesta notificação.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        clipboardListener = listener
        clipboard.addPrimaryClipChangedListener(listener)
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        automationHandler.removeCallbacksAndMessages(null)
        clipboardListener?.let { listener ->
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.removePrimaryClipChangedListener(listener)
        }
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_PROVIDER = "panel_provider"
        private const val MASTERFLIX_URL = "https://masterflix.sigmab.pro/#/sign-in?redirect=/dashboard"
        private const val STV_URL = "https://stvmais.sigma.vin/#/sign-in?redirect=/dashboard"
    }
}
