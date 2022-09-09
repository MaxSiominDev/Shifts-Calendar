package com.nik.shift.calendar.ui.customize

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nik.shift.calendar.R
import com.nik.shift.calendar.databinding.FragmentCustomizeBinding
import com.nik.shift.calendar.databinding.ItemConfigBinding
import com.nik.shift.calendar.model.ShiftsManager
import com.nik.shift.calendar.model.ShiftsManager.Companion.defaultItem
import com.nik.shift.calendar.util.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CustomizeFragment : Fragment(), HasActionBarOrNot {

    private val viewModel by viewModels<CustomizeViewModel>()

    private inline val scheduleId get() = requireArguments().getInt(ARG_ID)
    private inline val isNewSchedule get() = requireArguments().getBoolean(ARG_IS_NEW_SCHEDULE)

    private var _binding: FragmentCustomizeBinding? = null
    private val binding: FragmentCustomizeBinding get() = _binding!!

    private val recyclerViewAdapter by lazy {
        CustomizeAdapter()
    }

    private val onBackPressedCallback: OnBackPressedCallback by lazy {
        onBackPressedCallback {
            Timber.i("onBackPressedListener called")
            if (viewModel.isSavingShifts) {
                // Do nothing
            } else {
                goBack()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback,
        )
    }

    override fun onDetach() {
        onBackPressedCallback.remove()
        super.onDetach()
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomizeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.goBackFlow.collect {
                when (it) {
                    CustomizeViewModel.GoBack.BACK -> goBack()
                }
            }
        }

        binding.firstDayTextView.setOnClickListener {
            showCalendarDialog()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = recyclerViewAdapter

        viewModel.recyclerViewContent.observe(viewLifecycleOwner) {
            recyclerViewAdapter.values = it.map { CustomizeAdapter.Item(it.dayState.toInt(), it.numberOfDays) }
        }

        binding.addButton.setOnClickListener {
            recyclerViewAdapter.addItem(CustomizeAdapter.Item(defaultItem.dayState.toInt(), defaultItem.numberOfDays))
        }

        binding.saveButton.setOnClickListener {
            if (!isNewSchedule) {
                showSavingConfirmationDialog {
                    onSave()
                }
            } else {
                onSave()
            }
        }

        viewModel.firstDayText.observe(viewLifecycleOwner) {
            binding.firstDayTextView.text = it
        }

        setFragmentResultListenerOnActivity("SelectShiftTypeDialog/onSuccess") { _, b: Bundle ->
            val position = b.getInt("position")
            onShiftTypeSelected(position)
        }

        showSelectShiftTypeDialog()
    }

    private fun onShiftTypeSelected(position: Int) {
        val config = viewModel.getNamedShiftTypes().elementAt(position).config
        if (!isNewSchedule) {
            showSavingConfirmationDialog() {
                onSave(pConfig = config)
            }
        } else {
            onSave(pConfig = config)
        }
    }

    /**
     * User is to confirm that he agrees to wipe calendar
     */
    private fun showSavingConfirmationDialog(save: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.data_will_be_erased)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { _, _ ->
                save.invoke()
            }
            .show()
    }

    private fun onSave(pConfig: List<ShiftsManager.ShiftItem>? = null) {
        // Prevent user click the button between starting saving schedule
        // and calling findNavController.popBackStack()
        binding.saveButton.isClickable = false

        val config = pConfig ?: recyclerViewAdapter.spinnerValues.map {
            ShiftsManager.ShiftItem(DayState.fromInt(it.type), it.numberOfDays)
        }
        Timber.i(config.joinToString())
        viewModel.saveNewConfiguration(scheduleId, config)
    }

    private fun showCalendarDialog() {
        val datePickerDialog =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_first_day)
                .build()

        datePickerDialog.addOnPositiveButtonClickListener { millis: Long ->
            val selectedDay = calendarOfEpochMillis(millis)
            viewModel.onFirstDayChanged(selectedDay) // Length of calendar list is always 1
        }

        datePickerDialog.show(supportFragmentManager, tag)
    }

    private fun showSelectShiftTypeDialog() {
        val shifts = viewModel.getNamedShiftTypes().map { getString(it.nameRes) }.toTypedArray()
        SelectShiftTypeDialog
            .newInstance(shifts)
            .show(supportFragmentManager, tag)
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_IS_NEW_SCHEDULE = "isNewSchedule"
    }

    @SuppressLint("NotifyDataSetChanged")
    private class CustomizeAdapter : RecyclerView.Adapter<CustomizeAdapter.ViewHolder>() {

        var values: List<Item>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        fun addItem(item: Item) {
            val values = mutableListOf<Item>()
            this.values!!.forEachIndexed { i, _ ->
                values.add(Item(spinnerValues[i].type, spinnerValues[i].numberOfDays))
            }
            values.add(item)
            this.values = values
        }

        private var _spinners = mutableListOf<_Spinners>()
        val spinnerValues: List<Item> get() = _spinners.map {
            Item(it.typeSpinner.selectedItemPosition, it.countSpinner.selectedItemPosition + 1)
        }

        private data class _Spinners(
            val typeSpinner: AppCompatSpinner,
            val countSpinner: AppCompatSpinner,
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                ItemConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                Timber.i("position = $position")
                itemView.setOnClickListener {
                    Timber.i("clicked position = $position")
                }

                if (_spinners.size < position + 1)
                    _spinners.add(_Spinners(shiftType, dayCount))
                else
                    _spinners[position] = _Spinners(shiftType, dayCount)

                shiftType.setSelection(values!![position].type)
                /*shiftType.addOnItemSelectedListener {
                    onTypeChanged(position, it)
                }*/

                dayCount.setSelection(values!![position].numberOfDays - 1)
                /*dayCount.addOnItemSelectedListener {
                    onCountChanged(position, it + 1) // 1st elem is 0
                }*/

                if (position >= 2) {
                    deleteButton.visibility = View.VISIBLE
                    deleteButton.setOnClickListener {
                        values = values!!.toMutableList().apply { removeAt(position) }
                    }
                } else {
                    deleteButton.visibility = View.GONE
                }
            }
        }

        override fun getItemCount() = values?.size ?: 0

        private inner class ViewHolder(binding: ItemConfigBinding) : RecyclerView.ViewHolder(binding.root) {

            val shiftType = binding.shiftType
            val dayCount = binding.dayCount
            val deleteButton = binding.deleteButton

        }

        data class Item(
            val type: Int,
            val numberOfDays: Int,
        )
    }

    override fun hasActionBar() = false
}
