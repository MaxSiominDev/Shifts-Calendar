package com.nik.shift.calendar.util

import androidx.activity.OnBackPressedCallback

fun onBackPressedCallback(action: Runnable): OnBackPressedCallback {
    return object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            action.run()
        }
    }
}
