package com.example.animalcrossing.data.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.repository.User
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
    suspend fun getIsland() {

    }

    fun createIsland(name: String) {
        val currentUser = auth.currentUser

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
        val currentUser = auth.currentUser

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

    suspend fun createLoan(loan: LoansEntity) {
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

            islandDoc.reference.collection("loans")
                .add(loanData)
        }
    }


    // AUTH
    suspend fun getCurrentUser(): User {
        var currentUser = auth.currentUser

        if (currentUser != null) {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()

                val user = userDoc.data
                return User(
                    currentUser.uid,
                    currentUser.email.toString(),
                    user?.get("username") as String,
                    user["profile_picture"] as String
                )

            }
        return User(
            "",
            "",
            "",
            ""
        )
    }
}