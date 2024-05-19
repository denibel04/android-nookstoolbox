package com.example.animalcrossing.data.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.repository.Island
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseService @Inject constructor() {
    val db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // VILLAGERS
    suspend fun getAllVillagers(): List<VillagerDetail> {
        val villagers: MutableList<VillagerDetail> = mutableListOf()
            val villagerDocs = db.collection("villagers").get().await()
            for (document in villagerDocs) {
                val villagerListItemMap = document.data
                val villagerDetail = VillagerDetail(
                    villagerListItemMap["name"] as String,
                    villagerListItemMap["species"] as String,
                    villagerListItemMap["personality"] as String,
                    villagerListItemMap["image_url"] as String,
                    villagerListItemMap["gender"] as String,
                    villagerListItemMap["birthday_month"] as String,
                    (villagerListItemMap["birthday_day"] as Long).toInt()
                )
                villagers.add(villagerDetail)
            }
        return villagers
    }



    // ISLAND
    suspend fun getIsland(): IslandDetail {
        currentUser?.let { user ->
            val islandCollection = db.collection("users").document(user.uid).collection("island").get().await()
            val island = islandCollection.documents.firstOrNull()

            island?.let { document ->
                IslandDetail(
                    islandId = document.id,
                    name = document.getString("name") ?: "",
                    villagers = document.get("villagers") as? List<String> ?: emptyList()
                )
            }
        }
        return IslandDetail("", "", emptyList())
    }

    fun createIsland(name: String) {

        currentUser?.let { user ->
            val islandData = hashMapOf(
                "name" to name,
                "villagers" to emptyList<String>(),
            )

            db.collection("users").document(user.uid).collection("island")
                .add(islandData)
        }
    }

    suspend fun renameIsland(name: String) {

        currentUser?.let { user ->
                val islandCollection = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                if (!islandCollection.isEmpty) {
                    val islandDoc = islandCollection.documents[0]

                    islandDoc.reference.update("name", name)
                }
            }
        }



    suspend fun addVillagerToIsland(name: String) {

        currentUser?.let { user ->
                val island = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                    val islandDoc = island.documents[0]

                    val villager = db.collection("villagers")
                        .whereEqualTo("name", name)
                        .get()
                        .await()

                    if (!villager.isEmpty) {
                        val villagerDoc = villager.documents[0]

                        val villagerUid = villagerDoc.id

                        val islandUid = db.collection("users")
                            .document(user.uid)
                            .collection("island")
                            .document(islandDoc.id)

                        islandUid.update("villagers", FieldValue.arrayUnion(villagerUid))
                    }

            }
    }

    suspend fun deleteVillagerFromIsland(name: String) {

        currentUser?.let { user ->
                val island = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                    val islandDoc = island.documents[0]

                    val villager = db.collection("villagers")
                        .whereEqualTo("name", name)
                        .get()
                        .await()

                    if (!villager.isEmpty) {
                        val villagerDoc = villager.documents[0]

                        val villagerUid = villagerDoc.id

                        val islandUid = db.collection("users")
                            .document(user.uid)
                            .collection("island")
                            .document(islandDoc.id)

                        islandUid.update("villagers", FieldValue.arrayRemove(villagerUid))

                    }

            }
        }

    suspend fun deleteIsland() {
        currentUser?.let { user ->

                val islandCollection: QuerySnapshot = db.collection("users").document(user.uid)
                    .collection("island").get().await()

                for (document in islandCollection.documents) {
                    db.collection("users").document(user.uid)
                        .collection("island").document(document.id).delete().await()
                }
        }

    }


    // LOANS

    suspend fun createLoan(loan: LoansEntity): String {

        currentUser?.let { user ->
            val loanData = hashMapOf(
                "title" to loan.title,
                "type" to loan.type,
                "amountPaid" to loan.amountPaid,
                "amountTotal" to loan.amountTotal,
                "completed" to loan.completed
            )

            val islandCollection = db.collection("users")
                .document(user.uid)
                .collection("island")
                .get()
                .await()


                val islandDoc = islandCollection.documents[0]

            val newLoan = islandDoc.reference.collection("loans")
                .add(loanData).await()

            return newLoan.id
        }
        return ""
    }

    suspend fun editLoan(newLoan: Loan) {

        currentUser?.let { user ->
            val islandCollection = db.collection("users")
                .document(user.uid)
                .collection("island")
                .get()
                .await()

            if (!islandCollection.isEmpty) {
                val islandDoc = islandCollection.documents[0]

                val updatedLoanData = hashMapOf(
                    "title" to newLoan.title,
                    "type" to newLoan.type,
                    "amountPaid" to newLoan.amountPaid,
                    "amountTotal" to newLoan.amountTotal,
                    "completed" to newLoan.completed
                )

                islandDoc.reference.collection("loans")
                    .document(newLoan.firebaseId)
                    .update(updatedLoanData as Map<String, Any>)
                    .await()
            }
        }
    }

    suspend fun deleteLoan(firebaseId: String) {

        currentUser?.let { user ->
            val islandCollection = db.collection("users")
                .document(user.uid)
                .collection("island")
                .get()
                .await()

                val islandDoc = islandCollection.documents[0]
                val loanRef = islandDoc.reference
                    .collection("loans")
                    .document(firebaseId)

                loanRef.delete().await()

        }
    }


    // AUTH
    suspend fun getCurrentUser(): Flow<User?> = callbackFlow {

        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)

            val listener = userDocRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    trySend(user).isSuccess
                } else {
                    trySend(null).isSuccess
                }
            }

            awaitClose { listener.remove() }
        } else {
            close(IllegalStateException("No user is currently signed in."))
        }
    }

    suspend fun getUsers(): List<User> {
        val users: MutableList<User> = mutableListOf()
        val userCollection = db.collection("users").get().await()
        for (document in userCollection) {
            val userData = document.data
            val user = User(
                document.id,
                null,
                userData["username"] as? String ?: "",
                userData["profile_picture"] as? String ?: "",
                userData["followers"] as? List<String>,
                userData["following"] as? List<String>
            )
            users.add(user)
        }
        return users
    }

    suspend fun followUser(followedUid: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            val followedUserRef = db.collection("users").document(followedUid)

            currentUserRef.update("following", FieldValue.arrayUnion(followedUid)).await()

            followedUserRef.update("followers", FieldValue.arrayUnion(currentUser.uid)).await()
        }
    }

    suspend fun unfollowUser(followedUid: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            val followedUserRef = db.collection("users").document(followedUid)

            currentUserRef.update("following", FieldValue.arrayRemove(followedUid)).await()

            followedUserRef.update("followers", FieldValue.arrayRemove(currentUser.uid)).await()
        }
    }

}