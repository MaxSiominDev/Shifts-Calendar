package com.nik.shift.calendar.util

import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener

fun CalendarView.setOnDayClickListener(action: (EventDay) -> Unit) {
    this.setOnDayClickListener(object : OnDayClickListener {
        override fun onDayClick(eventDay: EventDay) {
            action.invoke(eventDay)
        }
    })
}

fun CalendarView.setOnMonthChangedListener(action: () -> Unit) {
    val listener = object : OnCalendarPageChangeListener {
        override fun onChange() {
            action.invoke()
        }
    }

    this.setOnPreviousPageChangeListener(listener)
    this.setOnForwardPageChangeListener(listener)
}
