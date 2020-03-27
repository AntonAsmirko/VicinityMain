package com.anton.vicinity.ui

import android.location.Geocoder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anton.vicinity.data.Event
import com.anton.vicinity.data.MapRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

object MapFragmentViewModel
    :ViewModel(){
    lateinit var cameraPosition: CameraPosition
    var isTopBarEmpty = true
    var topBarState: TopBarState = TopBarState.SHOW_PLACE
    val nearestEvents = MutableLiveData<MutableList<Event>>()
    enum class TopBarState {
        SEARCH, SHOW_PLACE, EMPTY
    }

    fun isCameraPositionInitialized(): Boolean{
        return ::cameraPosition.isInitialized
    }

    fun saveEvent(event: Event){
        MapRepository.saveEvent(event)
    }
}