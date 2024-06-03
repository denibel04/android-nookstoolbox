package com.example.animalcrossing.data.repository

class Loan(
    val firebaseId: String,
    val title:String,
    val type: String,
    var amountPaid: Int,
    val amountTotal: Int,
    var completed: Boolean

)
