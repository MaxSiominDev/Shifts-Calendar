package com.nik.shift.calendar.util

fun <K, V> Map<K, V>.keyAt(index: Int): K {
    return this.elementAt(index).first
}

fun <K, V> Map<K, V>.valueAt(index: Int): V {
    return this.elementAt(index).second
}

fun <K, V> Map<K, V>.elementAt(index: Int): Pair<K, V> {
    return this.toList().elementAt(index)
}
