package com.example.animalcrossing.ui.loansDetail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.databinding.LoansListItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

/**
 * Adapter for displaying a list of loans in a RecyclerView.
 *
 * @property context Context of the application or activity.
 * @property onLoanSlider Callback function for handling changes in loan repayment amounts.
 * @property onLoanEditClicked Callback function for handling edit button clicks.
 * @property onLoanDeleteClicked Callback function for handling delete button clicks.
 */
class LoansDetailAdapter(
    private val context: Context,
    private val onLoanSlider: ((Loan) -> Unit)? = null,
    private val onLoanEditClicked: ((Loan) -> Unit)? = null,
    private val onLoanDeleteClicked: ((String?) -> Unit)? = null
) : ListAdapter<Loan, LoansDetailAdapter.LoansViewHolder>(LoansDiffCallback) {

    /**
     * ViewHolder for displaying each loan item in the RecyclerView.
     *
     * @param binding View binding object for the loan item layout.
     */
    inner class LoansViewHolder(val binding: LoansListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var originalValue: Float = 0.0f
        private var isTrackingTouch: Boolean = false

        init {
            binding.loanSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    isTrackingTouch = true
                    originalValue = slider.value
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    if (isTrackingTouch) {
                        val loan = getItem(adapterPosition)
                        if (slider.value.toInt() == loan.amountTotal) {
                            showUpdateAlert(loan, slider, true)
                        } else {
                            showUpdateAlert(loan, slider, false)
                        }
                        isTrackingTouch = false
                    }
                }
            })

            // Set click listeners for edit and delete buttons
            binding.editLoan.setOnClickListener {
                onLoanEditClicked?.invoke(getItem(adapterPosition))
            }

            binding.deleteLoan.setOnClickListener {
                onLoanDeleteClicked?.invoke(getItem(adapterPosition).firebaseId)
            }
        }

        /**
         * Binds loan data to the ViewHolder.
         *
         * @param loan Loan object to bind.
         */
        fun bind(loan: Loan) {
            binding.titleLoan.text = loan.title
            binding.typeLoan.text = when (loan.type) {
                "bridge" -> context.getString(R.string.bridge)
                "stairs" -> context.getString(R.string.stairs)
                "house" -> context.getString(R.string.house)
                else -> loan.type
            }
            binding.loanSlider.valueFrom = 0.0f
            binding.loanSlider.valueTo = loan.amountTotal.toFloat()
            binding.loanSlider.value = loan.amountPaid.toFloat()
            originalValue = loan.amountPaid.toFloat()

            binding.debtProgress.text = context.getString(
                R.string.debt_progress, loan.amountPaid, context.getString(R.string.bayas), loan.amountTotal, context.getString(R.string.bayas))

            if (loan.completed) {
                binding.loanSlider.isEnabled = false
                binding.editLoan.visibility = View.GONE
            } else {
                binding.loanSlider.isEnabled = true
                binding.editLoan.visibility = View.VISIBLE
            }
        }

        /**
         * Displays an alert dialog for confirming loan updates.
         *
         * @param loan Loan object to update.
         * @param slider Slider representing the loan repayment progress.
         * @param complete True if the loan is marked as complete; false otherwise.
         */
        private fun showUpdateAlert(loan: Loan, slider: Slider, complete: Boolean) {
            val title = if (complete) context.getString(R.string.title_complete_debt) else context.getString(R.string.title_update_debt)
            val message = if (complete) context.getString(R.string.message_complete_debt) else context.getString(R.string.message_update_debt, slider.value.toInt())

            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.accept)) { _, _ ->
                    loan.amountPaid = slider.value.toInt()
                    if (complete) {
                        loan.completed = true
                    }
                    onLoanSlider?.invoke(loan)
                }
                .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                    slider.value = originalValue
                }
                .show()
        }
    }

    /**
     * DiffUtil callback for calculating the difference between two lists of loans.
     */
    private object LoansDiffCallback : DiffUtil.ItemCallback<Loan>() {
        override fun areItemsTheSame(oldItem: Loan, newItem: Loan) =
            oldItem.title == newItem.title

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Loan, newItem: Loan) = oldItem == newItem
    }

    /**
     * Creates a ViewHolder for displaying loan items.
     *
     * @param parent ViewGroup into which the new View will be added after it is bound.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoansViewHolder {
        val binding = LoansListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoansViewHolder(binding)
    }

    /**
     * Binds loan data to the ViewHolder at the specified position.
     *
     * @param holder ViewHolder to bind the data.
     * @param position Position of the item in the adapter's data set.
     */
    override fun onBindViewHolder(holder: LoansViewHolder, position: Int) {
        val loan = getItem(position)
        holder.bind(loan)
    }
}