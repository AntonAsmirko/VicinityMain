package com.anton.vicinity.data

import android.provider.Settings
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.anton.vicinity.Utils.LatLngUtils
import com.anton.vicinity.ui.MapFragmentViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.lang.Exception
import kotlin.math.log

object MapRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val eventRef: CollectionReference = db.collection("Events")

    fun saveEvent(event: Event) {
        eventRef.add(event).addOnSuccessListener {
            Log.i("EVENT_ADDED", "EVENT_ADDED")
        }.addOnFailureListener {
            Log.d("EVENT_WAS_NOT_ADDED", it.toString())
        }
    }

    fun loadNearestEvents(latLng: LatLng) {
        eventRef.get().addOnSuccessListener { docCollection ->
            val resultEvents = mutableListOf<Event>()
            docCollection?.forEach { it ->
                val resultEvent = it.toObject(Event::class.java)
                if (LatLngUtils.distance(
                        resultEvent.eventLat ?: 0.0, latLng.latitude,
                        resultEvent.eventLng ?: 0.0, latLng.longitude, 0.0, 0.0
                    ) <
                    2000.0
                ) {
                    resultEvents.add(resultEvent)
                    /*
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(resultEvent.eventLat?:0.0, resultEvent.eventLng?:0.0))
                    )

                     */

                }
            }
            MapFragmentViewModel.nearestEvents.value = resultEvents
        }
    }
}