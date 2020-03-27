package com.anton.vicinity.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.anton.vicinity.Utils.GlobalVariables
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object UserRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersRef: CollectionReference = db.collection("Users")
    private val eventsRef = db.collection("Events")
    private val userRef: DocumentReference = usersRef.document(GlobalVariables.userName)
    val usersFriends: MutableLiveData<MutableList<User>> =
        MutableLiveData(mutableListOf())
    private val usersEvents: MutableLiveData<MutableList<Event>> =
        MutableLiveData(mutableListOf())

    fun getUsersEvents(): MutableList<Event> {
        loadUserEvents()
        usersRef.whereEqualTo("Name", GlobalVariables.userName)
            .get()
            .addOnSuccessListener {
                val resultList = mutableListOf<Event>()
                for (event in it) {
                    resultList.add(event.toObject(Event::class.java))
                }
                usersEvents.value = resultList
            }
        return usersEvents.value!!
    }

    fun saveFriend(newFriend: User) {
        userRef.get().addOnSuccessListener { userOwner ->
            val user = userOwner.toObject(User::class.java)!!
            user.userFriends!!.add(newFriend.userName!!)
            usersRef.document(newFriend.userName).get().addOnSuccessListener {
                val newFriendFriends = it.toObject(User::class.java)!!.userFriends
                newFriendFriends!!.add(user.userName!!)
                usersRef
                    .document(newFriend.userName)
                    .update("userFriends", newFriendFriends)
            }
        }
    }

    private fun loadUserEvents() {
        eventsRef.whereEqualTo("CreatorsName", GlobalVariables.userName)
            .get()
            .addOnSuccessListener {
                val resultList = mutableListOf<Event>()
                for (event in it) {
                    resultList.add(event.toObject(Event::class.java))
                }
                usersEvents.value = resultList
            }
    }

    fun loadFriends() {
        val newValue = mutableListOf<User>()
        userRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            val setOfFriends = hashSetOf<String>()
            setOfFriends.addAll(user.userFriends!!)
            usersRef.get().addOnSuccessListener {result ->
                for( document in result){
                    val distinctFriend = document.toObject(User::class.java)
                    if (setOfFriends.contains(distinctFriend.userName)){
                        newValue.add(distinctFriend)
                    }
                }
                usersFriends.value = newValue
            }
        }
    }
}