package com.example.animalcrossing.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.animalcrossing.R
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.db.LoansDBRepository
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.db.asLoan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing loans, integrating local database operations
 * with remote API interactions.
 *
 * This repository handles operations related to loans:
 * - Fetching all loans from local database.
 * - Adding a new loan locally and remotely if online.
 * - Deleting a loan locally and remotely if online.
 * - Updating a loan locally and remotely if online.
 *
 * @property dbRepository The repository for accessing local database operations.
 * @property apiRepository The repository for accessing remote Firebase API operations.
 * @property context The application context for accessing system services like connectivity.
 */
@Singleton
class LoanRepository @Inject constructor(
    private val dbRepository: LoansDBRepository,
    private val apiRepository: AcnhFirebaseRepository,
    private val context: Context
) {
    val db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    private var loansListenerRegistration: ListenerRegistration? = null

    init {
        setupLoansListener()
    }

    /**
     * Flow representing all loans fetched from the local database.
     * Maps the [LoansEntity] to [Loan] for external usage.
     */
    val loans: Flow<List<Loan>>
        get() {
            return dbRepository.allLoans.map { it.asLoan() }
        }

    private fun setupLoansListener() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val islandRef = db.collection("users").document(user.uid).collection("island")

            islandRef.get().addOnSuccessListener { document ->
                val island = document.documents.firstOrNull()
                island?.reference?.collection("loans")?.let { loansCollection ->
                    loansListenerRegistration = loansCollection.addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }

                        snapshots?.let {
                            for (documentChange in snapshots.documentChanges) {
                                val loanDocument = documentChange.document
                                val loan = LoansEntity(
                                    firebaseId = loanDocument.id,
                                    title = loanDocument.getString("title") ?: "",
                                    type = loanDocument.getString("type") ?: "",
                                    amountPaid = loanDocument.getLong("amountPaid")?.toInt() ?: 0,
                                    amountTotal = loanDocument.getLong("amountTotal")?.toInt() ?: 0,
                                    completed = loanDocument.getBoolean("completed") ?: false
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    when (documentChange.type) {
                                        DocumentChange.Type.ADDED -> dbRepository.insert(loan)
                                        DocumentChange.Type.MODIFIED -> dbRepository.updateLoan(Loan(loan.firebaseId, loan.title, loan.type, loan.amountPaid, loan.amountTotal, loan.completed))
                                        DocumentChange.Type.REMOVED -> dbRepository.deleteLoan(loan.firebaseId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a new loan locally and remotely if online.
     *
     * @param title The title of the loan.
     * @param type The type of the loan.
     * @param amountPaid The amount paid towards the loan.
     * @param amountTotal The total amount of the loan.
     * @param completed Indicates if the loan is completed.
     * @return The Firebase ID of the newly created loan.
     */
    suspend fun addLoan(
        title: String,
        type: String,
        amountPaid: Int,
        amountTotal: Int,
        completed: Boolean
    ): Long {
        return if (isOnline()) {
            val newLoan = LoansEntity(
                firebaseId = "",
                title = title,
                type = type,
                amountPaid = amountPaid,
                amountTotal = amountTotal,
                completed = completed
            )
            val firebaseId = apiRepository.createLoan(newLoan)
            newLoan.firebaseId = firebaseId
            dbRepository.insert(newLoan)
        } else {
            showNoInternetToast()
            -1
        }
    }

    /**
     * Deletes a loan locally and remotely if online.
     *
     * @param firebaseId The Firebase ID of the loan to delete.
     */
    suspend fun deleteLoan(firebaseId: String) {
        if (isOnline()) {
            apiRepository.deleteLoan(firebaseId)
            dbRepository.deleteLoan(firebaseId)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Updates a loan locally and remotely if online.
     *
     * @param loan The updated [Loan] object.
     */
    suspend fun updateLoan(loan: Loan) {
        if (isOnline()) {
            apiRepository.editLoan(loan)
            dbRepository.updateLoan(loan)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Checks if the device is currently online.
     *
     * @return True if the device has internet connectivity, false otherwise.
     */
    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Displays a toast message indicating no internet connection.
     */
    private fun showNoInternetToast() {
        Toast.makeText(
            context,
            context.getString(R.string.no_internet_connection),
            Toast.LENGTH_SHORT
        ).show()
    }
}