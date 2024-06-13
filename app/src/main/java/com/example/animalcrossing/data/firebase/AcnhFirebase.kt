package com.example.animalcrossing.data.firebase

import android.util.Log
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.UserProfileDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseService @Inject constructor() {
    val db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()

    // VILLAGERS

    /**
     * Retrieves all villagers' details stored in Firebase Firestore.
     *
     * @return Suspended list of [VillagerDetail].
     */
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
                    (villagerListItemMap["birthday_day"] as String).toInt()
                )
                Log.d("VILLAGER", villagerDetail.toString())
                villagers.add(villagerDetail)
            }
        return villagers
    }



    // ISLAND

    /**
     * Retrieves the island details of the current user from Firebase Firestore.
     *
     * @return Suspended detail of [IslandDetail].
     */
    suspend fun getIsland(): IslandDetail {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val islandCollection = db.collection("users").document(user.uid).collection("island").get().await()
            val island = islandCollection.documents.firstOrNull()

            island?.let { document ->
                return IslandDetail(
                    islandId = document.id,
                    name = document.getString("name") ?: "",
                    hemisphere = document.getString("hemisphere") ?: "",
                    villagers = document.get("villagers") as? List<String> ?: emptyList()
                )
            }
        }
        return IslandDetail("", "", "", emptyList())
    }


    /**
     * Creates a new island for the current user in Firebase Firestore.
     *
     * @param name Name of the island.
     * @param hemisphere Hemisphere of the island.
     */
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


    /**
     * Renames the current user's island in Firebase Firestore.
     *
     * @param name New name of the island.
     */
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


    /**
     * Adds a villager to the current user's island in Firebase Firestore.
     *
     * @param name Name of the villager to add.
     */
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

    /**
     * Deletes a villager from the current user's island in Firebase Firestore.
     *
     * @param name Name of the villager to delete.
     */
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

    /**
     * Deletes the current user's island from Firebase Firestore.
     */
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

    /**
     * Retrieves the list of loans associated with the current user's island from Firebase Firestore.
     *
     * @return Suspended list of [LoansEntity].
     */
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

    /**
     * Creates a new loan entry under the current user's island in Firebase Firestore.
     *
     * @param loan [LoansEntity] object representing the loan to create.
     * @return Newly created loan ID.
     */
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

    /**
     * Updates an existing loan entry under the current user's island in Firebase Firestore.
     *
     * @param newLoan Updated [Loan] object representing the loan to update.
     */
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

    /**
     * Deletes a loan entry from the current user's island in Firebase Firestore.
     *
     * @param firebaseId ID of the loan entry to delete.
     */
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

    /**
     * Retrieves detailed information about the current authenticated user from Firebase Firestore.
     *
     * @return Suspended [UserDetail] object representing the current user.
     */
    suspend fun getCurrentUser(): UserDetail {
        val currentUser = auth.currentUser
        var profile = UserDetail("", "", "", "", "", "", emptyList(), emptyList())
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
                 userDoc.getString("role") ?: "",
                    followers,
                    following
                )

            }
        return profile
        }


    /**
     * Retrieves a list of friends (mutual followers) of the current user from Firebase Firestore.
     *
     * @return Suspended list of [UserDetail] representing friends.
     */
    suspend fun getFriends(): List<UserDetail> {
        val currentUser = auth.currentUser
        val users = mutableListOf<UserDetail>()

        if (currentUser != null) {
            val document = db.collection("users").document(currentUser.uid).get().await()
            val followers = document.get("followers") as? List<String> ?: emptyList()
            val following = document.get("following") as? List<String> ?: emptyList()

            val friendsUids = followers.intersect(following).toList()

            for (uid in friendsUids) {
                val doc = db.collection("users").document(uid).get().await()
                val user = UserDetail(
                    doc.id,
                    "",
                    doc["username"] as? String ?: "",
                    doc["profile_picture"] as? String ?: "",
                    doc["dream_code"] as? String,
                    doc["role"] as? String ?: "",
                    (doc["followers"] as? List<String>),
                    (doc["following"] as? List<String>)
                )
                users.add(user)
            }
        }

        return users
    }

    /**
     * Retrieves a list of followers of the current user from Firebase Firestore.
     *
     * @return Suspended list of [UserDetail] representing followers.
     */
    suspend fun getFollowers(): List<UserDetail> {
        val currentUser = auth.currentUser
        val users = mutableListOf<UserDetail>()

        if (currentUser != null) {
            val document = db.collection("users").document(currentUser.uid).get().await()
            val followers = document.get("followers") as? List<String> ?: emptyList()

            for (uid in followers) {
                val doc = db.collection("users").document(uid).get().await()
                val user = UserDetail(
                    doc.id,
                    "",
                    doc["username"] as? String ?: "",
                    doc["profile_picture"] as? String ?: "",
                    doc["dream_code"] as? String,
                    doc["role"] as? String ?: "",
                    (doc["followers"] as? List<String>),
                    (doc["following"] as? List<String>)
                )
                users.add(user)
            }
        }

        return users
    }

    /**
     * Retrieves a list of users followed by the current user from Firebase Firestore.
     *
     * @return Suspended list of [UserDetail] representing following.
     */
    suspend fun getFollowing(): List<UserDetail> {
        val currentUser = auth.currentUser
        val users = mutableListOf<UserDetail>()

        if (currentUser != null) {
            val document = db.collection("users").document(currentUser.uid).get().await()
            val following = document.get("following") as? List<String> ?: emptyList()

            for (uid in following) {
                val doc = db.collection("users").document(uid).get().await()
                val user = UserDetail(
                    doc.id,
                    "",
                    doc["username"] as? String ?: "",
                    doc["profile_picture"] as? String ?: "",
                    doc["dream_code"] as? String,
                    doc["role"] as? String ?: "",
                    (doc["followers"] as? List<String>),
                    (doc["following"] as? List<String>)
                )
                users.add(user)
            }
        }

        return users
    }

    /**
     * Changes the username of the current authenticated user in Firebase Firestore.
     *
     * @param newUsername New username to set.
     */
    fun changeUsername(newUsername: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            currentUserRef.update("username", newUsername)
        }
    }

    /**
     * Changes the dream code of the current authenticated user in Firebase Firestore.
     *
     * @param newDreamcode New dream code to set.
     */
    fun changeDreamCode(newDreamcode: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            currentUserRef.update("dream_code", newDreamcode)
        }
    }

    /**
     * Retrieves a list of all users registered in Firebase Firestore, excluding the current user.
     *
     * @return Suspended list of [UserDetail] representing other users.
     */
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
                userData["role"] as? String ?: "",
                (userData["followers"] as? List<String>),
                (userData["following"] as? List<String>)
            )
            users.add(user)
        }
        return users
    }

    /**
     * Retrieves detailed profile information of a specific user from Firebase Firestore.
     *
     * @param uid ID of the user to retrieve details for.
     * @return Suspended [UserProfileDetail] representing the detailed profile of the user.
     */
    suspend fun getUserDetail(uid: String): UserProfileDetail {
        val userRef = db.collection("users").document(uid)

        val userSnapshot = userRef.get().await()

        val userData = userSnapshot.data
        var user = UserProfileDetail(
            userSnapshot.id,
            userData?.get("email") as? String ?: "",
            userData?.get("username") as? String ?: "",
            userData?.get("profile_picture") as? String ?: "",
            userData?.get("dream_code") as? String,
            userData?.get("followers") as? List<String> ?: emptyList(),
            userData?.get("following") as? List<String> ?: emptyList()
        )
        val islandCollection = db.collection("users").document(user.uid).collection("island").get().await()
        val islandSnapshot = islandCollection.documents.firstOrNull()

        if (islandSnapshot != null) {
                val islandData = islandSnapshot.data
                user = user.copy(
                    islandName = islandData?.get("name") as? String ?: "",
                    hemisphere = islandData?.get("hemisphere") as? String ?: "",
                    islandExists = true,
                    villagers = islandData?.get("villagers") as? List<String> ?: emptyList()
                )
            } else {
                user = user.copy(islandExists = false)
            }

        return user
    }

    /**
     * Retrieves a filtered list of users whose usernames match the search query from Firebase Firestore.
     *
     * @param search Search query to filter usernames.
     * @return Suspended list of [UserDetail] representing filtered users.
     */
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
                userData["role"] as? String ?: "",
                (userData["followers"] as? List<String>),
                (userData["following"] as? List<String>)
            )
            users.add(user)
        }
        return users
    }

    /**
     * Follows another user by updating following and followers lists in Firebase Firestore.
     *
     * @param followedUid UID of the user to follow.
     */
    suspend fun followUser(followedUid: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserRef = db.collection("users").document(currentUser.uid)
            val followedUserRef = db.collection("users").document(followedUid)

            currentUserRef.update("following", FieldValue.arrayUnion(followedUid)).await()
            followedUserRef.update("followers", FieldValue.arrayUnion(currentUser.uid)).await()
        }
    }

    /**
     * Unfollows a user by updating following and followers lists in Firebase Firestore.
     *
     * @param followedUid UID of the user to unfollow.
     */
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