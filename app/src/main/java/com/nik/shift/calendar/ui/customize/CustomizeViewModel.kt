package com.nik.shift.calendar.ui.customize

import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.utils.midnightCalendar
import com.nik.shift.calendar.model.ShiftsManager
import com.nik.shift.calendar.model.ShiftsManager.Companion.defaultItem
import com.nik.shift.calendar.util.DayState
import com.nik.shift.calendar.util.getStringDateFromCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CustomizeViewModel @Inject constructor(private val shiftsManager: ShiftsManager) : ViewModel() {

    private val _firstDayText = MutableLiveData(shiftsManager.getDefaultFirstDay())
    val firstDayText: LiveData<String> = _firstDayText

    private val _recyclerViewContent: MutableLiveData<List<ShiftsManager.ShiftItem>> =
        MutableLiveData(listOf(ShiftsManager.ShiftItem(DayState.DAY, 2), defaultItem))
    val recyclerViewContent: LiveData<List<ShiftsManager.ShiftItem>> = _recyclerViewContent

    private val _addButtonVisibility = MutableLiveData(View.VISIBLE)
    val addButtonVisibility: LiveData<Int> = _addButtonVisibility

    private val _goBackChannel = Channel<GoBack>(Channel.BUFFERED)
    val goBackFlow = _goBackChannel.receiveAsFlow()

    enum class GoBack {
        BACK,
    }

    private var firstDay = midnightCalendar

    var isSavingShifts = false
        private set

    @MainThread // Invocation of LiveData.setValue() is available only on the UI thread
    fun onFirstDayChanged(day: Calendar) {
        firstDay = day
        _firstDayText.value = getStringDateFromCalendar(day)
    }

    @MainThread // Invocation of LiveData.setValue() is available only on the UI thread
    fun onTypeChanged(itemPosition: Int, newSpinnerPosition: Int) {
        Timber.d("onTypeChanged called")
        val content = _recyclerViewContent.value!!.toMutableList()
        Timber.i("newPos: $newSpinnerPosition")
        Timber.i("dayState: ${DayState.fromInt(newSpinnerPosition)}")
        content[itemPosition] =
            ShiftsManager.ShiftItem(DayState.fromInt(newSpinnerPosition), content[itemPosition].numberOfDays)
        _recyclerViewContent.value = content
    }

    @MainThread // Invocation of LiveData.setValue() is available only on the UI thread
    fun onCountChanged(itemPosition: Int, newSpinnerPosition: Int) {
        Timber.d("onCountChanged called")
        val content = _recyclerViewContent.value!!.toMutableList()
        //Timber.i("Before: $content")
        content[itemPosition] = ShiftsManager.ShiftItem(content[itemPosition].dayState, newSpinnerPosition)
        //Timber.i("After: $content")
        _recyclerViewContent.value = content
    }

    @MainThread // Invocation of LiveData.setValue() is available only on the UI thread
    fun onDeleteItem(itemPosition: Int) {
        Timber.d("onDeleteItem called")
        val content = _recyclerViewContent.value!!.toMutableList()
        content.removeAt(itemPosition)
        if (content.size == 4) {
            _addButtonVisibility.value = View.VISIBLE
        }
        _recyclerViewContent.value = content
    }

    @MainThread // Invocation of LiveData.setValue() is available only on the UI thread
    fun addItemToRecyclerView() {
        val content = _recyclerViewContent.value!!.toMutableList()
        content.add(defaultItem)
        if (content.size == 5) {
            _addButtonVisibility.value = View.GONE
        }
        _recyclerViewContent.value = content
    }

    fun saveNewConfiguration(scheduleId: Int, config: List<ShiftsManager.ShiftItem>) {
        isSavingShifts = true
        viewModelScope.launch(Dispatchers.IO) {
            shiftsManager.saveNewSchedule(scheduleId, firstDay, config)
            Timber.i("config: $config")
            isSavingShifts = false
            _goBackChannel.send(GoBack.BACK)
        }
    }

    fun getNamedShiftTypes(): List<ShiftsManager.NamedShiftType> {
        return ShiftsManager.shiftTypes
    }
}
