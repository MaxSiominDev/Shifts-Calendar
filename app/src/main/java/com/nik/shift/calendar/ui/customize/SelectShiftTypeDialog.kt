package com.nik.shift.calendar.ui.customize

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nik.shift.calendar.R
import com.nik.shift.calendar.databinding.ItemShiftTypeBinding
import com.nik.shift.calendar.ui.BaseDialogFragment
import com.nik.shift.calendar.util.setFragmentResultOnActivity

class SelectShiftTypeDialog : BaseDialogFragment() {

    override val width = ViewGroup.LayoutParams.MATCH_PARENT
    override val height = ViewGroup.LayoutParams.MATCH_PARENT

    private inline val shiftTypes get() = requireArguments().getStringArray(ARG_SHIFT_TYPES)!!
    private val recyclerViewAdapter by lazy {
        SelectShiftTypeDialogAdapter(shiftTypes.toList(), this::onItemClicked)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_select_shift_type)
        }

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        with (recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recyclerViewAdapter
        }

        val btnCancel = dialog.findViewById<MaterialButton>(R.id.button_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    private fun onItemClicked(position: Int) {
        val b = bundleOf("position" to position)
        setFragmentResultOnActivity("SelectShiftTypeDialog/onSuccess", b)
        dialog!!.dismiss()
    }

    companion object {

        @JvmStatic
        fun newInstance(shiftTypes: Array<String>): SelectShiftTypeDialog {
            return SelectShiftTypeDialog().apply {
                arguments = bundleOf(ARG_SHIFT_TYPES to shiftTypes)
            }
        }

        private const val ARG_SHIFT_TYPES = "shiftTypes"

    }

    private class SelectShiftTypeDialogAdapter(
        private val values: List<String>,
        private val onItemClicked: (Int) -> Unit,
    ) : RecyclerView.Adapter<SelectShiftTypeDialogAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                ItemShiftTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                shiftType.text = values[position]
                itemView.setOnClickListener {
                    onItemClicked.invoke(position)
                }
            }
        }

        override fun getItemCount() = values.size

        private inner class ViewHolder(binding: ItemShiftTypeBinding) : RecyclerView.ViewHolder(binding.root) {
            val shiftType = binding.shiftType
        }
    }

}
