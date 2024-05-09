package com.example.animalcrossing.data.repository

class Loan(
    val firebaseId: String,
    val title:String,
    val type: String,
    val amountPaid: Int,
    val amountTotal: Int,
    val completed: Boolean

)
