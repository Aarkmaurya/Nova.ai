package com.aark.novaai.ui.components

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.aark.novaai.core.NovaPalette

/** Shared Android View styling helpers used by NOVA screens. */
object NovaUi {
    fun dp(activity: Activity, value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()

    fun rounded(color: Int, radius: Float = 26f, stroke: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != Color.TRANSPARENT) setStroke(1, stroke)
        }

    fun gradient(start: Int, end: Int, radius: Float = 30f): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)).apply {
            cornerRadius = radius
        }

    fun label(activity: Activity, value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = true
        }

    fun centerLabel(activity: Activity, value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        label(activity, value, size, color, bold).apply { gravity = Gravity.CENTER }

    fun card(activity: Activity, padding: Int = 18): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        val p = dp(activity, padding)
        setPadding(p, p, p, p)
        background = rounded(NovaPalette.surface, 25f, Color.rgb(39, 58, 91))
        elevation = dp(activity, 5).toFloat()
    }

    fun actionTile(
        activity: Activity,
        title: String,
        subtitle: String,
        symbol: String,
        accent: Int,
        onTap: () -> Unit
    ): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val p = dp(activity, 17)
        setPadding(p, dp(activity, 15), p, dp(activity, 15))
        background = gradient(NovaPalette.surface2, NovaPalette.surface, 24f)
        elevation = dp(activity, 4).toFloat()
        isClickable = true
        isFocusable = true
        setOnClickListener { onTap() }
        addView(centerLabel(activity, symbol, 23f, accent, true).apply {
            background = rounded(Color.rgb(29, 47, 82), 18f)
        }, LinearLayout.LayoutParams(dp(activity, 54), dp(activity, 54)))
        val textBox = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 14), 0, 0, 0)
            addView(label(activity, title, 16f, Color.WHITE, true))
            addView(label(activity, subtitle, 12f, NovaPalette.muted).apply {
                setPadding(0, dp(activity, 4), 0, 0)
            })
        }
        addView(textBox, LinearLayout.LayoutParams(0, -2, 1f))
        addView(centerLabel(activity, "›", 27f, accent, true))
    }

    fun pageRoot(activity: Activity): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(activity, 20), dp(activity, 16), dp(activity, 20), dp(activity, 28))
        setBackgroundColor(NovaPalette.background)
    }

    fun installScrollable(activity: Activity, content: LinearLayout) {
        val scroll = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(NovaPalette.background)
            addView(content)
        }
        activity.setContentView(scroll)
    }
}
