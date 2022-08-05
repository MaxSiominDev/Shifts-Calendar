package com.nik.shift.calendar.util

import android.widget.NumberPicker

fun NumberPicker.setupWithValues(array: Array<String>, setValueAt: Int? = null) {
    this.setValuesRange(array.indices)
    this.displayedValues = array
    setValueAt?.let { this.value = it }
}

fun NumberPicker.setValuesRange(range: IntRange) {
    this.minValue = range.first
    this.maxValue = range.last
}
