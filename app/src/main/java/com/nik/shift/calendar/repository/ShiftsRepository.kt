package com.nik.shift.calendar.repository

import com.nik.shift.calendar.database.AppDao
import com.nik.shift.calendar.database.ScheduleEntity
import com.nik.shift.calendar.util.DayState
import javax.inject.Inject

class ShiftsRepository @Inject constructor(private val dao: AppDao) {

    suspend fun insertShifts(scheduleId: Int, schedule: List<Pair<Long, DayState>>) {
        val entity = dao.loadAllShifts(scheduleId)!!
        dao.updateSchedule(ScheduleEntity(scheduleId, entity.name, schedule.toMap()))
    }

    suspend fun processNewDay(scheduleId: Int, date: Long, dayState: DayState) {
        val schedule = dao.loadAllShifts(scheduleId)!!
        val shifts = schedule.shiftsMap.toMutableMap()
        shifts[date] = dayState
        dao.updateSchedule(ScheduleEntity(scheduleId, schedule.name, shifts))
    }

    suspend fun loadShiftsOfSchedule(scheduleId: Int): List<Pair<Long, DayState>> {
        return dao.loadAllShifts(scheduleId)?.shiftsMap?.toList() ?: emptyList()
    }

    suspend fun createNewSchedule(name: String): Int {
        return dao.insertSchedule(ScheduleEntity(0, name, emptyMap())).toInt()
    }

    suspend fun loadAllSchedules(): Map<Int, String> {
        return dao.loadAllSchedules()?.associate {
            it.id to it.name
        } ?: emptyMap()
    }

}
