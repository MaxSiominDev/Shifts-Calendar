package com.nik.shift.calendar.ui.shifts

import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.nik.shift.calendar.R
import com.nik.shift.calendar.model.ShiftsManager
import com.nik.shift.calendar.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by MaxSiominDev on 5/24/2022
 */
@HiltViewModel
class ShiftsViewModel @Inject constructor(private val shiftsManager: ShiftsManager) : ViewModel() {

    private val _coloredShifts = MutableLiveData<List<Pair<Calendar, DayState>>>()
    val coloredShifts: LiveData<List<Pair<Calendar, DayState>>> = _coloredShifts

    var selectedDay: EventDay? = null

    fun loadShifts(scheduleId: Int, month: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            val to = month.copy().apply {
                add(Calendar.MONTH, 1)
                add(Calendar.DAY_OF_MONTH, -1)
            }
            Timber.d("from = ${getStringDateFromCalendar(month)}")
            Timber.d("to = ${getStringDateFromCalendar(to)}")
            val shifts = shiftsManager.loadShifts(from = month, to = to, scheduleId = scheduleId)
            Timber.i(shifts.size.toString())

            withContext(Dispatchers.Main) {
                _coloredShifts.value = shifts
            }
        }
    }

    @MainThread
    fun updateItem(scheduleId: Int, calendar: Calendar, newSpinnerPosition: Int) {
        val shifts = coloredShifts.value!!.toMutableList()
        val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        shifts[dayNumber - 1] = calendar to DayState.fromInt(newSpinnerPosition)
        _coloredShifts.value = shifts

        viewModelScope.launch(Dispatchers.IO) {
            shiftsManager.updateDay(scheduleId, calendar, DayState.fromInt(newSpinnerPosition))
        }
    }

    fun getSpinnerPositionByCalendar(calendar: Calendar): Int {
        val shifts = _coloredShifts.value!!
        return if (shifts.isEmpty()) {
            DayState.NONE.toInt()
        } else {
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            coloredShifts.value!![dayNumber - 1].second.toInt()
        }
    }


    private var monthsAndYears: List<MonthAndYear>? = null

    data class MonthAndYear(@StringRes val monthRes: Int, val year: String)

    fun getMonthsAndYearsFromMinDateToMaxDate(): List<MonthAndYear> {
        val date = minCalendarDate
        val result = mutableListOf<MonthAndYear>()

        while (maxCalendarDate.notBefore(date)) {

            result.add(
                MonthAndYear(
                    monthRes = monthsList[date.get(Calendar.MONTH)],
                    year = date.get(Calendar.YEAR).toString()
                )
            )

            Timber.v("Month is ${date.get(Calendar.MONTH)}; year is ${date.get(Calendar.YEAR)}")

            date.add(Calendar.MONTH, +1)
        }

        monthsAndYears = result

        return result
    }

    fun getCalendarByIndexFromMonthsAndYears(index: Int): Calendar {
        val date = monthsAndYears!!.elementAt(index)
        val month = monthsList.indexOf(date.monthRes)
        val year = date.year.toInt()

        return emptyCalendar.apply {
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }
    }

    fun getPickerPositionByDate(date: Calendar): Int {
        val month = date.get(Calendar.MONTH)
        val year = date.get(Calendar.YEAR)
        val elem = monthsAndYears!!.find {
            it.monthRes == monthsList[month] && it.year.toInt() == year
        }
        return monthsAndYears!!.indexOf(elem)
    }

    companion object {
        private val monthsList = listOf(
            R.string.january,
            R.string.february,
            R.string.march,
            R.string.april,
            R.string.may,
            R.string.june,
            R.string.july,
            R.string.august,
            R.string.september,
            R.string.october,
            R.string.november,
            R.string.december,
        )
    }

}
