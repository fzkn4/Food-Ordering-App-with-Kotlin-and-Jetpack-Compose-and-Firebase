package com.example.foodorderingapp.data

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val phoneNumber: String = "",
    val address: String = "",
    val profileImageUrl: String = ""
)

