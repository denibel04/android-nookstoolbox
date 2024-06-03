package com.example.animalcrossing.ui.loansDetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.LoansListItemBinding
import com.example.animalcrossing.databinding.VillagerListItemBinding
import com.example.animalcrossing.ui.islandDetail.IslandDetailAdapter

class LoansCompletedAdapter(
    private val context: Context,
    private val onLoanDeleteClicked: ((String?) -> Unit)? = null
) : ListAdapter<Loan, LoansCompletedAdapter.LoansViewHolder>(LoansCompletedAdapter.LoansDiffCallback) {
    inner class LoansViewHolder(val binding: LoansListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(l: Loan) {
            binding.titleLoan.text = l.title

            binding.loanSlider.valueFrom = 0.0f
            binding.loanSlider.valueTo = l.amountTotal.toFloat()
            binding.loanSlider.value = l.amountTotal.toFloat()
            binding.loanSlider.isEnabled = false

            binding.editLoan.visibility = View.GONE
            binding.debtProgress.text = l.amountPaid.toString()+" bayas / "+l.amountTotal.toString()+" bayas"

        }
    }

    private object LoansDiffCallback : DiffUtil.ItemCallback<Loan>() {
        override fun areItemsTheSame(oldItem: Loan, newItem: Loan) =
            oldItem.title == newItem.title

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