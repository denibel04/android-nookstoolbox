package com.example.animalcrossing.data.api

import android.content.ContentValues.TAG
import android.util.Log
import com.example.animalcrossing.data.repository.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ApiService @Inject constructor() {
    val db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()


    suspend fun getAllVillagers(): List<VillagerDetail> {
        val villagers: MutableList<VillagerDetail> = mutableListOf()
        try {
            val querySnapshot = db.collection("villagers").get().await()
            for (document in querySnapshot) {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error getting villagers", e)
        }


        return villagers
    }

    fun createIsland(name: String) {
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            val islandData = hashMapOf(
                "name" to name,
                "villagers" to emptyList<String>(),
                "loans" to emptyList<String>()
            )

            db.collection("users").document(user.uid).collection("island")
                .add(islandData)
                .addOnSuccessListener { documentReference ->
                    println("Isla creada exitosamente con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    println("Error al crear la isla: $e")
                }
        }
    }

    suspend fun getIsland() {

    }

    suspend fun addVillagerToIsland(name: String) {
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            try {
                val island = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                if (!island.isEmpty) {
                    val islandDoc = island.documents[0]

                    val villager = db.collection("villagers")
                        .whereEqualTo("name", name)
                        .get()
                        .await()

                    if (!villager.isEmpty) {
                        val villagerDoc = villager.documents[0]

                        val villagerUid = villagerDoc.id

                        val islandRef = db.collection("users")
                            .document(user.uid)
                            .collection("island")
                            .document(islandDoc.id)

                        islandRef.update("villagers", FieldValue.arrayUnion(villagerUid))

                    } else {
                    }
                } else {
                    Log.e(TAG, "No se encontró ninguna isla para el usuario")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al agregar aldeano a la isla", e)
            }
        }
    }

    suspend fun deleteVillagerFromIsland(name: String) {
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            try {
                val island = db.collection("users")
                    .document(user.uid)
                    .collection("island")
                    .get()
                    .await()

                if (!island.isEmpty) {
                    val islandDoc = island.documents[0]

                    val villager = db.collection("villagers")
                        .whereEqualTo("name", name)
                        .get()
                        .await()

                    if (!villager.isEmpty) {
                        val villagerDoc = villager.documents[0]

                        val villagerUid = villagerDoc.id

                        val islandRef = db.collection("users")
                            .document(user.uid)
                            .collection("island")
                            .document(islandDoc.id)

                        islandRef.update("villagers", FieldValue.arrayRemove(villagerUid))

                    } else {
                    }
                } else {
                    Log.e(TAG, "No se encontró ninguna isla para el usuario")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al agregar aldeano a la isla", e)
            }
        }
    }

    suspend fun deleteIsland() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            try {
                val querySnapshot: QuerySnapshot = db.collection("users").document(user.uid)
                    .collection("island").get().await()

                for (document in querySnapshot.documents) {
                    db.collection("users").document(user.uid)
                        .collection("island").document(document.id).delete().await()
                    println("Isla eliminada con ID: ${document.id}")
                }
                println("Todas las islas han sido eliminadas exitosamente")
            } catch (e: Exception) {
                println("Error al eliminar las islas: $e")
            }
        }

    }

    suspend fun getCurrentUser(): User {
        var currentUser = auth.currentUser

        if (currentUser != null) {
            try {
                val querySnapshot = db.collection("users").document(currentUser.uid).get().await()

                val user = querySnapshot.data
                return User(
                    currentUser.uid,
                    currentUser.email.toString(),
                    user?.get("username") as String,
                    user["profile_picture"] as String
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error getting villagers", e)
            }


        }
        return User(
            "",
            "",
            "",
            ""
        )
    }
}