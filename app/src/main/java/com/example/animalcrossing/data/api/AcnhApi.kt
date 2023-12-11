package com.example.animalcrossing.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

interface AcnhApi {
    @GET("villagers?game=nh")
    suspend fun getAllVillagers(@Header("x-api-key") apiKey: String, @Query("limit") limit:Int=20, @Query("offset") offset:Int=0): List<VillagerDetail>

    @GET("villagers")
    suspend fun getVillagerDetail(
        @Header("x-api-key") apiKey: String,
        @Query("game") game: String = "nh",
        @Query("name") name: String
    ): List<VillagerDetail>

    @GET("nh/fish")
    suspend fun getAllFish(@Header("x-api-key") apiKey: String, @Query("limit") limit:Int=20, @Query("offset") offset:Int=0): List<FishDetail>
}

@Singleton
class ApiService @Inject constructor() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.nookipedia.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val api = retrofit.create(AcnhApi::class.java)
}