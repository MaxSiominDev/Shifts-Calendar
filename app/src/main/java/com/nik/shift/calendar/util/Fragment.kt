package com.nik.shift.calendar.util

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment

fun Fragment.setFragmentResultListenerOnActivity(requestKey: String, listener: (String, Bundle) -> Unit) {
    requireActivity().supportFragmentManager.setFragmentResultListener(requestKey, requireActivity(), listener)
}

fun Fragment.setFragmentResultOnActivity(requestKey: String, result: Bundle) {
    requireActivity().supportFragmentManager.setFragmentResult(requestKey, result)
}

val Fragment.supportFragmentManager get() = requireActivity().supportFragmentManager
