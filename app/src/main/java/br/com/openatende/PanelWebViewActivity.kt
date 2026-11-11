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
                fillSavedCredentials(view, provider)
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
              return 'credentials-filled';
            })();
        """.trimIndent()
        view.evaluateJavascript(script) { result ->
            if (result == "\"credentials-filled\"") {
                Toast.makeText(this, "Credenciais preenchidas. Confira e toque em Continuar; conclua qualquer verificação de segurança.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
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
