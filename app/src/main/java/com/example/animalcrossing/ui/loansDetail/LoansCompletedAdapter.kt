package com.example.animalcrossing.ui.loansDetail

import android.annotation.SuppressLint
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

class LoansCompletedAdapter(
    private val context: Context,
    private val onLoanDeleteClicked: ((String?) -> Unit)? = null
) : ListAdapter<Loan, LoansCompletedAdapter.LoansViewHolder>(LoansDiffCallback) {
    inner class LoansViewHolder(val binding: LoansListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(l: Loan) {
            binding.titleLoan.text = l.title
            binding.typeLoan.text = l.type

            binding.loanSlider.valueFrom = 0.0f
            binding.loanSlider.valueTo = l.amountTotal.toFloat()
            binding.loanSlider.value = l.amountTotal.toFloat()
            binding.loanSlider.isEnabled = false

            binding.editLoan.visibility = View.GONE
            binding.debtProgress.text = context.getString(R.string.debt_progress, l.amountPaid, context.getString(R.string.bayas), l.amountTotal, context.getString(R.string.bayas))

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
        holder.binding.deleteLoan.setOnClickListener {
            onLoanDeleteClicked?.invoke(loan.firebaseId)
        }

    }

}