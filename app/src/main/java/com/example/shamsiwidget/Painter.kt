package com.example.shamsiwidget

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class Painter(ctx: Context, id: Int = 0) {
    val fa = Locale.getDefault().language == "fa"
    private val sp = ctx.getSharedPreferences("w", Context.MODE_PRIVATE)
    private val a = sp.getInt("op_$id", 85) * 255 / 100
    private val dark = when (sp.getInt("th_$id", 0)) {
        1 -> false
        2 -> true
        else -> (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    private val bg = Color.parseColor(if (dark) "#161616" else "#F7F7F9")
    private val fg = if (dark) Color.WHITE else Color.BLACK
    private val soft = Color.parseColor(if (dark) "#000000" else "#FFFFFF")
    private val red = Color.parseColor(if (dark) "#E5333B" else "#E0242E")
    private val todayBg = if (dark) Color.WHITE else red
    private val todayFg = if (dark) Color.BLACK else Color.WHITE
    private val gray = Color.parseColor("#9E9EA3")
    private val al = if (fa) Paint.Align.RIGHT else Paint.Align.LEFT
    private val wdFa = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

    private fun num(n: Int) = if (fa) n.toString().map { '۰' + (it - '0') }.joinToString("") else n.toString()
    private fun x(v: Float, w: Int) = if (fa) w - v else v
    private fun fill(c: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c }
    private fun pt(size: Float, c: Int, bold: Boolean, a: Paint.Align) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; color = c; textAlign = a
        typeface = Typeface.create(if (bold) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
    }
    private fun txt(c: Canvas, s: String, x: Float, y: Float, size: Float, col: Int, bold: Boolean = false, a: Paint.Align = al) =
        c.drawText(s, x, y, pt(size, col, bold, a))
    private fun canvas(w: Int, h: Int, r: Float): Pair<Bitmap, Canvas> {
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(), r, r, fill(bg).also { it.alpha = a })
        return Pair(b, c)
    }
    private fun isRed(t: LocalDate) = t.dayOfWeek == DayOfWeek.FRIDAY || Cal.holiday(Cal.toJ(t), fa) != null
    private fun dayName(t: LocalDate) =
        if (fa) wdFa[(t.dayOfWeek.value + 1) % 7] else t.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    private fun wrap(s: String, size: Float, maxW: Float): List<String> {
        val p = pt(size, fg, false, al)
        val lines = mutableListOf<String>(); var cur = ""
        for (word in s.split(" ")) {
            val t = if (cur.isEmpty()) word else "$cur $word"
            if (cur.isEmpty() || p.measureText(t) <= maxW) cur = t else { lines.add(cur); cur = word }
        }
        if (cur.isNotEmpty()) lines.add(cur)
        return lines
    }

    fun small(t: LocalDate): Bitmap {
        val w = 660; val h = 300
        val (b, c) = canvas(w, h, 150f)
        val j = Cal.toJ(t); val cx = x(150f, w)
        c.drawCircle(cx, 150f, 112f, fill(soft))
        txt(c, num(j.d), cx, 172f, 62f, fg, true, Paint.Align.CENTER)
        txt(c, Cal.month(j.m, fa), x(285f, w), 139f, 66f, fg)
        txt(c, dayName(t), x(285f, w), 213f, 54f, if (isRed(t)) red else fg, true)
        return b
    }

    fun medium(t: LocalDate): Bitmap {
        val w = 600; val h = 660
        val (b, c) = canvas(w, h, 110f)
        val j = Cal.toJ(t); val n = num(j.d)
        txt(c, "${Cal.month(j.m, fa)} ${num(j.y)}", x(70f, w), 140f, 56f, fg, true)
        txt(c, n, x(70f, w), 430f, 300f, fg, true)
        val col = if (isRed(t)) red else fg
        if (fa) txt(c, dayName(t), x(70f, w), 520f, 54f, col, true)
        else {
            val nw = pt(300f, fg, true, al).measureText(n)
            txt(c, dayName(t).take(3).uppercase(), 70f + nw + 24f, 430f, 54f, col, true)
        }
        return b
    }

    fun large(t: LocalDate): Bitmap {
        val w = 1200; val h = 650
        val (b, c) = canvas(w, h, 100f)
        val j = Cal.toJ(t)
        val heads = if (fa) listOf("ش", "ی", "د", "س", "چ", "پ", "ج") else listOf("S", "S", "M", "T", "W", "T", "F")
        fun cx(i: Int) = x(50f + 84f * i + 42f, w)
        heads.forEachIndexed { i, s -> txt(c, s, cx(i), 100f, 36f, if (i == 6) red else fg, true, Paint.Align.CENTER) }
        val (start, len) = Cal.grid(t)
        val off = (start.dayOfWeek.value + 1) % 7
        for (d in 1..len) {
            val k = off + d - 1; val i = k % 7; val y = 185f + 80f * (k / 7)
            if (d == j.d) c.drawRoundRect(cx(i) - 36f, y - 47f, cx(i) + 36f, y + 17f, 18f, 18f, fill(todayBg))
            val col = if (d == j.d) todayFg else if (isRed(start.plusDays((d - 1).toLong()))) red else fg
            txt(c, num(d), cx(i), y, 38f, col, false, Paint.Align.CENTER)
        }
        txt(c, "${num(j.d)} ${Cal.month(j.m, fa)}", x(700f, w), 100f, 56f, fg, true)
        val px = x(1120f, w)
        c.drawCircle(px, 75f, 38f, fill(gray))
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; strokeWidth = 7f; strokeCap = Paint.Cap.ROUND }
        c.drawLine(px - 16f, 75f, px + 16f, 75f, line)
        c.drawLine(px, 59f, px, 91f, line)
        val hol = Cal.holiday(j, fa)
        val msg = hol ?: (if (fa) "روز خوبی داشته باشید!" else "Enjoy your day!")
        wrap(msg, 40f, 440f).take(4).forEachIndexed { n, line ->
            txt(c, line, x(700f, w), 250f + 56f * n, 40f, if (hol != null) red else fg)
        }
        return b
    }
}
