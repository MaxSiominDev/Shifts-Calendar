package com.nik.shift.calendar.database

import androidx.room.TypeConverter
import com.nik.shift.calendar.util.DayState
import com.nik.shift.calendar.util.moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

object Converters {

    @TypeConverter
    fun stringToMap(value: String): Map<Long, DayState> {
        val jsonAdapter: JsonAdapter<Map<Long, DayState>> = moshi().adapter(
            Types.newParameterizedType(
                Map::class.java,
                Long::class.javaObjectType,
                DayState::class.java
            )
        )
        return jsonAdapter.fromJson(value) ?: emptyMap()
    }

    @TypeConverter
    fun mapToString(value: Map<Long, DayState>): String {
        val jsonAdapter: JsonAdapter<Map<Long, DayState>> = moshi().adapter(
            Types.newParameterizedType(
                Map::class.java,
                Long::class.javaObjectType,
                DayState::class.java
            )
        )
        return jsonAdapter.toJson(value)
    }

}
