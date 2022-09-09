package com.nik.shift.calendar.ui.shifts

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.annotation.MainThread
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.applandeo.materialcalendarview.CalendarDay
import com.google.android.material.button.MaterialButton
import com.nik.shift.calendar.R
import com.nik.shift.calendar.databinding.FragmentShiftsBinding
import com.nik.shift.calendar.ui.BaseDialogFragment
import com.nik.shift.calendar.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

/**
 * Created by MaxSiominDev on 5/24/2022
 */
@AndroidEntryPoint
class ShiftsFragment : Fragment(), HasActionBarOrNot {

    private var _binding: FragmentShiftsBinding? = null
    private val binding: FragmentShiftsBinding get() = _binding!!

    private val viewModel by viewModels<ShiftsViewModel>()

    private inline val scheduleId get() = requireArguments().getInt(ARG_ID)
    private inline val isNewSchedule get() = requireArguments().getBoolean(ARG_NEW)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (isNewSchedule) {
            goToCustomizeFragment()
        }
    }

    override fun hasActionBar() = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShiftsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        binding.calendarView.setMinimumDate(minCalendarDate)
        binding.calendarView.setMaximumDate(maxCalendarDate)

        binding.calendarView.setOnDayClickListener { day ->

            // Prevent double click
            if (viewModel.selectedDay != null) {
                return@setOnDayClickListener
            }

            val firstDayOfMonth = binding.calendarView.currentPageDate
            val lastDayOfMonth = binding.calendarView.currentPageDate.apply {
                add(Calendar.MONTH, 1)
                add(Calendar.DAY_OF_YEAR, -1)
            }

            if (day.calendar.notBefore(firstDayOfMonth) && day.calendar.notAfter(lastDayOfMonth)) {
                viewModel.selectedDay = day
                showSelectedDayDialog()
            }
        }

        binding.calendarView.setOnMonthChangedListener {
            viewModel.loadShifts(scheduleId, binding.calendarView.currentPageDate)
        }

        viewModel.coloredShifts.observe(viewLifecycleOwner) {

            /*Timber.d("coloredShifts updated, newValue is ${it.map {
                "${getStringDateFromCalendar(it.first)} to ${it.second}"
            }}")*/

            /**
             * If all days are empty, I don't need to setup calendar days
             */
            if (it.all { elem -> elem.second == DayState.NONE }) {
                return@observe
            }

            // Prevent freezing (problem of this library).
            // Can be fixed if calendar view is used by activity, not by fragment
            viewLifecycleOwner.lifecycleScope.launch {
                delay(200)
                withContext(Dispatchers.Main) {
                    setupCalendarDays(it)
                }
            }
        }
        viewModel.loadShifts(scheduleId, binding.calendarView.currentPageDate)

        binding.btnGoTo.setOnClickListener {
            showSelectDayToGoTo_Dialog()
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCustomize.setOnClickListener {
            goToCustomizeFragment()
        }

        setFragmentResultListenerOnActivity("SelectedDayDialog/onDismiss") { _, _ ->
            viewModel.selectedDay = null
        }

        setFragmentResultListenerOnActivity("SelectedDayDialog/onSuccess") { _, bundle ->
            viewModel.updateItem(scheduleId, viewModel.selectedDay!!.calendar, bundle.getInt("spinnerPosition"))
        }

        setFragmentResultListenerOnActivity("SelectDayToGoTo_Dialog/onSuccess") { _, b: Bundle ->
            val index = b.getInt("pickerValue")
            val calendarToGoTo = viewModel.getCalendarByIndexFromMonthsAndYears(index)
            goTo(calendarToGoTo)
            viewModel.loadShifts(scheduleId, calendarToGoTo)
        }
    }

    private fun goToCustomizeFragment() {
        findNavController()
            .navigate(ShiftsFragmentDirections.actionShiftsFragmentToCustomizeFragment(scheduleId, isNewSchedule))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Timber.d("onDestroyView called")
    }

    @MainThread
    private fun setupCalendarDays(days: List<Pair<Calendar, DayState>>) {

        val calendarDays: List<CalendarDay> = days.map {
            val calendarDay = CalendarDay(it.first)
            val drawableRes: Int? = when (it.second) {
                DayState.WORK_DAY -> R.drawable.work_day
                DayState.DAY -> R.drawable.day
                DayState.NIGHT -> R.drawable.night
                DayState.DAY_OFF -> R.drawable.day_off
                else -> null
            }
            if (drawableRes != null) {
                calendarDay.backgroundResource = drawableRes
            }
            return@map calendarDay
        }

        binding.calendarView.setCalendarDays(calendarDays)
    }

    private fun showSelectedDayDialog() {
        SelectedDayDialog
            .newInstance(viewModel.getSpinnerPositionByCalendar(viewModel.selectedDay!!.calendar))
            .show(supportFragmentManager, tag)
    }

    private fun showSelectDayToGoTo_Dialog() {
        val pickerContent: Array<String> = viewModel.getMonthsAndYearsFromMinDateToMaxDate().map {
            "${getString(it.monthRes)}, ${it.year}"
        }.toTypedArray()

        val pickerPosition: Int = viewModel.getPickerPositionByDate(binding.calendarView.currentPageDate)

        Timber.v(pickerContent.joinToString())

        SelectDayToGoTo_Dialog
            .newInstance(pickerContent, pickerPosition)
            .show(supportFragmentManager, tag)
    }

    private fun goTo(calendar: Calendar) {
        binding.calendarView.setDate(calendar)
    }

    class SelectedDayDialog : BaseDialogFragment() {

        override val width = ViewGroup.LayoutParams.WRAP_CONTENT
        override val height = ViewGroup.LayoutParams.WRAP_CONTENT

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val dialog = Dialog(requireContext())

            dialog.setContentView(R.layout.dialog_selected_day)

            val spinner = dialog.findViewById<AppCompatSpinner>(R.id.appCompatSpinner)
            spinner.setSelection(requireArguments().getInt(ARG_SPINNER_POSITION))

            val btnCancel = dialog.findViewById<MaterialButton>(R.id.button_cancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            val btnOk = dialog.findViewById<MaterialButton>(R.id.button_ok)
            btnOk.setOnClickListener {
                val position = spinner.selectedItemPosition
                setFragmentResultOnActivity("SelectedDayDialog/onSuccess", bundleOf("spinnerPosition" to position))
                dialog.dismiss()
            }

            return dialog
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            setFragmentResultOnActivity("SelectedDayDialog/onDismiss", bundleOf())
        }

        companion object {

            @JvmStatic
            fun newInstance(spinnerPosition: Int): SelectedDayDialog {
                return SelectedDayDialog().apply {
                    arguments = bundleOf(ARG_SPINNER_POSITION to spinnerPosition)
                }
            }

            private const val ARG_SPINNER_POSITION = "spinnerPosition"
        }
    }

    class SelectDayToGoTo_Dialog : BaseDialogFragment() {

        override val width = ViewGroup.LayoutParams.MATCH_PARENT
        override val height = ViewGroup.LayoutParams.WRAP_CONTENT

        private inline val values get() = requireArguments().getStringArray(ARG_PICKER_CONTENT)!!
        private inline val index get() = requireArguments().getInt(ARG_PICKER_POSITION)

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val dialog = Dialog(requireContext())

            dialog.setContentView(R.layout.dialog_selected_day_to_go_to)

            val picker = dialog.findViewById<NumberPicker>(R.id.datePicker)
            picker.setupWithValues(values, setValueAt = index)

            val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            val btnApply = dialog.findViewById<MaterialButton>(R.id.btnApply)
            btnApply.setOnClickListener {
                val pickerValue = picker.value
                val b = bundleOf("pickerValue" to pickerValue)
                setFragmentResultOnActivity("SelectDayToGoTo_Dialog/onSuccess", b)
                dialog.dismiss()
            }

            return dialog
        }

        companion object {

            @JvmStatic
            fun newInstance(pickerContent: Array<String>, pickerPosition: Int): SelectDayToGoTo_Dialog {
                return SelectDayToGoTo_Dialog().apply {
                    Timber.v(pickerContent.joinToString())
                    val bundle = Bundle().apply {
                        putStringArray(ARG_PICKER_CONTENT, pickerContent)
                        putInt(ARG_PICKER_POSITION, pickerPosition)
                    }
                    arguments = bundle
                }
            }

            private const val ARG_PICKER_CONTENT = "pickerContent"
            private const val ARG_PICKER_POSITION = "pickerPosition"
        }

    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_NEW = "scheduleOpenedFirstTime"
    }

}
