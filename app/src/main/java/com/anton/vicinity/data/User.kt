package com.anton.vicinity.data

data class User(
    val userName: String? = null,
    val userImage: String? = null,
    val userBio: String? = null,
    val userEvents: MutableList<String>? = null,
    val userFriends: MutableList<String>? = null
)