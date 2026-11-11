package br.com.openatende

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class TestCenterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply {
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
            text = "Gerencie a geração de demonstrações MasterFlix e STV+."
            textSize = 14f
            setTextColor(Color.rgb(93, 108, 124))
            setPadding(0, dp(5), 0, dp(18))
        })

        providerCard(
            root,
            "MASTERFLIX",
            "Painel de testes MasterFlix",
            "https://masterflix.sigmab.pro/#/sign-in?redirect=/dashboard"
        )
        providerCard(
            root,
            "STV+",
            "Painel de testes STV+",
            "https://stvmais.sigma.vin/#/sign-in?redirect=/dashboard"
        )

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = panelBackground()
        }
        info.addView(TextView(this).apply {
            text = "INTEGRAÇÃO AUTOMÁTICA"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(111, 126, 140))
        })
        info.addView(TextView(this).apply {
            text = "Próxima etapa: conectar uma API oficial/autorizada de cada painel para solicitar testes, receber as credenciais e registrar a validade."
            textSize = 14f
            setTextColor(Color.rgb(54, 68, 82))
            setPadding(0, dp(8), 0, dp(8))
        })
        info.addView(TextView(this).apply {
            text = "A geração automática ainda não está ativa. Até a integração ser configurada, use os botões acima para abrir os painéis. Não armazene senhas ou tokens do painel neste aplicativo."
            textSize = 12f
            setTextColor(Color.rgb(112, 124, 137))
        })
        root.addView(info, spaced())

        val back = Button(this).apply {
            text = "VOLTAR AO PAINEL"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener { finish() }
        }
        root.addView(back, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
        ).apply { topMargin = dp(8) })
    }

    private fun providerCard(root: LinearLayout, title: String, description: String, url: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = panelBackground()
            elevation = dp(2).toFloat()
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 61, 88))
        })
        card.addView(TextView(this).apply {
            text = description
            textSize = 13f
            setTextColor(Color.rgb(93, 108, 124))
            setPadding(0, dp(5), 0, dp(8))
        })
        card.addView(TextView(this).apply {
            text = "Integração automática: aguardando acesso à API"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(170, 111, 32))
        })
        card.addView(Button(this).apply {
            text = "ABRIR PAINEL $title"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = panelButton()
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
        ).apply { topMargin = dp(10) })
        root.addView(card, spaced())
    }

    private fun panelBackground() = android.graphics.drawable.GradientDrawable().apply {
        setColor(Color.WHITE)
        cornerRadius = dp(16).toFloat()
    }

    private fun panelButton() = android.graphics.drawable.GradientDrawable().apply {
        setColor(Color.rgb(24, 75, 111))
        cornerRadius = dp(10).toFloat()
    }

    private fun spaced() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(14) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
