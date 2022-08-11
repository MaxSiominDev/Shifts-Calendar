package com.nik.shift.calendar.util

import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import timber.log.Timber

/*fun AppCompatSpinner.addOnItemSelectedListener(action: (Int) -> Unit) {
    this.onItemSelectedListener = onItemSelectedListener {
        Timber.v("invoke called")
        action.invoke(it)
    }
}

private fun onItemSelectedListener(action: (Int) -> Unit): AdapterView.OnItemSelectedListener {
    return object : AdapterView.OnItemSelectedListener {
        //private var onItemSelectCallsCount = 0

        override fun onItemSelected(parent: AdapterView<*>?, view: View, pos: Int, id: Long) {
            //Timber.v("onItemSelected called; count = $onItemSelectCallsCount")
            action.invoke(pos)
            //onItemSelectCallsCount++
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }
}*/
