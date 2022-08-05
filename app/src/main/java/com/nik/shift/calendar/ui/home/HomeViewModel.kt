package com.nik.shift.calendar.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nik.shift.calendar.model.ShiftsManager
import com.nik.shift.calendar.util.keyAt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val shiftsManager: ShiftsManager) : ViewModel() {

    var dialogShowedAutomatically = false
        private set

    fun onDialogShowedAutomatically() {
        dialogShowedAutomatically = true
    }

    private val _schedulesList = MutableLiveData<List<String>>()
    val schedulesList: LiveData<List<String>> = _schedulesList

    private val _navState = Channel<NavigationState>(Channel.BUFFERED)
    val navState = _navState.receiveAsFlow()
    data class NavigationState(
        val scheduleId: Int? = null,
        val isNewSchedule: Boolean? = null,
    )

    private var schedules: Map<Int, String>? = null
        set(value) {
            field = value!!
            viewModelScope.launch(Dispatchers.Main) {
                _schedulesList.value = value.map { it.value }
            }
        }

    init {
        loadSchedules()
    }

    fun onNewSchedule(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = shiftsManager.createNewSchedule(name)
            loadSchedules()
            viewModelScope.launch {
                _navState.send(NavigationState(id, true))
            }
        }
    }

    private fun loadSchedules() {
        viewModelScope.launch(Dispatchers.IO) {
            schedules = shiftsManager.loadSchedules()
        }
    }

    private fun getScheduleIdByIndex(index: Int): Int {
        return schedules!!.keyAt(index)
    }

    fun onRecyclerViewItemClicked(itemPosition: Int) {
        val scheduleId = getScheduleIdByIndex(itemPosition)
        viewModelScope.launch {
            _navState.send(NavigationState(scheduleId, false))
        }
    }

}
