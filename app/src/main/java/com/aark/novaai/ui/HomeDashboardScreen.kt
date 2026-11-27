package com.aark.novaai.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.ui.components.NovaUi

/** Compact NOVA home screen: chat is primary, settings live behind the gear. */
class HomeDashboardScreen(
    private val activity: Activity,
    private val onOpenApiSettings: () -> Unit,
    private val onOpenChat: () -> Unit
) {
    fun create(): LinearLayout {
        val root = NovaUi.pageRoot(activity).apply {
            setPadding(NovaUi.dp(activity, 20), NovaUi.dp(activity, 14), NovaUi.dp(activity, 20), NovaUi.dp(activity, 24))
        }
        val top = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(NovaUi.label(activity, "NOVA", 28f, NovaPalette.cyan, true))
        brand.addView(NovaUi.label(activity, "YOUR AI SPACE", 10f, NovaPalette.muted, true))
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val settings = NovaUi.centerLabel(activity, "⚙", 24f, Color.WHITE, true).apply {
            background = NovaUi.rounded(NovaPalette.surface2, 28f)
            setPadding(NovaUi.dp(activity, 14), NovaUi.dp(activity, 10), NovaUi.dp(activity, 14), NovaUi.dp(activity, 10))
            contentDescription = "Settings"
            isClickable = true
            isFocusable = true
            setOnClickListener { onOpenApiSettings() }
        }
        top.addView(settings)
        root.addView(top)

        val hero = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(NovaUi.dp(activity, 18), NovaUi.dp(activity, 38), NovaUi.dp(activity, 18), NovaUi.dp(activity, 38))
            background = NovaUi.gradient(Color.rgb(20, 48, 83), Color.rgb(37, 31, 78), 30f)
            elevation = NovaUi.dp(activity, 3).toFloat()
        }
        val orb = NovaUi.centerLabel(activity, "✦", 58f, NovaPalette.cyan, true).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(35, 88, 132), Color.rgb(76, 58, 132))).apply {
                shape = GradientDrawable.OVAL
                setStroke(NovaUi.dp(activity, 2), Color.rgb(93, 189, 255))
            }
        }
        hero.addView(orb, LinearLayout.LayoutParams(NovaUi.dp(activity, 112), NovaUi.dp(activity, 112)))
        hero.addView(NovaUi.centerLabel(activity, "How can I help you?", 21f, Color.WHITE, true).apply {
            setPadding(0, NovaUi.dp(activity, 20), 0, NovaUi.dp(activity, 6))
        })
        hero.addView(NovaUi.centerLabel(activity, "Ask, learn and explore with NOVA.", 13f, NovaPalette.muted))
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = NovaUi.dp(activity, 42) })

        root.addView(NovaUi.label(activity, "CONTINUE WITH NOVA", 11f, NovaPalette.muted, true).apply {
            setPadding(NovaUi.dp(activity, 3), NovaUi.dp(activity, 32), 0, NovaUi.dp(activity, 12))
        })
        root.addView(NovaUi.actionTile(activity, "New conversation", "Start chatting with NOVA", "✦", NovaPalette.cyan, onOpenChat),
            LinearLayout.LayoutParams(-1, -2))

        val note = NovaUi.card(activity, 16).apply {
            addView(NovaUi.label(activity, "Your space, your control", 14f, Color.WHITE, true))
            addView(NovaUi.label(activity, "Manage your Gemini API key and app preferences from Settings.", 12f, NovaPalette.muted).apply {
                setPadding(0, NovaUi.dp(activity, 6), 0, 0)
            })
        }
        root.addView(note, LinearLayout.LayoutParams(-1, -2).apply { topMargin = NovaUi.dp(activity, 18) })
        root.addView(NovaUi.centerLabel(activity, "NOVA AI", 10f, Color.rgb(105, 124, 158)).apply {
            setPadding(0, NovaUi.dp(activity, 30), 0, NovaUi.dp(activity, 4))
        })
        return root
    }

    fun show() = NovaUi.installScrollable(activity, create())
}
