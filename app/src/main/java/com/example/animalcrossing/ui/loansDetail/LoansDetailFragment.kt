package com.example.animalcrossing.ui.loansDetail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.databinding.FragmentLoansDetailBinding
import com.example.animalcrossing.databinding.LoanDialogBinding
import com.google.android.material.checkbox.MaterialCheckBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoansDetailFragment : Fragment() {

    private lateinit var binding: FragmentLoansDetailBinding
    private val viewModel: LoansDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoansDetailBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.island_title)
        super.onViewCreated(view, savedInstanceState)
        val adapter = LoansDetailAdapter(requireContext(), onLoanClicked = { loan ->
        onCreateDialog(loan).show()
        }, onLoanDeleteClicked = { loanId ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (loanId != null) {
                    viewModel.deleteLoan(loanId)
                }
            }
        })
        binding.createLoan.setOnClickListener {
            onCreateDialog(null).show()
        }

        val rv = binding.loanList
        rv.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.loans)
                }
            }
        }
    }

    fun onCreateDialog(loanToEdit: Loan?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val binding: LoanDialogBinding = LoanDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = binding.root

        val titleEdit = binding.title
        val typeSpinner = binding.typeSpinner
        val amountPaidEdit = binding.amountPaid
        val amountTotalEdit = binding.amountTotal
        val completedCheckBox = binding.completedStatus

        val types = arrayOf("Puente", "Escalera", "Casa")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        if (loanToEdit != null) {
            binding.titleTextView.visibility = View.GONE
            titleEdit.visibility = View.GONE
            typeSpinner.visibility = View.GONE
            binding.amountTotalTextView.visibility = View.GONE
            amountTotalEdit.visibility = View.GONE

            amountPaidEdit.setText(loanToEdit.amountPaid.toString())
            completedCheckBox.isChecked = loanToEdit.completed
        }

        builder.setView(dialogView)
            .setPositiveButton("okay") { dialog, id ->
                val title = titleEdit.text.toString()
                val type = types[typeSpinner.selectedItemPosition]
                val amountPaidText = amountPaidEdit.text.toString()
                val amountTotalText = amountTotalEdit.text.toString()
                val completed = completedCheckBox.isChecked



                val amountPaid = amountPaidText.toIntOrNull() ?: 0
                val amountTotal = amountTotalText.toIntOrNull() ?: 0
                viewLifecycleOwner.lifecycleScope.launch {
                    if (loanToEdit != null) {
                        // UPDATE LOAN
                        val loanEdited = Loan(loanToEdit.loanId, loanToEdit.title, loanToEdit.type, amountPaid, loanToEdit.amountTotal, completed)
                        viewModel.editLoan(loanEdited)
                    } else {
                        viewModel.addLoan(title, type, amountPaid, amountTotal, completed)
                    }
                }
            }
            .setNegativeButton("no") { dialog, id ->
                dialog.cancel()
            }
        return builder.create()
    }

}