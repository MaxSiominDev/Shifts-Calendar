package com.nik.shift.calendar.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

interface HasActionBarOrNot {

    fun hasActionBar(): Boolean

    fun Fragment.setupActionBar() {
        if (hasActionBar())
            (activity as AppCompatActivity).supportActionBar?.show()
        else
            (activity as AppCompatActivity).supportActionBar?.hide()
    }

}
