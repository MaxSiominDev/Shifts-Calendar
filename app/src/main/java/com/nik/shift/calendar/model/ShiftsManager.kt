package com.nik.shift.calendar.model

import androidx.annotation.StringRes
import com.applandeo.materialcalendarview.utils.midnightCalendar
import com.applandeo.materialcalendarview.utils.setMidnight
import com.nik.shift.calendar.R
import com.nik.shift.calendar.repository.ShiftsRepository
import com.nik.shift.calendar.util.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ShiftsManager @Inject constructor(private val repo: ShiftsRepository) {

    private fun createShiftsConfiguration(from: Calendar, config: List<ShiftItem>): List<Pair<Calendar, DayState>> {

        from.setMidnight()

        var i = 0
        var k = 0
        val finalList = mutableListOf<Pair<Calendar, DayState>>()
        val date = minCalendarDate
        while (maxCalendarDate.notBefore(date)) {

            val current = date.copy()

            if (date.before(from)) {
                finalList.add(current to DayState.NONE)
            } else {
                val pair = config[i]

                finalList.add(current to pair.dayState)
                if (++k == pair.numberOfDays) {
                    k = 0
                    if (++i == config.size) {
                        i = 0
                    }
                }
            }

            date.add(Calendar.DAY_OF_MONTH, +1)

            Timber.v(getStringDateFromCalendar(current))
        }

        return finalList
    }

    suspend fun saveNewSchedule(scheduleId: Int, from: Calendar, config: List<ShiftItem>) {
        val schedule = createShiftsConfiguration(from, config).map {
            it.first.timeInMillis to it.second
        }
        /*Timber.i("scheduleBefore: $config")
        Timber.i("scheduleAfter: ${schedule.map { 
            getStringDateFromCalendar(calendarOfEpochMillis(it.first)) to it.second}.filter { 
                "2022" in it.first && it.first.startsWith("7")
        }}")*/
        repo.insertShifts(scheduleId, schedule)
    }

    fun getDefaultFirstDay(): String {
        return getStringDateFromCalendar(midnightCalendar)
    }

    suspend fun loadShifts(scheduleId: Int, from: Calendar, to: Calendar): List<Pair<Calendar, DayState>> {
        return repo.loadShiftsOfSchedule(scheduleId).map {
            calendarOfEpochMillis(it.first) to it.second
        }.filter {
            val c: Calendar = it.first
            return@filter !c.before(from) && !c.after(to)
        }
    }

    suspend fun updateDay(scheduleId: Int, calendar: Calendar, dayState: DayState) {
        calendar.setMidnight()
        repo.processNewDay(scheduleId, calendar.timeInMillis, dayState)
    }

    /**
     * @return new schedule's id
     */
    suspend fun createNewSchedule(name: String): Int {
        return repo.createNewSchedule(name)
    }

    /**
     * Key in the map is id of entity and String is name of entity
     */
    suspend fun loadSchedules(): Map<Int, String> {
        return repo.loadAllSchedules()
    }

    data class ShiftItem(val dayState: DayState, val numberOfDays: Int)

    data class NamedShiftType(

        @StringRes
        val nameRes: Int,

        val config: List<ShiftItem>,

    )

    companion object {

        @JvmStatic
        val defaultItem = ShiftItem(DayState.DAY_OFF, 3)

        @JvmStatic
        val shiftTypes = listOf(
            NamedShiftType(R.string.morning_dayoff, listOf(
                ShiftItem(DayState.WORK_DAY, 1), ShiftItem(DayState.DAY_OFF, 1),
            )),
            NamedShiftType(R.string.morning2x_dayoff2x, listOf(
                ShiftItem(DayState.WORK_DAY, 2), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning3x_dayoff3x, listOf(
                ShiftItem(DayState.WORK_DAY, 3), ShiftItem(DayState.DAY_OFF, 3),
            )),
            NamedShiftType(R.string.morning4x_dayoff4x, listOf(
                ShiftItem(DayState.WORK_DAY, 4), ShiftItem(DayState.DAY_OFF, 4),
            )),
            NamedShiftType(R.string.morning_dayoff2x, listOf(
                ShiftItem(DayState.WORK_DAY, 1), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning_dayoff3x, listOf(
                ShiftItem(DayState.WORK_DAY, 1), ShiftItem(DayState.DAY_OFF, 3),
            )),
            NamedShiftType(R.string.morning2x_dayoff4x, listOf(
                ShiftItem(DayState.WORK_DAY, 2), ShiftItem(DayState.DAY_OFF, 4),
            )),
            NamedShiftType(R.string.morning5x_dayoff2x, listOf(
                ShiftItem(DayState.WORK_DAY, 5), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning4x_dayoff2x, listOf(
                ShiftItem(DayState.WORK_DAY, 4), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning3x_dayoff2x, listOf(
                ShiftItem(DayState.WORK_DAY, 3), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning2x_dayoff, listOf(
                ShiftItem(DayState.WORK_DAY, 2), ShiftItem(DayState.DAY_OFF, 1),
            )),
            NamedShiftType(R.string.morning3x_dayoff, listOf(
                ShiftItem(DayState.WORK_DAY, 3), ShiftItem(DayState.DAY_OFF, 1),
            )),
            NamedShiftType(R.string.morning_night_dayoff, listOf(
                ShiftItem(DayState.WORK_DAY, 1), ShiftItem(DayState.NIGHT, 1), ShiftItem(DayState.DAY_OFF, 1),
            )),
            NamedShiftType(R.string.morning_night_dayoff2x, listOf(
                ShiftItem(DayState.WORK_DAY, 1), ShiftItem(DayState.NIGHT, 1), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning2x_night2x_dayoff_2x, listOf(
                ShiftItem(DayState.WORK_DAY, 2), ShiftItem(DayState.NIGHT, 2), ShiftItem(DayState.DAY_OFF, 2),
            )),
            NamedShiftType(R.string.morning2x_night2x_dayoff_4x, listOf(
                ShiftItem(DayState.WORK_DAY, 2), ShiftItem(DayState.NIGHT, 2), ShiftItem(DayState.DAY_OFF, 4),
            )),
        )
    }
}
