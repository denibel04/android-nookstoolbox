package com.example.animalcrossing.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.db.LoansDBRepository
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.db.asLoan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val dbRepository: LoansDBRepository,
    private val apiRepository: AcnhFirebaseRepository,
    private val context: Context
) {

    val loans: Flow<List<Loan>>
        get() {
            return dbRepository.allLoans.map { it.asLoan() }
        }

    suspend fun addLoan(title: String, type: String, amountPaid: Int, amountTotal: Int, completed: Boolean): Long {
        return if (isOnline()) {
            val newLoan = LoansEntity(firebaseId = "", title = title, type = type, amountPaid = amountPaid, amountTotal = amountTotal, completed = completed)
            val firebaseId = apiRepository.createLoan(newLoan)
            newLoan.firebaseId = firebaseId
            dbRepository.insert(newLoan)
        } else {
            showNoInternetToast()
            -1
        }
    }


    suspend fun deleteLoan(firebaseId: String) {
        if (isOnline()) {
            apiRepository.deleteLoan(firebaseId)
            dbRepository.deleteLoan(firebaseId)
        } else {
            showNoInternetToast()
        }
    }

    suspend fun updateLoan(loan: Loan) {
        if (isOnline()) {
            apiRepository.editLoan(loan)
            dbRepository.updateLoan(loan)
        } else {
            showNoInternetToast()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetToast() {
        Toast.makeText(
            context,
            "No hay conexión a Internet. Por favor, comprueba tu conexión e inténtalo de nuevo.",
            Toast.LENGTH_SHORT
        ).show()
    }
}