package com.nik.shift.calendar.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nik.shift.calendar.util.DayState

@Entity(tableName = "scheduleTable")
data class ScheduleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "shiftsMap")
    val shiftsMap: Map<Long, DayState>

)
