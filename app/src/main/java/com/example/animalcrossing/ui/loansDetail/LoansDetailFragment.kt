package com.example.animalcrossing.ui.loansDetail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.databinding.FragmentLoansDetailBinding
import com.example.animalcrossing.databinding.LoanDialogBinding
import com.example.animalcrossing.databinding.LoanEditDialogBinding
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.tabs.TabLayout
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
        setupTabs()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "My Loans"
        super.onViewCreated(view, savedInstanceState)
        val adapter = LoansDetailAdapter(requireContext(),
        onLoanSlider = {
                       loan ->
                       viewLifecycleOwner.lifecycleScope.launch {
                           viewModel.editLoan(loan)
                       }
        },
            onLoanEditClicked = { loan ->
        onCreateDialog(loan).show()
        }, onLoanDeleteClicked = { firebaseId ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (firebaseId != null) {
                    viewModel.deleteLoan(firebaseId)
                }
            }})


        val completedAdapter = LoansCompletedAdapter(requireContext()) { firebaseId ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (firebaseId != null) {
                    viewModel.deleteLoan(firebaseId)
                }
            }
        }

        binding.createLoan.setOnClickListener {
            onCreateDialog(null).show()
        }

        val rv = binding.loanList
        rv.adapter = adapter

        val rvCompleted = binding.completedLoanList
        rvCompleted.adapter = completedAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {uiState ->
                    if (uiState.islandExists) {
                        binding.createLoan.visibility = View.VISIBLE
                        binding.loanList.visibility = View.VISIBLE
                        binding.noIslandText.visibility = View.GONE
                        adapter.submitList(uiState.loans)
                        completedAdapter.submitList(uiState.completedLoans)
                    } else {
                        binding.createLoan.visibility = View.GONE
                        binding.loanList.visibility = View.GONE
                        binding.noIslandText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun onCreateDialog(loanToEdit: Loan?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val dialogView: View

        if (loanToEdit != null) {
            val editBinding: LoanEditDialogBinding = LoanEditDialogBinding.inflate(LayoutInflater.from(requireContext()))
            dialogView = editBinding.root

            val amountPaidEdit = editBinding.amountPaid
            val completedCheckBox = editBinding.completedStatus

            amountPaidEdit.setText(loanToEdit.amountPaid.toString())
            completedCheckBox.isChecked = loanToEdit.completed

            builder.setView(dialogView)
                .setPositiveButton("okay") { dialog, id ->
                    val amountPaidText = amountPaidEdit.text.toString()
                    var completed = completedCheckBox.isChecked

                    var amountPaid = amountPaidText.toIntOrNull() ?: 0
                    val amountTotal = loanToEdit.amountTotal
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (amountPaid >= amountTotal || completed) {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Complete Debt")
                                .setMessage("Do you want to mark the debt as completed?")
                                .setPositiveButton("Yes") { _, _ ->
                                    completed = true
                                    amountPaid = amountTotal
                                    processLoanAction(loanToEdit, loanToEdit.title, loanToEdit.type, amountPaid, amountTotal, completed)
                                }
                                .setNegativeButton("No") { _, _ ->
                                }
                                .show()
                        } else {
                            processLoanAction(loanToEdit, loanToEdit.title, loanToEdit.type, amountPaid, amountTotal, completed)
                        }
                    }
                }
                .setNegativeButton("no") { dialog, id ->
                    dialog.cancel()
                }

        } else {
            val addBinding: LoanDialogBinding = LoanDialogBinding.inflate(LayoutInflater.from(requireContext()))
            dialogView = addBinding.root

            val titleEdit = addBinding.title
            val typeSpinner = addBinding.typeSpinner
            val amountPaidEdit = addBinding.amountPaid
            val amountTotalEdit = addBinding.amountTotal
            val completedCheckBox = addBinding.completedStatus

            val types = arrayOf(
                getString(R.string.bridge),
                getString(R.string.stairs),
                getString(R.string.house)
            )
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = adapter

            builder.setView(dialogView)
                .setPositiveButton("okay") { dialog, id ->
                    val title = titleEdit.text.toString()
                    val selectedType = types[typeSpinner.selectedItemPosition]
                    val amountPaidText = amountPaidEdit.text.toString()
                    val amountTotalText = amountTotalEdit.text.toString()
                    var completed = completedCheckBox.isChecked

                    val typeMap = mapOf(
                        getString(R.string.bridge) to "bridge",
                        getString(R.string.stairs) to "stairs",
                        getString(R.string.house) to "house"
                    )

                    val type = typeMap[selectedType] ?: "bridge"

                    var amountPaid = amountPaidText.toIntOrNull() ?: 0
                    val amountTotal = amountTotalText.toIntOrNull() ?: 0
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (amountPaid >= amountTotal || completed) {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Complete Debt")
                                .setMessage("Do you want to mark the debt as completed?")
                                .setPositiveButton("Yes") { _, _ ->
                                    completed = true
                                    amountPaid = amountTotal
                                    processLoanAction(null, title, type, amountPaid, amountTotal, completed)
                                }
                                .setNegativeButton("No") { _, _ ->
                                }
                                .show()
                        } else {
                            processLoanAction(null, title, type, amountPaid, amountTotal, completed)
                        }
                    }
                }
                .setNegativeButton("no") { dialog, id ->
                    dialog.cancel()
                }
        }

        return builder.create()
    }

    private fun processLoanAction(loanToEdit: Loan?, title: String, type: String, amountPaid: Int, amountTotal: Int, completed: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (loanToEdit != null) {
                val loanEdited = Loan(
                    loanToEdit.firebaseId,
                    loanToEdit.title,
                    loanToEdit.type,
                    amountPaid,
                    loanToEdit.amountTotal,
                    completed
                )
                viewModel.editLoan(loanEdited)
            } else {
                viewModel.addLoan(title, type, amountPaid, amountTotal, completed)
            }
        }
    }

    private fun setupTabs() {
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Sin Completar"))
        tabLayout.addTab(tabLayout.newTab().setText("Completadas"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        binding.loanList.visibility = View.VISIBLE
                        binding.completedLoanList.visibility = View.GONE
                    }
                    1 -> {
                        binding.loanList.visibility = View.GONE
                        binding.completedLoanList.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

}