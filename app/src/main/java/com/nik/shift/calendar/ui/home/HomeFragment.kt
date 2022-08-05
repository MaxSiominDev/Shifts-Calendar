package com.nik.shift.calendar.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nik.shift.calendar.R
import com.nik.shift.calendar.databinding.FragmentHomeBinding
import com.nik.shift.calendar.databinding.ItemScheduleBinding
import com.nik.shift.calendar.ui.BaseDialogFragment
import com.nik.shift.calendar.ui.ViewBindingFragment
import com.nik.shift.calendar.util.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : ViewBindingFragment<FragmentHomeBinding>() {

    private val viewModel by viewModels<HomeViewModel>()

    private val recyclerViewData = mutableListOf<String>()
    private val recyclerViewAdapter by lazy {
        HomeAdapter(recyclerViewData, viewModel::onRecyclerViewItemClicked)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreatedCalled")
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.navState.collect { navState ->
                Timber.i(navState.toString())
                if (navState.scheduleId == null || navState.isNewSchedule == null) {
                    return@collect
                }

                goToShiftsFragment(navState.scheduleId, navState.isNewSchedule)
            }
        }

        with (binding.recyclerView) {
            val layoutManager = LinearLayoutManager(requireContext())
            setLayoutManager(layoutManager)

            val decoration = DividerItemDecoration(context, layoutManager.orientation)
            addItemDecoration(decoration)

            adapter = recyclerViewAdapter
        }

        viewModel.schedulesList.observe(viewLifecycleOwner) {

            Timber.v("schedulesList - $it")

            if (it.isEmpty() && !viewModel.dialogShowedAutomatically) {
                viewModel.onDialogShowedAutomatically()
                showNewScheduleDialog()
            }

            recyclerViewData.apply {
                clear()
                addAll(it)
            }
            recyclerViewAdapter.notifyDataSetChanged()
        }

        binding.fabAdd.setOnClickListener {
            showNewScheduleDialog()
        }

        setFragmentResultListenerOnActivity("NewScheduleDialog/onSuccess") { s: String, b: Bundle ->
            Timber.d("$s called")
            val name = b.getString("newScheduleName")!!
            viewModel.onNewSchedule(name)
        }
    }

    private fun goToShiftsFragment(scheduleId: Int, newSchedule: Boolean) {
        findNavController()
            .navigate(HomeFragmentDirections.actionHomeFragmentToShiftsFragment(scheduleId, newSchedule))
    }

    private fun showNewScheduleDialog() {
        Timber.d("showNewScheduleDialog called")
        NewScheduleDialog()
            .show(supportFragmentManager, tag)
    }

    private class HomeAdapter(
        private val values: List<String>,
        private val onItemClicked: (Int) -> Unit,
    ) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                name.text = values[position]
                itemView.setOnClickListener {
                    onItemClicked.invoke(position)
                }
            }
        }

        override fun getItemCount() = values.size

        private inner class ViewHolder(binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
            val name = binding.name
        }
    }

    class NewScheduleDialog : BaseDialogFragment() {

        override val width = ViewGroup.LayoutParams.MATCH_PARENT
        override val height = ViewGroup.LayoutParams.WRAP_CONTENT

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = Dialog(requireContext()).apply {
                setContentView(R.layout.dialog_new_schedule)
            }

            val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            val textInputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
            val textInputEditText = dialog.findViewById<TextInputEditText>(R.id.text_input_edit_text)
            textInputEditText.addTextChangedListener {
                textInputLayout.removeError()
            }

            val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

            btnSave.setOnClickListener {
                val name = textInputEditText.text.toString().trimStartAndEnd()

                if (name.isBlank()) {
                    textInputLayout.error = getString(R.string.schedule_name_empty)
                    return@setOnClickListener
                }

                setFragmentResultOnActivity(
                    "NewScheduleDialog/onSuccess",
                    bundleOf("newScheduleName" to name),
                )

                dialog.dismiss()
            }

            return dialog
        }
    }
}
