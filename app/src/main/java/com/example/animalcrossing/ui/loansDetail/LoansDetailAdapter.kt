package com.example.animalcrossing.ui.loansDetail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.databinding.LoansListItemBinding
import com.google.android.material.slider.Slider

class LoansDetailAdapter(
    private val context: Context,
    private val onLoanSlider: ((Loan) -> Unit)? = null,
    private val onLoanEditClicked: ((Loan) -> Unit)? = null,
    private val onLoanDeleteClicked: ((String?) -> Unit)? = null
) : ListAdapter<Loan, LoansDetailAdapter.LoansViewHolder>(LoansDiffCallback) {
    inner class LoansViewHolder(val binding: LoansListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var originalValue: Float = 0.0f
        private var isTrackingTouch: Boolean = false


        fun bind(l: Loan) {
            binding.titleLoan.text = l.title
            binding.typeLoan.text = l.type
            binding.loanSlider.valueFrom = 0.0f
            binding.loanSlider.valueTo = l.amountTotal.toFloat()
            binding.loanSlider.value = l.amountPaid.toFloat()
            originalValue = l.amountPaid.toFloat()

            binding.debtProgress.text = context.getString(
                R.string.debt_progress, l.amountPaid, context.getString(
                    R.string.bayas), l.amountTotal, context.getString(R.string.bayas))

            binding.loanSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    isTrackingTouch = true
                    originalValue = l.amountPaid.toFloat()
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    if (isTrackingTouch) {
                        if (slider.value.toInt() == l.amountTotal) {
                            showUpdateAlert(l, slider, true)
                        } else {
                            showUpdateAlert(l, slider, false)
                        }
                        isTrackingTouch = false
                    }
                }
            })

        }

        private fun showUpdateAlert(loan: Loan, slider: Slider, complete: Boolean) {
            val title = if (complete) context.getString(R.string.title_complete_debt) else context.getString(R.string.title_update_debt)
            val message = if (complete) context.getString(R.string.message_complete_debt) else context.getString(R.string.message_update_debt, slider.value.toInt())

            AlertDialog.Builder(context)
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




    private object LoansDiffCallback : DiffUtil.ItemCallback<Loan>() {
        override fun areItemsTheSame(oldItem: Loan, newItem: Loan) =
            oldItem.title == newItem.title

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Loan, newItem: Loan) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoansViewHolder =
        LoansViewHolder(
            LoansListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: LoansViewHolder, position: Int) {
        val loan = getItem(position)
        holder.bind(loan)
        holder.binding.editLoan.setOnClickListener {
            onLoanEditClicked?.invoke(loan)
        }
        holder.binding.deleteLoan.setOnClickListener {
            onLoanDeleteClicked?.invoke(loan.firebaseId)
        }

    }

}