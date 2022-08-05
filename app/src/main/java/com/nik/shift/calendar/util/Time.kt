package com.nik.shift.calendar.util

import java.util.*

fun Calendar.copy() = this.clone() as Calendar

inline val emptyCalendar: Calendar get() =  Calendar.getInstance().apply { clear() }

fun Calendar.notBefore(date: Calendar): Boolean = !this.before(date)

fun Calendar.notAfter(date: Calendar): Boolean = !this.after(date)

fun calendarOfEpochMillis(millis: Long): Calendar {
    return emptyCalendar.apply { timeInMillis = millis }
}

inline val minCalendarDate get() = emptyCalendar.apply {
    this[Calendar.YEAR] = 2020
    this[Calendar.MONTH] = 0 // January
    this[Calendar.DAY_OF_MONTH] = 1
}

inline val maxCalendarDate get() = emptyCalendar.apply {
    this[Calendar.YEAR] = 2024
    this[Calendar.MONTH] = 11 // December
    this[Calendar.DAY_OF_MONTH] = 31
}

fun getStringDateFromCalendar(c: Calendar): String {
    return "${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.YEAR)}"
}
