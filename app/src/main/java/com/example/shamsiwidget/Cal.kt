package com.example.shamsiwidget

import java.time.LocalDate

class J(val y: Int, val m: Int, val d: Int)

object Cal {
    // Gregorian -> Jalali (Shamsi)
    fun toJ(t: LocalDate): J {
        val gy = t.year; val gm = t.monthValue; val gd = t.dayOfMonth
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365 }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return J(jy, jm, jd)
    }

    // first day of the Shamsi month containing t, and its length
    fun grid(t: LocalDate): Pair<LocalDate, Int> {
        val j = toJ(t)
        val start = t.minusDays((j.d - 1).toLong())
        var n = 0
        while (toJ(start.plusDays(n.toLong())).m == j.m) n++
        return Pair(start, n)
    }

    private val fa = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
    private val en = listOf("Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand")
    fun month(m: Int, persian: Boolean) = if (persian) fa[m - 1] else en[m - 1].uppercase()

    // Fixed-date official holidays (same every year)
    private val fixed = mapOf(
        101 to ("نوروز" to "Nowruz"), 102 to ("نوروز" to "Nowruz"),
        103 to ("نوروز" to "Nowruz"), 104 to ("نوروز" to "Nowruz"),
        112 to ("روز جمهوری اسلامی" to "Islamic Republic Day"),
        113 to ("روز طبیعت (سیزده بدر)" to "Nature Day (Sizdah Bedar)"),
        314 to ("رحلت امام خمینی" to "Demise of Imam Khomeini"),
        315 to ("قیام ۱۵ خرداد" to "15 Khordad Uprising"),
        1122 to ("پیروزی انقلاب اسلامی" to "Revolution Victory Day"),
        1229 to ("ملی شدن صنعت نفت" to "Oil Nationalization Day")
    )

    // Lunar-based official holidays: dates move every year, so only 1405 is filled in
    private val lunar1405 = mapOf(
        125 to ("شهادت امام جعفر صادق (ع)" to "Martyrdom of Imam al-Sadiq"),
        306 to ("عید سعید قربان" to "Eid al-Adha"),
        314 to ("عید سعید غدیر خم" to "Eid al-Ghadir"),
        403 to ("تاسوعای حسینی" to "Tasu'a"),
        404 to ("عاشورای حسینی" to "Ashura"),
        513 to ("اربعین حسینی" to "Arbaeen"),
        521 to ("رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)" to "Demise of the Prophet and Martyrdom of Imam Hasan"),
        522 to ("شهادت امام رضا (ع)" to "Martyrdom of Imam Reza"),
        530 to ("شهادت امام حسن عسکری (ع)" to "Martyrdom of Imam Hasan al-Askari"),
        608 to ("میلاد پیامبر اکرم (ص) و امام جعفر صادق (ع)" to "Birth of the Prophet and Imam al-Sadiq"),
        822 to ("شهادت حضرت فاطمه زهرا (س)" to "Martyrdom of Fatima al-Zahra"),
        1002 to ("ولادت امام علی (ع) و روز پدر" to "Birth of Imam Ali (Father's Day)"),
        1016 to ("مبعث پیامبر اکرم (ص)" to "Mab'ath of the Prophet"),
        1104 to ("ولادت حضرت قائم (عج)" to "Birth of Imam al-Mahdi"),
        1209 to ("شهادت امام علی (ع)" to "Martyrdom of Imam Ali"),
        1219 to ("عید سعید فطر" to "Eid al-Fitr"),
        1220 to ("تعطیل به مناسبت عید فطر" to "Eid al-Fitr holiday")
    )

    fun holiday(j: J, persian: Boolean): String? {
        val k = j.m * 100 + j.d
        val list = listOfNotNull(fixed[k], if (j.y == 1405) lunar1405[k] else null)
        if (list.isEmpty()) return null
        return list.joinToString(" / ") { if (persian) it.first else it.second }
    }
}
