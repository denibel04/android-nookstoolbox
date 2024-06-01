package com.example.animalcrossing.data.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.db.ProfileEntity
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
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val islandCollection = db.collection("users").document(user.uid).collection("island").get().await()
            val island = islandCollection.documents.firstOrNull()

            island?.let { document ->
                val islandDetail = IslandDetail(
                    islandId = document.id,
                    name = document.getString("name") ?: "",
                    hemisphere = document.getString("hemisphere") ?: "",
                    villagers = document.get("villagers") as? List<String> ?: emptyList()
                )
                return islandDetail
            }
        }
        return IslandDetail("", "", "", emptyList())
    }

    fun createIsland(name: String, hemisphere: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val islandData = hashMapOf(
                "name" to name,
                "hemisphere" to hemisphere,
                "villagers" to emptyList<String>(),
            )

            db.collection("users").document(user.uid).collection("island")
                .add(islandData)
        }
    }

    suspend fun renameIsland(name: String) {
        val currentUser = auth.currentUser
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
        val currentUser = auth.currentUser
        currentUser?.let { user ->
                val island = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                    val islandDoc = island.documents[0]

                        val islandUid = db.collection("users")
                            .document(user.uid)
                            .collection("island")
                            .document(islandDoc.id)

                        islandUid.update("villagers", FieldValue.arrayUnion(name))


            }
    }

    suspend fun deleteVillagerFromIsland(name: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
                val island = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                    val islandDoc = island.documents[0]

                        val islandUid = db.collection("users")
                            .document(user.uid)
                            .collection("island")
                            .document(islandDoc.id)

                        islandUid.update("villagers", FieldValue.arrayRemove(name))



            }
        }

    suspend fun deleteIsland() {
        val currentUser = auth.currentUser
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

    suspend fun getLoans(): List<LoansEntity> {
        val currentUser = auth.currentUser

        val loansList = mutableListOf<LoansEntity>()

        if (currentUser != null) {
            val document =
                db.collection("users").document(currentUser.uid).collection("island").get().await()
            val island = document.documents.firstOrNull()

            val loansDocuments = island?.reference?.collection("loans")?.get()?.await()

            loansDocuments?.forEach { loanDocument ->
                val loan = LoansEntity(
                    firebaseId = loanDocument.id,
                    title = loanDocument.getString("title") ?: "",
                    type = loanDocument.getString("type") ?: "",
                    amountPaid = loanDocument.getLong("amountPaid")?.toInt() ?: 0,
                    amountTotal = loanDocument.getLong("amountTotal")?.toInt() ?: 0,
                    completed = loanDocument.getBoolean("completed") ?: false
                )
                loansList.add(loan)
            }
        }
        return loansList
    }

    suspend fun createLoan(loan: LoansEntity): String {
        val currentUser = auth.currentUser

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
        val currentUser = auth.currentUser

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
        val currentUser = auth.currentUser

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
    suspend fun getCurrentUser(): UserDetail {
        val currentUser = auth.currentUser
        var profile = UserDetail("", "", "", "", "", emptyList(), emptyList())
         if (currentUser != null) {
             val userDoc = db.collection("users").document(currentUser.uid).get().await()
             val followers = userDoc.get("followers") as? List<String> ?: emptyList()
             val following = userDoc.get("following") as? List<String> ?: emptyList()
             profile = UserDetail(
                    userDoc.id,
                 "",
                 userDoc.getString("username") ?: "",
                 userDoc.getString("profile_picture") ?: "",
                    userDoc.getString("dream_code") ?: "",
                    followers,
                    following
                )

            }
        return profile
        }


    suspend fun getFriends(): List<UserDetail> {
        val currentUser = auth.currentUser
        val users = mutableListOf<UserDetail>()

        if (currentUser != null) {
            val document = db.collection("users").document(currentUser.uid).get().await()
            val followers = document.get("followers") as? List<String> ?: emptyList()
            val following = document.get("following") as? List<String> ?: emptyList()

            val friendsUids = followers.intersect(following).toList()

            for (uid in friendsUids) {
                val document = db.collection("users").document(uid).get().await()
                val user = document.toObject(UserDetail::class.java)
                if (user != null) {
                    users.add(user)
                }
            }
        }

        return users
    }

    suspend fun changeUsername(newUsername: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            currentUserRef.update("username", newUsername)
        }
    }

    suspend fun changeDreamCode(newDreamcode: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            currentUserRef.update("dream_code", newDreamcode)
        }
    }

    suspend fun getUsers(): List<UserDetail> {
        val currentUser = auth.currentUser
        val users: MutableList<UserDetail> = mutableListOf()
        val userCollection = db.collection("users").orderBy("username").get().await()
        for (document in userCollection) {
            if (document.id == currentUser?.uid ) {
                continue
            }
            val userData = document.data
            val user = UserDetail(
                document.id,
                "",
                userData["username"] as? String ?: "",
                userData["profile_picture"] as? String ?: "",
                userData["dream_code"] as? String,
                (userData["followers"] as? List<String>),
                (userData["following"] as? List<String>)
            )
            users.add(user)
        }
        return users
    }

    suspend fun getFilteredUsers(search: String): List<UserDetail> {
        val currentUser = auth.currentUser
        val users: MutableList<UserDetail> = mutableListOf()
        val userCollection = db.collection("users")

        val userQuery = userCollection.orderBy("username").startAt(search).endAt(search + "\uf8ff")
        val usersFiltered = userQuery.get().await()

        for (document in usersFiltered) {
            if (document.id == currentUser?.uid ) {
                continue
            }
            val userData = document.data
            val user = UserDetail(
                document.id,
                "",
                userData["username"] as? String ?: "",
                userData["profile_picture"] as? String ?: "",
                userData["dream_code"] as? String,
                (userData["followers"] as? List<String>),
                (userData["following"] as? List<String>)
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