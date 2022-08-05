package com.nik.shift.calendar.util

enum class DayState {
    WORK_DAY, DAY, NIGHT, DAY_OFF, NONE;

    companion object {

        fun fromInt(int: Int): DayState {
            return when (int) {
                0 -> WORK_DAY
                1 -> DAY
                2 -> NIGHT
                3 -> DAY_OFF
                4 -> NONE
                else -> throw IllegalArgumentException("illegal DayState")
            }
        }
    }

}

fun DayState.toInt(): Int {
    return when (this) {
        DayState.WORK_DAY -> 0
        DayState.DAY -> 1
        DayState.NIGHT -> 2
        DayState.DAY_OFF -> 3
        DayState.NONE -> 4
    }
}
