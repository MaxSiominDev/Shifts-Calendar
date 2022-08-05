package com.nik.shift.calendar.ui

import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.fragment.app.DialogFragment
import timber.log.Timber

abstract class BaseDialogFragment : DialogFragment() {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    annotation class LayoutSize

    @LayoutSize
    abstract val width: Int

    @LayoutSize
    abstract val height: Int

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            dialog!!.window!!.setLayout(width, height)
        } else {
            Timber.w("Dialog is null")
        }
    }
}
