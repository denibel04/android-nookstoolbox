package com.example.animalcrossing.ui.loansDetail

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.databinding.FragmentLoansDetailBinding
import com.example.animalcrossing.databinding.LoanDialogBinding
import com.example.animalcrossing.databinding.LoanEditDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Fragment for managing loan details including creation, editing,
 * deletion, and listing based on completion status.
 */
@AndroidEntryPoint
class LoansDetailFragment : Fragment() {

    private lateinit var binding: FragmentLoansDetailBinding
    private val viewModel: LoansDetailViewModel by viewModels()

    /**
     * Creates the view hierarchy of the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoansDetailBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    /**
     * Called immediately after onCreateView() has returned, and fragment's view hierarchy has been created.
     *
     * @param view The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.my_loans)
        super.onViewCreated(view, savedInstanceState)

        val adapter = LoansDetailAdapter(requireContext(),
            onLoanSlider = { loan ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.editLoan(loan)
                }
            },
            onLoanEditClicked = { loan ->
                onCreateDialog(loan).show()
            },
            onLoanDeleteClicked = { firebaseId ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (firebaseId != null) {
                        showDeleteConfirmationDialog(firebaseId)
                    }
                }
            })

        binding.createLoan.setOnClickListener {
            onCreateDialog(null).show()
        }

        val rv = binding.loanList
        rv.adapter = adapter

        setupTabs(adapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.islandExists) {
                        binding.createLoan.visibility = View.VISIBLE
                        binding.loanList.visibility = View.VISIBLE
                        binding.noIslandText.visibility = View.GONE
                        adapter.submitList(uiState.loans)
                    } else {
                        binding.createLoan.visibility = View.GONE
                        binding.loanList.visibility = View.GONE
                        binding.noIslandText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /**
     * Creates a dialog for loan creation or editing.
     *
     * @param loanToEdit Loan object to edit (null if creating a new loan).
     * @return Created dialog instance.
     */
    private fun onCreateDialog(loanToEdit: Loan?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView: View

        if (loanToEdit != null) {
            val editBinding: LoanEditDialogBinding = LoanEditDialogBinding.inflate(LayoutInflater.from(requireContext()))
            dialogView = editBinding.root

            val amountPaidEdit = editBinding.amountPaid
            val completedCheckBox = editBinding.completedStatus

            amountPaidEdit.setText(loanToEdit.amountPaid.toString())
            completedCheckBox.isChecked = loanToEdit.completed

            builder.setView(dialogView)
                .setPositiveButton(getString(R.string.accept)) { _, _ ->
                    val amountPaidText = amountPaidEdit.text.toString()
                    var completed = completedCheckBox.isChecked

                    if (amountPaidText.isEmpty() && !completed) {
                        Toast.makeText(requireContext(), getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                    } else {
                        var amountPaid = amountPaidText.toIntOrNull() ?: 0
                        val amountTotal = loanToEdit.amountTotal
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (amountPaid >= amountTotal || completed) {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.title_complete_debt))
                                    .setMessage(getString(R.string.message_complete_debt))
                                    .setPositiveButton(getString(R.string.accept)) { _, _ ->
                                        amountPaid = amountTotal
                                        completed = true
                                        processLoanAction(loanToEdit, loanToEdit.title, loanToEdit.type, amountPaid, amountTotal, completed)
                                    }
                                    .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                    }
                                    .show()
                            } else {
                                processLoanAction(loanToEdit, loanToEdit.title, loanToEdit.type, amountPaid, amountTotal, completed)
                            }
                        }
                    }
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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
                .setPositiveButton(getString(R.string.accept)) { _, _ ->
                    val title = titleEdit.text.toString()
                    val selectedType = types[typeSpinner.selectedItemPosition]
                    val amountPaidText = amountPaidEdit.text.toString()
                    val amountTotalText = amountTotalEdit.text.toString()
                    var completed = completedCheckBox.isChecked

                    if (title.isEmpty() || amountPaidText.isEmpty() || amountTotalText.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                    } else {
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
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.title_complete_debt))
                                    .setMessage(getString(R.string.message_complete_debt))
                                    .setPositiveButton(getString(R.string.accept)) { _, _ ->
                                        completed = true
                                        amountPaid = amountTotal
                                        processLoanAction(null, title, type, amountPaid, amountTotal, completed)
                                    }
                                    .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                    }
                                    .show()
                            } else {
                                processLoanAction(null, title, type, amountPaid, amountTotal, completed)
                            }
                        }
                    }
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }
        }

        return builder.create()
    }

    /**
     * Shows a delete confirmation dialog for loans.
     *
     * @param firebaseId Firebase ID of the loan to delete.
     */
    private fun showDeleteConfirmationDialog(firebaseId: String?) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_loan_title))
            .setMessage(getString(R.string.confirm_delete_loan_message))
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (!firebaseId.isNullOrBlank()) {
                        viewModel.deleteLoan(firebaseId)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Processes the action for editing or creating a loan.
     *
     * @param loanToEdit Loan object to edit (null if creating a new loan).
     * @param title Title of the loan.
     * @param type Type of the loan.
     * @param amountPaid Amount paid towards the loan.
     * @param amountTotal Total amount of the loan.
     * @param completed True if the loan is completed, false otherwise.
     */
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
                if (isOnline()) {
                    Toast.makeText(requireContext(), getString(R.string.loan_updated_toast), Toast.LENGTH_SHORT).show()
                }
            } else {
                viewModel.addLoan(title, type, amountPaid, amountTotal, completed)
                if (isOnline()) {
                    Toast.makeText(requireContext(), getString(R.string.loan_created_toast), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Sets up tabs for displaying uncompleted and completed loans using a TabLayout.
     *
     * @param adapter Adapter used to populate data in the RecyclerView based on tab selection.
     */
    private fun setupTabs(adapter: LoansDetailAdapter) {
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.uncompleted_loans)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.completed_loans)))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewLifecycleOwner.lifecycleScope.launch {

                    when (tab.position) {
                        0 -> {
                            viewModel.uiState.collect { uiState ->
                                adapter.submitList(uiState.loans)
                            }
                        }

                        1 -> {
                            viewModel.uiState.collect { uiState ->
                                adapter.submitList(uiState.completedLoans)
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    /**
     * Checks if the device is connected to the internet.
     *
     * @return true if the device is connected to the internet, false otherwise.
     */
    private fun isOnline(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


}