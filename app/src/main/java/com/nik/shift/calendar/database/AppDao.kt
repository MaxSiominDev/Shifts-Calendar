package com.nik.shift.calendar.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.*

/**
 * Created by MaxSiominDev on 5/24/2022
 */
@Dao
interface AppDao {

    /*@Query(value = "SELECT * FROM shiftsTable WHERE day=:day")
    suspend fun loadDay(day: Calendar): DayEntity?

    @Query(value = "SELECT * FROM shiftsTable")
    suspend fun loadAllShifts(): List<DayEntity>?

    @Insert
    suspend fun insertDay(dayEntity: DayEntity)

    @Update
    suspend fun updateDay(dayEntity: DayEntity)

    @Query(value = "DELETE FROM shiftsTable")
    suspend fun clearDatabase()*/

    @Query(value = "SELECT * FROM scheduleTable WHERE id=:scheduleId")
    suspend fun loadAllShifts(scheduleId: Int): ScheduleEntity?

    @Query(value = "SELECT * FROM scheduleTable")
    suspend fun loadAllSchedules(): List<ScheduleEntity>?

    @Query(value = "DELETE FROM scheduleTable WHERE id=:scheduleId")
    suspend fun clearSchedule(scheduleId: Int)

    /**
     * @return new [ScheduleEntity.id]
     */
    @Insert
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(scheduleEntity: ScheduleEntity)

}
