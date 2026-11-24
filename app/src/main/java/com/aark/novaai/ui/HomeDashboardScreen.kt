package com.aark.novaai.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.Toast
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.ui.components.NovaUi

class HomeDashboardScreen(
    private val activity: Activity,
    private val onOpenApiSettings: () -> Unit,
    private val onOpenChat: () -> Unit
) {
    fun create(): LinearLayout {
        val root = NovaUi.pageRoot(activity)
        val top = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            addView(NovaUi.label(activity, "NOVA AI", 27f, NovaPalette.cyan, true))
            addView(NovaUi.label(activity, "YOUR PERSONAL AI SPACE", 10f, NovaPalette.muted, true))
        }
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(NovaUi.centerLabel(activity, "●  READY", 11f, Color.rgb(112, 238, 190), true).apply {
            background = NovaUi.rounded(Color.rgb(20, 55, 55), 30f)
            val h = NovaUi.dp(activity, 9)
            val w = NovaUi.dp(activity, 12)
            setPadding(w, h, w, h)
        })
        root.addView(top)

        val hero = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            val h = NovaUi.dp(activity, 24)
            val w = NovaUi.dp(activity, 18)
            setPadding(w, h, w, h)
            background = NovaUi.gradient(Color.rgb(24, 48, 87), Color.rgb(37, 28, 78), 30f)
            elevation = NovaUi.dp(activity, 7).toFloat()
        }
        val orb = NovaUi.centerLabel(activity, "✦", 66f, NovaPalette.cyan, true).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 88, 132), Color.rgb(76, 58, 132))).apply {
                shape = GradientDrawable.OVAL
                setStroke(NovaUi.dp(activity, 2), Color.rgb(93, 189, 255))
            }
            elevation = NovaUi.dp(activity, 12).toFloat()
        }
        hero.addView(orb, LinearLayout.LayoutParams(NovaUi.dp(activity, 124), NovaUi.dp(activity, 124)))
        hero.addView(NovaUi.centerLabel(activity, "A smarter space, built around you", 18f, Color.WHITE, true).apply {
            setPadding(0, NovaUi.dp(activity, 18), 0, NovaUi.dp(activity, 5))
        })
        hero.addView(NovaUi.centerLabel(activity, "Your AI workspace is ready to explore", 12f, NovaPalette.muted))
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = NovaUi.dp(activity, 26) })

        root.addView(NovaUi.label(activity, "QUICK SETUP", 12f, NovaPalette.muted, true).apply {
            setPadding(NovaUi.dp(activity, 3), NovaUi.dp(activity, 25), 0, NovaUi.dp(activity, 12))
        })
        root.addView(NovaUi.actionTile(activity, "API key settings", "Connect your Gemini key securely", "⌘", NovaPalette.cyan, onOpenApiSettings),
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = NovaUi.dp(activity, 12) })
        root.addView(NovaUi.actionTile(activity, "Gemini chat", "Ask questions and get AI responses", "✧", NovaPalette.purple, onOpenChat),
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = NovaUi.dp(activity, 12) })
        root.addView(NovaUi.actionTile(activity, "Voice assistant", "Voice features are planned for a later stage", "♫", NovaPalette.purple) {
            Toast.makeText(activity, "Voice assistant is not implemented yet.", Toast.LENGTH_LONG).show()
        })

        root.addView(NovaUi.label(activity, "YOUR NOVA WORKSPACE", 12f, NovaPalette.muted, true).apply {
            setPadding(NovaUi.dp(activity, 3), NovaUi.dp(activity, 25), 0, NovaUi.dp(activity, 12))
        })
        val grid = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        grid.addView(miniCard("◉", "Screen vision", "Coming later", NovaPalette.cyan),
            LinearLayout.LayoutParams(0, NovaUi.dp(activity, 130), 1f).apply { marginEnd = NovaUi.dp(activity, 8) })
        grid.addView(miniCard("◇", "Privacy", "You stay in control", NovaPalette.purple),
            LinearLayout.LayoutParams(0, NovaUi.dp(activity, 130), 1f).apply { marginStart = NovaUi.dp(activity, 8) })
        root.addView(grid)
        root.addView(NovaUi.centerLabel(activity, "NOVA AI  •  GEMINI CHAT PREVIEW", 10f, Color.rgb(105, 124, 158)).apply {
            setPadding(0, NovaUi.dp(activity, 26), 0, NovaUi.dp(activity, 4))
        })
        return root
    }

    fun show() = NovaUi.installScrollable(activity, create())

    private fun miniCard(icon: String, title: String, subtitle: String, accent: Int): LinearLayout =
        NovaUi.card(activity, 14).apply {
            addView(NovaUi.centerLabel(activity, icon, 25f, accent, true).apply { gravity = android.view.Gravity.START })
            addView(NovaUi.label(activity, title, 14f, Color.WHITE, true).apply {
                setPadding(0, NovaUi.dp(activity, 9), 0, NovaUi.dp(activity, 3))
            })
            addView(NovaUi.label(activity, subtitle, 11f, NovaPalette.muted))
        }
}
