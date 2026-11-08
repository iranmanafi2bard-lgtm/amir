package com.example.shamsiwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import java.util.Locale

class ConfigActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setResult(RESULT_CANCELED)
        val id = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: 0
        if (id == 0) { finish(); return }
        val fa = Locale.getDefault().language == "fa"
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val fg = if (dark) Color.WHITE else Color.BLACK
        val dp = resources.displayMetrics.density
        fun px(v: Int) = (v * dp).toInt()
        val sp = getSharedPreferences("w", MODE_PRIVATE)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(px(24), px(48), px(24), px(24)) }
        col.addView(TextView(this).apply { text = if (fa) "تنظیمات ویجت" else "Widget settings"; textSize = 26f; setTextColor(fg) })
        val label = TextView(this).apply { textSize = 16f; setTextColor(fg); setPadding(0, px(28), 0, px(8)) }
        val seek = SeekBar(this).apply { max = 100; progress = 100 - sp.getInt("op_$id", 85) }
        fun upd() { label.text = (if (fa) "شفافیت: " else "Transparency: ") + seek.progress + "%" }
        upd()
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(b: SeekBar?, p: Int, u: Boolean) = upd()
            override fun onStartTrackingTouch(b: SeekBar?) {}
            override fun onStopTrackingTouch(b: SeekBar?) {}
        })
        col.addView(label); col.addView(seek)
        col.addView(TextView(this).apply { text = if (fa) "تم" else "Theme"; textSize = 16f; setTextColor(fg); setPadding(0, px(28), 0, px(4)) })
        val group = RadioGroup(this)
        listOf(if (fa) "خودکار" else "Auto", if (fa) "روشن" else "Light", if (fa) "دارک" else "Dark").forEachIndexed { i, t ->
            group.addView(RadioButton(this).apply { text = t; this.id = i + 1; setTextColor(fg); isChecked = i == sp.getInt("th_$id", 0) })
        }
        col.addView(group)
        col.addView(Button(this).apply {
            text = if (fa) "تأیید" else "Done"; isAllCaps = false; textSize = 16f; setTextColor(Color.WHITE)
            background = GradientDrawable().apply { cornerRadius = px(26).toFloat(); setColor(Color.parseColor("#E0242E")) }
            setOnClickListener {
                sp.edit().putInt("op_$id", 100 - seek.progress).putInt("th_$id", group.checkedRadioButtonId - 1).apply()
                val am = AppWidgetManager.getInstance(this@ConfigActivity)
                val cn = am.getAppWidgetInfo(id)?.provider?.className
                if (cn != null) (Class.forName(cn).getDeclaredConstructor().newInstance() as BaseWidget)
                    .onUpdate(this@ConfigActivity, am, intArrayOf(id))
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                finish()
            }
        }, LinearLayout.LayoutParams(-1, px(52)).apply { topMargin = px(32) })
        setContentView(ScrollView(this).apply { setBackgroundColor(Color.parseColor(if (dark) "#0E0E10" else "#F2F2F5")); addView(col) })
    }
}
