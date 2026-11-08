package com.example.shamsiwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.time.LocalDate
import java.util.Locale

class MainActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val fa = Locale.getDefault().language == "fa"
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val fg = if (dark) Color.WHITE else Color.BLACK
        val dp = resources.displayMetrics.density
        fun px(v: Int) = (v * dp).toInt()
        fun tv(t: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
            text = t; textSize = size; setTextColor(color); gravity = Gravity.CENTER
            if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(24), px(56), px(24), px(32))
        }
        col.addView(tv(if (fa) "تقویم شمسی" else "Shamsi Calendar", 30f, fg, true))
        col.addView(tv(if (fa) "ویجت تقویم شمسی با ظاهر One UI" else "Jalali calendar widgets in One UI style", 15f, Color.GRAY)
            .apply { setPadding(0, px(6), 0, px(24)) })
        col.addView(ImageView(this).apply {
            setImageBitmap(Painter(this@MainActivity).large(LocalDate.now())); adjustViewBounds = true
        }, LinearLayout.LayoutParams(-1, -2))
        val sizes = listOf("2×1" to SmallWidget::class.java, "2×2" to MediumWidget::class.java, "4×2" to LargeWidget::class.java)
        for ((label, cls) in sizes) {
            col.addView(Button(this).apply {
                text = if (fa) "افزودن ویجت $label" else "Add $label widget"
                isAllCaps = false; textSize = 16f; setTextColor(Color.WHITE)
                background = GradientDrawable().apply { cornerRadius = px(26).toFloat(); setColor(Color.parseColor("#E0242E")) }
                setOnClickListener {
                    val m = AppWidgetManager.getInstance(this@MainActivity)
                    if (m.isRequestPinAppWidgetSupported) m.requestPinAppWidget(ComponentName(this@MainActivity, cls), null, null)
                    else Toast.makeText(this@MainActivity,
                        if (fa) "صفحه اصلی را نگه دارید، ویجت‌ها، تقویم شمسی" else "Long-press home, Widgets, Shamsi Calendar",
                        Toast.LENGTH_LONG).show()
                }
            }, LinearLayout.LayoutParams(-1, px(52)).apply { topMargin = px(12) })
        }
        col.addView(tv(if (fa) "برای تنظیم شفافیت و تم: روی ویجت نگه دارید و تنظیمات را بزنید" else
            "To change transparency and theme: long-press a widget, then Settings", 13f, Color.GRAY)
            .apply { setPadding(0, px(24), 0, 0) })
        setContentView(ScrollView(this).apply {
            setBackgroundColor(Color.parseColor(if (dark) "#0E0E10" else "#F2F2F5")); addView(col)
        })
    }
}
