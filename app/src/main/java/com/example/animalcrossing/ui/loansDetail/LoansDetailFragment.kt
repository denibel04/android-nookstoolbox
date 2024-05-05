package com.example.animalcrossing.ui.loansDetail

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentIslandDetailBinding
import com.example.animalcrossing.databinding.FragmentLoansDetailBinding
import com.example.animalcrossing.ui.islandDetail.IslandDetailAdapter
import com.example.animalcrossing.ui.islandDetail.IslandDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

        binding.createLoan.setOnClickListener {
            onCreateDialog(null).show()
        }
    }

    fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater

        val dialogView = inflater.inflate(R.layout.loan_dialog, null)
        builder.setView(dialogView)
            .setPositiveButton("okay") { dialog, id ->
                val title = dialogView.findViewById<EditText>(R.id.title).text.toString()
                val type = dialogView.findViewById<EditText>(R.id.type).text.toString()
                val amountPaidText = dialogView.findViewById<EditText>(R.id.amountPaid).text.toString()
                val amountTotalText = dialogView.findViewById<EditText>(R.id.amountTotal).text.toString()

                val amountPaid = amountPaidText.toIntOrNull() ?: 0
                val amountTotal = amountTotalText.toIntOrNull() ?: 0

            viewModel.addLoan(title, type, amountPaid, amountTotal)
                }
            .setNegativeButton("no") { dialog, id ->
                dialog.cancel()
            }
        return builder.create()
    }

}