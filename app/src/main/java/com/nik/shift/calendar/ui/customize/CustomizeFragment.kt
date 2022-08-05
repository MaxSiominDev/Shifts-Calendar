package com.nik.shift.calendar.ui.customize

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nik.shift.calendar.R
import com.nik.shift.calendar.databinding.FragmentCustomizeBinding
import com.nik.shift.calendar.databinding.ItemConfigBinding
import com.nik.shift.calendar.model.ShiftsManager
import com.nik.shift.calendar.ui.ViewBindingFragment
import com.nik.shift.calendar.util.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CustomizeFragment : ViewBindingFragment<FragmentCustomizeBinding>() {

    private val viewModel by viewModels<CustomizeViewModel>()

    private inline val scheduleId get() = requireArguments().getInt(ARG_ID)
    private inline val isNewSchedule get() = requireArguments().getBoolean(ARG_IS_NEW_SCHEDULE)

    private val recyclerViewData = mutableListOf<CustomizeAdapter.Item>()
    private val recyclerViewAdapter by lazy {
        CustomizeAdapter(
            recyclerViewData,
            viewModel::onTypeChanged,
            viewModel::onCountChanged,
            viewModel::onDeleteItem,
        )
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        binding.recyclerView.layoutManager = NonScrollingLinearLayoutManager(requireContext())
        binding.recyclerView.adapter = recyclerViewAdapter

        viewModel.recyclerViewContent.observe(viewLifecycleOwner) {
            recyclerViewData.apply {
                clear()
                addAll(it.map { CustomizeAdapter.Item(it.dayState.toInt(), it.numberOfDays) })
            }
            Timber.i(recyclerViewData.toString())
            recyclerViewAdapter.notifyDataSetChanged()
        }

        binding.addButton.setOnClickListener {
            viewModel.addItemToRecyclerView()
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

        viewModel.addButtonVisibility.observe(viewLifecycleOwner) {
            binding.addButton.visibility = it
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

        val config = pConfig ?: viewModel.recyclerViewContent.value!!
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

    private class CustomizeAdapter(
        private val values: List<Item>,
        private val onTypeChanged: (Int, Int) -> Unit,
        private val onCountChanged: (Int, Int) -> Unit,
        private val onDeleteItem: (Int) -> Unit,
    ) : RecyclerView.Adapter<CustomizeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                ItemConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                shiftType.setSelection(values[position].type)
                shiftType.addOnItemSelectedListener {
                    onTypeChanged(position, it)
                }

                dayCount.setSelection(values[position].numberOfDays - 1)
                dayCount.addOnItemSelectedListener {
                    onCountChanged(position, it + 1) // 1st elem is 0
                }

                if (position >= 2) {
                    deleteButton.visibility = View.VISIBLE
                    deleteButton.setOnClickListener {
                        onDeleteItem(position)
                    }
                }
            }
        }

        override fun getItemCount() = values.size

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
}
