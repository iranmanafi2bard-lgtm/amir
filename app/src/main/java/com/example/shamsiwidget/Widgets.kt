package com.example.shamsiwidget

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import java.time.LocalDate

abstract class BaseWidget : AppWidgetProvider() {
    abstract fun draw(p: Painter, t: LocalDate): Bitmap

    override fun onUpdate(ctx: Context, m: AppWidgetManager, ids: IntArray) {
        val open = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pi = PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        for (id in ids) {
            val rv = RemoteViews(ctx.packageName, R.layout.widget_image)
            rv.setImageViewBitmap(R.id.img, draw(Painter(ctx, id), LocalDate.now()))
            rv.setOnClickPendingIntent(R.id.img, pi)
            m.updateAppWidget(id, rv)
        }
    }

    override fun onDeleted(ctx: Context, ids: IntArray) {
        val e = ctx.getSharedPreferences("w", Context.MODE_PRIVATE).edit()
        ids.forEach { e.remove("op_$it").remove("th_$it") }
        e.apply()
    }

    override fun onReceive(ctx: Context, i: Intent) {
        super.onReceive(ctx, i)
        when (i.action) {
            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_LOCALE_CHANGED -> {
                val m = AppWidgetManager.getInstance(ctx)
                onUpdate(ctx, m, m.getAppWidgetIds(ComponentName(ctx, javaClass)))
            }
        }
    }
}

class SmallWidget : BaseWidget() { override fun draw(p: Painter, t: LocalDate) = p.small(t) }
class MediumWidget : BaseWidget() { override fun draw(p: Painter, t: LocalDate) = p.medium(t) }
class LargeWidget : BaseWidget() { override fun draw(p: Painter, t: LocalDate) = p.large(t) }

fun refreshAll(ctx: Context) {
    val m = AppWidgetManager.getInstance(ctx)
    for (c in listOf(SmallWidget::class.java, MediumWidget::class.java, LargeWidget::class.java)) {
        val ids = m.getAppWidgetIds(ComponentName(ctx, c))
        if (ids.isNotEmpty()) ctx.sendBroadcast(
            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(ctx, c)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        )
    }
}

