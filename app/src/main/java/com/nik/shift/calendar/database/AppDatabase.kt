package com.nik.shift.calendar.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nik.shift.calendar.database.AppDatabase.Companion.VERSION

/**
 * Created by MaxSiominDev on 5/24/2022
 */
@Database(entities = [ScheduleEntity::class], version = VERSION)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Returns new instance of [AppDao]
     */
    abstract fun appDao(): AppDao

    companion object {
        const val VERSION = 3
        const val DATABASE_NAME = "appDatabase"
    }
}
