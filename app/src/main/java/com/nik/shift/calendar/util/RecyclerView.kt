package com.nik.shift.calendar.util

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

open class NonScrollingLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

    final override fun canScrollVertically() = false
    final override fun canScrollHorizontally() = false

}
