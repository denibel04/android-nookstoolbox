package com.example.animalcrossing.data.api

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.example.animalcrossing.data.repository.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
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

           db.collection("users").document(user.uid).collection("islands")
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

   suspend fun deleteIsland() {
       val currentUser = auth.currentUser
       currentUser?.let { user ->
           try {
               val querySnapshot: QuerySnapshot = db.collection("users").document(user.uid)
                   .collection("islands").get().await()

               for (document in querySnapshot.documents) {
                   db.collection("users").document(user.uid)
                       .collection("islands").document(document.id).delete().await()
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
                val querySnapshot = db.collection("users").whereEqualTo("uid", currentUser?.uid).get().await()
                for (document in querySnapshot) {
                    val user = document.data
                    return User(
                        currentUser.uid,
                        currentUser.email.toString(),
                        user["username"] as String
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting villagers", e)
            }
        }
        return User(
            "Not logged",
            "User",
            "User"
        )
    }

    }